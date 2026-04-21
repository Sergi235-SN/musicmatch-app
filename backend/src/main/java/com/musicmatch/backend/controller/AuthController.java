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
        String safeMessage = message != null ? message : "Ha ocurrido un error inesperado";

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
        String safeToken = token == null ? "" : token;
        String errorBlock = "";
        if (errorMessage != null && !errorMessage.isBlank()) {
            errorBlock = """
                    <div class="error-box">%s</div>
                    """.formatted(errorMessage);
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

                        input {
                            width: 100%%;
                            padding: 14px 14px;
                            margin-bottom: 18px;
                            border: 1px solid #d1d5db;
                            border-radius: 12px;
                            font-size: 15px;
                            outline: none;
                        }

                        input:focus {
                            border-color: var(--color-secundario);
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
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    placeholder="Mínimo 8 caracteres"
                                    required
                                />

                                <label for="confirmPassword">Repetir contraseña</label>
                                <input
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    type="password"
                                    placeholder="Vuelve a escribirla"
                                    required
                                />

                                <button class="button" type="submit">Guardar contraseña</button>
                            </form>

                            <div class="brand">MusicMatch</div>
                        </div>
                    </div>

                    <div class="bottombar"></div>
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
}
