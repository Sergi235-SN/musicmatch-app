package com.musicmatch.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicmatch.backend.dto.InstrumentLevelRequest;
import com.musicmatch.backend.dto.MusicalOptionsResponse;
import com.musicmatch.backend.dto.UpdateMusicalProfileRequest;
import com.musicmatch.backend.dto.UpdateProfileInfoRequest;
import com.musicmatch.backend.model.City;
import com.musicmatch.backend.model.Instrument;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.ProfileInstrument;
import com.musicmatch.backend.model.Style;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.repository.InstrumentRepository;
import com.musicmatch.backend.repository.ProfileInstrumentRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.StyleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final InstrumentRepository instrumentRepository;
    private final StyleRepository styleRepository;
    private final ProfileInstrumentRepository profileInstrumentRepository;
    private final CityRepository cityRepository;

    @Transactional
    public void updateProfileInfo(Long userId, UpdateProfileInfoRequest request) {

        Profile profile = profileRepository.findById(userId)
                .orElseThrow();

        profile.setBiography(request.getBiography());
        profile.setProfilePicture(request.getProfilePicture());

        if(request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow();
            profile.setCity(city);
        }

        profileRepository.save(profile);
    }

    public MusicalOptionsResponse getMusicalOptions() {

        return new MusicalOptionsResponse(
            instrumentRepository.findAll(),
            styleRepository.findAll()
        );
    }

    @Transactional
    public void updateMusicalProfile(Long userId, UpdateMusicalProfileRequest request) {

        Profile profile = profileRepository.findById(userId)
                .orElseThrow();

        // actualizar nivel global
        profile.setExperienceLevel(request.getExperienceLevel());

        // actualizar estilos
        Set<Style> styles = new HashSet<>(styleRepository.findAllById(request.getStyleIds()));
        profile.setStyles(styles);

        // eliminar instrumentos anteriores
        profileInstrumentRepository.deleteByProfileId(userId);

        List<ProfileInstrument> newInstruments = new ArrayList<>();

        for (InstrumentLevelRequest instrumentRequest : request.getInstruments()) {

            Instrument instrument = instrumentRepository
                    .findById(instrumentRequest.getInstrumentId())
                    .orElseThrow();

            ProfileInstrument pi = new ProfileInstrument();
            pi.setProfile(profile);
            pi.setInstrument(instrument);
            pi.setLevel(instrumentRequest.getLevel());

            newInstruments.add(pi);
        }

        profileInstrumentRepository.saveAll(newInstruments);
    }
}