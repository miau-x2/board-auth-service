package com.example.board.auth.credential.controller;

import com.example.board.auth.credential.controller.dto.request.MemberSignupRequest;
import com.example.board.auth.credential.orchestrator.MemberSignupOrchestrator;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
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
    private final MemberSignupOrchestrator memberSignupOrchestrator;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", MemberSignupRequest.empty());
        return "auth/member/signup";
    }

    @PostMapping("/signup")
    public String signup(String token, MemberSignupRequest request) {
        memberSignupOrchestrator.coordinateSignup(new MemberSignupCommand(request.username(), request.password(), request.email(), request.nickname(), token));
        return "redirect:/http://localhost:8000/";
    }
}
