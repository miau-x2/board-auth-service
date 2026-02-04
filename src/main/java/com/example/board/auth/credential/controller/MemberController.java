package com.example.board.auth.credential.controller;

import com.example.board.auth.credential.controller.dto.request.MemberSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", MemberSignupRequest.empty());
        return "auth/member/signup";
    }

    @PostMapping("/signup")
    public String signup(String token, MemberSignupRequest request) {
        return "redirect:/http://localhost:8000/";
    }
}
