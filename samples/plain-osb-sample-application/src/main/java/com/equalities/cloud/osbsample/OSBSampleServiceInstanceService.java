package com.equalities.cloud.osbsample;

import java.util.Date;
import java.util.UUID;

import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OSBSampleServiceInstanceService implements ServiceInstanceService {
  
  private Date startTime;

  @Override
  public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
    log.info("Received callback to CREATE service instance.");
    log.info("Platform allows async instance creation: {}", request.isAsyncAccepted());
    log.info("Request from Cloud Controller:           {}", request);
    log.info("Cloud Context:                           {}", request.getContext());
    
    // you can get all this information from the request context on CF (i.e. if platform == "cloudfoundry"):
    // final String platform            = request.getContext().getPlatform();
    // final String spaceGuid           = (String) request.getContext().getProperties().get("spaceGuid");
    // final String orgGuid             = (String) request.getContext().getProperties().get("organizationGuid");
    // final String spaceName           = (String) request.getContext().getProperties().get("spaceName");
    // final String orgName             = (String) request.getContext().getProperties().get("organizationName");
    // final String serviceInstanceName = (String) request.getContext().getProperty("instanceName");
    // final String originating_user_id = (String) request.getOriginatingIdentity().getProperties().get("user_id");
    
    startTime = new Date();
    
    String generatedOperationId = UUID.randomUUID().toString();
    
    return Mono.just(CreateServiceInstanceResponse.builder()
                                                  .async(true)
                                                  .operation(generatedOperationId)
                                                  .build());
  }

  @Override
  public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
    log.info("Received callback to QUERY (create) operation status.");
    log.info("Requested status of operation: {}", request.getOperation());
    log.info("Request from Cloud Controller: {}", request);
    
    //Simulating a long-running service creation.
    Date endTime = new Date();
    long creationDuration = endTime.getTime() - startTime.getTime(); 
    
    if(creationDuration < 10000) {
      return Mono.just(GetLastServiceOperationResponse.builder()
                                                      .operationState(OperationState.IN_PROGRESS)
                                                      .build());
    }
    else {
      return Mono.just(GetLastServiceOperationResponse.builder()
                                                      .operationState(OperationState.SUCCEEDED)
                                                      .build());
    }
  }

  @Override
  public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
    //delete service instance here.
    log.info("Received call to DELETE service instance.");
    log.info("Request from Cloud Controller: {}", request);
    return Mono.just(DeleteServiceInstanceResponse.builder().async(false).build());
  }
}
