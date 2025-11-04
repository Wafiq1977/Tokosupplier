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

    private String buildOrderConfirmationEmail(Order order) {
        return """
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
                    <h2>Halo, " + order.getBuyer().getUsername() + "!</h2>
                    <p>Terima kasih telah berbelanja di SAVORA. Pesanan Anda telah berhasil dibuat dengan detail sebagai berikut:</p>

                    <div class="order-details">
                        <h3>Detail Pesanan #" + order.getId() + "</h3>
                        <p><strong>Supplier:</strong> " + order.getSupplier().getCompanyName() + "</p>
                        <p><strong>Total:</strong> Rp " + String.format("%,.0f", order.getTotalAmount()) + "</p>
                        <p><strong>Status:</strong> " + order.getStatus().name() + "</p>
                        <p><strong>Tanggal:</strong> " + order.getCreatedAt().toString() + "</p>
                    </div>

                    <p>Anda akan menerima email update ketika status pesanan berubah.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;
    }

    private String buildOrderStatusEmail(Order order) {
        return """
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
                    <h2>Halo, " + order.getBuyer().getUsername() + "!</h2>
                    <p>Status pesanan Anda telah diperbarui:</p>

                    <div class="status-update">
                        <h3>Pesanan #" + order.getId() + "</h3>
                        <p><strong>Status Baru:</strong> <span style="font-weight: bold; color: #28a745;">" + order.getStatus().name() + "</span></p>
                        <p><strong>Supplier:</strong> " + order.getSupplier().getCompanyName() + "</p>
                    </div>

                    <p>Silakan cek dashboard Anda untuk detail lebih lanjut.</p>
                </div>

                <div class="footer">
                    <p>&copy; 2024 SAVORA. All rights reserved.</p>
                </div>
            </body>
            </html>
            """;
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
}