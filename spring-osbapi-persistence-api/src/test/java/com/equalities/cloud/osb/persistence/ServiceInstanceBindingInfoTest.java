package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.servicebroker.model.binding.Endpoint;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;

class ServiceInstanceBindingInfoTest {
  
  ServiceInstanceBindingInfo instance;

  @BeforeEach
  void setUp() throws Exception {
    instance = new ServiceInstanceBindingInfo();
  }

  @Test
  void testServiceInstanceBindingInfo() {
    new ServiceInstanceBindingInfo();
  }

  @Test
  void testGetCredentials() {
    final HashMap<String, Object> credentials = new HashMap<>();
    instance.setCredentials(credentials);
    assertThat(instance.getCredentials()).isEqualTo(credentials);
  }

  @Test
  void testSetCredentials() {
    final HashMap<String, Object> credentials = new HashMap<>();
    instance.setCredentials(credentials);
    assertThat(instance.getCredentials()).isEqualTo(credentials);
  }

  @Test
  void testGetParameters() {
    final HashMap<String, Object> params = new HashMap<>();
    instance.setParameters(params);
    assertThat(instance.getParameters()).isEqualTo(params);
  }

  @Test
  void testSetParameters() {
    final HashMap<String, Object> params = new HashMap<>();
    instance.setParameters(params);
    assertThat(instance.getParameters()).isEqualTo(params);
  }

  @Test
  void testGetEndpoints() {
    final List<Endpoint> endpoints = new ArrayList<>();
    instance.setEndpoints(endpoints);
    assertThat(instance.getEndpoints()).isEqualTo(endpoints);
  }

  @Test
  void testSetEndpoints() {
    final List<Endpoint> endpoints = new ArrayList<>();
    instance.setEndpoints(endpoints);
    assertThat(instance.getEndpoints()).isEqualTo(endpoints);
  }

  @Test
  void testGetVolumeMounts() {
    final List<VolumeMount> volumeMounts = new ArrayList<>();
    instance.setVolumeMounts(volumeMounts);
    assertThat(instance.getVolumeMounts()).isEqualTo(volumeMounts);
  }

  @Test
  void testSetVolumeMounts() {
    final List<VolumeMount> volumeMounts = new ArrayList<>();
    instance.setVolumeMounts(volumeMounts);
    assertThat(instance.getVolumeMounts()).isEqualTo(volumeMounts);
  }
}
