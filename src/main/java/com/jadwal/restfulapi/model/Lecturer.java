package com.jadwal.restfulapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

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
    private Boolean is_dlb;

    @Column(name = "is_male", nullable = false)
    private Boolean is_male;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", nullable = false)
    private Religion religion;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lecturer_type_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_lecturer_type"))
    private LecturerType lecturer_type_id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_created_by"))
    private User created_by;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_edited_by"))
    private User edited_by;

    @ManyToOne
    @JoinColumn(nullable = true, name = "prodi_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_prodi_id"))
    private Category prodi_id;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deleted_at;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "lecturer_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSpecialization> lecturer_specializations;

    @OneToMany(mappedBy = "lecturer_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSchedule> lecturer_schedules;

    @OneToMany(mappedBy = "lecturer_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> lectures;
}
