package com.equalities.osb.persistence.jpa.utils;

import java.time.Instant;

import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

public final class ServiceInstanceOperationEntityCreator {

  public static ServiceInstanceOperationEntity createServiceInstanceOperationEntity(String operationId) {
    return ServiceInstanceOperationEntity.builder()
            .id(operationId)
            .serviceInstanceId("serviceInstanceId")
            .servicePlanId("servicePlanId")
            .createdAt(Instant.now())
            .type(ServiceOperationEntity.Type.CREATE)
            .status(ServiceOperationStatus.builder()
                    .state(ServiceOperationStatus.State.IN_PROGRESS)
                    .description("description")
                    .build())
            .build();
  }

  public static ServiceInstanceOperationEntity createServiceInstanceOperationEntity() {
    return createServiceInstanceOperationEntity("operationId");
  }
}
