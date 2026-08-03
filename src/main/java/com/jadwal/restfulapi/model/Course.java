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

import java.time.LocalDateTime;

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

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "sks_count", nullable = false, length = 2)
    private int sks_count;

    @ManyToOne
    @JoinColumn(nullable = false, name = "category_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_category_id"))
    private Category category_id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_created_by"))
    private User created_by;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_edited_by"))
    private User edited_by;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deleted_at;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "course_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSchedule> course_schedules;

}
