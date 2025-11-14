package com.team.updevic001.dao.repositories;

import com.team.updevic001.dao.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRepository extends JpaRepository<EventEntity, String>, JpaSpecificationExecutor<EventEntity> {
}
