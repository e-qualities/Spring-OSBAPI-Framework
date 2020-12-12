package com.equalities.cloud.osb;

import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.DELETE;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.FAILED;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.SUCCEEDED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public abstract class DefaultServiceInstanceBindingService implements ServiceInstanceBindingService {
  private final PersistentStorage storage;
  private final OsbApiConfig config;
  
  private boolean createServiceInstanceBindingAsync = true;
  private boolean deleteServiceInstanceBindingAsync = true;

  public DefaultServiceInstanceBindingService(PersistentStorage persistentStorage, OsbApiConfig config) {
    this.storage = persistentStorage;
    this.config = config;
  }

  @Override
  public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
    log.info("Received callback to CREATE service instance binding.");
    log.info("Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.debug("Request from Cloud Controller:           {}", request);
    log.debug("Cloud Context:                           {}", request.getContext());
    
    final boolean platformSupportsAsyncInstanceCreation = request.isAsyncAccepted();
    final String serviceInstanceId = request.getServiceInstanceId();

    Mono<ServiceInstanceBindingOperationEntity> persistOperationAsync = storage.insertCreateServiceInstanceBindingOperation(request);
    Mono<ServiceInstanceEntity> readServiceInstanceAsync = storage.readServiceInstanceById(serviceInstanceId);

    if (platformSupportsAsyncInstanceCreation && createServiceInstanceBindingAsync) {
      return Mono.zip(persistOperationAsync, readServiceInstanceAsync)
          .flatMap( zippedResults -> {
            ServiceInstanceBindingOperationEntity bindingOperationEntity = zippedResults.getT1(); 
            ServiceInstanceEntity svcInstanceEntity = zippedResults.getT2();
            
            ServiceInstanceInfo serviceInstanceInfo = svcInstanceEntity.getData();
            return createServiceInstanceBindingAsync(request, bindingOperationEntity, serviceInstanceInfo); 
          });
    } else {
      return Mono.zip(persistOperationAsync, readServiceInstanceAsync)
          .flatMap( zippedResults -> {
            ServiceInstanceBindingOperationEntity bindingOperationEntity = zippedResults.getT1(); 
            ServiceInstanceEntity svcInstanceEntity = zippedResults.getT2();
            
            ServiceInstanceInfo serviceInstanceInfo = svcInstanceEntity.getData();
            return createServiceInstanceBindingSync(request, bindingOperationEntity, serviceInstanceInfo); 
      });
    }
  }
  
  protected Mono<? extends CreateServiceInstanceBindingResponse> createServiceInstanceBindingAsync(CreateServiceInstanceBindingRequest request, 
                                                                                                ServiceInstanceBindingOperationEntity operation,
                                                                                                ServiceInstanceInfo serviceInstanceInfo) {
    log.info("Creating service instance binding asynchronously.");
    
    addServiceInstanceBinding(request, serviceInstanceInfo)
      .switchIfEmpty(Mono.just(new ServiceInstanceBindingInfo()))
      .flatMap( serviceBindingInfo -> {
        return storage.insertServiceInstanceBinding(serviceBindingInfo, operation)
                      .then(updateOperationStatusAndPersist(operation, null));
      })
      .onErrorResume( error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
      .doOnError(error -> log.error("Error! Asynchronous creation of service instance binding failed.", error))
      .subscribeOn(Schedulers.boundedElastic()).subscribe();

    return createServiceInstanceBindingAsyncResponse(operation);
  }

  private Mono<CreateServiceInstanceAppBindingResponse> createServiceInstanceBindingAsyncResponse(ServiceInstanceBindingOperationEntity operation) {
    return Mono.just(CreateServiceInstanceAppBindingResponse.builder()
        .async(true)
        .operation(operation.getId())
        .bindingExisted(false)
        .build());
  }
  
  protected Mono<? extends CreateServiceInstanceBindingResponse> createServiceInstanceBindingSync(CreateServiceInstanceBindingRequest request, 
                                                                                                ServiceInstanceBindingOperationEntity operation,
                                                                                                ServiceInstanceInfo serviceInstanceInfo) {
    log.info("Creating service instance binding synchronously.");
    return addServiceInstanceBinding(request, serviceInstanceInfo)
        .switchIfEmpty(Mono.just(new ServiceInstanceBindingInfo()))
        .flatMap( serviceBindingInfo -> {
          return storage.insertServiceInstanceBinding(serviceBindingInfo, operation)
                        .then(updateOperationStatusAndPersist(operation, null))
                        .then(createServiceInstanceBindingSyncResponse(getCredentialsFrom(serviceBindingInfo), 
                                                                       getEndpointsFrom(serviceBindingInfo), 
                                                                       getVolumeMountsFrom(serviceBindingInfo)));
        })
        .onErrorResume( error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
        .doOnError(error -> log.error("Error! Synchronous creation of service instance binding failed.", error));
  }

  private Mono<CreateServiceInstanceAppBindingResponse> createServiceInstanceBindingSyncResponse(Map<String, Object> credentials, List<Endpoint> endpoints, List<VolumeMount> volumeMounts) {
    return Mono.just(
      CreateServiceInstanceAppBindingResponse.builder()
      .async(false)
      .bindingExisted(false)
      .credentials(credentials)
      .volumeMounts(volumeMounts)
      .endpoints(endpoints)
      .build());
  }
  
  @Override
  public Mono<GetServiceInstanceBindingResponse> getServiceInstanceBinding(GetServiceInstanceBindingRequest request) {
    log.info("Received callback to GET service instance binding.");
    log.debug("Request from Cloud Controller: {}", request);
    
    String serviceInstanceBindingId = request.getBindingId();
    return storage.readServiceInstanceBindingById(serviceInstanceBindingId)
                  .flatMap(binding -> {
                    ServiceInstanceBindingInfo serviceBindingInfo = binding.getData();
                    return getServiceInstanceBindingResponse(getCredentialsFrom(serviceBindingInfo), 
                                                             getParametersFrom(serviceBindingInfo), 
                                                             getEndpointsFrom(serviceBindingInfo), 
                                                             getVolumeMountsFrom(serviceBindingInfo));
                  });
  }

  private Mono<GetServiceInstanceAppBindingResponse> getServiceInstanceBindingResponse(Map<String, Object> credentials, Map<String, Object> parameters, List<Endpoint> endpoints, List<VolumeMount> volumeMounts) {
    return Mono.just(GetServiceInstanceAppBindingResponse.builder()
        .parameters(parameters)
        .credentials(credentials)
        .volumeMounts(volumeMounts)
        .endpoints(endpoints)
        .build());
  }
  
  private Map<String, Object> getCredentialsFrom(ServiceInstanceBindingInfo serviceBindingInfo) {
    Map<String, Object> credentials = serviceBindingInfo.getCredentials();
    return credentials != null ? credentials : new HashMap<String, Object>();
  }
  
  private List<Endpoint> getEndpointsFrom(ServiceInstanceBindingInfo serviceBindingInfo) {
    List<Endpoint> endpoints = serviceBindingInfo.getEndpoints();
    return endpoints != null ? endpoints : new ArrayList<Endpoint>();
  }
  
  private List<VolumeMount> getVolumeMountsFrom(ServiceInstanceBindingInfo serviceBindingInfo) {
    List<VolumeMount> volumeMounts = serviceBindingInfo.getVolumeMounts();
    return volumeMounts != null ? volumeMounts : new ArrayList<VolumeMount>();
  }
  
  private Map<String, Object> getParametersFrom(ServiceInstanceBindingInfo serviceBindingInfo) {
    Map<String, Object> parameters = serviceBindingInfo.getParameters();
    return parameters != null ? parameters : new HashMap<String, Object>();
  }

  @Override
  public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
    log.info("Received callback to DELETE service instance binding.");
    log.info("Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.debug("Request from Cloud Controller:           {}", request);
    
    final boolean platformSupportsAsyncInstanceBindingCreation = request.isAsyncAccepted();
    final String bindingId = request.getBindingId();
    
    Mono<ServiceInstanceBindingEntity> loadingInstance = storage.readServiceInstanceBindingById(bindingId);
    
    if (platformSupportsAsyncInstanceBindingCreation && deleteServiceInstanceBindingAsync) {
      return loadingInstance.flatMap( binding -> {
        return storage.insertDeleteServiceInstanceBindingOperation(request)
            .flatMap( operation -> {
              return deleteServiceInstanceBindingAsync(request, operation, binding);
            });
      })
      .onErrorResume(this::deleteErrorFallbackPredicate, this::deleteErrorFallback);
    }
    else {
      return loadingInstance.flatMap(binding -> {
        return storage.insertDeleteServiceInstanceBindingOperation(request).flatMap( operation -> {
          return deleteServiceInstanceBindingSync(request, operation, binding);
        });
      })
      .onErrorResume(this::deleteErrorFallbackPredicate, this::deleteErrorFallback);
    }
  }

  private boolean deleteErrorFallbackPredicate(Throwable error) {
    final boolean unknownInstanceOrBinding = (error instanceof ServiceInstanceNotFoundException ||
                                              error instanceof ServiceInstanceBindingNotFoundException);
    
    final boolean removeUnknownBindings =  unknownInstanceOrBinding && 
                                           config.getServiceBindings().isForceDeleteUnknown();
    
    log.warn("An error occurred during deletion of a service instance binding.");
    log.warn("Removing unknown service bindings: {}", removeUnknownBindings);
    return removeUnknownBindings;
  }

  private Mono<DeleteServiceInstanceBindingResponse> deleteErrorFallback(Throwable error) {
    log.warn("Removing unknown service binding.");
    return deleteServiceInstanceBindingSyncResponse();
  }

  protected Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBindingAsync(final DeleteServiceInstanceBindingRequest request, final ServiceInstanceBindingOperationEntity operation, final ServiceInstanceBindingEntity binding) {
    log.info("Deleting service instance binding asynchronously.");
    
    ServiceInstanceBindingInfo bindingInfo = binding.getData();
    
    removeServiceInstanceBinding(request, bindingInfo)
      .then(deleteServiceInstanceBinding(binding))
      .then(updateOperationStatusAndPersist(operation, null))
      .onErrorResume( error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
      .doOnError(error -> log.error("Error! Asynchronous deletion of service instance binding failed.", error))
      .subscribeOn(Schedulers.boundedElastic()).subscribe();
    
    return deleteServiceInstanceBindingAsyncResponse(operation);
  }

  private Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBindingAsyncResponse(final ServiceInstanceBindingOperationEntity operation) {
    return Mono.just(DeleteServiceInstanceBindingResponse.builder()
        .async(true)
        .operation(operation.getId())
        .build());
  }
  
  protected Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBindingSync(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingOperationEntity operation, final ServiceInstanceBindingEntity binding) {
    log.info("Deleting service instance binding synchronously.");
    
    ServiceInstanceBindingInfo bindingInfo = binding.getData();
    
    return removeServiceInstanceBinding(request, bindingInfo)
        .then(deleteServiceInstanceBinding(binding))
        .then(updateOperationStatusAndPersist(operation, null))
        .then(deleteServiceInstanceBindingSyncResponse())
        .onErrorResume( error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
        .doOnError(error -> log.error("Error! Synchronous deletion of service instance binding failed.", error));
  }

  private Mono<Void> deleteServiceInstanceBinding(final ServiceInstanceBindingEntity binding) {
    return storage.deleteServiceInstanceBinding(binding);
  }

  private Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBindingSyncResponse() {
    return Mono.just(
        DeleteServiceInstanceBindingResponse.builder()
        .async(false)
        .build());
  }
  
  
  @Override
  public Mono<GetLastServiceBindingOperationResponse> getLastOperation(GetLastServiceBindingOperationRequest request) {
    log.info("Received callback to GET LAST BINDING OPERATION endpoint.");
    log.debug("Request from Cloud Controller:           {}", request);
   
    String operationId = request.getOperation();
    Mono<ServiceInstanceBindingOperationEntity> bindingOperation = storage.readServiceInstanceBindingOperationById(operationId);
    
    return bindingOperation.flatMap(operation -> {

      ServiceInstanceBindingOperationEntity.Type operationType = operation.getType();
      ServiceOperationStatus.State state = operation.getStatus().getState();
      
      switch (state) {
      case SUCCEEDED:
        return Mono.just(GetLastServiceBindingOperationResponse.builder()
            .operationState(OperationState.SUCCEEDED)
            .description("Service instance binding " + operationType.name() + " operation succeeded.")
            .deleteOperation(operationType == DELETE)
            .build());
      case IN_PROGRESS:
        return Mono.just(GetLastServiceBindingOperationResponse.builder()
            .operationState(OperationState.IN_PROGRESS)
            .description("Service instance binding " + operationType.name() + " operation still in progress.")
            .deleteOperation(operationType == DELETE)
            .build());
      case FAILED:
        return Mono.just(GetLastServiceBindingOperationResponse.builder()
            .operationState(OperationState.FAILED)
            .description("Service instance binding " + operationType.name() + " operation failed.")
            .deleteOperation(operationType == DELETE)
            .build());
      default:
        throw new IllegalStateException("Unknown state of service instance binding creation operation '" + operation.getId() + "'. State was: " + state);
      }
    })
    .onErrorResume( error -> { 
      log.error("Error! Get last service instance binding operation failed.", error);
      return Mono.just(GetLastServiceBindingOperationResponse.builder()
                       .operationState(OperationState.FAILED)
                       .description(error.getMessage())
                       .build());
    });
  }

  private Mono<ServiceInstanceBindingOperationEntity> updateOperationStatusAndPersist(ServiceInstanceBindingOperationEntity operation, Throwable error) {
    return updateOperationStatus(operation, error)
           .flatMap(updatedOperation -> storage.updateServiceInstanceBindingOperation(updatedOperation));
  }
  
  private Mono<ServiceInstanceBindingOperationEntity> updateOperationStatus(ServiceInstanceBindingOperationEntity operation, Throwable error) {
    return Mono.fromCallable(() -> {
      log.debug("Updating service instance binding operation status of operation '{}'", operation);
      if (error == null) {
        log.debug("Updating service instance binding operation status to 'SUCCEEDED'.");
        operation.getStatus().setState(SUCCEEDED);
        operation.getStatus().setDescription("Service instance binding "+ operation.getType().name() +" operation succeeded.");
      } else {
        log.debug("Updating service instance binding operation status to 'FAILED'.");
        operation.getStatus().setState(FAILED);
        operation.getStatus().setDescription("Service instance binding "+ operation.getType().name() +" operation failed.");
      }
      return operation;
    });
  }
  
  /**
   * Getter to test whether service instance binding can be created asynchronously by the service broker implementation.
   * @return true, if service instances can be created asynchronously by the service broker implementation. False otherwise.
   */
  public boolean isCreateServiceInstanceBindingAsync() {
    return createServiceInstanceBindingAsync;
  }

  /**
   * Setter to indicate whether service instance bindings can be created asynchronously by the service broker implementation.
   * @param createServiceInstanceBindingAsync true, if the broker can create instances asynchronously. False otherwise.
   */
  public void setCreateServiceInstanceBindingAsync(boolean createServiceInstanceBindingAsync) {
    this.createServiceInstanceBindingAsync = createServiceInstanceBindingAsync;
  }

  /**
   * Getter to test whether service instance binding can be deleted asynchronously by the service broker implementation.
   * @return true, if service instances can be deleted asynchronously by the service broker implementation. False otherwise.
   */
  public boolean isDeleteServiceInstanceBindingAsync() {
    return deleteServiceInstanceBindingAsync;
  }

  /**
   * Setter to indicate whether service instance bindings can be deleted asynchronously by the service broker implementation.
   * @param deleteServiceInstanceBindingAsync true, if the broker can delete instances asynchronously. False otherwise.
   */
  public void setDeleteServiceInstanceBindingAsync(boolean deleteServiceInstanceBindingAsync) {
    this.deleteServiceInstanceBindingAsync = deleteServiceInstanceBindingAsync;
  }
  
  /**
   * Abstract method called by the framework to execute the concrete broker's business logic 
   * to create a service instance binding. Concrete broker implementations have to implement this method
   * and can code here whatever it is that "create a service instance binding" really means.
   * @param request the platform request to create a service instance binding. 
   * @param serviceInstanceInfo the info / context object that was created by the broker implementation when the <em>service instance</em> was created. 
   * This object can be used to transfer information between the creation of the service instance to the creation of
   * a service instance binding.
   * @return a Mono whose value is some context information about the creation of the service instance binding.
   */
  public abstract Mono<ServiceInstanceBindingInfo> addServiceInstanceBinding(CreateServiceInstanceBindingRequest request, ServiceInstanceInfo serviceInstanceInfo);
  
  /**
   * Abstract method called by the framework to execute the concrete broker's business logic 
   * to remove a service instance binding. Concrete broker implementations have to implement this method
   * and can code here whatever it is that "remove a service instance binding" really means.
   * @param request the platform request to remove a service instance binding.
   * @param serviceInstanceBindingInfo the info / context object that was created by the broker implementation when the service instance binding was created.
   * @return an empty Mono.
   */
  public abstract Mono<Void> removeServiceInstanceBinding(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingInfo serviceInstanceBindingInfo);
}
