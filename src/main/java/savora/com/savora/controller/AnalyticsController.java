package savora.com.savora.controller;

import savora.com.savora.model.User;
import savora.com.savora.service.AnalyticsService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserService userService;

    @GetMapping("/supplier")
    public String supplierAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier != null && supplier.getRole() == User.Role.SUPPLIER) {
            model.addAttribute("analytics", analyticsService.getSupplierAnalytics(supplier));
            return "analytics/supplier-dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/buyer")
    public String buyerAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (buyer != null && buyer.getRole() == User.Role.BUYER) {
            model.addAttribute("analytics", analyticsService.getBuyerAnalytics(buyer));
            return "analytics/buyer-dashboard";
        }
        return "redirect:/dashboard";
    }
}