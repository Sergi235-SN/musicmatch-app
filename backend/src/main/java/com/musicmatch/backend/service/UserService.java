package com.musicmatch.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.musicmatch.backend.dto.VerificationStatusResponse;

import lombok.RequiredArgsConstructor;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.EmailRequest;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RefreshTokenRequest;
import com.musicmatch.backend.dto.RefreshTokenResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.ResetPasswordRequest;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.model.EmailVerificationToken;
import com.musicmatch.backend.model.PasswordResetToken;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.EmailVerificationTokenRepository;
import com.musicmatch.backend.repository.PasswordResetTokenRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.UserRepository;
import com.musicmatch.backend.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private static final int VERIFICATION_TOKEN_HOURS = 24;
    private static final int RESET_TOKEN_MINUTES = 60;

    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            return new ApiResponse<>(false,
                    "El nombre de usuario debe tener al menos 3 caracteres",
                    null);
        }

        if (request.getUsername().contains(" ")) {
            return new ApiResponse<>(false,
                    "El nombre de usuario no puede contener espacios",
                    null);
        }

        if (request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            return new ApiResponse<>(false,
                    "El formato del email no es válido",
                    null);
        }

        if (request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return new ApiResponse<>(false,
                    "La contraseña no cumple el mínimo de seguridad",
                    null);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "El email ya está registrado", null);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ApiResponse<>(false, "El nombre de usuario ya está en uso", null);
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        User saved = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(saved);
        profileRepository.save(profile);

        createAndSendVerificationToken(saved);

        UserResponse response = new UserResponse(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            null,
            null,
            false
        );

        return new ApiResponse<>(
            true,
            "Usuario registrado correctamente. Te hemos enviado un correo para verificar tu cuenta.",
            response
        );
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

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

    @Transactional
    public ApiResponse<Void> resendVerification(EmailRequest request) {
        if (request == null || request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            return new ApiResponse<>(false, "El formato del email no es válido", null);
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);

        if (user == null) {
            return new ApiResponse<>(true, "Si el correo existe, te hemos enviado un nuevo email de verificación", null);
        }

        if (user.isEmailVerified()) {
            return new ApiResponse<>(true, "Tu correo ya está verificado", null);
        }

        createAndSendVerificationToken(user);

        return new ApiResponse<>(true, "Te hemos enviado un nuevo correo de verificación", null);
    }

    @Transactional
    public ApiResponse<Void> forgotPassword(EmailRequest request) {
        if (request == null || request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            return new ApiResponse<>(false, "El formato del email no es válido", null);
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);

        if (user == null) {
            return new ApiResponse<>(true, "Si el correo existe, te hemos enviado instrucciones para recuperar la contraseña", null);
        }

        createAndSendPasswordResetToken(user);

        return new ApiResponse<>(true, "Si el correo existe, te hemos enviado instrucciones para recuperar la contraseña", null);
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

    public ApiResponse<VerificationStatusResponse> getVerificationStatus(String email) {
        if (email == null || email.isBlank()) {
            return new ApiResponse<>(false, "Email requerido", null);
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        VerificationStatusResponse data = new VerificationStatusResponse(
                user.getId(),
                user.getEmail(),
                user.isEmailVerified()
        );

        return new ApiResponse<>(true, "Estado de verificación obtenido", data);
    }
}