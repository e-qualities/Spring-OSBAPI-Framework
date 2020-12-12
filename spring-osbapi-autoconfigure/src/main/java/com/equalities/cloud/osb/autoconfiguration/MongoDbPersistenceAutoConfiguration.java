package com.equalities.cloud.osb.autoconfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.equalities.cloud.osb.persistence.PersistentStorageConfiguration;
import com.equalities.cloud.osb.persistence.ScheduledPersistenceTasksConfiguration;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbPersistenceConfiguration;

@Configuration
@ConditionalOnClass(MongoDbPersistenceConfiguration.class)
@Import({
  MongoDbPersistenceConfiguration.class, 
  PersistentStorageConfiguration.class, 
  ScheduledPersistenceTasksConfiguration.class
})
public class MongoDbPersistenceAutoConfiguration {

}
