package com.musicmatch.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.MusicalOptionsResponse;
import com.musicmatch.backend.dto.PublicProfileResponse;
import com.musicmatch.backend.dto.UpdateProfileRequest;
import com.musicmatch.backend.model.City;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.service.ProfileService;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CityRepository cityRepository;
    private final JwtUtil jwtUtil;

    @PatchMapping
    public void updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request
    ) {
        Long userId = extractUserId(authHeader);
        profileService.updateProfilePartial(userId, request);
    }

    @GetMapping("/musical-options")
    public MusicalOptionsResponse getMusicalOptions() {
        return profileService.getMusicalOptions();
    }

    @GetMapping("/cities")
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @PostMapping("/avatar")
    public ApiResponse<String> uploadProfileImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Long userId = extractUserId(authHeader);
        String avatarPath = profileService.saveProfileImage(userId, file);

        return new ApiResponse<>(
                true,
                "Avatar subido correctamente",
                avatarPath
        );
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads")
                .toAbsolutePath().normalize();

        Path filePath = uploadPath.resolve(filename).normalize();

        if (!filePath.startsWith(uploadPath)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(resource);
    }

    @GetMapping("/public/{userId}")
    public PublicProfileResponse getPublicProfile(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader
    ) {

        Long meId = extractUserId(authHeader);
        return profileService.getPublicProfile(meId, userId);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token inválido");
        }

        return jwtUtil.extractUserId(token);
    }
}