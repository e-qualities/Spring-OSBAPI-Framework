package com.equalities.cloud.osb;

import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.DELETE;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.FAILED;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.SUCCEEDED;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.binding.GetLastServiceBindingOperationResponse;
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
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A specialized Spring Cloud Open Service Broker {@code ServiceInstanceService} instance that will received callbacks when a new service instance is created by the Cloud platform.
 * <p>
 * The implementation adds persistence of state, in case the service instance is created asynchronously. In such a case, the implementation generates an operation UUID, which it returns to the platform as a handle for the, asynchronous service
 * instance creation operation in process.
 * <p>
 * The Cloud platform then polls the status of the operation using the returned UUID. {@code SAPServiceInstanceService} keeps a mapping of the started operation and generated UUID along with the state of the operation, so that multiple operations can
 * be triggered by the platform asynchronously and in parallel.
 */
@Slf4j
public abstract class DefaultServiceInstanceService implements ServiceInstanceService {

  private final PersistentStorage storage;
  private final OsbApiConfig config;

  private boolean createServiceInstanceAsync = true;
  private boolean deleteServiceInstanceAsync = true;
  private boolean updateServiceInstanceAsync = true;

  public DefaultServiceInstanceService(PersistentStorage persistentStorage, OsbApiConfig config) {
    this.storage = persistentStorage;
    this.config = config;
  }

  @Override
  public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
    log.info("Received callback to CREATE service instance.");
    log.info("Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.debug("Request from Cloud Controller:           {}", request);
    log.debug("Cloud Context:                           {}", request.getContext());
    
    final boolean platformSupportsAsyncInstanceCreation = request.isAsyncAccepted();

    Mono<ServiceInstanceOperationEntity> persistingOperation = storage.insertCreateServiceInstanceOperation(request);

    if (platformSupportsAsyncInstanceCreation && createServiceInstanceAsync) {
      return persistingOperation.flatMap(operation -> {
        return createServiceInstanceAsync(request, operation);
      });
    } else {
      return persistingOperation.flatMap(operation -> {
        return createServiceInstanceSync(request, operation);
      });
    }
  }

  protected Mono<CreateServiceInstanceResponse> createServiceInstanceSync(CreateServiceInstanceRequest request, ServiceInstanceOperationEntity operation) {
    log.info("Creating service instance synchronously.");
    
    return addServiceInstance(request)
        .switchIfEmpty(createNewServiceInstanceInfo())
        .flatMap( serviceInstanceInfo -> {
          return storage.insertServiceInstance(serviceInstanceInfo, operation)
                        .then(updateOperationStatusAndPersist(operation, null))
                        .then(createServiceInstanceSyncResponse(serviceInstanceInfo));
            
        })
        .onErrorResume( error -> {
          return updateOperationStatusAndPersist(operation, error).then(Mono.error(error));
        })
        .doOnError(error -> log.error("Error! Synchronous creation of service instance failed.", error));
  }

  private Mono<CreateServiceInstanceResponse> createServiceInstanceSyncResponse(ServiceInstanceInfo serviceInstanceInfo) {
    return Mono.just(
        CreateServiceInstanceResponse.builder()
        .async(false)
        .dashboardUrl(serviceInstanceInfo.getDashboardUrl())
        .instanceExisted(false)
        .build());
  }

  protected Mono<CreateServiceInstanceResponse> createServiceInstanceAsync(CreateServiceInstanceRequest request, ServiceInstanceOperationEntity operation) {
    log.info("Creating service instance asynchronously.");
    
    addServiceInstance(request)
      .switchIfEmpty(createNewServiceInstanceInfo())
      .flatMap( serviceInstanceInfo -> {
        return storage.insertServiceInstance(serviceInstanceInfo, operation)
                      .then(updateOperationStatusAndPersist(operation, null));
      })
      .onErrorResume( error -> {
        return updateOperationStatusAndPersist(operation, error).then(Mono.error(error));
      })
      .doOnError(error -> log.error("Error! Asynchronous creation of service instance failed.", error))
      .subscribeOn(Schedulers.boundedElastic()).subscribe();

    return createServiceInstanceAsyncResponse(operation);
  }

  private Mono<CreateServiceInstanceResponse> createServiceInstanceAsyncResponse(ServiceInstanceOperationEntity operation) {
    return Mono.just(CreateServiceInstanceResponse.builder()
        .async(true)
        .operation(operation.getId())
        .instanceExisted(false)
        .build());
  }

  @Override
  public Mono<GetServiceInstanceResponse> getServiceInstance(GetServiceInstanceRequest request) {
    log.info("Received callback to GET service instance.");
    log.debug("Request from Cloud Controller: {}", request);
    
    String serviceInstanceId = request.getServiceInstanceId();
    return storage.readServiceInstanceById(serviceInstanceId)
        .flatMap(instance -> {
          ServiceInstanceInfo serviceInstanceInfo = instance.getData();
          Map<String, Object> parameters = serviceInstanceInfo.getParameters();
          parameters = parameters != null ? parameters : new HashMap<String, Object>();
          
          return Mono.just(GetServiceInstanceResponse.builder()
              .serviceDefinitionId(instance.getServiceDefinitionId())
              .planId(instance.getServicePlanId())
              .parameters(parameters)
              .dashboardUrl(serviceInstanceInfo.getDashboardUrl())
              .build());
        });
  }

  @Override
  public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
    log.info ("Received call to DELETE service instance.");
    log.info ("Platform allows async instance deletion: {}", request.isAsyncAccepted());
    log.debug("Request from Cloud Controller:           {}", request);
    
    final boolean platformSupportsAsyncInstanceCreation = request.isAsyncAccepted();
    final String instanceId = request.getServiceInstanceId();
    
    Mono<ServiceInstanceEntity> loadingInstance = storage.readServiceInstanceById(instanceId);
    
    if (platformSupportsAsyncInstanceCreation && deleteServiceInstanceAsync) {
      return loadingInstance.flatMap( instance -> {
        return storage.insertDeleteServiceInstanceOperation(request).flatMap( operation -> {
          return deleteServiceInstanceAsync(request, operation, instance);
        });
      }).onErrorResume(this::deleteErrorFallbackPredicate, this::deleteErrorFallback);
    }
    else {
      return loadingInstance.flatMap(instance -> {
        return storage.insertDeleteServiceInstanceOperation(request).flatMap( operation -> {
          return deleteServiceInstanceSync(request, operation, instance);
        });
      })
      .onErrorResume(this::deleteErrorFallbackPredicate, this::deleteErrorFallback);
    }
  }
  
  private boolean deleteErrorFallbackPredicate(Throwable error) {
    final boolean unkknownServiceInstance = error instanceof ServiceInstanceNotFoundException;
    final boolean removeUnknownInstances = unkknownServiceInstance && 
                                           config.getServiceInstances().isForceDeleteUnknown();
    
    log.warn("An error occurred during deletion of a service instance.");
    log.warn("Removing unknown service instance: {}", removeUnknownInstances);
    return removeUnknownInstances;
  }
  
  private Mono<DeleteServiceInstanceResponse> deleteErrorFallback(Throwable error) {
    log.warn("Removing unknown service instance.");
    return Mono.just(DeleteServiceInstanceResponse.builder()
        .async(false)
        .build());
  }
  
  protected Mono<DeleteServiceInstanceResponse> deleteServiceInstanceAsync(DeleteServiceInstanceRequest request, ServiceInstanceOperationEntity operation, final ServiceInstanceEntity instance) {
    log.info("Deleting service instance asynchronously.");
    
    ServiceInstanceInfo instanceInfo = instance.getData();
    
    removeServiceInstance(request, instanceInfo)
      .then(storage.deleteServiceInstance(instance))
      .then(updateOperationStatusAndPersist(operation, null))
      .onErrorResume( error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
      .doOnError(error -> log.error("Error! Asynchronous deletion of service instance failed.", error))
      .subscribeOn(Schedulers.boundedElastic()).subscribe();
    
    return deleteServiceInstanceAsyncResponse(operation);
  }

  private Mono<DeleteServiceInstanceResponse> deleteServiceInstanceAsyncResponse(ServiceInstanceOperationEntity operation) {
    return Mono.just(DeleteServiceInstanceResponse.builder()
        .async(true)
        .operation(operation.getId())
        .build());
  }
  
  protected Mono<DeleteServiceInstanceResponse> deleteServiceInstanceSync(DeleteServiceInstanceRequest request, ServiceInstanceOperationEntity operation, final ServiceInstanceEntity instance) {
    log.info("Deleting service instance synchronously.");
    ServiceInstanceInfo instanceInfo = instance.getData();
    
    return removeServiceInstance(request, instanceInfo)
        .then(deleteServiceInstance(instance))
        .then(updateOperationStatusAndPersist(operation, null))
        .then(deleteServiceInstanceSyncResponse())
        .onErrorResume(error -> updateOperationStatusAndPersist(operation, error).then(Mono.error(error)))
        .doOnError(error -> log.error("Error! Synchronous deletion of service instance failed.", error));
  }
  
  private Mono<Void> deleteServiceInstance(ServiceInstanceEntity entity) {
    return storage.deleteServiceInstance(entity);
  }
  
  private Mono<DeleteServiceInstanceResponse> deleteServiceInstanceSyncResponse() {
    return Mono.just(DeleteServiceInstanceResponse.builder()
                     .async(false)
                     .build());
  }
  
  @Override
  public Mono<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
    log.info("Received callback to UPDATE service instance.");
    log.info("Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.debug("Request from Cloud Controller:           {}", request);
    log.debug("Cloud Context:                           {}", request.getContext());
    
    final boolean platformSupportsAsyncInstanceCreation = request.isAsyncAccepted();
    final String instanceId = request.getServiceInstanceId();

    Mono<ServiceInstanceEntity> loadingInstance = storage.readServiceInstanceById(instanceId);
    
    if (platformSupportsAsyncInstanceCreation && updateServiceInstanceAsync) {
      return loadingInstance.flatMap( instance -> {
        return storage.insertUpdateServiceInstanceOperation(request).flatMap(operation -> {
          return updateServiceInstanceAsync(request, operation, instance);
        });
      });
      
    } else {
      return loadingInstance.flatMap( instance -> {
        return storage.insertUpdateServiceInstanceOperation(request).flatMap(operation -> {
          return updateServiceInstanceSync(request, operation, instance);
        });
      });
    }
  }
  
  protected Mono<UpdateServiceInstanceResponse> updateServiceInstanceSync(UpdateServiceInstanceRequest request, ServiceInstanceOperationEntity operation, ServiceInstanceEntity instance) {
    log.info("Updating service instance synchronously.");
    
    ServiceInstanceInfo instanceInfo = instance.getData();
    
    return changeServiceInstance(request, instanceInfo)
        .switchIfEmpty(createNewServiceInstanceInfo())
        .flatMap( updatedServiceInstanceInfo -> {
          return storage.updateServiceInstance(updatedServiceInstanceInfo, operation)
                        .then(updateOperationStatusAndPersist(operation, null))
                        .then(updateServiceInstanceSyncResponse(updatedServiceInstanceInfo));
        })
        .onErrorResume( error -> {
          return updateOperationStatusAndPersist(operation, error).then(Mono.error(error));
        })
        .doOnError( error -> log.error("Error! Synchronous update of service instance failed.", error));
  }

  private Mono<UpdateServiceInstanceResponse> updateServiceInstanceSyncResponse(ServiceInstanceInfo updatedServiceInstanceInfo) {
    return Mono.just(UpdateServiceInstanceResponse.builder()
        .dashboardUrl(updatedServiceInstanceInfo.getDashboardUrl())
        .async(false)
        .build());
  }

  private Mono<ServiceInstanceInfo> createNewServiceInstanceInfo() {
    return Mono.just(new ServiceInstanceInfo());
  }

  protected Mono<UpdateServiceInstanceResponse> updateServiceInstanceAsync(UpdateServiceInstanceRequest request, ServiceInstanceOperationEntity operation, ServiceInstanceEntity instance) {
    log.info("Updating service instance asynchronously.");
    
    ServiceInstanceInfo instanceInfo = instance.getData();
    
    changeServiceInstance(request, instanceInfo)
      .switchIfEmpty(createNewServiceInstanceInfo())
      .flatMap( updatedServiceInstanceInfo -> {
        return storage.updateServiceInstance(updatedServiceInstanceInfo, operation)
                      .then(updateOperationStatusAndPersist(operation, null));
      })
      .onErrorResume( error -> {
        return updateOperationStatusAndPersist(operation, error).then(Mono.error(error));
      })
      .doOnError(error -> log.error("Error! Asynchronous udpate of service instance failed.", error))
      .subscribeOn(Schedulers.boundedElastic()).subscribe();

    return updateServiceInstanceAsyncResponse(operation);
  }

  private Mono<UpdateServiceInstanceResponse> updateServiceInstanceAsyncResponse(ServiceInstanceOperationEntity operation) {
    return Mono.just(UpdateServiceInstanceResponse.builder()
        .async(true)
        .operation(operation.getId())
        .build());
  }
  
  @Override
  public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
    log.info("Received callback to GET LAST OPERATION endpoint.");
    log.debug("Request from Cloud Controller:           {}", request);
   
    String operationId = request.getOperation();
    Mono<ServiceInstanceOperationEntity> instanceOperation = storage.readServiceInstanceOperationById(operationId);
    
    return instanceOperation.flatMap(operation -> {
      ServiceInstanceOperationEntity.Type operationType = operation.getType();
      ServiceOperationStatus.State state = operation.getStatus().getState();
      
      switch (state) {
      case SUCCEEDED:
        return Mono.just(GetLastServiceOperationResponse.builder()
            .operationState(OperationState.SUCCEEDED)
            .description("Service instance " + operationType.name() + " operation succeeded.")
            .deleteOperation(operationType == DELETE)
            .build());
      case IN_PROGRESS:
        return Mono.just(GetLastServiceOperationResponse.builder()
            .operationState(OperationState.IN_PROGRESS)
            .description("Service instance " + operationType.name() + " operation still in progress.")
            .deleteOperation(operationType == DELETE)
            .build());
      case FAILED:
        return Mono.just(GetLastServiceOperationResponse.builder()
            .operationState(OperationState.FAILED)
            .description("Creating service " + operationType.name() + " operation instance failed.")
            .deleteOperation(operationType == DELETE)
            .build());
      default:
        throw new IllegalStateException("Unknown state of service instance creation operation '" + operation.getId() + "'. State was: " + state);
      }
    })
    .onErrorResume( error -> { 
      log.error("Error! Get last service instance operation failed.", error);
      return Mono.just(GetLastServiceOperationResponse.builder()
                 .operationState(OperationState.FAILED)
                 .description(error.getMessage())
                 .build());
    });
  }
  
  private Mono<ServiceInstanceOperationEntity> updateOperationStatusAndPersist(ServiceInstanceOperationEntity operation, Throwable error) {
    return updateOperationStatus(operation, error)
          .flatMap(updatedOperation -> storage.updateServiceInstanceOperation(updatedOperation));
  }
  
  public Mono<ServiceInstanceOperationEntity> updateOperationStatus(ServiceInstanceOperationEntity operation, Throwable error) {
   return Mono.fromCallable(() -> {
      log.debug("Updating service instance operation status of operation '{}'", operation);
      if (error == null) {
        log.debug("Updating service instance operation status to 'SUCCEEDED'.");
        operation.getStatus().setState(SUCCEEDED);
        operation.getStatus().setDescription("Service instance "+ operation.getType().name() +" operation succeeded.");
      } else {
        log.debug("Updating service instance operation status to 'FAILED'.");
        operation.getStatus().setState(FAILED);
        operation.getStatus().setDescription("Service instance "+ operation.getType().name() +" operation failed.");
      }
      return operation;
    });
  }
  
  /**
   * Getter to test whether service instances can be created asynchronously by the service broker implementation.
   * @return true, if service instances can be created asynchronously by the service broker implementation. False otherwise.
   */
  public boolean isCreateServiceInstanceAsync() {
    return createServiceInstanceAsync;
  }

  /**
   * Setter to indicate whether service instances can be created asynchronously by the service broker implementation.
   * @param createServiceInstanceAsync true, if the broker can create instances asynchronously. False otherwise.
   */
  public void setCreateServiceInstanceAsync(boolean createServiceInstanceAsync) {
    this.createServiceInstanceAsync = createServiceInstanceAsync;
  }

  /**
   * Getter to test whether service instances can be deleted asynchronously by the service broker implementation.
   * @return true, if service instances can be deleted asynchronously by the service broker implementation. False otherwise.
   */
  public boolean isDeleteServiceInstanceAsync() {
    return deleteServiceInstanceAsync;
  }

  /**
   * Setter to indicate whether service instances can be deleted asynchronously by the service broker implementation.
   * @param deleteServiceInstanceAsync true, if the broker can delete instances asynchronously. False otherwise.
   */
  public void setDeleteServiceInstanceAsync(boolean deleteServiceInstanceAsync) {
    this.deleteServiceInstanceAsync = deleteServiceInstanceAsync;
  }

  /**
   * Getter to test whether service instances can be updated asynchronously by the service broker implementation.
   * @return true, if service instances can be updated asynchronously by the service broker implementation. False otherwise.
   */
  public boolean isUpdateServiceInstanceAsync() {
    return updateServiceInstanceAsync;
  }

  /**
   * Setter to indicate whether service instances can be updated asynchronously by the service broker implementation.
   * @param updateServiceInstanceAsync true, if the broker can update instances asynchronously. False otherwise.
   */
  public void setUpdateServiceInstanceAsync(boolean updateServiceInstanceAsync) {
    this.updateServiceInstanceAsync = updateServiceInstanceAsync;
  }
  
  /**
   * A method to be implemented by a service broker implementation.
   * This method is called when OSBAPI's {code createServiceInstance(CreateServiceInstanceRequest)} method is called.
   * @param request the request from the OSBAPI method.
   * @return a {code Mono} holding a {code ServiceInstanceInfo} object containing optional state to be persisted alongside 
   * general information about the created service instance.
   */
  public abstract Mono<ServiceInstanceInfo> addServiceInstance(final CreateServiceInstanceRequest request);

  /**
   * A method to be implemented by a service broker implementation.
   * This method is called when OSBAPI's {code deleteServiceInstance(DeleteServiceInstanceRequest)} method is called.
   * @param request  the request from the OSBAPI method. 
   * @param instanceInfo the {@code ServiceInstanceInfo} object that was stored when the service instance was created.
   * @return a {code Mono} indicating the success or failure of the deletion of the service instance.
   */
  public abstract Mono<Void> removeServiceInstance(final DeleteServiceInstanceRequest request, final ServiceInstanceInfo instanceInfo);

  /**
   * A method to be implemented by a service broker implementation.
   * This method is called when OSBAPI's {code changeServiceInstance(UpdateServiceInstanceRequest)} method is called.
   * @param request the request from the OSBAPI method.
   * @return a {code Mono} holding a {code ServiceInstanceInfo} object containing optional state to be persisted alongside 
   * general information about the created service instance.
   */
  public abstract Mono<ServiceInstanceInfo> changeServiceInstance(final UpdateServiceInstanceRequest request, final ServiceInstanceInfo instanceInfo);
  
}
