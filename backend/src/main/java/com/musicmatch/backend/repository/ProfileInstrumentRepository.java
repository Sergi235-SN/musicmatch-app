package com.musicmatch.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.musicmatch.backend.model.ProfileInstrument;

public interface ProfileInstrumentRepository extends JpaRepository<ProfileInstrument, Long> {

    void deleteByProfileId(Long profileId);

}