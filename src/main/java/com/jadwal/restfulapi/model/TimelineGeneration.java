package com.jadwal.restfulapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "timeline_generations")
public class TimelineGeneration {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "is_odd", nullable = false)
    private Boolean isOdd;

    @Column(name = "academic_year", nullable = false, length = 4)
    private int academicYear;

    @ManyToOne
    @JoinColumn(nullable = false, name = "generated_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_timeline_generated_by"))
    private User generatedBy;

    @OneToOne(mappedBy = "generationTimelineId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FreeTable freeTableId;

    @OneToMany(mappedBy = "generationTimelineId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecture> generatedLectures;
}
