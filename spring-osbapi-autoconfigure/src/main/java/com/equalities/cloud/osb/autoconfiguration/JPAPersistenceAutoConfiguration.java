package com.equalities.cloud.osb.autoconfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.equalities.cloud.osb.persistence.PersistentStorageConfiguration;
import com.equalities.cloud.osb.persistence.ScheduledPersistenceTasksConfiguration;
import com.equalities.cloud.osb.persistence.jpa.JPAPersistenceConfiguration;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceRepository;
import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingEntity;

@Configuration
@EntityScan( basePackageClasses = { JPAServiceInstanceBindingEntity.class })
@EnableJpaRepositories( basePackageClasses = { ServiceInstanceRepository.class })
@ConditionalOnClass(JPAPersistenceConfiguration.class)
@Import({
  JPAPersistenceConfiguration.class, 
  PersistentStorageConfiguration.class, 
  ScheduledPersistenceTasksConfiguration.class
})
public class JPAPersistenceAutoConfiguration {

}
