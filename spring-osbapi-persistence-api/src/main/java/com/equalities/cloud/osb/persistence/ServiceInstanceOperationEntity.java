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
@TypeAlias("ServiceInstanceOperation")
public class ServiceInstanceOperationEntity extends ServiceOperationEntity {

  @Id
  private String id;

  private Instant createdAt;

  private String serviceInstanceId;

  private String serviceDefinitionId;

  private String servicePlanId;

  private Type type;

  private ServiceOperationStatus status;
}
