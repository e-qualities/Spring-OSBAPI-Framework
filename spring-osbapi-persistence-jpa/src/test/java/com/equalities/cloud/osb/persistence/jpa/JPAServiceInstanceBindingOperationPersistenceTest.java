package com.equalities.cloud.osb.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceBindingOperationRepository;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingOperationEntity;
import com.equalities.osb.persistence.jpa.utils.ServiceInstanceBindingOperationEntityCreator;

@ExtendWith(MockitoExtension.class)
public class JPAServiceInstanceBindingOperationPersistenceTest {

  static final Duration duration = Duration.ofSeconds(3);
  
  private JPAServiceInstanceBindingOperationPersistence persistence;
  private ServiceInstanceBindingOperationEntity osbApiEntity;
  
  @Mock
  JPAServiceInstanceBindingOperationEntity entity;
  
  @Mock
  ServiceInstanceBindingOperationRepository repository;
  
  @Captor
  ArgumentCaptor<JPAServiceInstanceBindingOperationEntity> captor;

  @BeforeEach
  public void setUp() throws Exception {
    persistence = new JPAServiceInstanceBindingOperationPersistence(repository);
    osbApiEntity = ServiceInstanceBindingOperationEntityCreator.createServiceInstanceBindingOperationEntity();
  }

  @Test
  public void testInsert() {
    when(repository.save(any(JPAServiceInstanceBindingOperationEntity.class))).thenReturn(entity);
    persistence.insert(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testUpdate() {
    when(repository.save(any(JPAServiceInstanceBindingOperationEntity.class))).thenReturn(entity);
    persistence.update(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testDelete() {
    persistence.delete(osbApiEntity).block();
    verify(repository).delete(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testRead() {
    when(repository.findById(anyString())).thenReturn(Optional.of(entity));
    persistence.readByOperationId(osbApiEntity.getId()).block();
    verify(repository).findById(osbApiEntity.getId());
  }
  
  @Test
  void testDeleteOperationsByStateOlderThanDuration() {
    persistence.deleteOperationsOlderThan(duration).block();
    verify(repository).deleteOperationsOlderThan(duration);
  }
  
  @ParameterizedTest
  @EnumSource
  void testDeleteOperationsByStateOlderThanStateDuration(State state) {
    persistence.deleteOperationsByStateOlderThan(state, duration).block();
    verify(repository).deleteOperationsByStateOlderThan(state, duration);
  }
}
