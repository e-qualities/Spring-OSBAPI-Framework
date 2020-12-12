package com.equalities.cloud.osb.persistence;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class ScheduledOperationRemovalTask {
  
  private ServiceInstanceOperationPersistence instanceOperationPersistence;
  
  private ServiceInstanceBindingOperationPersistence bindingOperationPersistence;
  
  private OsbApiConfig configs;
  
  public ScheduledOperationRemovalTask(ServiceInstanceOperationPersistence instanceOperationPersistence, 
                                       ServiceInstanceBindingOperationPersistence bindingOperationPersistence,
                                       OsbApiConfig configs) {
    this.instanceOperationPersistence = instanceOperationPersistence;
    this.bindingOperationPersistence = bindingOperationPersistence;
    this.configs = configs;
  }

  @Scheduled(cron = "${com.equalities.osbapi.service-instances.operations.cleanup-all:-}")
  public void removeAllServiceInstanceOperations() {
    log.debug("Removing ALL service instance operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds", configs.getServiceInstances().getOperations().getCleanupAge().getSeconds());
    instanceOperationPersistence.deleteOperationsOlderThan(configs.getServiceInstances().getOperations().getCleanupAge())
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
  }

  @Scheduled(cron = "${com.equalities.osbapi.service-instances.operations.cleanup-succeeded:-}")
  public void removeSucceededServiceInstanceOperations() {
    log.debug("Removing SUCCEEDED service instance operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds", configs.getServiceInstances().getOperations().getCleanupAge().getSeconds());
    instanceOperationPersistence.deleteOperationsByStateOlderThan(State.SUCCEEDED, configs.getServiceInstances().getOperations().getCleanupAge())
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
  }

  @Scheduled(cron = "${com.equalities.osbapi.service-instances.operations.cleanup-failed:-}")
  public void removeFailedServiceInstanceOperations() {
    log.debug("Removing FAILED service instance operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds", configs.getServiceInstances().getOperations().getCleanupAge().getSeconds());
    instanceOperationPersistence.deleteOperationsByStateOlderThan(State.FAILED, configs.getServiceInstances().getOperations().getCleanupAge())
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
  }
  
  @Scheduled(cron = "${com.equalities.osbapi.service-bindings.operations.cleanup-all:-}")
  public void removeAllServiceInstanceBindingOperations() {
    log.debug("Removing ALL service instance binding operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds", configs.getServiceBindings().getOperations().getCleanupAge().getSeconds());
    bindingOperationPersistence.deleteOperationsOlderThan(configs.getServiceBindings().getOperations().getCleanupAge())
                               .subscribeOn(Schedulers.boundedElastic())
                               .subscribe();
  }

  @Scheduled(cron = "${com.equalities.osbapi.service-bindings.operations.cleanup-succeeded:-}")
  public void removeSucceededServiceInstanceBindingOperations() {
    log.debug("Removing SUCCEEDED service instance binding operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds",configs.getServiceBindings().getOperations().getCleanupAge().getSeconds());
    bindingOperationPersistence.deleteOperationsByStateOlderThan(State.SUCCEEDED, configs.getServiceBindings().getOperations().getCleanupAge())
                               .subscribeOn(Schedulers.boundedElastic())
                               .subscribe();
  }

  @Scheduled(cron = "${com.equalities.osbapi.service-bindings.operations.cleanup-failed:-}")
  public void removeFailedServiceInstanceBindingOperations() {
    log.debug("Removing FAILED service instance binding operations older than specified cleanup-age.");
    log.debug("cleanup-age: {} seconds", configs.getServiceBindings().getOperations().getCleanupAge().getSeconds());
    bindingOperationPersistence.deleteOperationsByStateOlderThan(State.FAILED, configs.getServiceBindings().getOperations().getCleanupAge())
                               .subscribeOn(Schedulers.boundedElastic())
                               .subscribe();
  }
}
