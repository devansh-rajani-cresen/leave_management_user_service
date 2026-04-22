package com.cresensolutions.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "role", schema = "core")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @Column(name = "unique_name", unique = true)
    private String uniqueName;

    @Column(name = "role_desc")
    private String roleDesc;

    @Column(name = "create_date")
    private OffsetDateTime createDate;

    @Column(name = "update_date")
    private OffsetDateTime updateDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users;
}
