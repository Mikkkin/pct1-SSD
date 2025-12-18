package ru.mtuci.coursemanagement.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.UserService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;
    private final PasswordEncoder passwordEncoder; // FIX A02 Cryptographic Failures

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Удалил кастомную форму авторизации. Функционал авторизации выполняют другие файлы

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        // A01 Broken Access Control (все OWASP по 2021 редакции) - роль по умолчанию поставлена STUDENT, а не на выбор
        // Также тут используется захешированный пароль (от фикса A02)
        users.save(new User(null, username, encoded, "STUDENT"));
        return "redirect:/login";
    }
}
