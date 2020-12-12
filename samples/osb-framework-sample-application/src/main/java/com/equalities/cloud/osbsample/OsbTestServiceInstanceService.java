package com.equalities.cloud.osbsample;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;
import org.springframework.stereotype.Service;

import com.equalities.cloud.osb.DefaultServiceInstanceService;
import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OsbTestServiceInstanceService extends DefaultServiceInstanceService {
  
  public OsbTestServiceInstanceService(PersistentStorage storage, OsbApiConfig config) {
    super(storage, config);
    setCreateServiceInstanceAsync(true);
    setDeleteServiceInstanceAsync(true);
    setUpdateServiceInstanceAsync(true);
  }
  
  @Override
  public Mono<ServiceInstanceInfo> addServiceInstance(CreateServiceInstanceRequest request) {
    log.info("TestApp: Received call to CREATE service instance.");
    log.info("TestApp: Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.info("TestApp: Request from Cloud Controller:           {}", request);
    log.info("TestApp: Cloud Context:                           {}", request.getContext());
    
    Map<String, Object> params = new HashMap<>();
    params.put("Hello", "World");
    params.put("Array", Arrays.asList("One", "Two", "Three"));
    
    ServiceInstanceInfo info = new ServiceInstanceInfo();
    info.put("Test", "Test");
    info.setDashboardURL("http://www.google.com");
    info.setParameters(params);
    
    return Mono.delay(Duration.ofSeconds(5))
               .then(Mono.just(info));
  }

  @Override
  public Mono<Void> removeServiceInstance(DeleteServiceInstanceRequest request, ServiceInstanceInfo instanceInfo) {
    //delete service instance here.
    log.info("TestApp: Received call to DELETE service instance.");
    log.info("TestApp: Request from Cloud Controller: {}", request);
    return Mono.empty();
  }

  @Override
  public Mono<ServiceInstanceInfo> changeServiceInstance(UpdateServiceInstanceRequest request, ServiceInstanceInfo instanceInfo) {
    return Mono.empty();
  }
}
