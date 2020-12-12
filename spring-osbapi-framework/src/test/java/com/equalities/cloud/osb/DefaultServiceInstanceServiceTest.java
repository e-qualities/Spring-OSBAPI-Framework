package com.equalities.cloud.osb;

import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.FAILED;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.IN_PROGRESS;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.GetServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.GetServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceResponse;

import com.equalities.cloud.osb.DefaultServiceInstanceService;
import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.config.OsbApiConfig.ServiceInstancesConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DefaultServiceInstanceServiceTest {

  private static final String OPERATION_ID = "operationId";
  private static final String HTTP_DASHBOARD_URL = "http://dashboardUrl";
  private static final String SERVICE_PLAN_ID = "servicePlanId";
  private static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
  private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
  private static final String ERROR_GENERATED_BY_TEST = "ErrorGeneratedByTest";

  private <T> Mono<T> error() {
    return Mono.error(new RuntimeException(ERROR_GENERATED_BY_TEST));
  }

  @Mock
  PersistentStorage storage;
  
  @Mock
  OsbApiConfig config;
  
  @Mock
  GetServiceInstanceRequest getInstanceRequest;
  
  @Mock
  GetLastServiceOperationRequest getLastOperationRequest;
  
  @Mock
  ServiceInstanceOperationEntity operation;
  
  @Mock
  ServiceInstanceEntity entity;
  
  @Mock
  ServiceInstanceInfo instanceInfo;
  
  @Mock
  ServiceOperationStatus status;
  
  @Captor
  ArgumentCaptor<State> stateCaptor;
  
  DefaultServiceInstanceService instance;

  @BeforeEach
  void setUp() throws Exception {
    instance = spy(new DefaultServiceInstanceService(storage, config) {
      @Override
      public Mono<ServiceInstanceInfo> addServiceInstance(CreateServiceInstanceRequest request) {
        throw new UnsupportedOperationException("This exception must never occur in a test, since this method should have been stubbed using the spy around this instance."
            + " Make sure you have properly mocked this method in your test.");
      }
      @Override
      public Mono<Void> removeServiceInstance(DeleteServiceInstanceRequest request, ServiceInstanceInfo instanceInfo) {
        throw new UnsupportedOperationException("This exception must never occur in a test, since this method should have been stubbed using the spy around this instance."
            + " Make sure you have properly mocked this method in your test.");
      }
      @Override
      public Mono<ServiceInstanceInfo> changeServiceInstance(UpdateServiceInstanceRequest request, ServiceInstanceInfo instanceInfo) {
        throw new UnsupportedOperationException("This exception must never occur in a test, since this method should have been stubbed using the spy around this instance."
            + " Make sure you have properly mocked this method in your test.");
      }
    });
  }
  
  @Test
  void testGetServiceInstance() {
    final ServiceInstanceInfo data = new ServiceInstanceInfo();
    data.put("com_equalities_cloud_osb_DashboardUrl", HTTP_DASHBOARD_URL);
    
    when(getInstanceRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(storage.readServiceInstanceById(SERVICE_INSTANCE_ID)).thenReturn(Mono.just(entity));
    when(entity.getData()).thenReturn(data);
    when(entity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(entity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    
    GetServiceInstanceResponse response = instance.getServiceInstance(getInstanceRequest).block();
    
    verify(storage).readServiceInstanceById(SERVICE_INSTANCE_ID);
    
    assertThat(response.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(response.getPlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(response.getParameters()).isNotNull();
    assertThat(response.getDashboardUrl()).isEqualTo(HTTP_DASHBOARD_URL);
  }
  
  @ParameterizedTest
  @EnumSource
  void testGetLastOperation(State operationState) {
    HashMap<State, OperationState> stateTypesMap = new HashMap<>();
    stateTypesMap.put(SUCCEEDED, OperationState.SUCCEEDED);
    stateTypesMap.put(IN_PROGRESS, OperationState.IN_PROGRESS);
    stateTypesMap.put(FAILED, OperationState.FAILED);
	  
    when(getLastOperationRequest.getOperation()).thenReturn(OPERATION_ID);
    when(storage.readServiceInstanceOperationById(OPERATION_ID)).thenReturn(Mono.just(operation));
    when(operation.getType()).thenReturn(ServiceInstanceOperationEntity.Type.DELETE);
    when(operation.getStatus()).thenReturn(status);
    when(status.getState()).thenReturn(operationState);
    
    GetLastServiceOperationResponse response = instance.getLastOperation(getLastOperationRequest).block();
    
    verify(storage).readServiceInstanceOperationById(OPERATION_ID);
    assertThat(response.getState()).isEqualTo(stateTypesMap.get(operationState));
    assertThat(response.getDescription()).isNotNull();
    assertThat(response.isDeleteOperation()).isTrue();
  }
  
  @ParameterizedTest
  @EnumSource
  void testGetLastOperationFailsIfOperationNotFound(State operationState) {
    HashMap<State, OperationState> stateTypesMap = new HashMap<>();
    stateTypesMap.put(SUCCEEDED, OperationState.SUCCEEDED);
    stateTypesMap.put(IN_PROGRESS, OperationState.IN_PROGRESS);
    stateTypesMap.put(FAILED, OperationState.FAILED);
    
    when(getLastOperationRequest.getOperation()).thenReturn(OPERATION_ID);
    when(storage.readServiceInstanceOperationById(OPERATION_ID)).thenReturn(Mono.error(new ServiceInstanceOperationNotFoundException("TriggeredByTest")));
    
    GetLastServiceOperationResponse response = instance.getLastOperation(getLastOperationRequest).block();
    
    verify(storage).readServiceInstanceOperationById(OPERATION_ID);
    assertThat(response.getState()).isEqualTo(OperationState.FAILED);
    assertThat(response.getDescription()).isEqualTo("TriggeredByTest");
    assertThat(response.isDeleteOperation()).isFalse();
  }

  @Nested
  class CreateServiceInstanceTests {
    
    @Mock
    CreateServiceInstanceRequest createRequest;
    
    @Mock
    CreateServiceInstanceResponse createResponse;
    
    @Test
    void testCreateServiceInstance() {
      // handle async, since platform supports it and 
      // implementation as well.
      instance.setCreateServiceInstanceAsync(true);
      when(createRequest.isAsyncAccepted()).thenReturn(true);
      when(storage.insertCreateServiceInstanceOperation(any())).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse)).when(instance).createServiceInstanceAsync(createRequest, operation);
      
      instance.createServiceInstance(createRequest).block();
      
      verify(instance).createServiceInstanceAsync(createRequest, operation);
      
      // handle sync, though platform supports async but
      // implementation does not.
      instance.setCreateServiceInstanceAsync(false);
      when(createRequest.isAsyncAccepted()).thenReturn(true);
      when(storage.insertCreateServiceInstanceOperation(any())).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse)).when(instance).createServiceInstanceSync(createRequest, operation);
      
      instance.createServiceInstance(createRequest).block();
      
      verify(instance).createServiceInstanceSync(createRequest, operation);
      
      // handle sync, since platform does not support async
      // even though implementation does.
      instance.setCreateServiceInstanceAsync(true);
      when(createRequest.isAsyncAccepted()).thenReturn(false);
      when(storage.insertCreateServiceInstanceOperation(any())).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse)).when(instance).createServiceInstanceSync(createRequest, operation);
      
      instance.createServiceInstance(createRequest).block();
      
      verify(instance, times(2)).createServiceInstanceSync(createRequest, operation);
      
      // handle sync, since platform does not support async
      // and implementation does not either.
      instance.setCreateServiceInstanceAsync(false);
      when(createRequest.isAsyncAccepted()).thenReturn(false);
      when(storage.insertCreateServiceInstanceOperation(any())).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse)).when(instance).createServiceInstanceSync(createRequest, operation);
      
      instance.createServiceInstance(createRequest).block();
      
      verify(instance, times(3)).createServiceInstanceSync(createRequest, operation);
    }

    @Test
    void testCreateServiceInstanceSync() {
      doReturn(Mono.empty()).when(instance).addServiceInstance(createRequest);
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      when(storage.insertServiceInstance(any(ServiceInstanceInfo.class), 
                                         any(ServiceInstanceOperationEntity.class)))
                                        .thenReturn(Mono.just(entity));
      
      CreateServiceInstanceResponse response = instance.createServiceInstanceSync(createRequest, operation).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.getOperation()).isNull();
      assertThat(response.isAsync()).isFalse();
      assertThat(response.getDashboardUrl()).isNull();
      assertThat(response.isInstanceExisted()).isFalse();
    }
    
    @Test
    void testCreateServiceInstanceSyncPropagatesSubclassErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(error()).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceSync(createRequest, operation).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testCreateServiceInstanceSyncPropagatesInstancePersistenceErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(Mono.just(instanceInfo)).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.insertServiceInstance(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceSync(createRequest, operation).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).insertServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testCreateServiceInstanceSyncPropagatesOperationPersistenceErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(Mono.just(instanceInfo)).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.insertServiceInstance(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceSync(createRequest, operation).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage).insertServiceInstance(instanceInfo, operation);
      verify(storage, times(2)).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }

    @Test
    void testCreateServiceInstanceAsync() {
      doReturn(Mono.empty()).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      when(storage.insertServiceInstance(any(ServiceInstanceInfo.class), any(ServiceInstanceOperationEntity.class))).thenReturn(Mono.just(entity));
      
      CreateServiceInstanceResponse response = instance.createServiceInstanceAsync(createRequest, operation)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getDashboardUrl()).isNull();
      assertThat(response.isInstanceExisted()).isFalse();
    }
    
    @Test
    void testCreateServiceInstanceAsyncDoesNotPropagateSubclassErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(error()).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      
      CreateServiceInstanceResponse response = instance.createServiceInstanceAsync(createRequest, operation)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getDashboardUrl()).isNull();
      assertThat(response.isInstanceExisted()).isFalse();
    }
    
    @Test
    void testCreateServiceInstanceAsyncDoesNotPropagateInstancePersistenceErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(Mono.just(instanceInfo)).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.insertServiceInstance(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      
      CreateServiceInstanceResponse response = instance.createServiceInstanceAsync(createRequest, operation)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getDashboardUrl()).isNull();
      assertThat(response.isInstanceExisted()).isFalse();
    }
    
    @Test
    void testCreateServiceInstanceAsyncDoesNotPropagateOperationPersistenceErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(Mono.just(instanceInfo)).when(instance).addServiceInstance(createRequest);
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.insertServiceInstance(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());
      
      CreateServiceInstanceResponse response = instance.createServiceInstanceAsync(createRequest, operation)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage, times(2)).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getDashboardUrl()).isNull();
      assertThat(response.isInstanceExisted()).isFalse();
    }
  }

  @Nested
  class DeleteServiceInstanceTests {
    
    @Mock
    DeleteServiceInstanceRequest deleteRequest;
    
    @Mock
    DeleteServiceInstanceResponse deleteResponse;
    
    @Mock
    ServiceInstancesConfig serviceInstancesConfig;
    
    @Test
    void testDeleteServiceInstance() {
      // handle async, since platform supports it and 
      // implementation as well.
      instance.setDeleteServiceInstanceAsync(true);
      when(deleteRequest.isAsyncAccepted()).thenReturn(true);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceAsync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstance(deleteRequest).block();
      
      verify(instance).deleteServiceInstanceAsync(deleteRequest, operation, entity);
      
      // handle sync, though platform supports async but
      // implementation does not.
      instance.setDeleteServiceInstanceAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(true);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstance(deleteRequest).block();
      
      verify(instance).deleteServiceInstanceSync(deleteRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // even though implementation does.
      instance.setDeleteServiceInstanceAsync(true);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstance(deleteRequest).block();
      
      verify(instance, times(2)).deleteServiceInstanceSync(deleteRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // and implementation does not either.
      instance.setDeleteServiceInstanceAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstance(deleteRequest).block();
      
      verify(instance, times(3)).deleteServiceInstanceSync(deleteRequest, operation, entity);
    }
    
    @Test
    void testDeleteServiceInstanceForceDeletesUnknownServiceInstancesIfEnabled() {
      instance.setDeleteServiceInstanceAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.error(new ServiceInstanceNotFoundException()));
      
      //enable force deletion of unknowns.
      when(config.getServiceInstances()).thenReturn(serviceInstancesConfig);
      when(serviceInstancesConfig.isForceDeleteUnknown()).thenReturn(true);
      
      DeleteServiceInstanceResponse response = instance.deleteServiceInstance(deleteRequest).block();
      
      verify(instance, never()).deleteServiceInstanceSync(deleteRequest, operation, entity);
      assertThat(response.isAsync()).isFalse();
    }
    
    @Test
    void testDeleteServiceInstanceDoesNotForceDeleteForAnyOtherReasonThanInstanceNotFound() {
      instance.setDeleteServiceInstanceAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstance(deleteRequest).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(instance, never()).deleteServiceInstanceSync(deleteRequest, operation, entity);
      verify(config, never()).getServiceInstances();
      verify(serviceInstancesConfig, never()).isForceDeleteUnknown();
    }
    
    @Test
    void testDeleteServiceInstanceDoesNotForceDeleteUnknownServiceInstancesIfNotEnabled() {
      instance.setDeleteServiceInstanceAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.error(new ServiceInstanceNotFoundException()));
      
      //enable force deletion of unknowns.
      when(config.getServiceInstances()).thenReturn(serviceInstancesConfig);
      when(serviceInstancesConfig.isForceDeleteUnknown()).thenReturn(false);
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstance(deleteRequest).block();
      }).isInstanceOf(ServiceInstanceNotFoundException.class);
      
      verify(instance, never()).deleteServiceInstanceSync(deleteRequest, operation, entity);
    }
    
    @Test
    void testDeleteServiceInstanceSync() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty())
        .when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));
      
      DeleteServiceInstanceResponse response = instance.deleteServiceInstanceSync(deleteRequest, operation, entity).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      verify(storage).deleteServiceInstance(entity);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.isAsync()).isFalse();
    }
    
    @Test
    void testDeleteServiceInstanceSyncPropagatesErrorsFromSubclassAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      
      doReturn(error())
        .when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                              any(ServiceInstanceInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(any(State.class));
      verify(storage).deleteServiceInstance(entity);
      verify(storage).updateServiceInstanceOperation(operation);
    }
    
    @Test
    void testDeleteServiceInstanceSyncPropagatesErrorsFromOperationPersistenceAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage).deleteServiceInstance(entity);
      verify(storage, times(2)).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testDeleteServiceInstanceSyncPropagatesErrorsFromInstancePersistenceAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstance(entity)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).deleteServiceInstance(entity);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }

    @Test
    void testDeleteServiceInstanceAsync() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceResponse response = instance.deleteServiceInstanceAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceAsyncDoesNotPropagateSubclassErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(error()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceResponse response = instance.deleteServiceInstanceAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceAsyncDoesNotPropagateInstancePersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstance(entity)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceResponse response = instance.deleteServiceInstanceAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceAsyncDoesNotPropagateOperationPersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstance(any(DeleteServiceInstanceRequest.class), 
                                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstance(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());

      DeleteServiceInstanceResponse response = instance.deleteServiceInstanceAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage, times(2)).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
  }
  
  @Nested
  class UpdateServiceInstanceTests {
    @Mock
    UpdateServiceInstanceRequest updateRequest;
    
    @Mock
    UpdateServiceInstanceResponse updateResponse;
    
    @Test
    void testUpdateServiceInstance() {
      // handle async, since platform supports it and 
      // implementation as well.
      instance.setUpdateServiceInstanceAsync(true);
      when(updateRequest.isAsyncAccepted()).thenReturn(true);
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertUpdateServiceInstanceOperation(updateRequest)).thenReturn(Mono.just(operation));

      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(updateResponse))
        .when(instance).updateServiceInstanceAsync(updateRequest, operation, entity);
      
      instance.updateServiceInstance(updateRequest).block();
      
      verify(instance).updateServiceInstanceAsync(updateRequest, operation, entity);
      
      // handle sync, though platform supports async but
      // implementation does not.
      instance.setUpdateServiceInstanceAsync(false);
      when(updateRequest.isAsyncAccepted()).thenReturn(true);
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertUpdateServiceInstanceOperation(updateRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(updateResponse)).when(instance).updateServiceInstanceSync(updateRequest, operation, entity);
      
      instance.updateServiceInstance(updateRequest).block();
      
      verify(instance).updateServiceInstanceSync(updateRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // even though implementation does.
      instance.setUpdateServiceInstanceAsync(true);
      when(updateRequest.isAsyncAccepted()).thenReturn(false);
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertUpdateServiceInstanceOperation(updateRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(updateResponse)).when(instance).updateServiceInstanceSync(updateRequest, operation, entity);
      
      instance.updateServiceInstance(updateRequest).block();
      
      verify(instance, times(2)).updateServiceInstanceSync(updateRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // and implementation does not either.
      instance.setUpdateServiceInstanceAsync(false);
      when(updateRequest.isAsyncAccepted()).thenReturn(false);
      when(updateRequest.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
      when(storage.readServiceInstanceById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertUpdateServiceInstanceOperation(updateRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(updateResponse)).when(instance).updateServiceInstanceSync(updateRequest, operation, entity);
      
      instance.updateServiceInstance(updateRequest).block();
      
      verify(instance, times(3)).updateServiceInstanceSync(updateRequest, operation, entity);
    }
    
    @Test
    void testUpdateServiceInstanceSync() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(storage.updateServiceInstance(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      UpdateServiceInstanceResponse response = instance.updateServiceInstanceSync(updateRequest, operation, entity).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.isAsync()).isFalse();
      assertThat(response.getOperation()).isNull();
      assertThat(response.getDashboardUrl()).isNull();
    }
    
    @Test
    void testUpdateServiceInstanceSyncPropagatesErrorsFromSubclassAndUpdatesOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      doReturn(error())
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), 
                                              any(ServiceInstanceInfo.class));
                                          
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));
      
      assertThatThrownBy(() -> {
          instance.updateServiceInstanceSync(updateRequest, operation, entity).block();  
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(any(State.class));
      verify(storage, never()).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
    }
    
    @Test
    void testUpdateServiceInstanceSyncPropagatesErrorsFromInstancePersistenceAndUpdatesOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(storage.updateServiceInstance(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.just(operation));
      
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      assertThatThrownBy(() -> {
          instance.updateServiceInstanceSync(updateRequest, operation, entity).block();  
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testUpdateServiceInstanceSyncPropagatesErrorsFromOperationPersistenceAndUpdatesOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());
      
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      assertThatThrownBy(() -> {
          instance.updateServiceInstanceSync(updateRequest, operation, entity).block();  
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }

    @Test
    void testUpdateServiceInstanceAsync() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstance(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      UpdateServiceInstanceResponse response = instance.updateServiceInstanceAsync(updateRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getOperation()).isNotNull();
      assertThat(response.getDashboardUrl()).isNull();
    }
    
    @Test
    void testUpdateServiceInstanceAsyncDoesNotPropagateSubclassErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      doReturn(error())
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      UpdateServiceInstanceResponse response = instance.updateServiceInstanceAsync(updateRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage, never()).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getOperation()).isNotNull();
      assertThat(response.getDashboardUrl()).isNull();
    }

    @Test
    void testUpdateServiceInstanceAsyncDoesNotPropagateInstancePersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstance(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(Mono.empty());
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      UpdateServiceInstanceResponse response = instance.updateServiceInstanceAsync(updateRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getOperation()).isNotNull();
      assertThat(response.getDashboardUrl()).isNull();
    }
    
    @Test
    void testUpdateServiceInstanceAsyncDoesNotPropagateOperationPersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceInfo());
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.UPDATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstance(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceOperation(operation)).thenReturn(error());
      doReturn(Mono.just(instanceInfo))
        .when(instance).changeServiceInstance(any(UpdateServiceInstanceRequest.class), any(ServiceInstanceInfo.class));
      
      UpdateServiceInstanceResponse response = instance.updateServiceInstanceAsync(updateRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage).updateServiceInstance(instanceInfo, operation);
      verify(storage, times(2)).updateServiceInstanceOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.isAsync()).isTrue();
      assertThat(response.getOperation()).isNotNull();
      assertThat(response.getDashboardUrl()).isNull();
    }
  }

  @Nested
  public class AccessorTests {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testIsCreateServiceInstanceAsync(boolean async) {
      instance.setCreateServiceInstanceAsync(async);
      assertThat(instance.isCreateServiceInstanceAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSetCreateServiceInstanceAsync(boolean async) {
      instance.setCreateServiceInstanceAsync(async);
      assertThat(instance.isCreateServiceInstanceAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testIsDeleteServiceInstanceAsync(boolean async) {
      instance.setDeleteServiceInstanceAsync(async);
      assertThat(instance.isDeleteServiceInstanceAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSetDeleteServiceInstanceAsync(boolean async) {
      instance.setDeleteServiceInstanceAsync(async);
      assertThat(instance.isDeleteServiceInstanceAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testIsUpdateServiceInstanceAsync(boolean async) {
      instance.setUpdateServiceInstanceAsync(async);
      assertThat(instance.isUpdateServiceInstanceAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSetUpdateServiceInstanceAsync(boolean async) {
      instance.setUpdateServiceInstanceAsync(async);
      assertThat(instance.isUpdateServiceInstanceAsync()).isEqualTo(async);
    }
  }
}
