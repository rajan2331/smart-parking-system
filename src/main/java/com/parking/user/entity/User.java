package com.parking.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users",
uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_users_email",
            columnNames = "email"
        ),
        @UniqueConstraint(
            name = "uk_users_mobile",
            columnNames = "mobile"
        )
    })
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)	
	private String mobile;

	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(nullable = false)
	private String password;

	// DB default lets Hibernate add this NOT NULL column to a table that already has rows.
	// Never set from the register request; promote via ADMIN_EMAIL (see AdminBootstrap).
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@ColumnDefault("'USER'")
	private Role role = Role.USER;

}
