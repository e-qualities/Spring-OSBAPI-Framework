package com.equalities.cloud.osb.persistence.jpa.entities;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JPAServiceInstanceEntity {
  
  private static final ObjectMapper mapper = new ObjectMapper()
                                             .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                                             .activateDefaultTyping(new LaissezFaireSubTypeValidator(), 
                                                                    DefaultTyping.NON_CONCRETE_AND_ARRAYS);

  @Id
  private String serviceInstanceId;
  
  private Instant createdAt;

  private String serviceDefinitionId;

  private String servicePlanId;

  @Lob
  private String data;
  
  public static JPAServiceInstanceEntity jpaType(ServiceInstanceEntity entity) {
    try {
      ServiceInstanceInfo data = entity.getData();
      
      String serializedData = null;
      if(data != null) {
        serializedData = mapper.writeValueAsString(data);
      }
      
      return new JPAServiceInstanceEntity(entity.getServiceInstanceId(),
                                          entity.getCreatedAt(),
                                          entity.getServiceDefinitionId(), 
                                          entity.getServicePlanId(), 
                                          serializedData);
    } catch (JsonProcessingException e) {
      log.error("Error serializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.", e);
      throw new RuntimeException("Error serializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.");
    }
  }
  
  public static ServiceInstanceEntity osbType(JPAServiceInstanceEntity entity) {
    if(entity == null) {
      log.debug("Cannot map null-valued JPA entity to OSB persistence API entity. Returning null. This may be caused by entities not being found by ID.");
      return null;
    }
    
    try {
      String serializedData = entity.getData();
      
      ServiceInstanceInfo data = null; 
      
      if(serializedData != null) {
        data = mapper.readValue(serializedData, ServiceInstanceInfo.class);
      }
      
      return new ServiceInstanceEntity(entity.getServiceInstanceId(),
                                       entity.getCreatedAt(),
                                       entity.getServiceDefinitionId(), 
                                       entity.getServicePlanId(), 
                                       data);
    } 
    catch (JsonMappingException e) {
      log.error("Error deserializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.", e);
      throw new RuntimeException("Error deserializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.");
    }
    catch (JsonProcessingException e) {
      log.error("Error deserializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.", e);
      throw new RuntimeException("Error deserializing 'data' field of ServiceInstanceEntity. Make sure that all data is serializable to JSON.");
    }
  }
  
  public static ServiceInstanceEntity osbType(Optional<JPAServiceInstanceEntity> entity) {
    if(entity.isEmpty()) {
      return osbType((JPAServiceInstanceEntity) null);
    }
    return osbType(entity.get());
  }
}
