package com.jadwal.restfulapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "free_tables")
public class FreeTable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "is_generating", nullable = false)
    private Boolean isGenerating;

    @OneToOne
    @JoinColumn(nullable = true, name = "timeline_generation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_timeline_generation_id"))
    private TimelineGeneration timelineGenerationId;

    @Column(name = "secret_key", nullable = true, length = 255)
    private String secretKey;

}
