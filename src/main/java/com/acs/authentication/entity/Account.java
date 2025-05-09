package com.acs.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "accounts")
@Data
public class Account {

	/** Id of the account. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** Cloudstack's account uuid. */
	@Column(name = "uuid")
	private String uuid;

	/** Domain of the account. */
	// @JoinColumn(name = "domain_id", referencedColumnName = "Id", updatable =
	// false, insertable = false)
	// @ManyToOne
	// private Domain domain;

	/** Domain id of the account. */
	@Column(name = "domain_id")
	private Long domainId;

}
