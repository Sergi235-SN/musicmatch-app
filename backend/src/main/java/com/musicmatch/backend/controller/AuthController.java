package com.musicmatch.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.EmailRequest;
import com.musicmatch.backend.dto.InstrumentLevelResponse;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RefreshTokenRequest;
import com.musicmatch.backend.dto.RefreshTokenResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.ResetPasswordRequest;
import com.musicmatch.backend.dto.UserProfileResponse;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.dto.VerificationStatusResponse;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.Style;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.UserRepository;
import com.musicmatch.backend.service.UserService;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return userService.refresh(request);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        ApiResponse<Void> result = userService.verifyEmail(token);

        if (result.isSuccess()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildSuccessHtml());
        }

        return ResponseEntity.badRequest()
                .contentType(MediaType.TEXT_HTML)
                .body(buildErrorHtml(result.getMessage()));
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@RequestBody EmailRequest request) {
        return userService.resendVerification(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody EmailRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }

    @GetMapping("/reset-password-page")
    public ResponseEntity<String> resetPasswordPage(@RequestParam("token") String token) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(buildResetPasswordPageHtml(token, null));
    }

    @PostMapping(value = "/reset-password-page", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> submitResetPasswordPage(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword) {

        if (password == null || password.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildResetPasswordPageHtml(token, "Debes completar ambos campos."));
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildResetPasswordPageHtml(token, "Las contraseñas no coinciden."));
        }

        ApiResponse<Void> result = userService.resetPassword(new ResetPasswordRequest(token, password));

        if (result.isSuccess()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildResetPasswordSuccessHtml());
        }

        return ResponseEntity.badRequest()
                .contentType(MediaType.TEXT_HTML)
                .body(buildResetPasswordPageHtml(token, result.getMessage()));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ApiResponse<>(false, "Token inválido", null);
        }

        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token) || !jwtUtil.isAccessToken(token)) {
            return new ApiResponse<>(false, "Token inválido", null);
        }

        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        Profile profile = user.getProfile();

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),

                profile.getBiography(),

                profile.getCity() != null ? profile.getCity().getId() : null,
                profile.getCity() != null ? profile.getCity().getName() : null,

                profile.getExperienceLevel(),

                profile.getStyles()
                        .stream()
                        .map(Style::getId)
                        .toList(),

                profile.getProfileInstruments()
                        .stream()
                        .map(pi -> new InstrumentLevelResponse(
                                pi.getInstrument().getId(),
                                pi.getLevel()
                        ))
                        .toList(),

                profile.getProfilePicture(),
                user.getEmail()
        );

        return new ApiResponse<>(true, "Usuario encontrado", response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/verification-status")
    public ApiResponse<VerificationStatusResponse> getVerificationStatus(
            @RequestParam("email") String email) {
        return userService.getVerificationStatus(email);
    }

    private String buildSuccessHtml() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Correo verificado - MusicMatch</title>
                    <style>
                        :root {
                            --color-principal: #184f45;
                            --color-secundario: #2bb79f;
                            --color-fondo: #f6f8f7;
                            --color-texto: #1f2937;
                            --color-texto-suave: #6b7280;
                            --color-tarjeta: #ffffff;
                            --color-sombra: rgba(24, 79, 69, 0.12);
                        }
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, Helvetica, sans-serif;
                            background: var(--color-fondo);
                            color: var(--color-texto);
                            display: flex;
                            flex-direction: column;
                        }
                        .topbar, .bottombar {
                            height: 72px;
                            background: var(--color-principal);
                            flex-shrink: 0;
                        }
                        .wrapper {
                            flex: 1;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 32px 20px;
                        }
                        .card {
                            width: 100%;
                            max-width: 460px;
                            background: var(--color-tarjeta);
                            border-radius: 24px;
                            padding: 36px 28px;
                            text-align: center;
                            box-shadow: 0 16px 40px var(--color-sombra);
                        }
                        .icon {
                            width: 88px;
                            height: 88px;
                            margin: 0 auto 24px;
                            border-radius: 50%;
                            background: rgba(43, 183, 159, 0.14);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 42px;
                            color: var(--color-secundario);
                        }
                        h1 {
                            margin: 0 0 14px;
                            font-size: 28px;
                            color: var(--color-principal);
                        }
                        p {
                            margin: 0;
                            font-size: 16px;
                            line-height: 1.6;
                            color: var(--color-texto-suave);
                        }
                        .brand {
                            margin-top: 18px;
                            font-size: 14px;
                            color: var(--color-texto-suave);
                        }
                    </style>
                </head>
                <body>
                    <div class="topbar"></div>
                    <div class="wrapper">
                        <div class="card">
                            <div class="icon">✓</div>
                            <h1>Correo verificado correctamente</h1>
                            <p>
                                Tu cuenta ya está activa. Puedes volver a MusicMatch
                                y continuar con la configuración de tu perfil.
                            </p>
                            <div class="brand">MusicMatch</div>
                        </div>
                    </div>
                    <div class="bottombar"></div>
                </body>
                </html>
                """;
    }

    private String buildErrorHtml(String message) {
        String safeMessage = escapeHtml(
                message != null ? message : "Ha ocurrido un error inesperado"
        );

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Error de verificación - MusicMatch</title>
                    <style>
                        :root {
                            --color-principal: #184f45;
                            --color-secundario: #2bb79f;
                            --color-fondo: #f6f8f7;
                            --color-texto: #1f2937;
                            --color-texto-suave: #6b7280;
                            --color-error: #d9534f;
                            --color-error-fondo: #fdeceb;
                            --color-tarjeta: #ffffff;
                            --color-sombra: rgba(24, 79, 69, 0.12);
                        }
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, Helvetica, sans-serif;
                            background: var(--color-fondo);
                            color: var(--color-texto);
                            display: flex;
                            flex-direction: column;
                        }
                        .topbar, .bottombar {
                            height: 72px;
                            background: var(--color-principal);
                            flex-shrink: 0;
                        }
                        .wrapper {
                            flex: 1;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 32px 20px;
                        }
                        .card {
                            width: 100%;
                            max-width: 460px;
                            background: var(--color-tarjeta);
                            border-radius: 24px;
                            padding: 36px 28px;
                            text-align: center;
                            box-shadow: 0 16px 40px var(--color-sombra);
                        }
                        .icon {
                            width: 88px;
                            height: 88px;
                            margin: 0 auto 24px;
                            border-radius: 50%;
                            background: var(--color-error-fondo);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 40px;
                            color: var(--color-error);
                        }
                        h1 {
                            margin: 0 0 14px;
                            font-size: 28px;
                            color: var(--color-principal);
                        }
                        p {
                            margin: 0;
                            font-size: 16px;
                            line-height: 1.6;
                            color: var(--color-texto-suave);
                        }
                        .error-box {
                            margin-top: 20px;
                            padding: 14px 16px;
                            border-radius: 14px;
                            background: var(--color-error-fondo);
                            color: var(--color-error);
                            font-size: 14px;
                            line-height: 1.5;
                            font-weight: bold;
                        }
                        .brand {
                            margin-top: 18px;
                            font-size: 14px;
                            color: var(--color-texto-suave);
                        }
                    </style>
                </head>
                <body>
                    <div class="topbar"></div>
                    <div class="wrapper">
                        <div class="card">
                            <div class="icon">!</div>
                            <h1>No se pudo verificar el correo</h1>
                            <p>
                                El enlace no es válido, ha caducado o ya fue utilizado.
                                Puedes solicitar uno nuevo desde la aplicación.
                            </p>
                            <div class="error-box">%s</div>
                            <div class="brand">MusicMatch</div>
                        </div>
                    </div>
                    <div class="bottombar"></div>
                </body>
                </html>
                """.formatted(safeMessage);
    }

    private String buildResetPasswordPageHtml(String token, String errorMessage) {
        String safeToken = escapeHtml(token == null ? "" : token);
        String errorBlock = "";

        if (errorMessage != null && !errorMessage.isBlank()) {
            errorBlock = """
                    <div class="error-box">%s</div>
                    """.formatted(escapeHtml(errorMessage));
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Restablecer contraseña - MusicMatch</title>
                    <style>
                        :root {
                            --color-principal: #184f45;
                            --color-secundario: #2bb79f;
                            --color-fondo: #f6f8f7;
                            --color-texto: #1f2937;
                            --color-texto-suave: #6b7280;
                            --color-error: #d9534f;
                            --color-error-fondo: #fdeceb;
                            --color-tarjeta: #ffffff;
                            --color-sombra: rgba(24, 79, 69, 0.12);
                        }

                        * { box-sizing: border-box; }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, Helvetica, sans-serif;
                            background: var(--color-fondo);
                            color: var(--color-texto);
                            display: flex;
                            flex-direction: column;
                        }

                        .topbar, .bottombar {
                            height: 72px;
                            background: var(--color-principal);
                            flex-shrink: 0;
                        }

                        .wrapper {
                            flex: 1;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 32px 20px;
                        }

                        .card {
                            width: 100%%;
                            max-width: 480px;
                            background: var(--color-tarjeta);
                            border-radius: 24px;
                            padding: 36px 28px;
                            box-shadow: 0 16px 40px var(--color-sombra);
                        }

                        h1 {
                            margin: 0 0 12px;
                            text-align: center;
                            color: var(--color-principal);
                            font-size: 28px;
                        }

                        p {
                            margin: 0 0 22px;
                            text-align: center;
                            font-size: 15px;
                            line-height: 1.6;
                            color: var(--color-texto-suave);
                        }

                        label {
                            display: block;
                            margin-bottom: 8px;
                            font-weight: bold;
                            color: var(--color-texto);
                            font-size: 14px;
                        }

                        .password-wrapper {
                            position: relative;
                            width: 100%%;
                            margin-bottom: 18px;
                        }

                        input {
                            width: 100%%;
                            padding: 14px 50px 14px 14px;
                            border: 1px solid #d1d5db;
                            border-radius: 12px;
                            font-size: 15px;
                            outline: none;
                            transition: border-color 0.2s ease, box-shadow 0.2s ease;
                        }

                        input:focus {
                            border-color: var(--color-secundario);
                            box-shadow: 0 0 0 3px rgba(43, 183, 159, 0.12);
                        }

                        .toggle-password {
                            position: absolute;
                            top: 50%%;
                            right: 12px;
                            transform: translateY(-50%%);
                            width: 32px;
                            height: 32px;
                            border: none;
                            background: transparent;
                            cursor: pointer;
                            padding: 0;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            border-radius: 8px;
                            color: var(--color-texto-suave);
                            transition: background-color 0.2s ease, color 0.2s ease;
                        }

                        .toggle-password:hover {
                            background: rgba(24, 79, 69, 0.06);
                            color: var(--color-principal);
                        }

                        .toggle-password:focus-visible {
                            outline: 2px solid rgba(43, 183, 159, 0.35);
                            outline-offset: 2px;
                        }

                        .toggle-password svg {
                            width: 20px;
                            height: 20px;
                            stroke: currentColor;
                            fill: none;
                            stroke-width: 2;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                        }

                        .button {
                            width: 100%%;
                            border: none;
                            padding: 15px;
                            border-radius: 12px;
                            background: var(--color-secundario);
                            color: white;
                            font-size: 16px;
                            font-weight: bold;
                            cursor: pointer;
                        }

                        .button:hover {
                            filter: brightness(0.98);
                        }

                        .error-box {
                            margin-bottom: 18px;
                            padding: 14px 16px;
                            border-radius: 14px;
                            background: var(--color-error-fondo);
                            color: var(--color-error);
                            font-size: 14px;
                            line-height: 1.5;
                            font-weight: bold;
                        }

                        .brand {
                            margin-top: 18px;
                            text-align: center;
                            font-size: 14px;
                            color: var(--color-texto-suave);
                        }
                    </style>
                </head>
                <body>
                    <div class="topbar"></div>

                    <div class="wrapper">
                        <div class="card">
                            <h1>Restablecer contraseña</h1>
                            <p>
                                Introduce tu nueva contraseña para continuar.
                            </p>

                            %s

                            <form method="post" action="/api/auth/reset-password-page">
                                <input type="hidden" name="token" value="%s" />

                                <label for="password">Nueva contraseña</label>
                                <div class="password-wrapper">
                                    <input
                                        id="password"
                                        name="password"
                                        type="password"
                                        placeholder="Mínimo 8 caracteres"
                                        required
                                    />
                                    <button
                                        class="toggle-password"
                                        type="button"
                                        onclick="togglePassword('password', this)"
                                        aria-label="Mostrar contraseña"
                                        aria-pressed="false"
                                    >
                                        <span class="icon-eye">
                                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"></path>
                                                <circle cx="12" cy="12" r="3"></circle>
                                            </svg>
                                        </span>
                                    </button>
                                </div>

                                <label for="confirmPassword">Repetir contraseña</label>
                                <div class="password-wrapper">
                                    <input
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        type="password"
                                        placeholder="Vuelve a escribirla"
                                        required
                                    />
                                    <button
                                        class="toggle-password"
                                        type="button"
                                        onclick="togglePassword('confirmPassword', this)"
                                        aria-label="Mostrar contraseña"
                                        aria-pressed="false"
                                    >
                                        <span class="icon-eye">
                                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"></path>
                                                <circle cx="12" cy="12" r="3"></circle>
                                            </svg>
                                        </span>
                                    </button>
                                </div>

                                <button class="button" type="submit">Guardar contraseña</button>
                            </form>

                            <div class="brand">MusicMatch</div>
                        </div>
                    </div>

                    <div class="bottombar"></div>

                    <script>
                        const eyeIcon = `
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"></path>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        `;

                        const eyeOffIcon = `
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <path d="M3 3l18 18"></path>
                                <path d="M10.6 10.7a3 3 0 0 0 4.2 4.2"></path>
                                <path d="M9.9 5.1A10.9 10.9 0 0 1 12 5c6.5 0 10 7 10 7a17.7 17.7 0 0 1-3.2 4.2"></path>
                                <path d="M6.6 6.7C4.1 8.2 2.5 12 2.5 12A17.3 17.3 0 0 0 12 19c1.8 0 3.4-.4 4.8-1.1"></path>
                            </svg>
                        `;

                        function togglePassword(inputId, button) {
                            const input = document.getElementById(inputId);
                            const isHidden = input.type === 'password';

                            input.type = isHidden ? 'text' : 'password';
                            button.innerHTML = isHidden ? eyeOffIcon : eyeIcon;
                            button.setAttribute('aria-pressed', isHidden ? 'true' : 'false');
                            button.setAttribute(
                                'aria-label',
                                isHidden ? 'Ocultar contraseña' : 'Mostrar contraseña'
                            );
                        }
                    </script>
                </body>
                </html>
                """.formatted(errorBlock, safeToken);
    }

    private String buildResetPasswordSuccessHtml() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Contraseña actualizada - MusicMatch</title>
                    <style>
                        :root {
                            --color-principal: #184f45;
                            --color-secundario: #2bb79f;
                            --color-fondo: #f6f8f7;
                            --color-texto: #1f2937;
                            --color-texto-suave: #6b7280;
                            --color-tarjeta: #ffffff;
                            --color-sombra: rgba(24, 79, 69, 0.12);
                        }

                        * { box-sizing: border-box; }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, Helvetica, sans-serif;
                            background: var(--color-fondo);
                            color: var(--color-texto);
                            display: flex;
                            flex-direction: column;
                        }

                        .topbar, .bottombar {
                            height: 72px;
                            background: var(--color-principal);
                            flex-shrink: 0;
                        }

                        .wrapper {
                            flex: 1;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 32px 20px;
                        }

                        .card {
                            width: 100%;
                            max-width: 460px;
                            background: var(--color-tarjeta);
                            border-radius: 24px;
                            padding: 36px 28px;
                            text-align: center;
                            box-shadow: 0 16px 40px var(--color-sombra);
                        }

                        .icon {
                            width: 88px;
                            height: 88px;
                            margin: 0 auto 24px;
                            border-radius: 50%;
                            background: rgba(43, 183, 159, 0.14);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 42px;
                            color: var(--color-secundario);
                        }

                        h1 {
                            margin: 0 0 14px;
                            font-size: 28px;
                            color: var(--color-principal);
                        }

                        p {
                            margin: 0;
                            font-size: 16px;
                            line-height: 1.6;
                            color: var(--color-texto-suave);
                        }

                        .brand {
                            margin-top: 18px;
                            font-size: 14px;
                            color: var(--color-texto-suave);
                        }
                    </style>
                </head>
                <body>
                    <div class="topbar"></div>

                    <div class="wrapper">
                        <div class="card">
                            <div class="icon">✓</div>
                            <h1>Contraseña actualizada</h1>
                            <p>
                                Tu contraseña se ha cambiado correctamente.
                                Ya puedes volver a iniciar sesión en MusicMatch.
                            </p>
                            <div class="brand">MusicMatch</div>
                        </div>
                    </div>

                    <div class="bottombar"></div>
                </body>
                </html>
                """;
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}