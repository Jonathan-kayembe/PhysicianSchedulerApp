package com.saas.medicalapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller
 * Redirects root path to login page
 */
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/client/login.html";
    }
}

