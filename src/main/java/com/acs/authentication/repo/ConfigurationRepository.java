package com.acs.authentication.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.acs.authentication.entity.AppConfiguration;

@Repository
public interface ConfigurationRepository extends JpaRepository<AppConfiguration, Long> {
	AppConfiguration findTopByOrderByIdAsc(); // or any logic to get the right row
}
