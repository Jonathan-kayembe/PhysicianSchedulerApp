package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Role Repository
 * JPA repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}

