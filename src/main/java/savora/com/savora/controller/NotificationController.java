package savora.com.savora.controller;

import savora.com.savora.model.User;
import savora.com.savora.service.NotificationService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("notifications", notificationService.getUserNotifications(user));
            model.addAttribute("unreadCount", notificationService.countUnreadNotifications(user));
        }
        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean redirect) {
        notificationService.markAsRead(id);
        if (redirect) {
            return "redirect:/notifications";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            notificationService.markAllAsRead(user);
        }
        return "redirect:/notifications";
    }

    // AJAX endpoint for notification count
    @GetMapping("/count")
    @ResponseBody
    public Long getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        return user != null ? notificationService.countUnreadNotifications(user) : 0L;
    }
}