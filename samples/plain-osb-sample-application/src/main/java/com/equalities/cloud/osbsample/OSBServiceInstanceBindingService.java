package com.equalities.cloud.osbsample;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.Endpoint;
import org.springframework.cloud.servicebroker.model.binding.Endpoint.Protocol;
import org.springframework.cloud.servicebroker.model.binding.GetLastServiceBindingOperationRequest;
import org.springframework.cloud.servicebroker.model.binding.GetLastServiceBindingOperationResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OSBServiceInstanceBindingService implements ServiceInstanceBindingService {

  private Date startTime;
  
  @Override
  public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
    log.info("Received callback to CREATE service binding instance.");
    log.info("Platform accepts async binding creation: {}", request.isAsyncAccepted());
    log.info("Request from Cloud Controller:           {}", request);
    log.info("Cloud Context:                           {}", request.getContext());
    
    // you can get all this information from the request context on CF (i.e. if platform == "cloudfoundry"):
    final String platform            = request.getContext().getPlatform();
    final String spaceGuid           = (String) request.getContext().getProperties().get("spaceGuid");
    final String orgGuid             = (String) request.getContext().getProperties().get("organizationGuid");
    final String spaceName           = (String) request.getContext().getProperties().get("spaceName");
    final String orgName             = (String) request.getContext().getProperties().get("organizationName");
    final String originating_user_id = (String) request.getOriginatingIdentity().getProperties().get("user_id");
    
    // False on SAP CF.
    boolean asyncAccepted = request.isAsyncAccepted();
    
    startTime = new Date();
    
    String generatedOperationId = UUID.randomUUID().toString();
    
    if(asyncAccepted) {
      return Mono.just(CreateServiceInstanceAppBindingResponse.builder()
          .async(asyncAccepted) 
          .operation(generatedOperationId)
          .build());
    }
    else {
      Map<String, Object> credentials = createCredentials();
      List<Endpoint> endpoints = createEndpoints();
      
      // Currently SAP CF only supports synchronous binding. Duh!
      return Mono.just(CreateServiceInstanceAppBindingResponse.builder()
          .async(asyncAccepted) 
          .credentials(credentials)
          .endpoints(endpoints)
          .build());
    }
  }
  
  @Override
  public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
    //delete service instance binding here.
    log.info("Received call to DELETE service instance binding.");
    return Mono.just(DeleteServiceInstanceBindingResponse.builder()
                                                         .async(false)
                                                         .build());
  }
  
  @Override
  public Mono<GetLastServiceBindingOperationResponse> getLastOperation(GetLastServiceBindingOperationRequest request) {
    log.info("Received callback to QUERY (create binding) operation status.");
    log.info("Requested status of operation: {}", request.getOperation());
    log.info("Request from Cloud Controller: {}", request);
    
    //Simulating a long-running service creation.
    Date endTime = new Date();
    long creationDuration = endTime.getTime() - startTime.getTime(); 
    
    if(creationDuration < 10000) {
      return Mono.just(GetLastServiceBindingOperationResponse.builder()
                                                             .operationState(OperationState.IN_PROGRESS)
                                                             .build());
    }
    else {
      return Mono.just(GetLastServiceBindingOperationResponse.builder()
                                                             .operationState(OperationState.SUCCEEDED)
                                                             .build());
    }
  }
  
  @Override
  public Mono<GetServiceInstanceBindingResponse> getServiceInstanceBinding(GetServiceInstanceBindingRequest request) {
    log.info("Received callback to QUERY binding instance information.");
    log.info("Request from Cloud Controller: {}", request);
    
    Map<String, Object> credentials = createCredentials();
    List<Endpoint> endpoints = createEndpoints();
    
    return Mono.just(GetServiceInstanceAppBindingResponse.builder()
                                                         .credentials(credentials)
                                                         .endpoints(endpoints)
                                                         .build());
  }

  private List<Endpoint> createEndpoints() {
    List<Endpoint> endpoints = new ArrayList<>();
    endpoints.add(new Endpoint("example.com", List.of("8080", "9090", "10000"), Protocol.TCP));
    return endpoints;
  }

  private Map<String, Object> createCredentials() {
    Map<String, Object> credentials = new HashMap<>();
    credentials.put("username", "value");
    credentials.put("password", "value");
    credentials.put("url",      "value");
    return credentials;
  }
}
