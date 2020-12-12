package com.equalities.cloud.osb.persistence;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.config.OsbApiConfig.OperationsConfig;
import com.equalities.cloud.osb.config.OsbApiConfig.ServiceInstanceBindingsConfig;
import com.equalities.cloud.osb.config.OsbApiConfig.ServiceInstancesConfig;
import com.equalities.cloud.osb.persistence.ScheduledOperationRemovalTask;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ScheduledOperationRemovalTaskTest {

  @Mock
  ServiceInstanceOperationPersistence instanceOperationPersistence;
  
  @Mock
  ServiceInstanceBindingOperationPersistence bindingOperationPersistence;
  
  @Mock
  OsbApiConfig configs;
  
  @Mock 
  ServiceInstancesConfig serviceInstanceConfigs;
  
  @Mock 
  ServiceInstanceBindingsConfig serviceBindingConfigs;
  
  @Mock 
  OperationsConfig operationsConfigs;
  
  final Duration cleanupAge = Duration.ofSeconds(3);
  
  ScheduledOperationRemovalTask task;
  
  @BeforeEach
  void setUp() {
    task = new ScheduledOperationRemovalTask(instanceOperationPersistence, bindingOperationPersistence, configs);
  }

  private void mockServiceInstanceBindingConfigs() {
    when(configs.getServiceBindings()).thenReturn(serviceBindingConfigs);
    when(serviceBindingConfigs.getOperations()).thenReturn(operationsConfigs);
    when(operationsConfigs.getCleanupAge()).thenReturn(cleanupAge);
  }

  private void mockServiceInstanceConfigs() {
    when(configs.getServiceInstances()).thenReturn(serviceInstanceConfigs);
    when(serviceInstanceConfigs.getOperations()).thenReturn(operationsConfigs);
    when(operationsConfigs.getCleanupAge()).thenReturn(cleanupAge);
  }
  
  @Test
  void testScheduledOperationRemovalTask() {
    new ScheduledOperationRemovalTask(instanceOperationPersistence, bindingOperationPersistence, configs);
  }

  @Test
  void testRemoveAllServiceInstanceOperations() {
    mockServiceInstanceConfigs();
    when(instanceOperationPersistence.deleteOperationsOlderThan(any(Duration.class))).thenReturn(Mono.empty());
    task.removeAllServiceInstanceOperations();
    verify(instanceOperationPersistence).deleteOperationsOlderThan(cleanupAge);
  }

  @Test
  void testRemoveSucceededServiceInstanceOperations() {
    mockServiceInstanceConfigs();
    when(instanceOperationPersistence.deleteOperationsByStateOlderThan(any(State.class), any(Duration.class))).thenReturn(Mono.empty());
    task.removeSucceededServiceInstanceOperations();
    verify(instanceOperationPersistence).deleteOperationsByStateOlderThan(State.SUCCEEDED, cleanupAge);
  }

  @Test
  void testRemoveFailedServiceInstanceOperations() {
    mockServiceInstanceConfigs();
    when(instanceOperationPersistence.deleteOperationsByStateOlderThan(any(State.class), any(Duration.class))).thenReturn(Mono.empty());
    task.removeFailedServiceInstanceOperations();
    verify(instanceOperationPersistence).deleteOperationsByStateOlderThan(State.FAILED, cleanupAge);
  }

  @Test
  void testRemoveAllServiceInstanceBindingOperations() {
    mockServiceInstanceBindingConfigs();
    when(bindingOperationPersistence.deleteOperationsOlderThan(any(Duration.class))).thenReturn(Mono.empty());
    task.removeAllServiceInstanceBindingOperations();
    verify(bindingOperationPersistence).deleteOperationsOlderThan(cleanupAge);
  }

  @Test
  void testRemoveSucceededServiceInstanceBindingOperations() {
    mockServiceInstanceBindingConfigs();
    when(bindingOperationPersistence.deleteOperationsByStateOlderThan(any(State.class), any(Duration.class))).thenReturn(Mono.empty());
    task.removeSucceededServiceInstanceBindingOperations();
    verify(bindingOperationPersistence).deleteOperationsByStateOlderThan(State.SUCCEEDED, cleanupAge);
  }

  @Test
  void testRemoveFailedServiceInstanceBindingOperations() {
    mockServiceInstanceBindingConfigs();
    when(bindingOperationPersistence.deleteOperationsByStateOlderThan(any(State.class), any(Duration.class))).thenReturn(Mono.empty());
    task.removeFailedServiceInstanceBindingOperations();
    verify(bindingOperationPersistence).deleteOperationsByStateOlderThan(State.FAILED, cleanupAge);
  }
}
