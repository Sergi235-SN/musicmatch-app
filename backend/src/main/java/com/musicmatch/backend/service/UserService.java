package com.musicmatch.backend.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }

        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Usuario ya registrado");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
                    if(matches) {
                        return new LoginResponse(true, "Login exitoso", user.getId(), user.getUsername());
                    } else {
                        return new LoginResponse(false, "Credenciales inválidas", null, null);
                    }
                })
                .orElse(new LoginResponse(false, "Usuario no encontrado", null, null));
    }
}