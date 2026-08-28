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
@Table(name = "lecture_lecturers")
public class LectureLecturer {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "is_main_lecturer", nullable = false)
    private Boolean isMainLecturer;

    @ManyToOne
    @JoinColumn(nullable = true, name = "lecturer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecture_lecturer_lecturer_id"))
    private Lecturer lecturerId;

    @ManyToOne
    @JoinColumn(nullable = true, name = "lecture_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecture_lecturer_lecture_id"))
    private Lecture lectureId;

}
