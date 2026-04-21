package com.musicmatch.backend.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@musicmatch.com}")
    private String from;

    @Value("${app.auth.verify-email-url-base}")
    private String verifyEmailUrlBase;

    @Value("${app.auth.reset-password-url-base}")
    private String resetPasswordUrlBase;

    public void sendVerificationEmail(String to, String username, String token) {
        String verifyUrl = verifyEmailUrlBase + token;
        String safeUsername = escape(username);

        String subject = "Verifica tu cuenta de MusicMatch";

        String plainText =
                "Hola " + username + ",\n\n" +
                "Gracias por registrarte en MusicMatch.\n\n" +
                "Verifica tu cuenta desde este enlace:\n" +
                verifyUrl + "\n\n" +
                "Si no has creado una cuenta, puedes ignorar este mensaje.";

        String html = buildEmailTemplate(
                "Verifica tu cuenta",
                """
                Hola <strong>%s</strong>,<br><br>
                Gracias por registrarte en MusicMatch.<br><br>
                Pulsa el botón para verificar tu cuenta y continuar.
                """.formatted(safeUsername),
                "Verificar cuenta",
                verifyUrl,
                """
                Si el botón no funciona, copia y pega este enlace en tu navegador:
                """,
                verifyUrl,
                null
        );

        sendHtmlEmail(to, subject, plainText, html);
    }

    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetUrl = resetPasswordUrlBase + token;
        String safeUsername = escape(username);
        String safeToken = escape(token);

        String subject = "Recupera tu contraseña de MusicMatch";

        String plainText =
                "Hola " + username + ",\n\n" +
                "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
                "Puedes hacerlo desde este enlace:\n" +
                resetUrl + "\n\n" +
                "Si prefieres usar el token manualmente, aquí lo tienes:\n" +
                token + "\n\n" +
                "Si no has solicitado este cambio, ignora este mensaje.";

        String html = buildEmailTemplate(
                "Recupera tu contraseña",
                """
                Hola <strong>%s</strong>,<br><br>
                Hemos recibido una solicitud para restablecer tu contraseña.<br><br>
                Pulsa el botón para continuar con el proceso.
                """.formatted(safeUsername),
                "Restablecer contraseña",
                resetUrl,
                """
                Si el botón no funciona, prueba con este enlace o utiliza el código manual dentro de la app.
                """,
                resetUrl,
                safeToken
        );

        sendHtmlEmail(to, subject, plainText, html);
    }

    private void sendHtmlEmail(String to, String subject, String plainText, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo construir el correo", e);
        }
    }

    private String buildEmailTemplate(
            String title,
            String bodyHtml,
            String buttonText,
            String buttonUrl,
            String fallbackText,
            String fallbackUrl,
            String token
    ) {
        String tokenBlock = "";

        if (token != null && !token.isBlank()) {
            tokenBlock = """
                    <div class="token-box">
                        <div class="token-label">Código de recuperación</div>
                        <div class="token-value">%s</div>
                    </div>
                    """.formatted(token);
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>MusicMatch</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #f5f7f6;
                            font-family: Arial, Helvetica, sans-serif;
                            color: #1f2937;
                        }

                        .wrapper {
                            width: 100%%;
                            padding: 32px 16px;
                            box-sizing: border-box;
                        }

                        .card {
                            max-width: 560px;
                            margin: 0 auto;
                            background: #ffffff;
                            border-radius: 20px;
                            overflow: hidden;
                            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
                        }

                        .header {
                            background: #184f45;
                            padding: 28px 24px;
                            text-align: center;
                        }

                        .header h1 {
                            margin: 0;
                            color: white;
                            font-size: 26px;
                        }

                        .content {
                            padding: 32px 24px;
                            line-height: 1.65;
                            font-size: 16px;
                            color: #374151;
                        }

                        .button-wrap {
                            text-align: center;
                            margin: 28px 0;
                        }

                        .button {
                            display: inline-block;
                            background: #2bb79f;
                            color: white !important;
                            text-decoration: none;
                            padding: 14px 24px;
                            border-radius: 12px;
                            font-weight: bold;
                            font-size: 15px;
                        }

                        .fallback {
                            margin-top: 20px;
                            font-size: 14px;
                            color: #6b7280;
                            word-break: break-word;
                        }

                        .link-box {
                            margin-top: 10px;
                            padding: 12px 14px;
                            background: #f3f4f6;
                            border-radius: 12px;
                            font-size: 13px;
                            color: #374151;
                            word-break: break-all;
                        }

                        .token-box {
                            margin-top: 22px;
                            padding: 16px;
                            background: #eef8f6;
                            border-radius: 14px;
                            text-align: center;
                        }

                        .token-label {
                            font-size: 13px;
                            color: #6b7280;
                            margin-bottom: 8px;
                        }

                        .token-value {
                            font-size: 18px;
                            font-weight: bold;
                            color: #184f45;
                            word-break: break-all;
                        }

                        .footer {
                            padding: 18px 24px 28px;
                            text-align: center;
                            font-size: 13px;
                            color: #6b7280;
                        }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="card">
                            <div class="header">
                                <h1>%s</h1>
                            </div>

                            <div class="content">
                                %s

                                <div class="button-wrap">
                                    <a class="button" href="%s">%s</a>
                                </div>

                                <div class="fallback">
                                    %s
                                </div>

                                <div class="link-box">%s</div>

                                %s
                            </div>

                            <div class="footer">
                                Si no has solicitado esta acción, puedes ignorar este mensaje.<br>
                                MusicMatch
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                escape(title),
                bodyHtml,
                escape(buttonUrl),
                escape(buttonText),
                escape(fallbackText),
                escape(fallbackUrl),
                tokenBlock
        );
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}