package com.jadwal.restfulapi.model;

import com.jadwal.restfulapi.model.enums.Day;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "lecturer_schedules")
public class LecturerSchedule {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lecturer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_schedule_lecturer_id"))
    private Lecturer lecturerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "`day`", nullable = false)
    private Day day;

    @OneToMany(mappedBy = "lecturerScheduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerScheduleTime> lecturerScheduleTimes = new HashSet<>();

}
