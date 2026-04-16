package com.musicmatch.backend.dto;

import com.musicmatch.backend.model.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileSearchCardDTO {

    private Long id;
    private String username;
    private String city;
    private String profilePicture;
    private ExperienceLevel experienceLevel;
}