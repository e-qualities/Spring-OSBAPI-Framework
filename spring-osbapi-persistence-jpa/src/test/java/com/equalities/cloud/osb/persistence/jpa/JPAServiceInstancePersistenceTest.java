package com.equalities.cloud.osb.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceRepository;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceEntity;
import com.equalities.osb.persistence.jpa.utils.ServiceInstanceEntityCreator;

@ExtendWith(MockitoExtension.class)
public class JPAServiceInstancePersistenceTest {

  JPAServiceInstancePersistence persistence;
  ServiceInstanceEntity osbApiEntity;

  @Mock
  JPAServiceInstanceEntity entity;
  
  @Mock
  ServiceInstanceRepository repository;
  
  @Captor
  ArgumentCaptor<JPAServiceInstanceEntity> captor;

  @BeforeEach
  public void setUp() throws Exception {
    persistence = new JPAServiceInstancePersistence(repository);
    osbApiEntity = ServiceInstanceEntityCreator.createServiceInstanceOperationEntity();
  }

  @Test
  public void testInsert() {
    when(repository.save(any(JPAServiceInstanceEntity.class))).thenReturn(entity);
    persistence.insert(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getServiceInstanceId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceId()).isEqualTo(osbApiEntity.getServiceInstanceId());
  }

  @Test
  public void testDelete() {
    persistence.delete(osbApiEntity).block();
    verify(repository).delete(captor.capture());
    assertThat(captor.getValue().getServiceInstanceId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceId()).isEqualTo(osbApiEntity.getServiceInstanceId());
  }

  @Test
  public void testUpdate() {
    when(repository.save(any(JPAServiceInstanceEntity.class))).thenReturn(entity);
    persistence.update(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getServiceInstanceId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceId()).isEqualTo(osbApiEntity.getServiceInstanceId());
  }

  @Test
  public void testRead() {
    when(repository.findById(anyString())).thenReturn(Optional.of(entity));
    persistence.readByServiceInstanceId(osbApiEntity.getServiceInstanceId());
    verify(repository).findById(osbApiEntity.getServiceInstanceId());
  }
}
