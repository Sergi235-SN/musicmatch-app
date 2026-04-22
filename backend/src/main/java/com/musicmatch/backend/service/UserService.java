package com.musicmatch.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.EmailRequest;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RefreshTokenRequest;
import com.musicmatch.backend.dto.RefreshTokenResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.ResetPasswordRequest;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.dto.VerificationState;
import com.musicmatch.backend.dto.VerificationStatusResponse;
import com.musicmatch.backend.model.EmailVerificationToken;
import com.musicmatch.backend.model.PasswordResetToken;
import com.musicmatch.backend.model.PendingRegistration;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.EmailVerificationTokenRepository;
import com.musicmatch.backend.repository.PasswordResetTokenRepository;
import com.musicmatch.backend.repository.PendingRegistrationRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.UserRepository;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final EmailService emailService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private static final int VERIFICATION_TOKEN_HOURS = 24;
    private static final int RESET_TOKEN_MINUTES = 60;

    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {

        String username = normalizeUsername(request != null ? request.getUsername() : null);
        String email = normalizeEmail(request != null ? request.getEmail() : null);
        String password = request != null ? request.getPassword() : null;

        if (username.length() < 3) {
            return new ApiResponse<>(false,
                    "El nombre de usuario debe tener al menos 3 caracteres",
                    null);
        }

        if (username.contains(" ")) {
            return new ApiResponse<>(false,
                    "El nombre de usuario no puede contener espacios",
                    null);
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ApiResponse<>(false,
                    "El formato del email no es válido",
                    null);
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            return new ApiResponse<>(false,
                    "La contraseña no cumple el mínimo de seguridad",
                    null);
        }

        User existingUserByEmail = userRepository.findByEmail(email).orElse(null);
        if (existingUserByEmail != null) {
            if (existingUserByEmail.isEmailVerified()) {
                return new ApiResponse<>(false, "El email ya está registrado", null);
            }

            createAndSendVerificationToken(existingUserByEmail);

            UserResponse response = new UserResponse(
                    existingUserByEmail.getId(),
                    existingUserByEmail.getUsername(),
                    existingUserByEmail.getEmail(),
                    null,
                    null,
                    false
            );

            return new ApiResponse<>(
                    true,
                    "Ya existía una cuenta pendiente de verificación. Te hemos enviado un nuevo correo.",
                    response
            );
        }

        User existingUserByUsername = userRepository.findByUsername(username).orElse(null);
        if (existingUserByUsername != null) {
            return new ApiResponse<>(false, "El nombre de usuario ya está en uso", null);
        }

        PendingRegistration pendingByEmail = pendingRegistrationRepository.findByEmail(email).orElse(null);
        if (pendingByEmail != null && isReusablePending(pendingByEmail)) {
            pendingRegistrationRepository.delete(pendingByEmail);
            pendingByEmail = null;
        }

        PendingRegistration pendingByUsername = pendingRegistrationRepository.findByUsername(username).orElse(null);
        if (pendingByUsername != null && isReusablePending(pendingByUsername)) {
            pendingRegistrationRepository.delete(pendingByUsername);

            if (pendingByEmail != null && Objects.equals(pendingByEmail.getId(), pendingByUsername.getId())) {
                pendingByEmail = null;
            }

            pendingByUsername = null;
        }

        if (pendingByUsername != null && !email.equals(pendingByUsername.getEmail())) {
            return new ApiResponse<>(false, "El nombre de usuario ya está en uso", null);
        }

        PendingRegistration pending = pendingByEmail;

        if (pending == null && pendingByUsername != null && email.equals(pendingByUsername.getEmail())) {
            pending = pendingByUsername;
        }

        if (pending == null) {
            pending = new PendingRegistration();
            pending.setCreatedAt(LocalDateTime.now());
        }

        pending.setUsername(username);
        pending.setEmail(email);
        pending.setPasswordHash(passwordEncoder.encode(password));
        pending.setUsedAt(null);

        createAndSendPendingRegistrationToken(pending);

        UserResponse response = new UserResponse(
                null,
                username,
                email,
                null,
                null,
                false
        );

        return new ApiResponse<>(
                true,
                "Solicitud registrada correctamente. Te hemos enviado un correo para verificar tu cuenta.",
                response
        );
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {

        String email = normalizeEmail(request != null ? request.getEmail() : null);
        String password = request != null ? request.getPassword() : null;

        if (email.isBlank() || password == null || password.isBlank()) {
            return new ApiResponse<>(false, "Email y contraseña son obligatorios", null);
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            PendingRegistration pending = pendingRegistrationRepository.findByEmail(email).orElse(null);

            if (pending != null && pending.getUsedAt() == null) {
                if (isExpired(pending)) {
                    return new ApiResponse<>(false,
                            "Tu solicitud de verificación ha expirado. Solicita un nuevo correo de verificación.",
                            null);
                }

                return new ApiResponse<>(false,
                        "Debes verificar tu correo antes de iniciar sesión",
                        null);
            }

            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());

        if (!matches) {
            return new ApiResponse<>(false, "Credenciales inválidas", null);
        }

        if (!user.isEmailVerified()) {
            return new ApiResponse<>(false, "Debes verificar tu correo antes de iniciar sesión", null);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        LoginResponse data = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                accessToken,
                refreshToken,
                true
        );

        return new ApiResponse<>(true, "Login exitoso", data);
    }

    public ApiResponse<RefreshTokenResponse> refresh(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return new ApiResponse<>(false, "Refresh token requerido", null);
        }

        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return new ApiResponse<>(false, "Refresh token inválido", null);
        }

        Long userId = jwtUtil.extractUserId(refreshToken);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        if (!user.isEmailVerified()) {
            return new ApiResponse<>(false, "Debes verificar tu correo antes de continuar", null);
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());

        RefreshTokenResponse data = new RefreshTokenResponse(newAccessToken);

        return new ApiResponse<>(true, "Token renovado correctamente", data);
    }

    @Transactional
    public ApiResponse<Void> verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            return new ApiResponse<>(false, "Token de verificación requerido", null);
        }

        ApiResponse<Void> pendingVerificationResult = verifyPendingRegistrationToken(token);
        if (pendingVerificationResult != null) {
            return pendingVerificationResult;
        }

        return verifyLegacyEmailToken(token);
    }

    @Transactional
    public ApiResponse<Void> resendVerification(EmailRequest request) {
        String email = normalizeEmail(request != null ? request.getEmail() : null);

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ApiResponse<>(false, "El formato del email no es válido", null);
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (user.isEmailVerified()) {
                return new ApiResponse<>(true, "Tu correo ya está verificado", null);
            }

            createAndSendVerificationToken(user);
            return new ApiResponse<>(true, "Te hemos enviado un nuevo correo de verificación", null);
        }

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(email).orElse(null);

        if (pending == null) {
            return new ApiResponse<>(true,
                    "Si el correo existe, te hemos enviado un nuevo email de verificación",
                    null);
        }

        if (isReusablePending(pending)) {
            pendingRegistrationRepository.delete(pending);
            return new ApiResponse<>(true,
                    "Si el correo existe, te hemos enviado un nuevo email de verificación",
                    null);
        }

        pending.setUsedAt(null);
        createAndSendPendingRegistrationToken(pending);

        return new ApiResponse<>(true, "Te hemos enviado un nuevo correo de verificación", null);
    }

    @Transactional
    public ApiResponse<Void> forgotPassword(EmailRequest request) {
        String email = normalizeEmail(request != null ? request.getEmail() : null);

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ApiResponse<>(false, "El formato del email no es válido", null);
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return new ApiResponse<>(true,
                    "Si el correo existe, te hemos enviado instrucciones para recuperar la contraseña",
                    null);
        }

        createAndSendPasswordResetToken(user);

        return new ApiResponse<>(true,
                "Si el correo existe, te hemos enviado instrucciones para recuperar la contraseña",
                null);
    }

    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            return new ApiResponse<>(false, "Token de recuperación requerido", null);
        }

        if (request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return new ApiResponse<>(false, "La nueva contraseña no cumple el mínimo de seguridad", null);
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken()).orElse(null);

        if (resetToken == null) {
            return new ApiResponse<>(false, "Token de recuperación inválido", null);
        }

        if (resetToken.getUsedAt() != null) {
            return new ApiResponse<>(false, "Este enlace de recuperación ya fue utilizado", null);
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new ApiResponse<>(false, "El enlace de recuperación ha expirado", null);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.deleteAllByUser_Id(user.getId());

        return new ApiResponse<>(true, "Contraseña actualizada correctamente", null);
    }

    public ApiResponse<VerificationStatusResponse> getVerificationStatus(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            return new ApiResponse<>(false, "Email requerido", null);
        }

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user != null) {
            VerificationStatusResponse data = new VerificationStatusResponse(
                    user.getId(),
                    user.getEmail(),
                    user.isEmailVerified(),
                    user.isEmailVerified() ? VerificationState.VERIFIED : VerificationState.PENDING
            );

            return new ApiResponse<>(true, "Estado de verificación obtenido", data);
        }

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(normalizedEmail).orElse(null);

        if (pending != null && pending.getUsedAt() == null) {
            VerificationStatusResponse data = new VerificationStatusResponse(
                    null,
                    pending.getEmail(),
                    false,
                    isExpired(pending) ? VerificationState.EXPIRED : VerificationState.PENDING
            );

            return new ApiResponse<>(true, "Estado de verificación obtenido", data);
        }

        VerificationStatusResponse data = new VerificationStatusResponse(
                null,
                normalizedEmail,
                false,
                VerificationState.NOT_FOUND
        );

        return new ApiResponse<>(true, "Estado de verificación obtenido", data);
    }

    private ApiResponse<Void> verifyPendingRegistrationToken(String token) {
        String tokenHash = hashToken(token);

        PendingRegistration pending = pendingRegistrationRepository.findByTokenHash(tokenHash).orElse(null);

        if (pending == null) {
            return null;
        }

        if (pending.getUsedAt() != null) {
            return new ApiResponse<>(false, "Este enlace de verificación ya fue utilizado", null);
        }

        if (isExpired(pending)) {
            return new ApiResponse<>(false, "El enlace de verificación ha expirado", null);
        }

        if (userRepository.findByEmail(pending.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "Ese correo ya fue verificado previamente", null);
        }

        if (userRepository.findByUsername(pending.getUsername()).isPresent()) {
            return new ApiResponse<>(false, "El nombre de usuario ya está en uso", null);
        }

        User user = new User();
        user.setUsername(pending.getUsername());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPasswordHash());
        user.setEmailVerified(true);

        User saved = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(saved);
        profileRepository.save(profile);

        pending.setUsedAt(LocalDateTime.now());
        pendingRegistrationRepository.save(pending);

        return new ApiResponse<>(true, "Correo verificado correctamente", null);
    }

    private ApiResponse<Void> verifyLegacyEmailToken(String token) {
        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null) {
            return new ApiResponse<>(false, "Token de verificación inválido", null);
        }

        if (verificationToken.getUsedAt() != null) {
            return new ApiResponse<>(false, "Este enlace de verificación ya fue utilizado", null);
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new ApiResponse<>(false, "El enlace de verificación ha expirado", null);
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        return new ApiResponse<>(true, "Correo verificado correctamente", null);
    }

    private void createAndSendPendingRegistrationToken(PendingRegistration pending) {
        String rawToken = UUID.randomUUID().toString();

        if (pending.getCreatedAt() == null) {
            pending.setCreatedAt(LocalDateTime.now());
        }

        pending.setTokenHash(hashToken(rawToken));
        pending.setExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));

        pendingRegistrationRepository.save(pending);

        emailService.sendVerificationEmail(
                pending.getEmail(),
                pending.getUsername(),
                rawToken
        );
    }

    private void createAndSendVerificationToken(User user) {
        emailVerificationTokenRepository.deleteAllByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));
        verificationToken.setUsedAt(null);

        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
    }

    private void createAndSendPasswordResetToken(User user) {
        passwordResetTokenRepository.deleteAllByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
        resetToken.setUsedAt(null);

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
    }

    private boolean isExpired(PendingRegistration pending) {
        return pending.getExpiresAt() == null || pending.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isReusablePending(PendingRegistration pending) {
        return pending.getUsedAt() != null || isExpired(pending);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar el hash del token", e);
        }
    }
}