package com.equalities.cloud.osb.persistence.jpa.entities;

import java.time.Instant;
import java.util.Optional;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JPAServiceInstanceBindingOperationEntity {

  @Id
  private String id;
  
  private Instant createdAt;

  private String serviceInstanceBindingId;
  
  private String serviceInstanceId;

  private String serviceDefinitionId;

  private String servicePlanId;

  private Type type;

  @Embedded
  private ServiceOperationStatus status;
  
  public static JPAServiceInstanceBindingOperationEntity jpaType(ServiceInstanceBindingOperationEntity entity) {
    return new JPAServiceInstanceBindingOperationEntity(entity.getId(),
                                                        entity.getCreatedAt(),
                                                        entity.getServiceInstanceBindingId(),
                                                        entity.getServiceInstanceId(),
                                                        entity.getServiceDefinitionId(),
                                                        entity.getServicePlanId(),
                                                        entity.getType(),
                                                        entity.getStatus());
  }
  
  public static ServiceInstanceBindingOperationEntity osbType(JPAServiceInstanceBindingOperationEntity entity) {
    if(entity == null) {
      log.debug("Cannot map null-valued JPA entity to OSB persistence API entity. Returning null. This may be caused by entities not being found by ID.");
      return null;
    }
    
    return new ServiceInstanceBindingOperationEntity(entity.getId(),
                                                     entity.getCreatedAt(),
                                                     entity.getServiceInstanceBindingId(),
                                                     entity.getServiceInstanceId(),
                                                     entity.getServiceDefinitionId(),
                                                     entity.getServicePlanId(),
                                                     entity.getType(),
                                                     entity.getStatus());
  }
  
  public static ServiceInstanceBindingOperationEntity osbType(Optional<JPAServiceInstanceBindingOperationEntity> entity) {
    if(entity.isEmpty()) {
      return osbType((JPAServiceInstanceBindingOperationEntity) null);
    }
    return osbType(entity.get());
  }
}