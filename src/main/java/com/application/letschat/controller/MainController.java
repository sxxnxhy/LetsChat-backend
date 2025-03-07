package com.application.letschat.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

//    @GetMapping("/")
//    public String home(HttpServletRequest request) {
//        if (request.getSession().getAttribute("user") == null) {
//            return "redirect:/login.html";
//        }
//        return "redirect:chat-list.html";
//    }
//    @GetMapping("/")
//    public String home(HttpServletRequest request) {
//        if (request.getSession().getAttribute("user") == null) {
//            return "redirect:https://syoo.shop/login"; // Nginx serves this
//        }
//        return "redirect:https://syoo.shop/chat-list"; // Nginx serves this
//    }

}
