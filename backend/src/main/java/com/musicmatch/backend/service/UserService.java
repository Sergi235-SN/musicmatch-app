package com.musicmatch.backend.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.UserRepository;
import com.musicmatch.backend.utils.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;

    public ApiResponse<UserResponse> register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "El email ya está registrado", null);
        }

        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ApiResponse<>(false, "El nombre de usuario ya está en uso", null);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        Profile profile = new Profile();

        profile.setUser(saved);

        profileRepository.save(profile);

        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername());

        UserResponse response = new UserResponse(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            token
        );

        return new ApiResponse<>(true, "Usuario registrado correctamente", response);
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if(user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!matches) {
            return new ApiResponse<>(false, "Credenciales inválidas", null);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginResponse data = new LoginResponse(
                user.getId(),
                user.getUsername(),
                token
        );

        return new ApiResponse<>(true, "Login exitoso", data);
    }

}