package com.stinjoss.springbootmvc.app.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor

public class User extends Person {

    @NotEmpty
    @Size(min = 4, max = 16)
    private String username;

    @NotEmpty
    @Size(min = 8, max = 60)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Transient
    private boolean admin;

    @JsonIgnoreProperties({"users,hibernateLazyInitializer", "handler"})
    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role_id"})}
    )
    private List<Role> roles = new ArrayList<>();


    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        this.lastAccess = LocalDateTime.now();
    }

}