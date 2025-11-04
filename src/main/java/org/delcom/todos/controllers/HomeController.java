package org.delcom.todos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/") /*  */
    public String hello() {
        return "Hay, selamat datang di Spring Boot!";
    }

}
