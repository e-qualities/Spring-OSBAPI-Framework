package com.equalities.cloud.osb.persistence.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.mongodb.utils.ServiceInstanceBindingOperationEntityCreator;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class MongoDbServiceInstanceBindingOperationPersistenceTest {

  static final Duration duration = Duration.ofSeconds(3);
  
  ServiceInstanceBindingOperationEntity entity;
  
  MongoDbServiceInstanceBindingOperationPersistence persistence;
  
  @Mock
  ReactiveMongoTemplate mongoTemplate;
  
  @Captor
  ArgumentCaptor<Query> queryCaptor;
  
  @Captor
  ArgumentCaptor<Class<?>> classCaptor;

  @BeforeEach
  public void setUp() throws Exception {
    persistence = new MongoDbServiceInstanceBindingOperationPersistence(mongoTemplate);
    entity = ServiceInstanceBindingOperationEntityCreator.createServiceInstanceBindingOperationEntity();
  }

  @Test
  public void testInsert() {
    when(mongoTemplate.insert(entity)).thenReturn(Mono.just(entity));
    persistence.insert(entity).block();
    verify(mongoTemplate).insert(entity);
  }

  @Test
  public void testUpdate() {
    when(mongoTemplate.save(entity)).thenReturn(Mono.just(entity));
    persistence.update(entity).block();
    verify(mongoTemplate).save(entity);
  }

  @Test
  public void testDelete() {
    when(mongoTemplate.remove(entity)).thenReturn(Mono.just(DeleteResult.acknowledged(1)));
    persistence.delete(entity).block();
    verify(mongoTemplate).remove(entity);
  }

  @Test
  public void testRead() {
    when(mongoTemplate.findById(any(), any())).thenReturn(Mono.empty());
    persistence.readByOperationId(entity.getId()).block();
    verify(mongoTemplate).findById(entity.getId(), ServiceInstanceBindingOperationEntity.class);
  }
  
  @Test
  void testDeleteOperationsByStateOlderThanDuration() {
    when(mongoTemplate.remove(any(Query.class), any(Class.class))).thenReturn(Mono.empty());
    persistence.deleteOperationsOlderThan(duration).block();
    verify(mongoTemplate).remove(queryCaptor.capture(), classCaptor.capture());
    assertThat(queryCaptor.getValue()).isNotNull();
    assertThat(classCaptor.getValue()).isEqualTo(ServiceInstanceBindingOperationEntity.class);
  }
  
  @ParameterizedTest
  @EnumSource
  void testDeleteOperationsByStateOlderThanStateDuration(State state) {
    when(mongoTemplate.remove(any(Query.class), any(Class.class))).thenReturn(Mono.empty());
    persistence.deleteOperationsByStateOlderThan(state, duration).block();
    verify(mongoTemplate).remove(queryCaptor.capture(), classCaptor.capture());
    assertThat(queryCaptor.getValue()).isNotNull();
    assertThat(classCaptor.getValue()).isEqualTo(ServiceInstanceBindingOperationEntity.class);
  }
}
