package ru.mtuci.coursemanagement.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    // A10: SSRF - Ограничение с помощью белого списка
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "127.0.0.1",
            "localhost"
    );

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/proxy")
    public ResponseEntity<String> proxy(@RequestParam("targetUrl") String targetUrl) {
        if (!StringUtils.hasText(targetUrl)) {
            return ResponseEntity
                    .badRequest()
                    .body("Parameter 'targetUrl' is required");
        }

        URI uri;
        try {
            uri = new URI(targetUrl);
        } catch (URISyntaxException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null ||
                !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Only http/https");
        }

        String host = uri.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Host is not allowed");
        }

        try {
            String body = restTemplate.getForObject(uri, String.class);
            return ResponseEntity.ok(body);
        } catch (RestClientException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Upstream request failed");
        }
    }
}