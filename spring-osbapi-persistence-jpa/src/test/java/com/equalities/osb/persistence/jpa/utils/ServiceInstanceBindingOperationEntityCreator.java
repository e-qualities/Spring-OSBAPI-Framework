package com.equalities.osb.persistence.jpa.utils;

import java.time.Instant;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

public final class ServiceInstanceBindingOperationEntityCreator {

  public static ServiceInstanceBindingOperationEntity createServiceInstanceBindingOperationEntity(String operationId) {
    return ServiceInstanceBindingOperationEntity.builder()
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

  public static ServiceInstanceBindingOperationEntity createServiceInstanceBindingOperationEntity() {
    return createServiceInstanceBindingOperationEntity("operationId");
  }
}
