package savora.com.savora.controller;

import savora.com.savora.model.Notification;
import savora.com.savora.model.User;
import savora.com.savora.service.NotificationService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                java.util.List<Notification> notifications = notificationService.getUserNotifications(user);
                long readCount = notifications.stream().filter(Notification::getIsRead).count();
                model.addAttribute("notifications", notifications);
                model.addAttribute("unreadCount", notificationService.countUnreadNotifications(user));
                model.addAttribute("readCount", readCount);
                model.addAttribute("userRole", user.getRole());
            } else {
                model.addAttribute("notifications", new java.util.ArrayList<>());
                model.addAttribute("unreadCount", 0L);
                model.addAttribute("readCount", 0L);
            }
            return "notifications";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Terjadi kesalahan saat memuat notifikasi: " + e.getMessage());
            model.addAttribute("notifications", new java.util.ArrayList<>());
            model.addAttribute("unreadCount", 0L);
            model.addAttribute("readCount", 0L);
            return "notifications";
        }
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean redirect) {
        notificationService.markAsRead(id);
        if (redirect) {
            return "redirect:/notifications";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return "redirect:/notifications";
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

    // AJAX endpoint for latest notifications (for real-time updates)
    @GetMapping("/latest")
    @ResponseBody
    public List<Notification> getLatestNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        return user != null ? notificationService.getUserNotifications(user).stream().limit(10).toList() : new ArrayList<>();
    }
}