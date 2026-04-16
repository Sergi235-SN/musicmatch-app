package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatPreview {
    private Long chatId;
    private Long otherUserId;
    private String otherUsername;
    private String otherProfileImage;
    private String lastMessage;
    private String status;
}