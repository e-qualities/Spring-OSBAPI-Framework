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
@TypeAlias("ServiceInstance")
public class ServiceInstanceEntity {

  @Id
  private String serviceInstanceId;
  
  private Instant createdAt;

  private String serviceDefinitionId;

  private String servicePlanId;

  private ServiceInstanceInfo data;
}
