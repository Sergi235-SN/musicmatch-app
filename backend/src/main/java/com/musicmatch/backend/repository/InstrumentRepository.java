package com.musicmatch.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.musicmatch.backend.model.Instrument;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
}