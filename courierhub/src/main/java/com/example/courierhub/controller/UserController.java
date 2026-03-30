package com.example.courierhub.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/session")
    public String getSessionUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // Получаем userId из сессии
        if (userId != null) {
            return "User ID из сессии: " + userId;
        } else {
            return "Пользователь не авторизован.";
        }
    }
}
