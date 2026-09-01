package com.jadwal.restfulapi.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
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
@Table(name = "submajors")
public class SubMajor {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false, name = "category_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_submajor_category"))
    private Category categoryId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_submajor_created_by"))
    private User createdBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "edited_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_submajor_edited_by"))
    private User editedBy;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subMajorId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Course> subMajorCourses;

}
