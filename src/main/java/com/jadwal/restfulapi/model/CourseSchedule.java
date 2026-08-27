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

import java.util.Set;

import com.jadwal.restfulapi.model.enums.Day;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course_schedules")
public class CourseSchedule {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "course_index", nullable = false, length = 1)
    private String courseIndex;

    @Column(name = "sks_count", nullable = false)
    private int sksCount;

    @Column(name = "is_lab", nullable = false)
    private Boolean isLab;

    // Nama kolom di-quote (backtick) karena "day" adalah reserved word di H2
    // (dipakai environment mvn test) -- Hibernate otomatis translate ke tanda kutip
    // yang benar per dialect (backtick di MariaDB, double-quote di H2), jadi aman
    // untuk keduanya tanpa perlu migrasi/rename kolom yang sudah ada di production.
    @Enumerated(EnumType.STRING)
    @Column(name = "`day`", nullable = false)
    private Day day;

    @ManyToOne
    @JoinColumn(nullable = false, name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_schedule_id"))
    private Schedule scheduleId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "course_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_course_id"))
    private Course courseId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "room_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_room_id"))
    private Room roomId;

    @OneToMany(mappedBy = "courseScheduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> lectures;

}
