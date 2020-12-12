package com.equalities.cloud.osb.persistence;

import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.CREATE;
import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.DELETE;
import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.UPDATE;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersistentStorage")
class PersistentStorageTest {
  
  @Mock
  ServiceInstancePersistence instancePersistence;
  
  @Mock
  ServiceInstanceOperationPersistence instanceOperationPersistence;
  
  @Mock
  ServiceInstanceBindingPersistence bindingPersistence;
  
  @Mock
  ServiceInstanceBindingOperationPersistence bindingOperationPersistence;
  
  PersistentStorage instance;
  
  @BeforeEach
  void setUp() throws Exception {
    instance = new PersistentStorage(instancePersistence, instanceOperationPersistence, bindingPersistence, bindingOperationPersistence);
  }
  
  @Test
  @DisplayName("can be created with required beans.")
  void testPersistentStorage() {
    new PersistentStorage(instancePersistence, instanceOperationPersistence, bindingPersistence, bindingOperationPersistence);
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("Service Instance Persistence")
  class ServiceInstancePersistenceTests {
    
    @Mock
    ServiceInstanceInfo serviceInstanceInfo;
    
    @Mock
    ServiceInstanceOperationEntity operation;
    
    @Mock
    ServiceInstanceEntity serviceInstanceEntity;
    
    @Captor
    ArgumentCaptor<ServiceInstanceEntity> instanceEntityCaptor;
    
    @Test
    @DisplayName("can insert a service instance.")
    void testInsertServiceInstance() {
      final String serviceInstanceId = "serviceInstanceId";
      final String serviceDefinitionId = "serviceDefinitionId";
      final String servicePlanId = "servicePlanId";

      when(operation.getServiceInstanceId()).thenReturn(serviceInstanceId);
      when(operation.getServiceDefinitionId()).thenReturn(serviceDefinitionId);
      when(operation.getServicePlanId()).thenReturn(servicePlanId);
      
      when(instancePersistence.insert(any(ServiceInstanceEntity.class))).thenReturn(Mono.just(serviceInstanceEntity));
      
      instance.insertServiceInstance(serviceInstanceInfo, operation).block();
      
      verify(instancePersistence).insert(instanceEntityCaptor.capture());
      
      assertThat(instanceEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(serviceInstanceId);
      assertThat(instanceEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(serviceDefinitionId);
      assertThat(instanceEntityCaptor.getValue().getServicePlanId()).isEqualTo(servicePlanId);
    }
    
    @Test
    @DisplayName("fails to insert service instance if persistence layer implementation is wrong.")
    void testInsertServiceInstanceFailsIfPersistenceLayerWronglyImplemented() {
      final String serviceInstanceId = "serviceInstanceId";
      final String serviceDefinitionId = "serviceDefinitionId";
      final String servicePlanId = "servicePlanId";

      when(operation.getServiceInstanceId()).thenReturn(serviceInstanceId);
      when(operation.getServiceDefinitionId()).thenReturn(serviceDefinitionId);
      when(operation.getServicePlanId()).thenReturn(servicePlanId);
      
      when(instancePersistence.insert(any(ServiceInstanceEntity.class))).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertServiceInstance(serviceInstanceInfo, operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert service instance returned empty result.");
      
      verify(instancePersistence).insert(instanceEntityCaptor.capture());
      
      assertThat(instanceEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(serviceInstanceId);
      assertThat(instanceEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(serviceDefinitionId);
      assertThat(instanceEntityCaptor.getValue().getServicePlanId()).isEqualTo(servicePlanId);
    }

    @Test
    @DisplayName("can update persisted service instance.")
    void testUpdateServiceInstance() {
      final String serviceInstanceId = "serviceInstanceId";
      final String serviceDefinitionId = "serviceDefinitionId";
      final String servicePlanId = "servicePlanId";

      when(operation.getServiceInstanceId()).thenReturn(serviceInstanceId);
      when(operation.getServiceDefinitionId()).thenReturn(serviceDefinitionId);
      when(operation.getServicePlanId()).thenReturn(servicePlanId);
      
      when(instancePersistence.insert(any(ServiceInstanceEntity.class))).thenReturn(Mono.just(serviceInstanceEntity));
      
      instance.updateServiceInstance(serviceInstanceInfo, operation).block();
      
      verify(instancePersistence).insert(instanceEntityCaptor.capture());
      
      assertThat(instanceEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(serviceInstanceId);
      assertThat(instanceEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(serviceDefinitionId);
      assertThat(instanceEntityCaptor.getValue().getServicePlanId()).isEqualTo(servicePlanId);
    }
    
    @Test
    @DisplayName("fails to update service instance if persistence layer implementation is wrong.")
    void testUpdateServiceInstanceFailsIfPersistenceLayerWronglyImplemented() {
      final String serviceInstanceId = "serviceInstanceId";
      final String serviceDefinitionId = "serviceDefinitionId";
      final String servicePlanId = "servicePlanId";

      when(operation.getServiceInstanceId()).thenReturn(serviceInstanceId);
      when(operation.getServiceDefinitionId()).thenReturn(serviceDefinitionId);
      when(operation.getServicePlanId()).thenReturn(servicePlanId);
      
      when(instancePersistence.insert(any(ServiceInstanceEntity.class))).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.updateServiceInstance(serviceInstanceInfo, operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert service instance returned empty result.");
      
      verify(instancePersistence).insert(instanceEntityCaptor.capture());
      
      assertThat(instanceEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(serviceInstanceId);
      assertThat(instanceEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(serviceDefinitionId);
      assertThat(instanceEntityCaptor.getValue().getServicePlanId()).isEqualTo(servicePlanId);
    }

    @Test
    @DisplayName("can delete persisted service instance.")
    void testDeleteServiceInstance() {
      when(instancePersistence.delete(any(ServiceInstanceEntity.class))).thenReturn(Mono.empty());
      instance.deleteServiceInstance(serviceInstanceEntity).block();
      verify(instancePersistence).delete(serviceInstanceEntity);
    }

    @Test
    @DisplayName("can read persisted service instance by ID.")
    void testReadServiceInstanceById() {
      final String instanceId = "instanceId";
      when(instancePersistence.readByServiceInstanceId(anyString())).thenReturn(Mono.just(serviceInstanceEntity));
      instance.readServiceInstanceById(instanceId).block();
      verify(instancePersistence).readByServiceInstanceId(instanceId);
    }
    
    @Test
    @DisplayName("throws if persisted service instance not found by ID.")
    void testReadServiceInstanceByIdThrowsIfInstanceNotFound() {
      final String instanceId = "instanceId";
      when(instancePersistence.readByServiceInstanceId(anyString())).thenReturn(Mono.empty());
      
      assertThatThrownBy(()-> {
        instance.readServiceInstanceById(instanceId).block();
      }).isInstanceOf(ServiceInstanceNotFoundException.class).hasMessageContaining("Service instance with ID", "not found");
      
      verify(instancePersistence).readByServiceInstanceId(instanceId);
    }
  }
  
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("Service Instance Operation Persistence")
  class ServiceInstanceOperationPersistenceTests {
    static final String PLAN_ID = "planId";

    static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";

    static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    @Mock
    CreateServiceInstanceRequest createRequest;
    
    @Mock
    UpdateServiceInstanceRequest updateRequest;
    
    @Mock
    DeleteServiceInstanceRequest deleteRequest;
    
    @Mock
    ServiceInstanceOperationEntity operation;
    
    @Captor
    ArgumentCaptor<ServiceInstanceOperationEntity> operationEntityCaptor;
    
    @Test
    @DisplayName("can insert operations to create service instances.")
    void testInsertCreateServiceInstanceOperation() {
      when(createRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(createRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(createRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.insert(any())).thenReturn(Mono.just(operation));
      
      instance.insertCreateServiceInstanceOperation(createRequest).block();
      
      verify(instanceOperationPersistence).insert(operationEntityCaptor.capture());
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(CREATE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }
    
    @Test
    @DisplayName("fails to insert operations to create service instances if persistence layer implemented wrongly.")
    void testInsertCreateServiceInstanceOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(createRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(createRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(createRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.insert(any())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertCreateServiceInstanceOperation(createRequest).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert create service instance operation returned empty result.");
      
      verify(instanceOperationPersistence).insert(operationEntityCaptor.capture());
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(CREATE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }

    @Test
    @DisplayName("can insert operations to update service instances.")
    void testInsertUpdateServiceInstanceOperation() {
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(updateRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(updateRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.update(any())).thenReturn(Mono.just(operation));
      
      instance.insertUpdateServiceInstanceOperation(updateRequest).block();
      
      verify(instanceOperationPersistence).update(operationEntityCaptor.capture());
      
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(UPDATE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }
    
    @Test
    @DisplayName("fails to insert operations to update service instances if persistence layer implemented wrongly.")
    void testInsertUpdateServiceInstanceOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(updateRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(updateRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.update(any())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertUpdateServiceInstanceOperation(updateRequest).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert update service instance operation returned empty result.");
      
      verify(instanceOperationPersistence).update(operationEntityCaptor.capture());
      
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(UPDATE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }

    @Test
    @DisplayName("can insert operations to delete service instances.")
    void testInsertDeleteServiceInstanceOperation() {
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(deleteRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(deleteRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.insert(any())).thenReturn(Mono.just(operation));
      
      instance.insertDeleteServiceInstanceOperation(deleteRequest).block();
      
      verify(instanceOperationPersistence).insert(operationEntityCaptor.capture());
      
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(DELETE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }

    @Test
    @DisplayName("fails to insert operations to delete service instances if persistence layer implemented wrongly.")
    void testInsertDeleteServiceInstanceOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(deleteRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(deleteRequest.getPlanId()).thenReturn(PLAN_ID);
      when(instanceOperationPersistence.insert(any())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertDeleteServiceInstanceOperation(deleteRequest).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert delete service instance operation returned empty result.");
      
      verify(instanceOperationPersistence).insert(operationEntityCaptor.capture());
      
      assertThat(operationEntityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationEntityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationEntityCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationEntityCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getType()).isEqualTo(DELETE);
      assertThat(operationEntityCaptor.getValue().getId()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus()).isNotNull();
      assertThat(operationEntityCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationEntityCaptor.getValue().getStatus().getDescription()).isNotNull();
    }
    
    @Test
    @DisplayName("can update persisted operations.")
    void testUpdateServiceInstanceOperation() {
      when(instanceOperationPersistence.update(any())).thenReturn(Mono.just(operation));
      instance.updateServiceInstanceOperation(operation).block();
      
      verify(instanceOperationPersistence).update(operation);
    }
    
    @Test
    @DisplayName("fails to update persisted operations if persistence layer implemented wrongly.")
    void testUpdateServiceInstanceOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(instanceOperationPersistence.update(any())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.updateServiceInstanceOperation(operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Update service instance operation returned empty result.");
      
      verify(instanceOperationPersistence).update(operation);
    }

    @Test
    @DisplayName("can delete persisted operations.")
    void testDeleteServiceInstanceOperation() {
      when(instanceOperationPersistence.delete(any())).thenReturn(Mono.empty());
      instance.deleteServiceInstanceOperation(operation).block();
      verify(instanceOperationPersistence).delete(operation);
    }

    @Test
    @DisplayName("can read persisted operations by ID.")
    void testReadServiceInstanceOperationById() {
      final String instanceId = "instanceId";
      when(instanceOperationPersistence.readByOperationId(anyString())).thenReturn(Mono.just(operation));
      instance.readServiceInstanceOperationById(instanceId).block();
      verify(instanceOperationPersistence).readByOperationId(instanceId);
    }
    
    @Test
    @DisplayName("fails to read persisted operations by ID if ID not found.")
    void testReadServiceInstanceOperationByIdThrowsIfInstanceIdNotFound() {
      final String instanceId = "instanceId";
      when(instanceOperationPersistence.readByOperationId(anyString())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.readServiceInstanceOperationById(instanceId).block();
      }).isInstanceOf(RuntimeException.class).hasMessageContaining("Error! Read service instance operation by ID returned empty result. Service Instance Operation with ID", "not found");
      
      verify(instanceOperationPersistence).readByOperationId(instanceId);
    }
  }
  

  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("Service Instance Binding Persistence")
  class ServiceInstanceBindingPersistenceTests {
    private static final String SERVICE_PLAN_ID = "servicePlanId";
    private static final String SERVICE_BINDING_ID = "serviceBindingId";
    private static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    @Mock
    ServiceInstanceBindingInfo serviceBindingInfo;
    
    @Mock
    ServiceInstanceBindingOperationEntity operation;
    
    @Mock
    ServiceInstanceBindingEntity entity;
    
    @Captor
    ArgumentCaptor<ServiceInstanceBindingEntity> entityCaptor;
    
    @Test
    @DisplayName("can insert service instance bindings.")
    void testInsertServiceInstanceBinding() {
      when(operation.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(operation.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(operation.getServiceInstanceBindingId()).thenReturn(SERVICE_BINDING_ID);
      when(operation.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
      when(bindingPersistence.insert(any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.just(entity));
      
      instance.insertServiceInstanceBinding(serviceBindingInfo, operation).block();
      
      verify(bindingPersistence).insert(entityCaptor.capture());
      assertThat(entityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(entityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(entityCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(SERVICE_BINDING_ID);
      assertThat(entityCaptor.getValue().getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
      assertThat(entityCaptor.getValue().getData()).isEqualTo(serviceBindingInfo);
    }
    
    @Test
    @DisplayName("fails to insert service instance bindings if persistence layer implemented wrongly.")
    void testInsertServiceInstanceBindingFailsIfPersistanceLayerWronglyImplemented() {
      when(operation.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(operation.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(operation.getServiceInstanceBindingId()).thenReturn(SERVICE_BINDING_ID);
      when(operation.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
      when(bindingPersistence.insert(any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.empty());
      
      
      assertThatThrownBy(() -> {
        instance.insertServiceInstanceBinding(serviceBindingInfo, operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert service instance binding returned empty result.");
      
      verify(bindingPersistence).insert(entityCaptor.capture());
      assertThat(entityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(entityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(entityCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(SERVICE_BINDING_ID);
      assertThat(entityCaptor.getValue().getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
      assertThat(entityCaptor.getValue().getData()).isEqualTo(serviceBindingInfo);
    }

    @Test
    @DisplayName("can update service instance bindings.")
    void testUpdateServiceInstanceBinding() {
      when(operation.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(operation.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(operation.getServiceInstanceBindingId()).thenReturn(SERVICE_BINDING_ID);
      when(operation.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
      when(bindingPersistence.insert(any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.just(entity));
      
      instance.updateServiceInstanceBinding(serviceBindingInfo, operation).block();
      
      verify(bindingPersistence).insert(entityCaptor.capture());
      assertThat(entityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(entityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(entityCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(SERVICE_BINDING_ID);
      assertThat(entityCaptor.getValue().getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
      assertThat(entityCaptor.getValue().getData()).isEqualTo(serviceBindingInfo);
    }
    
    @Test
    @DisplayName("fails to update service instance bindings if persistence layer implemented wrongly.")
    void testUpdateServiceInstanceBindingFailsIfPersistanceLayerWronglyImplemented() {
      when(operation.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(operation.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(operation.getServiceInstanceBindingId()).thenReturn(SERVICE_BINDING_ID);
      when(operation.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
      when(bindingPersistence.insert(any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.empty());
      
      
      assertThatThrownBy(() -> {
        instance.updateServiceInstanceBinding(serviceBindingInfo, operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert service instance binding returned empty result.");
      
      verify(bindingPersistence).insert(entityCaptor.capture());
      assertThat(entityCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(entityCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(entityCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(SERVICE_BINDING_ID);
      assertThat(entityCaptor.getValue().getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
      assertThat(entityCaptor.getValue().getData()).isEqualTo(serviceBindingInfo);
    }

    @Test
    @DisplayName("can delete service instance bindings.")
    void testDeleteServiceInstanceBinding() {
      when(bindingPersistence.delete(any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.empty());
      instance.deleteServiceInstanceBinding(entity).block();
      verify(bindingPersistence).delete(entity);
    }

    @Test
    @DisplayName("can read service instance bindings by ID.")
    void testReadServiceInstanceBindingById() {
      final String bindingId = "bindingId";
      when(bindingPersistence.readByServiceInstanceBindingId(anyString())).thenReturn(Mono.just(entity));
      instance.readServiceInstanceBindingById(bindingId).block();
      verify(bindingPersistence).readByServiceInstanceBindingId(bindingId);
    }
    
    @Test
    @DisplayName("fails to read service instance bindings by ID if ID not found.")
    void testReadServiceInstanceBindingByIdThrowsIfBindingIdNotFound() {
      final String bindingId = "bindingId";
      when(bindingPersistence.readByServiceInstanceBindingId(anyString())).thenReturn(Mono.empty());
      assertThatThrownBy(() -> {
        instance.readServiceInstanceBindingById(bindingId).block();
      }).isInstanceOf(ServiceInstanceBindingNotFoundException.class).hasMessage("Error! Service Instance Binding Persistence layer 'readById' implementation returned empty result. Service Instance Binding with ID not found.");
      
      verify(bindingPersistence).readByServiceInstanceBindingId(bindingId);
    }
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("Service Instance Binding Operation Persistence")
  class ServiceInstanceBindingOperationPersistenceTests {
    
    private static final String BINDING_ID = "bindingId";
    private static final String PLAN_ID = "planId";
    private static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    @Mock
    CreateServiceInstanceBindingRequest createRequest;
    
    @Mock
    DeleteServiceInstanceBindingRequest deleteRequest;
    
    @Mock
    ServiceInstanceBindingOperationEntity operation;
    
    @Captor
    ArgumentCaptor<ServiceInstanceBindingOperationEntity> operationCaptor;
    
    @Test
    @DisplayName("can insert operations to create service instance bindings.")
    void testInsertCreateServiceInstanceBindingOperation() {
      when(createRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(createRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(createRequest.getPlanId()).thenReturn(PLAN_ID);
      when(createRequest.getBindingId()).thenReturn(BINDING_ID);
      when(bindingOperationPersistence.insert(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.just(operation));
      
      instance.insertCreateServiceInstanceBindingOperation(createRequest).block();
      
      verify(bindingOperationPersistence).insert(operationCaptor.capture());
      assertThat(operationCaptor.getValue().getId()).isNotNull();
      assertThat(operationCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationCaptor.getValue().getType()).isEqualTo(CREATE);
      assertThat(operationCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationCaptor.getValue().getStatus().getDescription()).isNotNull();
      assertThat(operationCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(BINDING_ID);
    }
    
    @Test
    @DisplayName("fails to insert operations to create service instance bindings if persistence layer wrongly implemented.")
    void testInsertCreateServiceInstanceBindingOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(createRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(createRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(createRequest.getPlanId()).thenReturn(PLAN_ID);
      when(createRequest.getBindingId()).thenReturn(BINDING_ID);
      when(bindingOperationPersistence.insert(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertCreateServiceInstanceBindingOperation(createRequest).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert create service instance binding operation returned empty result.");
      
      verify(bindingOperationPersistence).insert(operationCaptor.capture());
      assertThat(operationCaptor.getValue().getId()).isNotNull();
      assertThat(operationCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationCaptor.getValue().getType()).isEqualTo(CREATE);
      assertThat(operationCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationCaptor.getValue().getStatus().getDescription()).isNotNull();
      assertThat(operationCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(BINDING_ID);
    }

    @Test
    @DisplayName("can insert operations to delete service instance bindings.")
    void testInsertDeleteServiceInstanceBindingOperation() {
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(deleteRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(deleteRequest.getPlanId()).thenReturn(PLAN_ID);
      when(deleteRequest.getBindingId()).thenReturn(BINDING_ID);
      when(bindingOperationPersistence.insert(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.just(operation));
      
      instance.insertDeleteServiceInstanceBindingOperation(deleteRequest).block();
      
      verify(bindingOperationPersistence).insert(operationCaptor.capture());
      assertThat(operationCaptor.getValue().getId()).isNotNull();
      assertThat(operationCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationCaptor.getValue().getType()).isEqualTo(DELETE);
      assertThat(operationCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationCaptor.getValue().getStatus().getDescription()).isNotNull();
      assertThat(operationCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(BINDING_ID);
    }
    
    @Test
    @DisplayName("fails to insert operations to delete service instance bindings if persistence layer wrongly implemented.")
    void testInsertDeleteServiceInstanceBindingOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(deleteRequest.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
      when(deleteRequest.getPlanId()).thenReturn(PLAN_ID);
      when(deleteRequest.getBindingId()).thenReturn(BINDING_ID);
      when(bindingOperationPersistence.insert(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.insertDeleteServiceInstanceBindingOperation(deleteRequest).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Insert delete service instance binding operation returned empty result.");
      
      verify(bindingOperationPersistence).insert(operationCaptor.capture());
      assertThat(operationCaptor.getValue().getId()).isNotNull();
      assertThat(operationCaptor.getValue().getCreatedAt()).isNotNull();
      assertThat(operationCaptor.getValue().getType()).isEqualTo(DELETE);
      assertThat(operationCaptor.getValue().getStatus().getState()).isEqualTo(IN_PROGRESS);
      assertThat(operationCaptor.getValue().getStatus().getDescription()).isNotNull();
      assertThat(operationCaptor.getValue().getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
      assertThat(operationCaptor.getValue().getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
      assertThat(operationCaptor.getValue().getServicePlanId()).isEqualTo(PLAN_ID);
      assertThat(operationCaptor.getValue().getServiceInstanceBindingId()).isEqualTo(BINDING_ID);
    }

    @Test
    @DisplayName("can update persisted operations.")
    void testUpdateServiceInstanceBindingOperation() {
      when(bindingOperationPersistence.update(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.just(operation));
      instance.updateServiceInstanceBindingOperation(operation).block();
      verify(bindingOperationPersistence).update(operation);
    }
    
    @Test
    @DisplayName("fails to update persisted operations if persistence layer wrongly implemented.")
    void testUpdateServiceInstanceBindingOperationFailsIfPersistenceLayerWronglyImplemented() {
      when(bindingOperationPersistence.update(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.empty());
      assertThatThrownBy(() -> {
        instance.updateServiceInstanceBindingOperation(operation).block();
      }).isInstanceOf(IllegalStateException.class).hasMessage("Persistence Error! Update service instance binding operation returned empty result.");
      
      verify(bindingOperationPersistence).update(operation);
    }

    @Test
    @DisplayName("can delete persisted operations.")
    void testDeleteServiceInstanceBindingOperation() {
      when(bindingOperationPersistence.delete(any(ServiceInstanceBindingOperationEntity.class))).thenReturn(Mono.empty());
      instance.deleteServiceInstanceBindingOperation(operation).block();
      verify(bindingOperationPersistence).delete(operation);
    }

    @Test
    @DisplayName("can read persisted operations by ID.")
    void testReadServiceInstanceBindingOperationById() {
      final String operationId = "operationId";
      when(bindingOperationPersistence.readByOperationId(anyString())).thenReturn(Mono.just(operation));
      instance.readServiceInstanceBindingOperationById(operationId).block();
      verify(bindingOperationPersistence).readByOperationId(operationId);
    }
    
    @Test
    @DisplayName("fails to read persisted operations by ID if persistence layer wrongly implemented.")
    void testReadServiceInstanceBindingOperationByIdFailsIfBindingOperationIdNotFound() {
      final String operationId = "operationId";
      when(bindingOperationPersistence.readByOperationId(anyString())).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.readServiceInstanceBindingOperationById(operationId).block();
      }).isInstanceOf(RuntimeException.class).hasMessageContaining("Error! Read service instance binding operation by ID returned empty result. Service Instance Binding Operation with ID", "not found.");

      verify(bindingOperationPersistence).readByOperationId(operationId);
    }
  }
}
