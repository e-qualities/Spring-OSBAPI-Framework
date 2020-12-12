package com.equalities.cloud.osb.persistence;

import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.CREATE;
import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.DELETE;
import static com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type.UPDATE;
import static com.equalities.cloud.osb.persistence.ServiceOperationStatus.State.IN_PROGRESS;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * A single persistent storage class that helps in persisting operations and service instances using the
 * persistence API interfaces defined in {@link ServiceInstancePersistence} and {@link ServiceInstanceOperationPersistence}.
 */
@Slf4j
public class PersistentStorage {

  private ServiceInstancePersistence instancePersistence;
  private ServiceInstanceOperationPersistence instanceOperationPersistence;
  private ServiceInstanceBindingPersistence bindingPersistence;
  private ServiceInstanceBindingOperationPersistence bindingOperationPersistence;

  public PersistentStorage(ServiceInstancePersistence instancePersistence, 
                           ServiceInstanceOperationPersistence instanceOperationPersistence,
                           ServiceInstanceBindingPersistence bindingPersistence,
                           ServiceInstanceBindingOperationPersistence bindingOperationPersistence) {
    this.instancePersistence = instancePersistence;
    this.instanceOperationPersistence = instanceOperationPersistence;
    this.bindingPersistence = bindingPersistence;
    this.bindingOperationPersistence = bindingOperationPersistence;
  }

  ///////////////////////// Service Instance Persistence /////////////////////////

  public Mono<ServiceInstanceEntity> insertServiceInstance(final ServiceInstanceInfo serviceInstanceInfo, final ServiceInstanceOperationEntity operation) {
    log.debug("Persisting service instance.");
    final ServiceInstanceEntity serviceInstanceEntity = ServiceInstanceEntity.builder()
        .serviceInstanceId(operation.getServiceInstanceId())
        .serviceDefinitionId(operation.getServiceDefinitionId())
        .servicePlanId(operation.getServicePlanId())
        .createdAt(Instant.now())
        .data(serviceInstanceInfo)
        .build();
    
    return instancePersistence.insert(serviceInstanceEntity)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert service instance returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service instance with ID '{}'.", entity.getServiceInstanceId())
      );
  }
  
  public Mono<ServiceInstanceEntity> updateServiceInstance(final ServiceInstanceInfo serviceInstanceInfo, final ServiceInstanceOperationEntity operation) {
    return insertServiceInstance(serviceInstanceInfo, operation);
  }
  
  public Mono<Void> deleteServiceInstance(ServiceInstanceEntity entity) {
    return instancePersistence.delete(entity)
      .doOnSuccess(Void -> 
        log.info("Successfully removed service instance with ID '{}' from persistence.", entity.getServiceInstanceId())
      );
  }
  

  public Mono<ServiceInstanceEntity> readServiceInstanceById(final String instanceId) {
    return instancePersistence.readByServiceInstanceId(instanceId)
      .switchIfEmpty(Mono.error(new ServiceInstanceNotFoundException("Error! Read service instance by ID returned empty result. Service instance with ID '" + instanceId + "' not found.")))
      .doOnSuccess( entity -> 
        log.info("Successfully read service instance by ID '{}'.", instanceId)
      );
  }

  ///////////////////////// Service Instance Operation Persistence /////////////////////////

  public Mono<ServiceInstanceOperationEntity> insertCreateServiceInstanceOperation(final CreateServiceInstanceRequest request) {
    log.debug("Persisting 'CREATE' service instance operation.");
    ServiceInstanceOperationEntity operation = ServiceInstanceOperationEntity.builder()
        .id(UUID.randomUUID().toString())
        .serviceInstanceId(request.getServiceInstanceId())
        .serviceDefinitionId(request.getServiceDefinitionId())
        .servicePlanId(request.getPlanId())
        .createdAt(Instant.now())
        .type(CREATE)
        .status(ServiceOperationStatus.builder()
            .state(IN_PROGRESS)
            .description("Service instance creation is in progress.")
            .build())
        .build();

    return instanceOperationPersistence.insert(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert create service instance operation returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service instance CREATE operation with ID '{}'.", entity.getId())
      );
  }
  
  public Mono<ServiceInstanceOperationEntity> insertUpdateServiceInstanceOperation(final UpdateServiceInstanceRequest request) {
    log.debug("Persisting 'CREATE' service instance operation.");
    ServiceInstanceOperationEntity operation = ServiceInstanceOperationEntity.builder()
        .id(UUID.randomUUID().toString())
        .serviceInstanceId(request.getServiceInstanceId())
        .serviceDefinitionId(request.getServiceDefinitionId())
        .servicePlanId(request.getPlanId()).createdAt(Instant.now())
        .type(UPDATE)
        .status(ServiceOperationStatus.builder()
            .state(IN_PROGRESS)
            .description("Service instance update is in progress.")
            .build())
        .build();

    return instanceOperationPersistence.update(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert update service instance operation returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service instance UPDATE operation with ID '{}'.", entity.getId())
      );
  }
  
  public Mono<ServiceInstanceOperationEntity> insertDeleteServiceInstanceOperation(final DeleteServiceInstanceRequest request) {
    log.debug("Persisting 'DELETE' service instance operation.");
    ServiceInstanceOperationEntity operation = ServiceInstanceOperationEntity.builder()
        .id(UUID.randomUUID().toString())
        .serviceInstanceId(request.getServiceInstanceId())
        .serviceDefinitionId(request.getServiceDefinitionId())
        .servicePlanId(request.getPlanId())
        .createdAt(Instant.now())
        .type(DELETE)
        .status(ServiceOperationStatus.builder()
            .description("Service instance deletion is in progress")
            .state(IN_PROGRESS)
            .build())
        .build();
    return instanceOperationPersistence.insert(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert delete service instance operation returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service instance DELETE operation with ID '{}'.", entity.getId())
      );
  }
  
  public Mono<ServiceInstanceOperationEntity> updateServiceInstanceOperation(final ServiceInstanceOperationEntity operation) {
    log.debug("Persisting instance operation update.");
    return instanceOperationPersistence.update(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Update service instance operation returned empty result.")))
      .doOnSuccess( updatedOperation -> 
        log.info("Successfully persisted update of instance operation with ID '{}' for service instance with ID '{}'. Changed state to '{}'.", updatedOperation.getId(), updatedOperation.getServiceInstanceId(), updatedOperation.getStatus())
      );
  }
  
  public Mono<Void> deleteServiceInstanceOperation(final ServiceInstanceOperationEntity operation) {
    log.debug("Deleting instance operation.");
    return instanceOperationPersistence.delete(operation)
      .doOnSuccess( Void -> 
        log.info("Successfully deleted operation with ID '{}' for service instance with ID '{}'. Changed state to '{}'.", operation.getId(), operation.getServiceInstanceId(), operation.getStatus())
      ).then();
  }
  
  public Mono<ServiceInstanceOperationEntity> readServiceInstanceOperationById(final String operationId) {
    return instanceOperationPersistence.readByOperationId(operationId)
      .switchIfEmpty(Mono.error(new ServiceInstanceOperationNotFoundException("Error! Read service instance operation by ID returned empty result. Service Instance Operation with ID '"+ operationId +"' not found.")))
      .doOnSuccess( entity -> 
        log.info("Successfully read service instance operation by ID '{}'.", operationId)
      );
  }

  ///////////////////////// Service Binding Persistence /////////////////////////

  public Mono<ServiceInstanceBindingEntity> insertServiceInstanceBinding(final ServiceInstanceBindingInfo serviceBindingInfo, final ServiceInstanceBindingOperationEntity operation) {
    log.debug("Persisting service instance.");
    final ServiceInstanceBindingEntity serviceBindingEntity = ServiceInstanceBindingEntity.builder()
        .serviceInstanceBindingId(operation.getServiceInstanceBindingId())
        .serviceInstanceId(operation.getServiceInstanceId())
        .serviceDefinitionId(operation.getServiceDefinitionId())
        .servicePlanId(operation.getServicePlanId())
        .createdAt(Instant.now())
        .data(serviceBindingInfo)
        .build();

    return bindingPersistence.insert(serviceBindingEntity)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert service instance binding returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service binding instance with ID '{}'.", entity.getServiceInstanceId())
      );
  }

  public Mono<ServiceInstanceBindingEntity> updateServiceInstanceBinding(final ServiceInstanceBindingInfo serviceBindingInfo, final ServiceInstanceBindingOperationEntity operation) {
    return insertServiceInstanceBinding(serviceBindingInfo, operation);
  }

  public Mono<Void> deleteServiceInstanceBinding(ServiceInstanceBindingEntity entity) {
    return bindingPersistence.delete(entity)
      .doOnSuccess(Void -> 
        log.info("Successfully removed service binding instance with ID '{}' from persistence.", entity.getServiceInstanceId())
      );
  }

  public Mono<ServiceInstanceBindingEntity> readServiceInstanceBindingById(final String bindingId) {
    return bindingPersistence.readByServiceInstanceBindingId(bindingId)
      .switchIfEmpty(Mono.error(new ServiceInstanceBindingNotFoundException("Error! Service Instance Binding Persistence layer 'readById' implementation returned empty result. Service Instance Binding with ID not found.")))
      .doOnSuccess( entity -> 
        log.info("Successfully read service instance by ID '{}'.", bindingId)
      );
  }

  ///////////////////////// Service Binding Operation Persistence /////////////////////////

  public Mono<ServiceInstanceBindingOperationEntity> insertCreateServiceInstanceBindingOperation(final CreateServiceInstanceBindingRequest request) {
    log.debug("Persisting 'CREATE' service binding operation.");
    ServiceInstanceBindingOperationEntity operation = ServiceInstanceBindingOperationEntity.builder()
        .id(UUID.randomUUID().toString())
        .serviceInstanceBindingId(request.getBindingId())
        .serviceInstanceId(request.getServiceInstanceId())
        .serviceDefinitionId(request.getServiceDefinitionId())
        .servicePlanId(request.getPlanId())
        .createdAt(Instant.now())
        .type(CREATE)
        .status(ServiceOperationStatus.builder()
            .state(IN_PROGRESS)
            .description("Service binding creation is in progress.")
            .build())
        .build();

    return bindingOperationPersistence.insert(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert create service instance binding operation returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service binding CREATE operation with ID '{}'.", entity.getId())
      );
  }
  
  public Mono<ServiceInstanceBindingOperationEntity> insertDeleteServiceInstanceBindingOperation(final DeleteServiceInstanceBindingRequest request) {
    log.debug("Persisting 'DELETE' service binding operation.");
    ServiceInstanceBindingOperationEntity operation = ServiceInstanceBindingOperationEntity.builder()
        .id(UUID.randomUUID().toString())
        .serviceInstanceBindingId(request.getBindingId())
        .serviceDefinitionId(request.getServiceDefinitionId())
        .serviceInstanceId(request.getServiceInstanceId())
        .servicePlanId(request.getPlanId())
        .createdAt(Instant.now())
        .type(DELETE)
        .status(ServiceOperationStatus.builder()
            .description("Service binding deletion is in progress")
            .state(IN_PROGRESS)
            .build())
        .build();
    return bindingOperationPersistence.insert(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Insert delete service instance binding operation returned empty result.")))
      .doOnSuccess(entity -> 
        log.info("Successfully persisted service binding DELETE operation with ID '{}'.", entity.getId())
      );
  }
  
  public Mono<ServiceInstanceBindingOperationEntity> updateServiceInstanceBindingOperation(final ServiceInstanceBindingOperationEntity operation) {
    log.debug("Persisting binding operation update.");
    return bindingOperationPersistence.update(operation)
      .switchIfEmpty(Mono.error(new IllegalStateException("Persistence Error! Update service instance binding operation returned empty result.")))
      .doOnSuccess( updatedOperation -> 
        log.info("Successfully persisted update of binding operation with ID '{}' for service instance with ID '{}'. Changed state to '{}'.", updatedOperation.getId(), updatedOperation.getServiceInstanceId(), updatedOperation.getStatus())
      );
  }
  
  public Mono<Void> deleteServiceInstanceBindingOperation(final ServiceInstanceBindingOperationEntity operation) {
    log.debug("Deleting binding operation.");
    return bindingOperationPersistence.delete(operation)
      .doOnSuccess( Void -> 
        log.info("Successfully deleted operation with ID '{}' for service instance with ID '{}'. Changed state to '{}'.", operation.getId(), operation.getServiceInstanceId(), operation.getStatus())
      ).then();
  }
  
  public Mono<ServiceInstanceBindingOperationEntity> readServiceInstanceBindingOperationById(final String operationId) {
    return bindingOperationPersistence.readByOperationId(operationId)
      .switchIfEmpty(Mono.error(new ServiceInstanceBindingOperationNotFoundException("Error! Read service instance binding operation by ID returned empty result. Service Instance Binding Operation with ID " + operationId + " not found.")))
      .doOnSuccess( entity -> 
        log.info("Successfully read service binding operation by ID '{}'.", operationId)
      );
  }
}
