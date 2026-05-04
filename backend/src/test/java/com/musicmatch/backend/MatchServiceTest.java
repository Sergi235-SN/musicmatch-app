package com.musicmatch.backend;

import com.musicmatch.backend.dto.MatchCandidatesResponse;
import com.musicmatch.backend.dto.SwipeResponse;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.repository.ChatRepository;
import com.musicmatch.backend.repository.MatchRepository;
import com.musicmatch.backend.repository.ProfileBlockRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.repository.ProfileSwipeRepository;
import com.musicmatch.backend.service.MatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileSwipeRepository swipeRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ProfileBlockRepository blockRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void getCandidatesReturnsMessageWhenProfileIsIncomplete() {
        Profile me = new Profile();
        me.setId(1L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(me));

        MatchCandidatesResponse response = matchService.getCandidates(1L);

        assertThat(response.isProfileComplete()).isFalse();
        assertThat(response.getMessage()).contains("Completa tu perfil");
        assertThat(response.getCandidates()).isEmpty();
        assertThat(response.isNoMoreCandidates()).isFalse();
    }

    @Test
    void swipeReturnsBlockedWhenUserIsBlocked() {
        Profile me = new Profile();
        me.setId(1L);

        Profile target = new Profile();
        target.setId(2L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(me));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(target));
        when(blockRepository.existsByBlockerAndBlocked(me, target)).thenReturn(true);

        SwipeResponse response = matchService.swipe(1L, 2L, true);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isMatch()).isFalse();
        assertThat(response.getMessage()).contains("Usuario bloqueado");

        verify(swipeRepository, never()).save(any());
    }

    @Test
    void swipeRejectsDuplicatedSwipe() {
        Profile me = new Profile();
        me.setId(1L);

        Profile target = new Profile();
        target.setId(2L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(me));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(target));
        when(swipeRepository.existsByFromProfileAndToProfile(me, target)).thenReturn(true);

        SwipeResponse response = matchService.swipe(1L, 2L, true);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isMatch()).isFalse();
        assertThat(response.getMessage()).contains("Ya hiciste swipe");

        verify(swipeRepository, never()).save(any());
    }
}