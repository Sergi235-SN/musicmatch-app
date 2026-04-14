package com.musicmatch.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchCandidatesResponse {

    private boolean profileComplete;
    private String message;
    private List<ProfileCardDTO> candidates;
    private boolean noMoreCandidates;
}