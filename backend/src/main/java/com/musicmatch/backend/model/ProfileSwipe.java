package com.musicmatch.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_swipe")
@Getter
@Setter
public class ProfileSwipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_profile_id")
    private Profile fromProfile;

    @ManyToOne
    @JoinColumn(name = "to_profile_id")
    private Profile toProfile;

    private boolean liked;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}