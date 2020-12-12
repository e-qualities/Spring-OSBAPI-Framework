package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingNotFoundException;

class ServiceInstanceBindingNotFoundExceptionTest {

  static final String INSTANCE_NOT_FOUND = "Instance binding not found";
  ServiceInstanceBindingNotFoundException instance; 

  @Test
  void testServiceInstanceBindingNotFoundException() {
    new ServiceInstanceBindingNotFoundException();
  }

  @Test
  void testServiceInstanceBindingNotFoundExceptionString() {
    instance = new ServiceInstanceBindingNotFoundException(INSTANCE_NOT_FOUND);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
  }

  @Test
  void testServiceInstanceBindingNotFoundExceptionThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingNotFoundException(ex);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceBindingNotFoundExceptionStringThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingNotFoundException(INSTANCE_NOT_FOUND, ex);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceBindingNotFoundExceptionStringThrowableBooleanBoolean() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceBindingNotFoundException(INSTANCE_NOT_FOUND, ex, true, true);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }
}
