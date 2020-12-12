package com.equalities.cloud.osb.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * A configuration properties container for 
 * instance-specific and instance binding-specific behaviour of the library.
 * On Cloud Foundry these configurations can also be filled with values 
 * from VCAP_SERVICES. On K8S another source may be 
 * used to fill the values into the environment. Using a configuration 
 * properties object, developers can use {@code application.yaml} to 
 * specify the configurations.
 * <p>
 * Note, that in {@code application.yaml} you can easily reference
 * environment variables, like VCAP_SERVICES using the Spring Cloud
 * syntax following this form:
 * 
 * <pre>${vcap.services.${service-instance-name}.credentials...}</pre>
 * 
 * ... where {@code service-instance-name} can itself be a (most often user-provided) environment variable.
 */
@Data
@Validated
@ConfigurationProperties("com.equalities.osbapi")
public class OsbApiConfig {
  
  private ServiceInstancesConfig serviceInstances;
  
  private ServiceInstanceBindingsConfig serviceBindings;
  
  @Data
  @Validated
  public static class ServiceInstancesConfig {
    /**
     * Whether to forcefully remove service
     * instance entries when the service instance ID
     * given in the deletion request from the platform
     * is unknown.
     */
    private boolean forceDeleteUnknown;
    
    /**
     * Operations-related configurations
     */
    private OperationsConfig operations = new OperationsConfig();
  }
  
  @Data
  @Validated
  public static class ServiceInstanceBindingsConfig {
    /**
     * Whether to forcefully remove service
     * instance binding entries when their IDs or that of 
     * their service instances given in the deletion request 
     * from the platform is unknown.
     */
    private boolean forceDeleteUnknown;
    
    /**
     * Operations-related configurations
     */
    private OperationsConfig operations = new OperationsConfig();
  }
  
  @Data
  public static class OperationsConfig {
    /**
     * A duration specifying the age, that operations
     * should exceed to qualify for scheduled cleanup.
     */
    private Duration cleanupAge = Duration.ofDays(3);
    /**
     * A CRON pattern used for scheduling the removal
     * of all operations qualifying for cleanup.
     */
    private String cleanupAll = "-";
    /**
     * A CRON pattern used for scheduling the removal
     * of all succeeded operations qualifying for cleanup.
     */
    private String cleanupSucceeded = "-";
    /**
     * A CRON pattern used for scheduling the removal
     * of all failed operations qualifying for cleanup.
     */
    private String cleanupFailed = "-";
  }
}