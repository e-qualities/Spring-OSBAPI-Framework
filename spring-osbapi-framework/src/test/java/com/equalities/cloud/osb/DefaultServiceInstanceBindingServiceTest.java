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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.Endpoint;
import org.springframework.cloud.servicebroker.model.binding.GetLastServiceBindingOperationRequest;
import org.springframework.cloud.servicebroker.model.binding.GetLastServiceBindingOperationResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;
import org.springframework.cloud.servicebroker.model.instance.OperationState;

import com.equalities.cloud.osb.DefaultServiceInstanceBindingService;
import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.config.OsbApiConfig.ServiceInstanceBindingsConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DefaultServiceInstanceBindingServiceTest {
  
  private static final String OPERATION_ID = "operationId";
  private static final String CREDENTIALS_KEY   = "com_equalities_cloud_osb_Credentials";
  private static final String PARAMETERS_KEY    = "com_equalities_cloud_osb_Parameters";
  private static final String ENDPOINTS_KEY     = "com_equalities_cloud_osb_Endpoints";
  private static final String VOLUME_MOUNTS_KEY = "com_equalities_cloud_osb_VolumeMounts";
  private static final String SERVICE_INSTANCE_BINDING_ID = "serviceInstanceId";
  private static final String ERROR_GENERATED_BY_TEST = "ErrorGeneratedByTest";
  
  private <T> Mono<T> error() {
    return Mono.error(new RuntimeException(ERROR_GENERATED_BY_TEST));
  }
  
  @Mock
  PersistentStorage storage;
  
  @Mock
  OsbApiConfig config;
  
  @Mock
  GetServiceInstanceBindingRequest getInstanceRequest;
  
  @Mock
  GetLastServiceBindingOperationRequest getLastOperationRequest;
  
  @Mock
  ServiceInstanceBindingOperationEntity operation;
  
  @Mock
  ServiceInstanceBindingEntity entity;
  
  @Mock
  ServiceInstanceBindingInfo instanceInfo;
  
  @Mock
  ServiceOperationStatus status;
  
  @Captor
  ArgumentCaptor<State> stateCaptor;
  
  DefaultServiceInstanceBindingService instance;

  @BeforeEach
  void setUp() throws Exception {
    instance = spy(new DefaultServiceInstanceBindingService(storage, config) {
      @Override
      public Mono<ServiceInstanceBindingInfo> addServiceInstanceBinding(CreateServiceInstanceBindingRequest request, ServiceInstanceInfo serviceInstanceInfo) {
        throw new UnsupportedOperationException("This exception must never occur in a test, since this method should have been stubbed using the spy around this instance."
            + " Make sure you have properly mocked this method in your test.");
      }
      @Override
      public Mono<Void> removeServiceInstanceBinding(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingInfo instanceInfo) {
        throw new UnsupportedOperationException("This exception must never occur in a test, since this method should have been stubbed using the spy around this instance."
            + " Make sure you have properly mocked this method in your test.");
      }
    });
  }
  
  @Test
  void testGetServiceInstanceBinding() {
    final ServiceInstanceBindingInfo data = new ServiceInstanceBindingInfo();
    
    when(getInstanceRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
    when(storage.readServiceInstanceBindingById(SERVICE_INSTANCE_BINDING_ID)).thenReturn(Mono.just(entity));
    when(entity.getData()).thenReturn(data);
    
    GetServiceInstanceBindingResponse response = instance.getServiceInstanceBinding(getInstanceRequest).block();
    
    verify(storage).readServiceInstanceBindingById(SERVICE_INSTANCE_BINDING_ID);
    
    assertThat(response).isInstanceOf(GetServiceInstanceAppBindingResponse.class);
    GetServiceInstanceAppBindingResponse appResponse = (GetServiceInstanceAppBindingResponse) response;
    
    assertThat(appResponse.getParameters()).isNotNull();
    assertThat(appResponse.getCredentials()).isNotNull();
    assertThat(appResponse.getEndpoints()).isNotNull();
    assertThat(appResponse.getVolumeMounts()).isNotNull();
  }
  
  @Test
  void testGetServiceInstanceBindingContainsDataFromServiceInstanceBindingInfo() {
    final ServiceInstanceBindingInfo data = new ServiceInstanceBindingInfo();
    final HashMap<String, Object> credentials = new HashMap<>(); credentials.put("test", "credential");
    final HashMap<String, Object> parameters = new HashMap<>();  parameters.put("test", "parameter");
    final List<Endpoint> endpoints = new ArrayList<>();          endpoints.add(new Endpoint());
    final List<VolumeMount> volumeMounts = new ArrayList<>();    volumeMounts.add(new VolumeMount());
    
    data.put(CREDENTIALS_KEY, credentials);
    data.put(ENDPOINTS_KEY, endpoints);
    data.put(VOLUME_MOUNTS_KEY, volumeMounts);
    data.put(PARAMETERS_KEY, parameters);
    
    when(getInstanceRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
    when(storage.readServiceInstanceBindingById(SERVICE_INSTANCE_BINDING_ID)).thenReturn(Mono.just(entity));
    when(entity.getData()).thenReturn(data);
    
    GetServiceInstanceBindingResponse response = instance.getServiceInstanceBinding(getInstanceRequest).block();
    
    verify(storage).readServiceInstanceBindingById(SERVICE_INSTANCE_BINDING_ID);
    
    assertThat(response).isInstanceOf(GetServiceInstanceAppBindingResponse.class);
    GetServiceInstanceAppBindingResponse appResponse = (GetServiceInstanceAppBindingResponse) response;
    
    assertThat(appResponse.getParameters()).containsAllEntriesOf(parameters);
    assertThat(appResponse.getCredentials()).containsAllEntriesOf(credentials);
    assertThat(appResponse.getEndpoints()).containsAll(endpoints);
    assertThat(appResponse.getVolumeMounts()).containsAll(volumeMounts);
  }

  @ParameterizedTest
  @EnumSource
  void testGetLastOperation(State operationState) {
    HashMap<State, OperationState> stateTypesMap = new HashMap<>();
    stateTypesMap.put(SUCCEEDED, OperationState.SUCCEEDED);
    stateTypesMap.put(IN_PROGRESS, OperationState.IN_PROGRESS);
    stateTypesMap.put(FAILED, OperationState.FAILED);
    
    when(getLastOperationRequest.getOperation()).thenReturn(OPERATION_ID);
    when(storage.readServiceInstanceBindingOperationById(OPERATION_ID)).thenReturn(Mono.just(operation));
    when(operation.getType()).thenReturn(ServiceInstanceOperationEntity.Type.DELETE);
    when(operation.getStatus()).thenReturn(status);
    when(status.getState()).thenReturn(operationState);
    
    GetLastServiceBindingOperationResponse response = instance.getLastOperation(getLastOperationRequest).block();
    
    verify(storage).readServiceInstanceBindingOperationById(OPERATION_ID);
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
    when(storage.readServiceInstanceBindingOperationById(OPERATION_ID)).thenReturn(Mono.error(new ServiceInstanceBindingOperationNotFoundException("TriggeredByTest")));
    
    GetLastServiceBindingOperationResponse response = instance.getLastOperation(getLastOperationRequest).block();
    
    verify(storage).readServiceInstanceBindingOperationById(OPERATION_ID);
    assertThat(response.getState()).isEqualTo(OperationState.FAILED);
    assertThat(response.getDescription()).isEqualTo("TriggeredByTest");
    assertThat(response.isDeleteOperation()).isFalse();
  }
  
  @Nested
  class CreateServiceInstanceBindingTests {

    @Mock
    CreateServiceInstanceBindingRequest createRequest;
    
    @Mock
    CreateServiceInstanceBindingResponse createResponse;
    
    @Mock
    ServiceInstanceInfo serviceInstanceInfo;
    
    @Mock
    ServiceInstanceEntity serviceInstanceEntity;
    
    @Test
    void testCreateServiceInstanceBinding() {
      ServiceInstanceInfo data = new ServiceInstanceInfo();
      // handle async, since platform supports it and 
      // implementation as well.
      instance.setCreateServiceInstanceBindingAsync(true);
      when(createRequest.isAsyncAccepted()).thenReturn(true);
      when(storage.insertCreateServiceInstanceBindingOperation(any())).thenReturn(Mono.just(operation));
      when(storage.readServiceInstanceById(any())).thenReturn(Mono.just(serviceInstanceEntity));
      when(serviceInstanceEntity.getData()).thenReturn(data);
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse))
        .when(instance).createServiceInstanceBindingAsync(any(CreateServiceInstanceBindingRequest.class), 
                                                          any(ServiceInstanceBindingOperationEntity.class), 
                                                          any(ServiceInstanceInfo.class));
      
      instance.createServiceInstanceBinding(createRequest).block();
      
      verify(instance).createServiceInstanceBindingAsync(any(CreateServiceInstanceBindingRequest.class), 
                                                         any(ServiceInstanceBindingOperationEntity.class), 
                                                         any(ServiceInstanceInfo.class));
      
      // handle sync, though platform supports async but
      // implementation does not.
      instance.setCreateServiceInstanceBindingAsync(false);
      when(createRequest.isAsyncAccepted()).thenReturn(true);
      when(storage.insertCreateServiceInstanceBindingOperation(any())).thenReturn(Mono.just(operation));
      when(storage.readServiceInstanceById(any())).thenReturn(Mono.just(serviceInstanceEntity));
      when(serviceInstanceEntity.getData()).thenReturn(data);
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse))
        .when(instance).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                          any(ServiceInstanceBindingOperationEntity.class), 
                                                          any(ServiceInstanceInfo.class));
      
      instance.createServiceInstanceBinding(createRequest).block();
      
      verify(instance).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                        any(ServiceInstanceBindingOperationEntity.class), 
                                                        any(ServiceInstanceInfo.class));
      
      // handle sync, since platform does not support async
      // even though implementation does.
      instance.setCreateServiceInstanceBindingAsync(true);
      when(createRequest.isAsyncAccepted()).thenReturn(false);
      when(storage.insertCreateServiceInstanceBindingOperation(any())).thenReturn(Mono.just(operation));
      when(storage.readServiceInstanceById(any())).thenReturn(Mono.just(serviceInstanceEntity));
      when(serviceInstanceEntity.getData()).thenReturn(data);
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse))
        .when(instance).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                        any(ServiceInstanceBindingOperationEntity.class), 
                                                        any(ServiceInstanceInfo.class));
      
      instance.createServiceInstanceBinding(createRequest).block();
      
      verify(instance, times(2)).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingOperationEntity.class), 
                                                                  any(ServiceInstanceInfo.class));
      
      // handle sync, since platform does not support async
      // and implementation does not either.
      instance.setCreateServiceInstanceBindingAsync(false);
      when(createRequest.isAsyncAccepted()).thenReturn(false);
      when(storage.insertCreateServiceInstanceBindingOperation(any())).thenReturn(Mono.just(operation));
      when(storage.readServiceInstanceById(any())).thenReturn(Mono.just(serviceInstanceEntity));
      when(serviceInstanceEntity.getData()).thenReturn(data);
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(createResponse))
        .when(instance).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                          any(ServiceInstanceBindingOperationEntity.class), 
                                                          any(ServiceInstanceInfo.class));
      
      instance.createServiceInstanceBinding(createRequest).block();
      
      verify(instance, times(3)).createServiceInstanceBindingSync(any(CreateServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingOperationEntity.class), 
                                                                  any(ServiceInstanceInfo.class));
    }

    @Test
    void testCreateServiceInstanceBindingSync() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      when(storage.insertServiceInstanceBinding(any(ServiceInstanceBindingInfo.class), 
                                                any(ServiceInstanceBindingOperationEntity.class)))
                                                .thenReturn(Mono.just(entity));
      
      CreateServiceInstanceBindingResponse response = instance.createServiceInstanceBindingSync(createRequest, operation, serviceInstanceInfo).block();
      
      assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
      CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(appResponse.isAsync()).isFalse();
      assertThat(appResponse.getOperation()).isNull();
      assertThat(appResponse.getCredentials()).isNotNull();
      assertThat(appResponse.getEndpoints()).isNotNull();
      assertThat(appResponse.getVolumeMounts()).isNotNull();
    }
    
    @Test
    void testCreateServiceInstanceBindingSyncPropagatesSubclassErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(error())
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceBindingSync(createRequest, operation, serviceInstanceInfo).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testCreateServiceInstanceSyncPropagatesInstancePersistenceErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.insertServiceInstanceBinding(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceBindingSync(createRequest, operation, serviceInstanceInfo).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).insertServiceInstanceBinding(instanceInfo, operation);
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testCreateServiceInstanceBindingSyncPropagatesOperationPersistenceErrorsAndUpdatesOperationStatusToFailed() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(storage.insertServiceInstanceBinding(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.createServiceInstanceBindingSync(createRequest, operation, serviceInstanceInfo).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage).insertServiceInstanceBinding(instanceInfo, operation);
      verify(storage, times(2)).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }

    @Test
    void testCreateServiceInstanceBindingAsync() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      when(storage.insertServiceInstanceBinding(any(ServiceInstanceBindingInfo.class), 
                                                any(ServiceInstanceBindingOperationEntity.class)))
                                                .thenReturn(Mono.just(entity));
      
      CreateServiceInstanceBindingResponse response = instance.createServiceInstanceBindingAsync(createRequest, operation, serviceInstanceInfo)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
      CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(appResponse.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(appResponse.isAsync()).isTrue();
      assertThat(appResponse.getCredentials()).isNotNull();
      assertThat(appResponse.getEndpoints()).isNotNull();
      assertThat(appResponse.getVolumeMounts()).isNotNull();
    }
    
    @Test
    void testCreateServiceInstanceBindingAsyncDoesNotPropagateSubclassErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(error())
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      
      CreateServiceInstanceBindingResponse response = instance.createServiceInstanceBindingAsync(createRequest, operation, serviceInstanceInfo)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
      CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(appResponse.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(appResponse.isAsync()).isTrue();
      assertThat(appResponse.getCredentials()).isNotNull();
      assertThat(appResponse.getEndpoints()).isNotNull();
      assertThat(appResponse.getVolumeMounts()).isNotNull();
    }
    
    @Test
    void testCreateServiceInstanceBindingAsyncDoesNotPropagateInstancePersistenceErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.insertServiceInstanceBinding(instanceInfo, operation)).thenReturn(error());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.empty());
      
      CreateServiceInstanceBindingResponse response = instance.createServiceInstanceBindingAsync(createRequest, operation, serviceInstanceInfo)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
      CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(appResponse.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(appResponse.isAsync()).isTrue();
      assertThat(appResponse.getCredentials()).isNotNull();
      assertThat(appResponse.getEndpoints()).isNotNull();
      assertThat(appResponse.getVolumeMounts()).isNotNull();
    }
    
    @Test
    void testCreateServiceInstanceBindingAsyncDoesNotPropagateOperationPersistenceErrorsButSetsOperationStatusToFailedOnError() {
      doReturn(Mono.just(instanceInfo))
        .when(instance).addServiceInstanceBinding(any(CreateServiceInstanceBindingRequest.class), 
                                                  any(ServiceInstanceInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.CREATE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.insertServiceInstanceBinding(instanceInfo, operation)).thenReturn(Mono.just(entity));
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(error());
      
      CreateServiceInstanceBindingResponse response = instance.createServiceInstanceBindingAsync(createRequest, operation, serviceInstanceInfo)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
      CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage, times(2)).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(appResponse.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(appResponse.isAsync()).isTrue();
      assertThat(appResponse.getCredentials()).isNotNull();
      assertThat(appResponse.getEndpoints()).isNotNull();
      assertThat(appResponse.getVolumeMounts()).isNotNull();
    }
  }
  
  @Nested
  class DeleteServiceInstanceTests {
    
    @Mock
    DeleteServiceInstanceBindingRequest deleteRequest;
    
    @Mock
    DeleteServiceInstanceBindingResponse deleteResponse;
    
    @Mock
    ServiceInstanceBindingsConfig serviceInstanceBindingsConfig;
    
    @Test
    void testDeleteServiceInstanceBinding() {
      // handle async, since platform supports it and 
      // implementation as well.
      instance.setDeleteServiceInstanceBindingAsync(true);
      when(deleteRequest.isAsyncAccepted()).thenReturn(true);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceBindingOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceBindingAsync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstanceBinding(deleteRequest).block();
      
      verify(instance).deleteServiceInstanceBindingAsync(deleteRequest, operation, entity);
      
      // handle sync, though platform supports async but
      // implementation does not.
      instance.setDeleteServiceInstanceBindingAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(true);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceBindingOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstanceBinding(deleteRequest).block();
      
      verify(instance).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // even though implementation does.
      instance.setDeleteServiceInstanceBindingAsync(true);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceBindingOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstanceBinding(deleteRequest).block();
      
      verify(instance, times(2)).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      
      // handle sync, since platform does not support async
      // and implementation does not either.
      instance.setDeleteServiceInstanceBindingAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.just(entity));
      when(storage.insertDeleteServiceInstanceBindingOperation(deleteRequest)).thenReturn(Mono.just(operation));
      // you need to use this syntax for spies that stub existing methods.
      doReturn(Mono.just(deleteResponse)).when(instance).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      
      instance.deleteServiceInstanceBinding(deleteRequest).block();
      
      verify(instance, times(3)).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
    }
    
    @Test
    void testDeleteServiceInstanceBindingForceDeletesUnknownServiceInstancesIfEnabled() {
      instance.setDeleteServiceInstanceBindingAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.error(new ServiceInstanceNotFoundException()));
      
      //enable force deletion of unknowns.
      when(config.getServiceBindings()).thenReturn(serviceInstanceBindingsConfig);
      when(serviceInstanceBindingsConfig.isForceDeleteUnknown()).thenReturn(true);
      
      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBinding(deleteRequest).block();
      
      verify(instance, never()).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      assertThat(response.isAsync()).isFalse();
    }
    
    @Test
    void testDeleteServiceInstanceBindingDoesNotForceDeleteForAnyOtherReasonThanInstanceNotFound() {
      instance.setDeleteServiceInstanceBindingAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceBinding(deleteRequest).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(instance, never()).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
      verify(config, never()).getServiceBindings();
      verify(serviceInstanceBindingsConfig, never()).isForceDeleteUnknown();
    }
    
    @Test
    void testDeleteServiceInstanceBindingDoesNotForceDeleteUnknownServiceInstancesIfNotEnabled() {
      instance.setDeleteServiceInstanceBindingAsync(false);
      when(deleteRequest.isAsyncAccepted()).thenReturn(false);
      when(deleteRequest.getBindingId()).thenReturn(SERVICE_INSTANCE_BINDING_ID);
      when(storage.readServiceInstanceBindingById(anyString())).thenReturn(Mono.error(new ServiceInstanceNotFoundException()));
      
      //enable force deletion of unknowns.
      when(config.getServiceBindings()).thenReturn(serviceInstanceBindingsConfig);
      when(serviceInstanceBindingsConfig.isForceDeleteUnknown()).thenReturn(false);
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceBinding(deleteRequest).block();
      }).isInstanceOf(ServiceInstanceNotFoundException.class);
      
      verify(instance, never()).deleteServiceInstanceBindingSync(deleteRequest, operation, entity);
    }
    
    @Test
    void testDeleteServiceInstanceBindingSync() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty())
        .when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), any(ServiceInstanceBindingInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));
      
      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBindingSync(deleteRequest, operation, entity).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      verify(storage).deleteServiceInstanceBinding(entity);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.isAsync()).isFalse();
    }
    
    @Test
    void testDeleteServiceInstanceBindingSyncPropagatesErrorsFromSubclassAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      
      doReturn(error())
        .when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                              any(ServiceInstanceBindingInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceBindingSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(any(State.class));
      verify(storage).deleteServiceInstanceBinding(entity);
      verify(storage).updateServiceInstanceBindingOperation(operation);
    }
    
    @Test
    void testDeleteServiceInstanceBindingSyncPropagatesErrorsFromOperationPersistenceAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(error());
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceBindingSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage).deleteServiceInstanceBinding(entity);
      verify(storage, times(2)).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }
    
    @Test
    void testDeleteServiceInstanceBindingSyncPropagatesErrorsFromInstancePersistenceAndSetsOperationStatusToFailed() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));

      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(error());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));
      
      assertThatThrownBy(() -> {
        instance.deleteServiceInstanceBindingSync(deleteRequest, operation, entity).block();
      }).isInstanceOf(RuntimeException.class).hasMessage(ERROR_GENERATED_BY_TEST);
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).deleteServiceInstanceBinding(entity);
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
    }

    @Test
    void testDeleteServiceInstanceBindingAsync() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBindingAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(SUCCEEDED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceBindingAsyncDoesNotPropagateSubclassErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(error()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBindingAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceBindingAsyncDoesNotPropagateInstancePersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(error());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(Mono.just(operation));

      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBindingAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status).setState(stateCaptor.capture());
      verify(storage).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
    
    @Test
    void testDeleteServiceInstanceBindingAsyncDoesNotPropagateOperationPersistenceErrorsButSetsOperationStatusToFailedOnError() {
      when(entity.getData()).thenReturn(new ServiceInstanceBindingInfo());
      doReturn(Mono.empty()).when(instance).removeServiceInstanceBinding(any(DeleteServiceInstanceBindingRequest.class), 
                                                                  any(ServiceInstanceBindingInfo.class));
      
      when(operation.getStatus()).thenReturn(status);
      when(operation.getType()).thenReturn(Type.DELETE);
      when(operation.getId()).thenReturn(OPERATION_ID);
      when(storage.deleteServiceInstanceBinding(entity)).thenReturn(Mono.empty());
      when(storage.updateServiceInstanceBindingOperation(operation)).thenReturn(error());

      DeleteServiceInstanceBindingResponse response = instance.deleteServiceInstanceBindingAsync(deleteRequest, operation, entity)
          .delaySubscription(Duration.ofMillis(800)).block();
      
      verify(status, times(2)).setState(stateCaptor.capture());
      verify(storage, times(2)).updateServiceInstanceBindingOperation(operation);
      assertThat(stateCaptor.getValue()).isEqualTo(FAILED);
      assertThat(response.getOperation()).isEqualTo(OPERATION_ID);
      assertThat(response.isAsync()).isTrue();
    }
  }
  

  @Nested
  public class AccessorTests {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testIsCreateServiceInstanceBindingAsync(boolean async) {
      instance.setCreateServiceInstanceBindingAsync(async);
      assertThat(instance.isCreateServiceInstanceBindingAsync()).isEqualTo(async);
    }
    
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSetCreateServiceInstanceBindingAsync(boolean async) {
      instance.setCreateServiceInstanceBindingAsync(async);
      assertThat(instance.isCreateServiceInstanceBindingAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testIsDeleteServiceInstanceBindingAsync(boolean async) {
      instance.setDeleteServiceInstanceBindingAsync(async);
      assertThat(instance.isDeleteServiceInstanceBindingAsync()).isEqualTo(async);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSetDeleteServiceInstanceBindingAsync(boolean async) {
      instance.setDeleteServiceInstanceBindingAsync(async);
      assertThat(instance.isDeleteServiceInstanceBindingAsync()).isEqualTo(async);
    }
  }
}
