package com.musicmatch.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicmatch.backend.dto.InstrumentLevelRequest;
import com.musicmatch.backend.dto.InstrumentLevelResponse;
import com.musicmatch.backend.dto.MusicalOptionDTO;
import com.musicmatch.backend.dto.MusicalOptionsResponse;
import com.musicmatch.backend.dto.PublicProfileResponse;
import com.musicmatch.backend.dto.UpdateProfileRequest;
import com.musicmatch.backend.model.City;
import com.musicmatch.backend.model.Instrument;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.ProfileInstrument;
import com.musicmatch.backend.model.Style;
import com.musicmatch.backend.repository.ChatRepository;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.repository.InstrumentRepository;
import com.musicmatch.backend.repository.ProfileBlockRepository;
import com.musicmatch.backend.repository.ProfileInstrumentRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.StyleRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final InstrumentRepository instrumentRepository;
    private final StyleRepository styleRepository;
    private final ProfileInstrumentRepository profileInstrumentRepository;
    private final CityRepository cityRepository;
    private final ProfileBlockRepository blockRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public void updateProfilePartial(Long userId, UpdateProfileRequest request) {

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        if (request.getBiography() != null) {
            profile.setBiography(request.getBiography());
        }

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
            profile.setCity(city);
        }

        if (request.getExperienceLevel() != null) {
            profile.setExperienceLevel(request.getExperienceLevel());
        }

        if (request.getStyleIds() != null) {

            if (request.getStyleIds().isEmpty()) {
                profile.setStyles(new HashSet<>());
            } else {
                List<Style> foundStyles = styleRepository.findAllById(request.getStyleIds());

                if (foundStyles.size() != request.getStyleIds().size()) {
                    throw new RuntimeException("Algunos estilos no existen");
                }

                profile.setStyles(new HashSet<>(foundStyles));
            }
        }

        if (request.getInstruments() != null) {

            profileInstrumentRepository.deleteByProfileId(userId);

            if (!request.getInstruments().isEmpty()) {

                List<ProfileInstrument> newInstruments = new ArrayList<>();

                for (InstrumentLevelRequest instrumentRequest : request.getInstruments()) {

                    Instrument instrument = instrumentRepository
                            .findById(instrumentRequest.getInstrumentId())
                            .orElseThrow(() -> new RuntimeException("Instrumento no encontrado"));

                    ProfileInstrument pi = new ProfileInstrument();
                    pi.setProfile(profile);
                    pi.setInstrument(instrument);
                    pi.setLevel(instrumentRequest.getLevel());

                    newInstruments.add(pi);
                }

                profileInstrumentRepository.saveAll(newInstruments);
            }
        }
    }

    public MusicalOptionsResponse getMusicalOptions() {

        List<MusicalOptionDTO> instruments =
                instrumentRepository.findAll()
                        .stream()
                        .map(i -> new MusicalOptionDTO(i.getId(), i.getName()))
                        .toList();

        List<MusicalOptionDTO> styles =
                styleRepository.findAll()
                        .stream()
                        .map(s -> new MusicalOptionDTO(s.getId(), s.getName()))
                        .toList();

        return new MusicalOptionsResponse(instruments, styles);
    }

    public String saveProfileImage(Long userId, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }

        if (file.getSize() > 5_000_000) {
            throw new RuntimeException("Archivo demasiado grande");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean isJpg = "image/jpeg".equalsIgnoreCase(contentType)
                || "image/jpg".equalsIgnoreCase(contentType);

        boolean isPng = "image/png".equalsIgnoreCase(contentType);

        boolean hasValidExtension =
                filename != null &&
                (filename.toLowerCase().endsWith(".jpg")
                || filename.toLowerCase().endsWith(".jpeg")
                || filename.toLowerCase().endsWith(".png"));

        if (!(isJpg || isPng || hasValidExtension)) {
            throw new RuntimeException("Solo JPG o PNG");
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        String originalName = Paths.get(file.getOriginalFilename())
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        String fileName = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        Files.createDirectories(uploadPath);

        if (profile.getProfilePicture() != null) {
            Files.deleteIfExists(uploadPath.resolve(profile.getProfilePicture()));
        }

        Files.copy(file.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        profile.setProfilePicture(fileName);
        profileRepository.save(profile);

        return "/api/profile/avatar/" + fileName;
    }
    
    public PublicProfileResponse getPublicProfile(Long meId, Long targetId) {

        Profile me = profileRepository.findById(meId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Profile target = profileRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        var chatOpt = chatRepository.findBetweenProfiles(me, target);

        String chatStatus = "NONE";
        Long chatId = null;

        if (chatOpt.isPresent()) {
            var chat = chatOpt.get();
            chatStatus = chat.getStatus().name();
            chatId = chat.getId();
        }

        return new PublicProfileResponse(
            target.getId(),
            target.getUser().getUsername(),
            target.getBiography(),
            target.getCity() != null ? target.getCity().getName() : "",
            target.getCity() != null ? target.getCity().getId() : null,
            target.getProfilePicture(),
            target.getStyles().stream().map(Style::getId).toList(),
            target.getProfileInstruments().stream()
                .map(pi -> new InstrumentLevelResponse(
                    pi.getInstrument().getId(),
                    pi.getLevel()
                ))
                .toList(),
            target.getExperienceLevel(),

            blockedByMe(me, target),
            blockedMe(me, target),

            chatStatus,
            chatId
        );
    }

    private boolean blockedByMe(Profile me, Profile other) {
        return blockRepository.existsByBlockerAndBlocked(me, other);
    }

    private boolean blockedMe(Profile me, Profile other) {
        return blockRepository.existsByBlockerAndBlocked(other, me);
    }

}