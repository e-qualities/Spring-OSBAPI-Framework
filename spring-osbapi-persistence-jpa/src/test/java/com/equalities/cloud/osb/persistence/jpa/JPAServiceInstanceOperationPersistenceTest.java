package com.equalities.cloud.osb.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceOperationRepository;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceOperationEntity;
import com.equalities.osb.persistence.jpa.utils.ServiceInstanceOperationEntityCreator;

@ExtendWith(MockitoExtension.class)
public class JPAServiceInstanceOperationPersistenceTest {

  static final Duration duration = Duration.ofSeconds(3);
  
  JPAServiceInstanceOperationPersistence persistence;
  ServiceInstanceOperationEntity osbApiEntity;

  @Mock
  JPAServiceInstanceOperationEntity entity;
  
  @Mock
  ServiceInstanceOperationRepository repository;
  
  @Captor
  ArgumentCaptor<JPAServiceInstanceOperationEntity> captor;

  @BeforeEach
  public void setUp() throws Exception {
    persistence = new JPAServiceInstanceOperationPersistence(repository);
    osbApiEntity = ServiceInstanceOperationEntityCreator.createServiceInstanceOperationEntity();
  }

  @Test
  public void testInsertExpectRepositoryMethodCallInsert() {
    when(repository.save(any(JPAServiceInstanceOperationEntity.class))).thenReturn(entity);
    persistence.insert(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testUpdateExpectRepositoryMethodCallInsert() {
    when(repository.save(any(JPAServiceInstanceOperationEntity.class))).thenReturn(entity);
    persistence.update(osbApiEntity).block();
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testDeleteExpectRepositoryMethodCallInsert() {
    persistence.delete(osbApiEntity).block();
    verify(repository).delete(captor.capture());
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(captor.getValue().getId()).isEqualTo(osbApiEntity.getId());
  }

  @Test
  public void testReadExpectRepositoryMethodCallInsert() {
    when(repository.findById(any())).thenReturn(Optional.of(entity));
    persistence.readByOperationId(osbApiEntity.getId());
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
