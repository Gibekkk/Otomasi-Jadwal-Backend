package com.jadwal.restfulapi.model;

import java.time.LocalDateTime;

import com.jadwal.restfulapi.model.enums.SampleFood;

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
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sample_food_preferences")
public class UserSampleFoodPreference {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_preference_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_userSampleFoodPreferenceUserPreference"))
    private UserPreference userPreferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_food", nullable = false)
    private SampleFood sampleFood;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
