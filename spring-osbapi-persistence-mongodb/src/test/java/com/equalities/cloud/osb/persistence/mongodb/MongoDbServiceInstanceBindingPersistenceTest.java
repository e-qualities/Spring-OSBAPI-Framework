package com.equalities.cloud.osb.persistence.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.mongodb.utils.ServiceInstanceBindingEntityCreator;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstanceBindingPersistenceTest {

  private MongoDbServiceInstanceBindingPersistence persistence;
  private ReactiveMongoTemplate mongoTemplate;
  private ServiceInstanceBindingEntity entity;

  @BeforeEach
  public void setUp() throws Exception {
    mongoTemplate = mock(ReactiveMongoTemplate.class);
    persistence = new MongoDbServiceInstanceBindingPersistence(mongoTemplate);
    entity = ServiceInstanceBindingEntityCreator.createServiceInstanceBindingEntity();
  }

  @Test
  public void testInsert() {
    when(mongoTemplate.insert(Mockito.any(ServiceInstanceBindingEntity.class))).thenAnswer((Answer<Mono<ServiceInstanceBindingEntity>>) invocation -> {
      final ServiceInstanceBindingEntity serviceInstanceBindingEntity = invocation.getArgument(0);
      return Mono.just(serviceInstanceBindingEntity);
    });

    final ServiceInstanceBindingEntity inserted = persistence.insert(entity).block();

    verify(mongoTemplate).insert(entity);
    assertThat(inserted).isSameAs(entity);
  }

  @Test
  public void testDelete() {
    when(mongoTemplate.remove(Mockito.any(ServiceInstanceBindingEntity.class))).thenReturn(Mono.just(DeleteResult.acknowledged(1)));
    persistence.delete(entity).block();
    verify(mongoTemplate).remove(entity);
  }

  @Test
  public void testUpdate() {
    when(mongoTemplate.save(Mockito.any(ServiceInstanceBindingEntity.class))).thenAnswer((Answer<Mono<ServiceInstanceBindingEntity>>) invocation -> {
      final ServiceInstanceBindingEntity serviceInstanceBindingEntity = invocation.getArgument(0);
      return Mono.just(serviceInstanceBindingEntity);
    });
    final ServiceInstanceBindingEntity updated = persistence.update(entity).block();
    verify(mongoTemplate).save(entity);
    assertThat(updated).isSameAs(entity);
  }

  @Test
  public void testRead() {
    when(mongoTemplate.findById(any(), any())).thenReturn(Mono.empty());
    persistence.readByServiceInstanceBindingId(entity.getServiceInstanceId()).block();
    verify(mongoTemplate).findById(entity.getServiceInstanceId(), ServiceInstanceBindingEntity.class);
  }
}
