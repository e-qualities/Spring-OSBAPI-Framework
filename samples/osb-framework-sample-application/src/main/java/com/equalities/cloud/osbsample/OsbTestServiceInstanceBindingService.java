package com.equalities.cloud.osbsample;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.Endpoint;
import org.springframework.cloud.servicebroker.model.binding.Endpoint.Protocol;
import org.springframework.stereotype.Service;

import com.equalities.cloud.osb.DefaultServiceInstanceBindingService;
import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingInfo;
import com.equalities.cloud.osb.persistence.ServiceInstanceInfo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OsbTestServiceInstanceBindingService extends DefaultServiceInstanceBindingService {

  public OsbTestServiceInstanceBindingService(PersistentStorage persistentStorage, OsbApiConfig config) {
    super(persistentStorage, config);
    setCreateServiceInstanceBindingAsync(true);
    setDeleteServiceInstanceBindingAsync(true);
  }

  @Override
  public Mono<ServiceInstanceBindingInfo> addServiceInstanceBinding(CreateServiceInstanceBindingRequest request, ServiceInstanceInfo serviceInstanceInfo) {
    ServiceInstanceBindingInfo info = new ServiceInstanceBindingInfo();
    info.setCredentials(createCredentials());
    info.setEndpoints(createEndpoints());
    return Mono.delay(Duration.ofSeconds(5))
               .then(Mono.just(info));
  }
  
  private List<Endpoint> createEndpoints() {
    List<Endpoint> endpoints = new ArrayList<>();
    List<String> ports = Arrays.asList("8080", "9090", "10000");
    endpoints.add(new Endpoint("example.com", ports, Protocol.TCP));
    return endpoints;
  }

  private Map<String, Object> createCredentials() {
    Map<String, Object> credentials = new HashMap<>();
    credentials.put("username", "value");
    credentials.put("password", "value");
    credentials.put("url",      "value");
    return credentials;
  }

  @Override
  public Mono<Void> removeServiceInstanceBinding(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingInfo bindingInfo) {
    log.info("Received call to delete service instance binding. Not doing anything since nothing special is required.");
    return Mono.empty();
  }
}
