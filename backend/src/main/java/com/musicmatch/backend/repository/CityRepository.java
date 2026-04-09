package com.musicmatch.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.musicmatch.backend.model.City;

public interface CityRepository extends JpaRepository<City, Long> {
}