package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationNotFoundException;

class ServiceInstanceBindingOperationNotFoundExceptionTest {
  static final String INSTANCE_NOT_FOUND = "Instance binding not found";
  ServiceInstanceBindingOperationNotFoundException instance; 

  @Test
  void testServiceInstanceBindingOperationNotFoundException() {
    new ServiceInstanceBindingOperationNotFoundException();
  }

  @Test
  void testServiceInstanceBindingOperationNotFoundExceptionString() {
    instance = new ServiceInstanceBindingOperationNotFoundException(INSTANCE_NOT_FOUND);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
  }

  @Test
  void testServiceInstanceBindingOperationNotFoundExceptionThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingOperationNotFoundException(ex);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceBindingOperationNotFoundExceptionStringThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingOperationNotFoundException(INSTANCE_NOT_FOUND, ex);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceBindingOperationNotFoundExceptionStringThrowableBooleanBoolean() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingOperationNotFoundException(INSTANCE_NOT_FOUND, ex, true, true);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }
}
