package com.example.vozni_park.service;

import com.example.vozni_park.entity.OtpCode;
import com.example.vozni_park.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String email) {
        // Delete any existing OTPs for this email before creating a new one
        otpCodeRepository.deleteAllByEmail(email);

        String code = generateCode();

        OtpCode otp = new OtpCode();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        otpCodeRepository.save(otp);

        sendOtpEmail(email, code);
    }

    @Transactional
    public boolean validateOtp(String email, String code) {
        Optional<OtpCode> otpOpt = otpCodeRepository.findValidOtp(email, code, LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            log.warn("Invalid or expired OTP attempt for email: {}", email);
            return false;
        }

        OtpCode otp = otpOpt.get();
        otp.setUsed(1);
        otpCodeRepository.save(otp);

        log.info("OTP validated successfully for email: {}", email);
        return true;
    }

    private String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendOtpEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Vozni Park — Kod za verifikaciju");
            helper.setText(buildEmailHtml(code), true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildEmailHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="sr">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f7fa; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                        .header { background-color: #2B5797; padding: 24px 32px; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 22px; }
                        .content { padding: 32px; }
                        .content p { color: #333; line-height: 1.6; font-size: 15px; }
                        .code-box { background-color: #f4f7fa; border: 2px dashed #2B5797; border-radius: 8px; text-align: center; padding: 24px; margin: 24px 0; }
                        .code-box span { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #2B5797; }
                        .footer { padding: 20px 32px; background-color: #f8f9fa; border-top: 1px solid #e9ecef; }
                        .footer p { color: #888; font-size: 12px; margin: 0; }
                    </style>
                </head>
                <body>
                <div class="container">
                    <div class="header"><h1>Vozni Park</h1></div>
                    <div class="content">
                        <p>Poštovani/a,</p>
                        <p>Vaš kod za verifikaciju identiteta je:</p>
                        <div class="code-box"><span>%s</span></div>
                        <p>Kod važi <strong>%d minuta</strong>. Nakon isteka, potrebno je ponovo zatražiti kod.</p>
                        <p>Ako niste Vi zatražili ovaj kod, slobodno zanemarite ovaj email.</p>
                    </div>
                    <div class="footer"><p>Vozni Park — Sistem za upravljanje voznim parkom</p></div>
                </div>
                </body>
                </html>
                """.formatted(code, otpExpiryMinutes);
    }
}