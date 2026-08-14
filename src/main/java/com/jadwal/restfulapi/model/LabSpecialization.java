package com.jadwal.restfulapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "lab_specializations")
public class LabSpecialization {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lab_group_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lab_specialization_lab_group_id"))
    private LabGroup labGroupId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "specialization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lab_specialization_specialization_id"))
    private Specialization specializationId;

}
