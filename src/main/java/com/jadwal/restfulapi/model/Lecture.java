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

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lectures")
public class Lecture {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "lecturer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecture_lecturer_id"))
    private Lecturer lecturerId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "course_schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecture_course_schedule_id"))
    private CourseSchedule courseScheduleId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "timeline_generation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecture_timeline_generation_id"))
    private TimelineGeneration timelineGenerationId;

    @Column(name = "fallback_reason", nullable = true, columnDefinition = "LONGTEXT")
    private String fallbackReason;

}
