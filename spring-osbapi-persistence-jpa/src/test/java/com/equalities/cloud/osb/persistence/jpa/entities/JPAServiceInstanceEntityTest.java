package com.equalities.cloud.osb.persistence.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ExtendWith(MockitoExtension.class)
class JPAServiceInstanceEntityTest {
  
  private static final ObjectMapper mapper = new ObjectMapper()
                                                  .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                                                  .activateDefaultTyping(new LaissezFaireSubTypeValidator(), 
                                                                        DefaultTyping.NON_CONCRETE_AND_ARRAYS);
  static final Instant NOW = Instant.now();
  static final String SERVICE_PLAN_ID = "servicePlanId";
  static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
  static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
  static final ServiceInstanceInfo INFO = new ServiceInstanceInfo();
  static final TestObject TESTOBJECT = new TestObject("test", true);
  static final String KEY = "key";
  
  static {
    INFO.put(KEY, TESTOBJECT);
  }

  @Mock
  ServiceInstanceEntity osbEntity;
  
  @Mock
  JPAServiceInstanceEntity jpaEntity;

  @Test
  void testJpaType() throws JsonProcessingException {
    
    String serialized = mapper.writeValueAsString(INFO);
    
    when(osbEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(osbEntity.getCreatedAt()).thenReturn(NOW);
    when(osbEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(osbEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(osbEntity.getData()).thenReturn(INFO);
    
    jpaEntity = JPAServiceInstanceEntity.jpaType(osbEntity);
    
    assertThat(jpaEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(jpaEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(jpaEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(jpaEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(jpaEntity.getData()).isEqualTo(serialized);
  }
  
  @Test
  void testJpaTypeSerializesNullIfDataIsNull() {
    
    when(osbEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(osbEntity.getCreatedAt()).thenReturn(NOW);
    when(osbEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(osbEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(osbEntity.getData()).thenReturn(null);
    
    jpaEntity = JPAServiceInstanceEntity.jpaType(osbEntity);
    
    assertThat(jpaEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(jpaEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(jpaEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(jpaEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(jpaEntity.getData()).isNull();
  }

  @Test
  void testOsbTypeJPAServiceInstanceEntity() throws JsonProcessingException {
    String serialized = mapper.writeValueAsString(INFO);
    
    when(jpaEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(jpaEntity.getCreatedAt()).thenReturn(NOW);
    when(jpaEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(jpaEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(jpaEntity.getData()).thenReturn(serialized);
    
    osbEntity = JPAServiceInstanceEntity.osbType(jpaEntity);
    
    assertThat(osbEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(osbEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(osbEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(osbEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(osbEntity.getData()).isEqualTo(INFO);
    assertThat(osbEntity.getData().get(KEY)).isEqualTo(TESTOBJECT);
  }
  
  @Test
  void testOsbTypeJPAServiceInstanceEntityReturnsNullOnNullInut() {
    assertThat(JPAServiceInstanceEntity.osbType((JPAServiceInstanceEntity)null)).isNull();
  }

  @Test
  void testOsbTypeOptionalOfJPAServiceInstanceEntity() throws JsonProcessingException {
    String serialized = mapper.writeValueAsString(INFO);
    
    when(jpaEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(jpaEntity.getCreatedAt()).thenReturn(NOW);
    when(jpaEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(jpaEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(jpaEntity.getData()).thenReturn(serialized);
    
    osbEntity = JPAServiceInstanceEntity.osbType(Optional.of(jpaEntity));
    
    assertThat(osbEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(osbEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(osbEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(osbEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(osbEntity.getData()).isEqualTo(INFO);
    assertThat(osbEntity.getData().get(KEY)).isEqualTo(TESTOBJECT);
  }

  @Test
  void testOsbTypeOptionalOfJPAServiceInstanceEntityReturnsNullOnEmptyInput() {
    assertThat(JPAServiceInstanceEntity.osbType((Optional.empty()))).isNull();
  }
  
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestObject {
    private String name;
    private Boolean isWorking;
  }
}
