package com.equalities.cloud.osb.persistence.mongodb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.mongodb.utils.ServiceInstanceEntityCreator;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstancePersistenceTest {

  private MongoDbServiceInstancePersistence persistence;
  private ReactiveMongoTemplate mongoTemplate;
  private ServiceInstanceEntity entity;

  @BeforeEach
  public void setUp() throws Exception {
    mongoTemplate = mock(ReactiveMongoTemplate.class);
    persistence = new MongoDbServiceInstancePersistence(mongoTemplate);
    entity = ServiceInstanceEntityCreator.createServiceInstanceOperationEntity();
  }

  @Test
  public void testInsert() {
    when(mongoTemplate.insert(Mockito.any(ServiceInstanceEntity.class))).thenAnswer((Answer<Mono<ServiceInstanceEntity>>) invocation -> {
      final ServiceInstanceEntity serviceInstanceEntity = invocation.getArgument(0);
      return Mono.just(serviceInstanceEntity);
    });

    final ServiceInstanceEntity inserted = persistence.insert(entity).block();

    verify(mongoTemplate).insert(entity);
    Assertions.assertThat(inserted).isSameAs(entity);
  }

  @Test
  public void testDelete() {
    when(mongoTemplate.remove(Mockito.any(ServiceInstanceEntity.class))).thenReturn(Mono.just(DeleteResult.acknowledged(1)));
    persistence.delete(entity).block();
    verify(mongoTemplate, times(1)).remove(entity);
  }

  @Test
  public void testUpdate() {
    when(mongoTemplate.save(Mockito.any(ServiceInstanceEntity.class))).thenAnswer((Answer<Mono<ServiceInstanceEntity>>) invocation -> {
      final ServiceInstanceEntity serviceInstanceEntity = invocation.getArgument(0);
      return Mono.just(serviceInstanceEntity);
    });
    final ServiceInstanceEntity updated = persistence.update(entity).block();
    verify(mongoTemplate).save(entity);
    Assertions.assertThat(updated).isSameAs(entity);
  }

  @Test
  public void testRead() {
    when(mongoTemplate.findById(any(), any())).thenReturn(Mono.empty());
    persistence.readByServiceInstanceId(entity.getServiceInstanceId()).block();
    verify(mongoTemplate, times(1)).findById(entity.getServiceInstanceId(), ServiceInstanceEntity.class);
  }
}
