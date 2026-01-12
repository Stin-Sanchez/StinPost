package com.stinjoss.springbootmvc.app.domain.entities;

import com.stinjoss.springbootmvc.app.domain.entities.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    @Size(min = 8, max = 16)
    private String password;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        this.lastAccess = LocalDateTime.now();
    }

}