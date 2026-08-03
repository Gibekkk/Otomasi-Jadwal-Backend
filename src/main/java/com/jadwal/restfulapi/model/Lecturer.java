package com.jadwal.restfulapi.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

import com.jadwal.restfulapi.model.enums.Religion;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lecturers")
public class Lecturer {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_dlb", nullable = false)
    private Boolean isDlb;

    @Column(name = "is_male", nullable = false)
    private Boolean isMale;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", nullable = false)
    private Religion religion;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lecturer_type_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_lecturer_type"))
    private LecturerType lecturerTypeId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_created_by"))
    private User createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_edited_by"))
    private User editedBy;

    @ManyToOne
    @JoinColumn(nullable = true, name = "prodi_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_prodi_id"))
    private Category prodiId;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "lecturerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSpecialization> lecturerSpecializations;

    @OneToMany(mappedBy = "lecturerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSchedule> lecturerSchedules;

    @OneToMany(mappedBy = "lecturerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> lectures;
}
