package com.acs.authentication.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name="domains")
@Data
public class Domain {
	
	 /** Unique ID of the domain. */
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Unique ID from Cloud Stack. */
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "level")
    private Integer level;
    
    /** Parent domain id. */
    @Column(name = "parent_domain_id")
    private Long parentdomainId;
    
    /** Sub domain path. */
    @Column(name = "path")
    private String path;

    @Column(name = "is_active")
    private Boolean isActive;
}
