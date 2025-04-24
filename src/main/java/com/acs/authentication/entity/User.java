package com.acs.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "email")
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type")
	private UserType userType;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "account_id")
	private Long accountId;

	@Column(name = "domain_id")
	private Long domainId;

	
	// Commented out until Domain entity is defined
	 @ManyToOne
	 @JoinColumn(name = "domain_id", referencedColumnName = "Id", updatable =
	 false, insertable = false)
	 private Domain domain;

	public enum UserType {
		DOMAIN_ADMIN, ROOT_ADMIN, USER;
	}
}
