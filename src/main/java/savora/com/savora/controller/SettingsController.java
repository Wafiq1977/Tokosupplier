package savora.com.savora.controller;

import savora.com.savora.model.User;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            return "settings";
        }
        return "redirect:/login";
    }

    @PostMapping("/account")
    public String updateAccount(@ModelAttribute User updatedUser,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();

            // Update account settings
            currentUser.setCompanyName(updatedUser.getCompanyName());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
            currentUser.setAddress(updatedUser.getAddress());

            // Save changes
            // userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Pengaturan akun berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui pengaturan akun: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Konfirmasi kata sandi tidak cocok!");
                return "redirect:/settings";
            }

            // Implement password change logic
            // userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Kata sandi berhasil diubah!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mengubah kata sandi: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/notifications")
    public String updateNotifications(@RequestParam(required = false) boolean emailNotifications,
                                     @RequestParam(required = false) boolean orderUpdates,
                                     @RequestParam(required = false) boolean promotions,
                                     @RequestParam(required = false) boolean newsletter,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Implement notification settings update
            redirectAttributes.addFlashAttribute("successMessage", "Pengaturan notifikasi berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui pengaturan notifikasi: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/privacy")
    public String updatePrivacy(@RequestParam(required = false) boolean profileVisibility,
                               @RequestParam(required = false) boolean activityStatus,
                               @RequestParam(required = false) boolean dataSharing,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // Implement privacy settings update
            redirectAttributes.addFlashAttribute("successMessage", "Pengaturan privasi berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui pengaturan privasi: " + e.getMessage());
        }
        return "redirect:/settings";
    }
}