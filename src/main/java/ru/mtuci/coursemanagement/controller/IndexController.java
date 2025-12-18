package ru.mtuci.coursemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {
        // A08 Software and Data Integrity Failures - убрал загрузчика плагина из вызова
        return "index";
    }
}
