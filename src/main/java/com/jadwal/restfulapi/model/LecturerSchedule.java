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

import java.time.LocalTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_groups")
public class UserGroup {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "lecturer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_group_lecturer_id"))
    private Lecturer lecturer_id;

    @ManyToOne
    @JoinColumn(nullable = true, name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_group_schedule_id"))
    private Schedule schedule_id;
}
