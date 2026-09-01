package com.jadwal.restfulapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
public class Course {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Min(1)
    @Max(9)
    @Column(name = "sks_count", nullable = false, length = 1)
    private int sksCount;

    @Min(1)
    @Max(8)
    @Column(name = "semester", nullable = false, length = 1)
    private int semester;

    @Min(1)
    @Max(3)
    @Column(name = "lecturer_count", nullable = false, length = 1)
    private int lecturerCount;

    @Min(5)
    @Max(300)
    @Column(name = "capacity", nullable = false, length = 3)
    private int capacity;

    @Column(name = "is_interdiscipline", nullable = false)
    private Boolean isInterdiscipline;

    @Column(name = "is_odd", nullable = false)
    private Boolean isOdd;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_lab", nullable = false)
    private Boolean isLab;

    @ManyToOne
    @JoinColumn(nullable = false, name = "category_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_category_id"))
    private Category categoryId;

    @ManyToOne
    @JoinColumn(nullable = true, name = "submajor_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_submajor_id"))
    private SubMajor subMajorId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_created_by"))
    private User createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_edited_by"))
    private User editedBy;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "courseId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSchedule> courseSchedules;

    @OneToMany(mappedBy = "courseId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSpecialization> courseSpecializations;

}
