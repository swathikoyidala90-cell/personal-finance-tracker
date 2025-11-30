package com.personal.financetracker;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepo userRepository;

    // ✅ Show signup page
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup"; // signup.html
    }

    // ✅ Handle signup form submission
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // Check if username or email already exists
        if (userRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists!");
            return "signup";
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already registered!");
            return "signup";
        }

        // Save the new user
        userRepository.save(user);

        model.addAttribute("success", "Account created successfully! Please login.");
        return "login"; // redirect to login page
    }

    // ✅ Show login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.html
    }

    // ✅ Handle login
    @PostMapping("/login")

    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {


        User user = userRepository.findByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid password!");
            return "login";
        }

        // Save user details in session
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        return "redirect:/dashboard";
    }

    // ✅ Handle logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // clear session
        return "redirect:/login";
    }
}