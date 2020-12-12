package com.equalities.cloud.osb.persistence.mongodb.utils;

import java.util.UUID;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;

public final class ServiceInstanceEntityCreator {

  public static final String INTERNAL_DATA_TENANT_ID = "tenantId";

  public static ServiceInstanceEntity createServiceInstanceOperationEntity() {
    return  createServiceInstanceOperationEntity(UUID.randomUUID().toString());
  }

  public static ServiceInstanceEntity createServiceInstanceOperationEntity(final String tenandId) {
    return  ServiceInstanceEntity.builder()
        .serviceInstanceId("siId")
        .serviceDefinitionId("spId")
        .servicePlanId("test")
        .data(createServiceInstanceInfo(tenandId))
        .build();
  }

  public static final ServiceInstanceInfo createServiceInstanceInfo(final String tenandId) {
    final ServiceInstanceInfo internalData = new ServiceInstanceInfo();
    internalData.put(INTERNAL_DATA_TENANT_ID, tenandId);
    return internalData;
  }
}
