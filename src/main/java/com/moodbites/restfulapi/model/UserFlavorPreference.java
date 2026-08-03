package com.moodbites.restfulapi.model;

import java.time.LocalDateTime;

import com.moodbites.restfulapi.model.enums.Flavor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_flavor_preferences")
public class UserFlavorPreference {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_preference_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_userFlavorPreferenceUserPreference"))
    private UserPreference userPreferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flavor", nullable = false)
    private Flavor flavor;

    @Max(5)
    @Min(1)
    @Column(name = "preference_scale", nullable = true)
    private Integer preferenceScale;

    @Max(5)
    @Min(1)
    @Column(name = "intensity_scale", nullable = true)
    private Integer intensityScale;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;

}
