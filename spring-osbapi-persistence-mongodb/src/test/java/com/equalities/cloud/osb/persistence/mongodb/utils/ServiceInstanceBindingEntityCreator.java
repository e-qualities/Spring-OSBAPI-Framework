package com.equalities.cloud.osb.persistence.mongodb.utils;

import java.util.UUID;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;

public final class ServiceInstanceBindingEntityCreator {

  public static final String INTERNAL_DATA_TENANT_ID = "tenantId";
  public static final String PLAN_ID = "planId";
  public static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
  public static final String SERVICE_INSTANCE_BINDING_ID = "serviceInstanceBindingId";
  private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
  
  

  public static ServiceInstanceBindingEntity createServiceInstanceBindingEntity() {
    return  createServiceInstanceBindingEntity(UUID.randomUUID().toString());
  }

  public static ServiceInstanceBindingEntity createServiceInstanceBindingEntity(final String tenandId) {
    return  ServiceInstanceBindingEntity.builder()
        .servicePlanId(PLAN_ID)
        .serviceDefinitionId(SERVICE_DEFINITION_ID)
        .serviceInstanceBindingId(SERVICE_INSTANCE_BINDING_ID)
        .serviceInstanceId(SERVICE_INSTANCE_ID)
        .data(createServiceInstanceBindingInfo(tenandId))
        .build();
  }

  public static final ServiceInstanceBindingInfo createServiceInstanceBindingInfo(final String tenandId) {
    final ServiceInstanceBindingInfo internalData = new ServiceInstanceBindingInfo();
    internalData.put(INTERNAL_DATA_TENANT_ID, tenandId);
    return internalData;
  }
}
