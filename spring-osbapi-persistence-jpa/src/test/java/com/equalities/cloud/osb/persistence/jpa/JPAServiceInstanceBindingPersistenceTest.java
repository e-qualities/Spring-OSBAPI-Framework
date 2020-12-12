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

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceBindingRepository;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingEntity;
import com.equalities.osb.persistence.jpa.utils.ServiceInstanceBindingEntityCreator;

@ExtendWith(MockitoExtension.class)
public class JPAServiceInstanceBindingPersistenceTest {

  JPAServiceInstanceBindingPersistence persistence;
  ServiceInstanceBindingEntity osbApiEntity;

  @Mock
  JPAServiceInstanceBindingEntity entity;
  
  @Mock
  ServiceInstanceBindingRepository repository;
  
  @Captor
  ArgumentCaptor<JPAServiceInstanceBindingEntity> captor;

  @BeforeEach
  public void setUp() throws Exception {
    persistence = new JPAServiceInstanceBindingPersistence(repository);
    osbApiEntity = ServiceInstanceBindingEntityCreator.createServiceInstanceBindingEntity();
  }

  @Test
  public void testInsert() {
    when(repository.save(any(JPAServiceInstanceBindingEntity.class))).thenReturn(entity);
    persistence.insert(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getServiceInstanceBindingId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceBindingId()).isEqualTo(osbApiEntity.getServiceInstanceBindingId());
  }

  @Test
  public void testDelete() {
    persistence.delete(osbApiEntity).block();
    verify(repository).delete(captor.capture());
    assertThat(captor.getValue().getServiceInstanceBindingId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceBindingId()).isEqualTo(osbApiEntity.getServiceInstanceBindingId());
  }

  @Test
  public void testUpdate() {
    when(repository.save(any(JPAServiceInstanceBindingEntity.class))).thenReturn(entity);
    persistence.update(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getServiceInstanceBindingId()).isNotNull();
    assertThat(captor.getValue().getServiceInstanceBindingId()).isEqualTo(osbApiEntity.getServiceInstanceBindingId());
  }

  @Test
  public void testRead() {
    when(repository.findById(anyString())).thenReturn(Optional.of(entity));
    persistence.readByServiceInstanceBindingId(osbApiEntity.getServiceInstanceId());
    verify(repository).findById(osbApiEntity.getServiceInstanceId());
  }
}
