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
@Table(name = "course_schedules")
public class CourseSchedule {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = true, name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_schedule_id"))
    private Schedule schedule_id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "course_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_course_id"))
    private Course course_id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "room_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_room_id"))
    private Room room_id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_created_by"))
    private User created_by;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_edited_by"))
    private User edited_by;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deleted_at;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "course_schedule_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> lectures;

}
