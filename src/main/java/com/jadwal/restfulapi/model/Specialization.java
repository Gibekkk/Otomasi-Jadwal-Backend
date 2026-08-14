package com.jadwal.restfulapi.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "specializations")
public class Specialization {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_specialization_created_by"))
    private User createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_specialization_edited_by"))
    private User editedBy;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "specializationId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LabSpecialization> labSpecializations;

    @OneToMany(mappedBy = "specializationId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSpecialization> lecturerSpecializations;

    @OneToMany(mappedBy = "specializationId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSpecialization> courseSpecializations;
}
