package com.equalities.cloud.osb.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TypeAlias("ServiceInstanceBindingOperation")
public class ServiceInstanceBindingOperationEntity extends ServiceOperationEntity {

  @Id
  private String id;

  private Instant createdAt;

  private String serviceInstanceBindingId;
  
  private String serviceInstanceId;

  private String serviceDefinitionId;

  private String servicePlanId;

  private Type type;

  private ServiceOperationStatus status;
}
