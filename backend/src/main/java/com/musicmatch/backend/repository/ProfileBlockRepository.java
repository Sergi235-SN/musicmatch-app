package com.musicmatch.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.ProfileBlock;

public interface ProfileBlockRepository extends JpaRepository<ProfileBlock, Long> {

    boolean existsByBlockerAndBlocked(Profile blocker, Profile blocked);

    void deleteByBlockerAndBlocked(Profile blocker, Profile blocked);

    List<ProfileBlock> findByBlocker(Profile blocker);
}