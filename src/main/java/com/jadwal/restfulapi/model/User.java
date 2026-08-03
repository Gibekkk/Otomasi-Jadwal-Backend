package com.jadwal.restfulapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

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

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deleted_at;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @ManyToOne
    @JoinColumn(nullable = true, name = "group_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_group_id"))
    private UserGroup group_id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "prodi_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_prodi_id"))
    private Category prodi_id;

    @OneToMany(mappedBy = "user_id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Session> user_sessions;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecturer> user_lecturer_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lecturer> user_lecturer_edited_by;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Course> user_course_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Course> user_course_edited_by;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Room> user_room_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Room> user_room_edited_by;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSchedule> user_course_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSchedule> user_course_edited_by;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> user_category_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> user_category_edited_by;

    @OneToMany(mappedBy = "created_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSpecialization> user_lecturer_specialization_created_by;

    @OneToMany(mappedBy = "edited_by", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerSpecialization> user_lecturer_specialization_edited_by;

}
