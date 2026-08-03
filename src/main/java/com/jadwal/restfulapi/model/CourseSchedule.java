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
    private Schedule scheduleId;

    @ManyToOne
    @JoinColumn(nullable = true, name = "course_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_course_id"))
    private Course courseId;

    @ManyToOne
    @JoinColumn(nullable = true, name = "room_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_room_id"))
    private Room roomId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_created_by"))
    private User createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_schedule_edited_by"))
    private User editedBy;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "courseScheduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> lectures;

}
