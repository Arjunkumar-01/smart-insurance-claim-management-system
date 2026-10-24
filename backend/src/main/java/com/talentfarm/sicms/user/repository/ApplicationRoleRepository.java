package com.talentfarm.sicms.user.repository;

import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRoleRepository extends JpaRepository<ApplicationRoleEntity, UUID> {

    Optional<ApplicationRoleEntity> findByName(ApplicationRole name);
}