package com.musicmatch.backend;

import com.musicmatch.backend.exception.BadRequestException;
import com.musicmatch.backend.repository.ChatRepository;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.repository.InstrumentRepository;
import com.musicmatch.backend.repository.ProfileBlockRepository;
import com.musicmatch.backend.repository.ProfileInstrumentRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.StyleRepository;
import com.musicmatch.backend.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private StyleRepository styleRepository;

    @Mock
    private ProfileInstrumentRepository profileInstrumentRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ProfileBlockRepository blockRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void saveProfileImageRejectsEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> profileService.saveProfileImage(1L, emptyFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Archivo vacío");
    }

    @Test
    void saveProfileImageRejectsUnsupportedFormat() {
        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "avatar.gif",
                "image/gif",
                "contenido".getBytes()
        );

        assertThatThrownBy(() -> profileService.saveProfileImage(1L, gifFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Solo JPG o PNG");
    }
}