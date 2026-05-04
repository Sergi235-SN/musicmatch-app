package com.musicmatch.backend;

import com.musicmatch.backend.dto.ChatResponse;
import com.musicmatch.backend.exception.BadRequestException;
import com.musicmatch.backend.exception.ForbiddenException;
import com.musicmatch.backend.model.Chat;
import com.musicmatch.backend.model.ChatStatus;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.repository.ChatRepository;
import com.musicmatch.backend.repository.MessageRepository;
import com.musicmatch.backend.repository.ProfileBlockRepository;
import com.musicmatch.backend.repository.ProfileRepository;
import com.musicmatch.backend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ProfileBlockRepository blockRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void requestOrGetChatRejectsChatWithMyself() {
        Profile profile = new Profile();
        profile.setId(1L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> chatService.requestOrGetChat(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No puedes iniciar un chat contigo mismo");
    }

    @Test
    void requestOrGetChatReturnsExistingChat() {
        Profile from = new Profile();
        from.setId(1L);

        Profile to = new Profile();
        to.setId(2L);

        Chat existingChat = new Chat();
        existingChat.setId(10L);
        existingChat.setUser1(from);
        existingChat.setUser2(to);
        existingChat.setStatus(ChatStatus.PENDING);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(from));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(to));
        when(chatRepository.findBetweenProfiles(from, to)).thenReturn(Optional.of(existingChat));

        ChatResponse response = chatService.requestOrGetChat(1L, 2L);

        assertThat(response.getChatId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.isCreated()).isFalse();
    }

    @Test
    void sendMessageRejectsBlankContent() {
        Profile user1 = new Profile();
        user1.setId(1L);

        Profile user2 = new Profile();
        user2.setId(2L);

        Chat chat = new Chat();
        chat.setId(10L);
        chat.setUser1(user1);
        chat.setUser2(user2);
        chat.setStatus(ChatStatus.ACTIVE);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> chatService.sendMessage(10L, 1L, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("El mensaje no puede estar vacío");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsUserThatIsNotParticipant() {
        Profile user1 = new Profile();
        user1.setId(1L);

        Profile user2 = new Profile();
        user2.setId(2L);

        Profile outsider = new Profile();
        outsider.setId(3L);

        Chat chat = new Chat();
        chat.setId(10L);
        chat.setUser1(user1);
        chat.setUser2(user2);
        chat.setStatus(ChatStatus.ACTIVE);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(profileRepository.findById(3L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> chatService.sendMessage(10L, 3L, "Hola"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("No autorizado");

        verify(messageRepository, never()).save(any());
    }
}