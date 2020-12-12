package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.ServiceInstanceNotFoundException;

class ServiceInstanceNotFoundExceptionTest {

  static final String INSTANCE_NOT_FOUND = "Instance not found";
  ServiceInstanceNotFoundException instance; 

  @Test
  void testServiceInstanceNotFoundException() {
    new ServiceInstanceNotFoundException();
  }

  @Test
  void testServiceInstanceNotFoundExceptionString() {
    instance = new ServiceInstanceNotFoundException(INSTANCE_NOT_FOUND);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
  }

  @Test
  void testServiceInstanceNotFoundExceptionThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceNotFoundException(ex);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceNotFoundExceptionStringThrowable() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceNotFoundException(INSTANCE_NOT_FOUND, ex);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }

  @Test
  void testServiceInstanceNotFoundExceptionStringThrowableBooleanBoolean() {
    RuntimeException ex = new RuntimeException();
    instance = new ServiceInstanceNotFoundException(INSTANCE_NOT_FOUND, ex, true, true);
    assertThat(instance.getMessage()).isEqualTo(INSTANCE_NOT_FOUND);
    assertThat(instance.getCause()).isEqualTo(ex);
  }
}
