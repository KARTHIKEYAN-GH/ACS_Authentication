package com.acs.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
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
	@JoinColumn(name = "domain_id", referencedColumnName = "Id", updatable = false, insertable = false)
	@ManyToOne
	private Domain domain;

	/** Domain id of the account. */
	@Column(name = "domain_id")
	private Long domainId;

}
