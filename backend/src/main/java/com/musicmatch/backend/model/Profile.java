package com.musicmatch.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "profile")
@Getter
@Setter
public class Profile {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "profile_picture_url")
    private String profilePicture;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
        name = "profile_style",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "style_id")
    )
    private Set<Style> styles;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<ProfileInstrument> profileInstruments;
}