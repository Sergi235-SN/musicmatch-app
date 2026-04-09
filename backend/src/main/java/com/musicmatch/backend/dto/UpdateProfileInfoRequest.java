package com.musicmatch.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileInfoRequest {

    private String biography;
    private String profilePicture;
    private Long cityId;

}