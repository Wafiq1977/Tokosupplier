package savora.com.savora.controller;

import savora.com.savora.model.User;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                           Model model) {
        if (userDetails != null) {
            savora.com.savora.model.User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                if (user.getRole() == savora.com.savora.model.User.Role.BUYER) {
                    return "redirect:/buyer/dashboard";
                } else if (user.getRole() == savora.com.savora.model.User.Role.SUPPLIER) {
                    return "redirect:/supplier/dashboard";
                }
            }
        }
        return "redirect:/login";
    }
}
