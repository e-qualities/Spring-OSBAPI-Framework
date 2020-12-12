package com.equalities.cloud.osb.persistence.jpa;

import java.time.Duration;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingOperationEntity;

@Repository
public interface ServiceInstanceBindingOperationRepository extends CrudRepository<JPAServiceInstanceBindingOperationEntity, String> {
  
  @Modifying
  @Transactional
  @Query("DELETE FROM JPAServiceInstanceBindingOperationEntity o WHERE o.createdAt < :#{ T(java.time.Instant).now().minus(#duration)}")
  void deleteOperationsOlderThan(Duration duration);
  
  @Modifying
  @Transactional
  @Query("DELETE FROM JPAServiceInstanceBindingOperationEntity o WHERE o.status.state = :state AND o.createdAt < :#{ T(java.time.Instant).now().minus(#duration)}")
  void deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration);
}