package ru.mtuci.coursemanagement.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mtuci.coursemanagement.model.Course;
import ru.mtuci.coursemanagement.repository.CourseRepository;
import ru.mtuci.coursemanagement.service.CourseService;

@Slf4j
@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CourseController {
    private final CourseRepository repo;
    private final CourseService service;

    private static final Set<String> ALLOWED_IMPORT_HOSTS = Set.of(
            "127.0.0.1",
            "localhost"
    );


    @GetMapping("/courses")
    public String coursesPage(Model model) {
        model.addAttribute("courses", repo.findAll());
        model.addAttribute("course", new Course());
        return "courses";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course c) {
        repo.save(c);
        return "redirect:/courses";
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public List<Course> all() {
        return repo.findAll();
    }

    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course payload) {
        return repo.findById(id).map(c -> {
            c.setTitle(payload.getTitle());
            c.setDescription(payload.getDescription());
            c.setTeacherId(payload.getTeacherId());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/courses/search")
    @ResponseBody
    public List<Course> search(@RequestParam String title) {
        return service.searchByTitle(title);
    }

    @GetMapping("/api/courses/import")
    @ResponseBody
    public ResponseEntity<String> importFromUrl(@RequestParam String url) {
        // A10: SSRF fix - ограничение импортов по белому списку
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().body("Invalid URL");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return ResponseEntity.badRequest().body("Only HTTP and HTTPS protocols are allowed");
        }

        String host = uri.getHost();
        if (host == null || !ALLOWED_IMPORT_HOSTS.contains(host)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Host is not allowed");
        }

        RestTemplate rt = new RestTemplate();
        try {
            String json = rt.getForObject(uri, String.class);
            log.info("Импортированы данные курсов (raw): {}", json);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.warn("Ошибка при импорте данных курсов с {}: {}", host, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Import failed");
        }
    }
}
