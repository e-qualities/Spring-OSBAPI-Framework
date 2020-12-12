package com.equalities.cloud.osb.persistence.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingEntity;

@Repository
public interface ServiceInstanceBindingRepository extends CrudRepository<JPAServiceInstanceBindingEntity, String> {
}