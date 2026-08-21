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
@Table(name = "course_specializations")
public class CourseSpecialization {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "course_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_specialization_course_id"))
    private Course courseId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "specialization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_course_specialization_specialization_id"))
    private Specialization specializationId;

}
