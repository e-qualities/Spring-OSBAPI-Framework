package com.equalities.cloud.osb.persistence.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceOperationEntity;

@ExtendWith(MockitoExtension.class)
class JPAServiceInstanceBindingOperationEntityTest {
  
  static final String SERVICE_PLAN_ID = "servicePlanId";
  static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
  static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
  static final String ID = "id";
  static final Instant NOW = Instant.now();
  static final ServiceOperationStatus STATUS = new ServiceOperationStatus();
  static final Type TYPE = Type.CREATE;

  @Mock
  ServiceInstanceBindingOperationEntity osbEntity;
  
  @Mock
  JPAServiceInstanceBindingOperationEntity jpaEntity;

  @Test
  void testJpaType() {
    
    when(osbEntity.getId()).thenReturn(ID);
    when(osbEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(osbEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(osbEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(osbEntity.getCreatedAt()).thenReturn(NOW);
    when(osbEntity.getType()).thenReturn(TYPE);
    when(osbEntity.getStatus()).thenReturn(STATUS);
    
    jpaEntity = JPAServiceInstanceBindingOperationEntity.jpaType(osbEntity);
    
    assertThat(jpaEntity.getId()).isEqualTo(ID);
    assertThat(jpaEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(jpaEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(jpaEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(jpaEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(jpaEntity.getType()).isEqualTo(TYPE);
    assertThat(jpaEntity.getStatus()).isEqualTo(STATUS);
  }

  @Test
  void testOsbTypeJPAServiceInstanceBindingOperationEntity() {
    when(jpaEntity.getId()).thenReturn(ID);
    when(jpaEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(jpaEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(jpaEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(jpaEntity.getCreatedAt()).thenReturn(NOW);
    when(jpaEntity.getType()).thenReturn(TYPE);
    when(jpaEntity.getStatus()).thenReturn(STATUS);
    
    osbEntity = JPAServiceInstanceBindingOperationEntity.osbType(jpaEntity);
    
    assertThat(osbEntity.getId()).isEqualTo(ID);
    assertThat(osbEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(osbEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(osbEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(osbEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(osbEntity.getType()).isEqualTo(TYPE);
    assertThat(osbEntity.getStatus()).isEqualTo(STATUS);
  }
  
  @Test
  void testOsbTypeJPAServiceInstanceBindingOperationEntityReturnsNullOnNullInut() {
    assertThat(JPAServiceInstanceOperationEntity.osbType((JPAServiceInstanceOperationEntity)null)).isNull();
  }

  @Test
  void testOsbTypeOptionalOfJPAServiceInstanceBindingOperationEntity() {
    when(jpaEntity.getId()).thenReturn(ID);
    when(jpaEntity.getServiceInstanceId()).thenReturn(SERVICE_INSTANCE_ID);
    when(jpaEntity.getServiceDefinitionId()).thenReturn(SERVICE_DEFINITION_ID);
    when(jpaEntity.getServicePlanId()).thenReturn(SERVICE_PLAN_ID);
    when(jpaEntity.getCreatedAt()).thenReturn(NOW);
    when(jpaEntity.getType()).thenReturn(TYPE);
    when(jpaEntity.getStatus()).thenReturn(STATUS);
    
    osbEntity = JPAServiceInstanceBindingOperationEntity.osbType(Optional.of(jpaEntity));
    
    assertThat(osbEntity.getId()).isEqualTo(ID);
    assertThat(osbEntity.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
    assertThat(osbEntity.getServiceDefinitionId()).isEqualTo(SERVICE_DEFINITION_ID);
    assertThat(osbEntity.getServicePlanId()).isEqualTo(SERVICE_PLAN_ID);
    assertThat(osbEntity.getCreatedAt()).isEqualTo(NOW);
    assertThat(osbEntity.getType()).isEqualTo(TYPE);
    assertThat(osbEntity.getStatus()).isEqualTo(STATUS);
  }

  @Test
  void testOsbTypeOptionalOfJPAServiceInstanceBindingOperationEntityReturnsNullOnEmptyInput() {
    assertThat(JPAServiceInstanceBindingOperationEntity.osbType((Optional.empty()))).isNull();
  }
}
