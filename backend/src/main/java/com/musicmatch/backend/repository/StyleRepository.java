package com.musicmatch.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.musicmatch.backend.model.Style;

public interface StyleRepository extends JpaRepository<Style, Long> {
}