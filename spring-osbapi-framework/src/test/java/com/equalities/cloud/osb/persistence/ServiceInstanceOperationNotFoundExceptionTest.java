package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.ServiceInstanceOperationNotFoundException;

class ServiceInstanceOperationNotFoundExceptionTest {

  static final String INSTANCE_NOT_FOUND = "Instance not found";
  ServiceInstanceOperationNotFoundException instance; 

  @Test
  void testServiceInstanceOperationNotFoundException() {
    new ServiceInstanceOperationNotFoundException();
  }

  @Test
  void testServiceInstanceOperationNotFoundExceptionString() {
    instance = new ServiceInstanceOperationNotFoundException(INSTANCE_NOT_FOUND);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
  }

  @Test
  void testServiceInstanceOperationNotFoundExceptionThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceOperationNotFoundException(ex);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstancOperationeNotFoundExceptionStringThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceOperationNotFoundException(INSTANCE_NOT_FOUND, ex);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceOperationNotFoundExceptionStringThrowableBooleanBoolean() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceOperationNotFoundException(INSTANCE_NOT_FOUND, ex, true, true);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }
}
