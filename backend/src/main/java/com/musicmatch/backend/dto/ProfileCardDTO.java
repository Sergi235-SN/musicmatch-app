package com.musicmatch.backend.dto;

import java.util.List;

import com.musicmatch.backend.model.ExperienceLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileCardDTO {

    private Long id;
    private String username;
    private String bio;
    private String city;
    private String profilePicture;
    private List<Long> styles;
    private List<InstrumentLevelResponse> instruments;
    private double score;
    private ExperienceLevel profileLevel;
}