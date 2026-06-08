package com.clinic.controller;

import com.clinic.entity.User;
import com.clinic.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // ── Root → redirect to login ──────────────────────────────────────────────
    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            return "ADMIN".equals(user.getRole())
                    ? "redirect:/admin/dashboard"
                    : "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    // ── Show Login Page ───────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    // ── Show Register Page ────────────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ── Handle Registration ───────────────────────────────────────────────────
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String role) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRole(role);
            authService.register(user);
            return "redirect:/login?success=true";
        } catch (RuntimeException e) {
            return "redirect:/register?error=true";
        }
    }

    // ── Handle Login ──────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        User user = authService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "ADMIN".equals(user.getRole())
                    ? "redirect:/admin/dashboard"
                    : "redirect:/dashboard";
        }
        return "redirect:/login?error=true";
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
