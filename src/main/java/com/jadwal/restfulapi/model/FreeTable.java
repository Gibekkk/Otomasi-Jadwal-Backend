package com.jadwal.restfulapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "free_tables")
public class FreeTable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "is_generating", nullable = false)
    private Boolean isGenerating;

    @Column(name = "is_odd", nullable = false)
    private Boolean isOdd;

    @Column(name = "academic_year", nullable = false, length = 4)
    private int academicYear;

    @Column(name = "secret_key", nullable = true, length = 255)
    private String secretKey;

}
