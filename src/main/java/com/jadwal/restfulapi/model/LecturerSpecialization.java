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
@Table(name = "lecturer_specializations")
public class LecturerSpecialization {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lecturer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_specialization_lecturer_id"))
    private Lecturer lecturerId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "specialization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lecturer_specialization_specialization_id"))
    private Specialization specializationId;

}
