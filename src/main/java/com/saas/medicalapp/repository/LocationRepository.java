package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Location Repository
 * JPA repository for Location entity
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
}

