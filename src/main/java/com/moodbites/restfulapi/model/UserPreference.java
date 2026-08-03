package com.moodbites.restfulapi.model;

import java.util.Set;

import com.moodbites.restfulapi.model.enums.Mood;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_userPreferenceUser"))
    private User userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = false)
    private Mood mood;

    @OneToMany(mappedBy = "userPreferenceId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserFlavorPreference> userFlavorPreferences;

    @OneToMany(mappedBy = "userPreferenceId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSampleFoodPreference> userSampleFoodPreferences;

}
