package com.musicmatch.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.musicmatch.backend.dto.ProfileSearchCardDTO;
import com.musicmatch.backend.dto.ProfileSearchRequest;
import com.musicmatch.backend.exception.ResourceNotFoundException;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.repository.ProfileBlockRepository;
import com.musicmatch.backend.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileSearchService {

    private final ProfileRepository profileRepository;
    private final ProfileBlockRepository blockRepository;

    private boolean isBlocked(Profile me, Profile other) {
        return blockRepository.existsByBlockerAndBlocked(me, other)
                || blockRepository.existsByBlockerAndBlocked(other, me);
    }

    public List<ProfileSearchCardDTO> search(Long userId, ProfileSearchRequest request) {

        Profile me = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Profile> results = profileRepository.searchProfiles(
                request.getQuery(),
                request.getExperienceLevel(),
                request.getStyleIds(),
                request.getInstrumentIds()
        );

        return results.stream()
                .filter(p -> !p.getId().equals(userId))
                .filter(p -> !isBlocked(me, p))
                .map(p -> new ProfileSearchCardDTO(
                        p.getId(),
                        p.getUser().getUsername(),
                        p.getCity() != null ? p.getCity().getName() : null,
                        p.getProfilePicture(),
                        p.getExperienceLevel()
                ))
                .collect(Collectors.toList());
    }
}