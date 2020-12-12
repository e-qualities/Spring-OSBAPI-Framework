package com.equalities.cloud.osb.persistence.jpa;

import java.time.Duration;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceOperationEntity;

@Repository
public interface ServiceInstanceOperationRepository extends CrudRepository<JPAServiceInstanceOperationEntity, String> {
  
  @Modifying
  @Transactional
  @Query("DELETE FROM JPAServiceInstanceOperationEntity o WHERE o.createdAt < :#{T(java.time.Instant).now().minus(#duration)}")
  void deleteOperationsOlderThan(Duration duration);
  
  @Modifying
  @Transactional
  @Query("DELETE FROM JPAServiceInstanceOperationEntity o WHERE o.status.state = :state AND o.createdAt < :#{ T(java.time.Instant).now().minus(#duration)}")
  void deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration);
}