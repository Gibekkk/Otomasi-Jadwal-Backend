package com.jadwal.restfulapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

import com.jadwal.restfulapi.model.enums.Role;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "username", nullable = false, length = 15)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(nullable = true, name = "prodi_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_prodi_id"))
    private Category prodiId;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Session> userSessions;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecturer> userLecturerCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecturer> userLecturerEditedBy;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Course> userCourseCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Course> userCourseEditedBy;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Room> userRoomCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Room> userRoomEditedBy;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> userCategoryCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> userCategoryEditedBy;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Specialization> userSpecializationCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Specialization> userSpecializationEditedBy;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LabGroup> userLabGroupCreatedBy;

    @OneToMany(mappedBy = "editedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LabGroup> userLabGroupEditedBy;

    @OneToMany(mappedBy = "generatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TimelineGeneration> userTimelineGenerationGeneratedBy;

}
