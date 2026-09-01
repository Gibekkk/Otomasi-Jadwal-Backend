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
@Table(name = "lecturer_schedules_time")
public class LecturerScheduleTime {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lecturer_schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_schedule_time_lecturer_id"))
    private LecturerSchedule lecturerScheduleId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_schedule_time_schedule_id"))
    private Schedule scheduleId;

}
