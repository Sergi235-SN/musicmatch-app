package com.musicmatch.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.musicmatch.backend.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}