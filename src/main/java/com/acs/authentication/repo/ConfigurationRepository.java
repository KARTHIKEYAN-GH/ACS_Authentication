package com.acs.authentication.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.acs.authentication.entity.AppConfiguration;

@Repository
public interface ConfigurationRepository extends JpaRepository<AppConfiguration, Long> {
	@Query("SELECT c.baseUrl FROM AppConfiguration c WHERE c.id = 1")
	String findBaseUrlByIdOne();
	// AppConfiguration findTopByOrderByIdAsc();
}
