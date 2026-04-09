package com.musicmatch.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.musicmatch.backend.dto.MusicalOptionsResponse;
import com.musicmatch.backend.dto.UpdateMusicalProfileRequest;
import com.musicmatch.backend.dto.UpdateProfileInfoRequest;
import com.musicmatch.backend.model.City;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CityRepository cityRepository;

    @PutMapping("/{userId}/info")
    public void updateProfileInfo(
            @PathVariable Long userId,
            @RequestBody UpdateProfileInfoRequest request) {

        profileService.updateProfileInfo(userId, request);
    }

    @GetMapping("/musical-options")
    public MusicalOptionsResponse getMusicalOptions() {
        return profileService.getMusicalOptions();
    }

    @GetMapping("/cities")
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @PutMapping("/{userId}/musical")
    public void updateMusicalProfile(
            @PathVariable Long userId,
            @RequestBody UpdateMusicalProfileRequest request) {

        profileService.updateMusicalProfile(userId, request);
    }

}