package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;

class ServiceInstanceInfoTest {

  ServiceInstanceInfo instance;
  
  @BeforeEach
  void setUp() throws Exception {
    instance = new ServiceInstanceInfo();
  }

  @Test
  void testServiceInstanceInfo() {
    new ServiceInstanceInfo();
  }

  @Test
  void testGetDashboardUrl() {
    final String url = "http://test";
    
    instance.setDashboardURL(url);
    assertThat(instance.getDashboardUrl()).isEqualTo(url);
  }

  @Test
  void testSetDashboardURL() {
    final String url = "http://test";
    instance.setDashboardURL(url);
    assertThat(instance.getDashboardUrl()).isEqualTo(url);
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

}
