package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getBuyer().getEmail());
            helper.setSubject("Konfirmasi Pesanan - SAVORA #" + order.getId());

            String htmlContent = buildOrderConfirmationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log error but don't throw exception to avoid breaking order flow
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    public void sendOrderStatusUpdate(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getBuyer().getEmail());
            helper.setSubject("Update Status Pesanan - SAVORA #" + order.getId());

            String htmlContent = buildOrderStatusEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send order status email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Selamat Datang di SAVORA!");

            String htmlContent = buildWelcomeEmail(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Reset Password - SAVORA");

            String htmlContent = buildPasswordResetEmail(user, resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    // Notification-specific email methods
    public void sendNewOrderNotification(User supplier, User buyer, String orderId, String productName, Integer quantity) {
        try {
            if (supplier == null || supplier.getEmail() == null || supplier.getEmail().isEmpty()) {
                System.err.println("Cannot send email: supplier or email is null/empty");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(supplier.getEmail());
            helper.setSubject("Pesanan Baru Masuk - SAVORA #" + orderId);

            String htmlContent = buildNewOrderEmail(supplier, buyer, orderId, productName, quantity);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send new order notification email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPaymentConfirmedNotification(User buyer, String orderId, Double amount, String paymentMethod) {
        try {
            if (buyer == null || buyer.getEmail() == null || buyer.getEmail().isEmpty()) {
                System.err.println("Cannot send email: buyer or email is null/empty");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(buyer.getEmail());
            helper.setSubject("Pembayaran Dikonfirmasi - SAVORA #" + orderId);

            String htmlContent = buildPaymentConfirmedEmail(buyer, orderId, amount, paymentMethod);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmed email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderShippedNotification(User buyer, String orderId, String trackingNumber, String estimatedDelivery) {
        try {
            if (buyer == null || buyer.getEmail() == null || buyer.getEmail().isEmpty()) {
                System.err.println("Cannot send email: buyer or email is null/empty");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(buyer.getEmail());
            helper.setSubject("Pesanan Dikirim - SAVORA #" + orderId);

            String htmlContent = buildOrderShippedEmail(buyer, orderId, trackingNumber, estimatedDelivery);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order shipped email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderConfirmedNotification(User buyer, String orderId, String estimatedShipping) {
        try {
            if (buyer == null || buyer.getEmail() == null || buyer.getEmail().isEmpty()) {
                System.err.println("Cannot send email: buyer or email is null/empty");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(buyer.getEmail());
            helper.setSubject("Pesanan Dikonfirmasi - SAVORA #" + orderId);

            String htmlContent = buildOrderConfirmedEmail(buyer, orderId, estimatedShipping);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmed email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPaymentApprovedNotification(User buyer, String orderId, Double amount) {
        try {
            if (buyer == null || buyer.getEmail() == null || buyer.getEmail().isEmpty()) {
                System.err.println("Cannot send email: buyer or email is null/empty");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(buyer.getEmail());
            helper.setSubject("Pembayaran Disetujui - SAVORA #" + orderId);

            String htmlContent = buildPaymentApprovedEmail(buyer, orderId, amount);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send payment approved email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildOrderConfirmationEmail(Order order) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .order-details { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Konfirmasi Pesanan Berhasil</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Terima kasih telah berbelanja di SAVORA. Pesanan Anda telah berhasil dibuat dengan detail sebagai berikut:</p>

                    <div class="order-details">
                        <h3>Detail Pesanan #%s</h3>
                        <p><strong>Supplier:</strong> %s</p>
                        <p><strong>Total:</strong> Rp %s</p>
                        <p><strong>Status:</strong> %s</p>
                        <p><strong>Tanggal:</strong> %s</p>
                    </div>

                    <p>Anda akan menerima email update ketika status pesanan berubah.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            order.getBuyer() != null ? order.getBuyer().getUsername() : "Pelanggan",
            order.getId() != null ? order.getId().toString() : "N/A",
            order.getSupplier() != null && order.getSupplier().getCompanyName() != null ?
                order.getSupplier().getCompanyName() : "Supplier",
            order.getTotalAmount() != null ? String.format("%,.0f", order.getTotalAmount()) : "0",
            order.getStatus() != null ? order.getStatus().name() : "PENDING",
            order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");
    }

    private String buildOrderStatusEmail(Order order) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .status-update { background: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Update Status Pesanan</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Status pesanan Anda telah diperbarui:</p>

                    <div class="status-update">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Status Baru:</strong> <span style="font-weight: bold; color: #28a745;">%s</span></p>
                        <p><strong>Supplier:</strong> %s</p>
                    </div>

                    <p>Silakan cek dashboard Anda untuk detail lebih lanjut.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            order.getBuyer() != null ? order.getBuyer().getUsername() : "Pelanggan",
            order.getId() != null ? order.getId().toString() : "N/A",
            order.getStatus() != null ? order.getStatus().name() : "PENDING",
            order.getSupplier() != null && order.getSupplier().getCompanyName() != null ?
                order.getSupplier().getCompanyName() : "Supplier");
    }

    private String buildWelcomeEmail(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .welcome-message { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Selamat Datang di SAVORA!</h1>
                </div>

                <div class="content">
                    <h2>Halo, " + user.getUsername() + "!</h2>

                    <div class="welcome-message">
                        <p>Selamat datang di platform SAVORA! Akun Anda telah berhasil dibuat sebagai <strong>" + user.getRole().name() + "</strong>.</p>

                        <p>Anda sekarang dapat:</p>
                        <ul>
                            """ + (user.getRole() == User.Role.BUYER ?
                                "<li>Jelajahi dan beli bahan baku UMKM</li><li>Lihat produk dari berbagai supplier</li><li>Kelola pesanan dan review</li>" :
                                "<li>Kelola katalog produk Anda</li><li>Terima dan proses pesanan</li><li>Lihat analisis penjualan</li>") + """
                        </ul>
                    </div>

                    <p><a href="http://localhost:8080/login" style="background: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Mulai Sekarang</a></p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;
    }

    private String buildPasswordResetEmail(User user, String resetToken) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .reset-section { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .reset-button { background: #ffc107; color: #212529; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Reset Password - SAVORA</h1>
                </div>

                <div class="content">
                    <h2>Halo, " + user.getUsername() + "!</h2>
                    <p>Anda menerima email ini karena ada permintaan reset password untuk akun SAVORA Anda.</p>

                    <div class="reset-section">
                        <p><strong>Klik link di bawah untuk reset password Anda:</strong></p>
                        <p><a href="http://localhost:8080/reset-password?token=" + resetToken + "" class="reset-button">Reset Password</a></p>
                        <p><small>Link ini akan kadaluarsa dalam 24 jam.</small></p>
                    </div>

                    <p>Jika Anda tidak meminta reset password, abaikan email ini.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;
    }

    private String buildNewOrderEmail(User supplier, User buyer, String orderId, String productName, Integer quantity) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .order-details { background: #e7f3ff; border: 1px solid #b8daff; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .action-button { background: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Pesanan Baru Masuk</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Anda menerima pesanan baru dari buyer:</p>

                    <div class="order-details">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Buyer:</strong> %s</p>
                        <p><strong>Produk:</strong> %s</p>
                        <p><strong>Jumlah:</strong> %d</p>
                    </div>

                    <p><a href="http://localhost:8080/supplier/orders/%s" class="action-button">Proses Pesanan</a></p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            supplier.getCompanyName() != null ? supplier.getCompanyName() : supplier.getUsername(),
            orderId,
            buyer.getCompanyName() != null ? buyer.getCompanyName() : buyer.getUsername(),
            productName != null ? productName : "Produk",
            quantity,
            orderId);
    }

    private String buildPaymentConfirmedEmail(User buyer, String orderId, Double amount, String paymentMethod) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .payment-details { background: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Pembayaran Dikonfirmasi</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Pembayaran untuk pesanan Anda telah dikonfirmasi:</p>

                    <div class="payment-details">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Jumlah:</strong> Rp %s</p>
                        <p><strong>Metode Pembayaran:</strong> %s</p>
                        <p><strong>Status:</strong> <span style="color: #28a745; font-weight: bold;">Dikonfirmasi</span></p>
                    </div>

                    <p>Supplier akan segera memproses pesanan Anda.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            buyer.getUsername() != null ? buyer.getUsername() : "Pelanggan",
            orderId,
            amount != null ? String.format("%,.0f", amount) : "0",
            paymentMethod != null ? paymentMethod : "Transfer Bank");
    }

    private String buildOrderShippedEmail(User buyer, String orderId, String trackingNumber, String estimatedDelivery) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .shipping-details { background: #d1ecf1; border: 1px solid #bee5eb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .action-button { background: #17a2b8; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Pesanan Dikirim</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Pesanan Anda telah dikirim oleh supplier:</p>

                    <div class="shipping-details">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Nomor Tracking:</strong> %s</p>
                        <p><strong>Estimasi Tiba:</strong> %s</p>
                    </div>

                    <p><a href="http://localhost:8080/buyer/orders/%s" class="action-button">Lacak Pengiriman</a></p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            buyer.getUsername() != null ? buyer.getUsername() : "Pelanggan",
            orderId,
            trackingNumber != null ? trackingNumber : "Belum tersedia",
            estimatedDelivery != null ? estimatedDelivery : "3-5 hari kerja",
            orderId);
    }

    private String buildOrderConfirmedEmail(User buyer, String orderId, String estimatedShipping) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .confirmation-details { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Pesanan Dikonfirmasi</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Pesanan Anda telah dikonfirmasi oleh supplier:</p>

                    <div class="confirmation-details">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Status:</strong> <span style="color: #856404; font-weight: bold;">Dikonfirmasi</span></p>
                        <p><strong>Estimasi Pengiriman:</strong> %s</p>
                    </div>

                    <p>Supplier sedang menyiapkan pesanan Anda.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            buyer.getUsername() != null ? buyer.getUsername() : "Pelanggan",
            orderId,
            estimatedShipping != null ? estimatedShipping : "2-3 hari kerja");
    }

    private String buildPaymentApprovedEmail(User buyer, String orderId, Double amount) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .approval-details { background: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .action-button { background: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { background: #343a40; color: white; padding: 20px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>SAVORA</h1>
                    <p>Pembayaran Disetujui</p>
                </div>

                <div class="content">
                    <h2>Halo, %s!</h2>
                    <p>Pembayaran untuk pesanan Anda telah disetujui:</p>

                    <div class="approval-details">
                        <h3>Pesanan #%s</h3>
                        <p><strong>Jumlah:</strong> Rp %s</p>
                        <p><strong>Status:</strong> <span style="color: #28a745; font-weight: bold;">Disetujui</span></p>
                    </div>

                    <p><a href="http://localhost:8080/buyer/orders/%s" class="action-button">Lanjutkan</a></p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;

        return String.format(template,
            buyer.getUsername() != null ? buyer.getUsername() : "Pelanggan",
            orderId,
            amount != null ? String.format("%,.0f", amount) : "0",
            orderId);
    }
}