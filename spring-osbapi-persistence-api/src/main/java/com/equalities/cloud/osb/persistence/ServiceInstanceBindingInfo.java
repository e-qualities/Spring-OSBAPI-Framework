package com.equalities.cloud.osb.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.binding.Endpoint;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;


/**
 * A context class carrying state data to be persisted when a Service Instance Binding was
 * created. Applications can use this structure to provide a key-value map of context
 * data that it wants to be stored alongside generic service instance binding information.
 * <p>
 * Out of the generic information an application can store here, the following properties
 * take a special role in that they are sent back to the Cloud Platform controller, if the
 * respective callback response permits it:
 * <ul>
 * <li> parameters    - some service instance binding-specific parameters.
 * <li> endpoints     - some service instance binding-specific endpoints.
 * <li> volume mounts - some service instance binding-specific volume mounts.
 * </ul>
 *
 */
public class ServiceInstanceBindingInfo extends HashMap<String, Object> {
  private static final long serialVersionUID = -5526715009968181663L;
  
  private static final String CREDENTIALS_KEY   = "com_equalities_cloud_osb_Credentials";
  private static final String PARAMETERS_KEY    = "com_equalities_cloud_osb_Parameters";
  private static final String ENDPOINTS_KEY     = "com_equalities_cloud_osb_Endpoints";
  private static final String VOLUME_MOUNTS_KEY = "com_equalities_cloud_osb_VolumeMounts";
  
  /**
   * Creates a new instance.
   */
  public ServiceInstanceBindingInfo() {}
  
  /**
   * Returns the credentials map for this service instance binding.
   * @return the credentials map for this service instance binding, or null, if none was set.
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getCredentials() {
    return (Map<String, Object>) get(CREDENTIALS_KEY);
  }
  
  /**
   * Sets the credentials map for this service instance binding.
   * @param credentials the credentials for this service instance binding.
   */
  public void setCredentials(Map<String, Object> credentials) {
    put(CREDENTIALS_KEY, credentials);
  }
  
  /**
   * Returns the parameter map for this service instance binding.
   * @return the parameter map for this service instance binding, or null, if none was set.
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getParameters() {
    return (Map<String, Object>) get(PARAMETERS_KEY);
  }
  
  /**
   * Sets the parameter map for this service instance binding.
   * @param parameters the parameters for this service instance binding.
   */
  public void setParameters(Map<String, Object> parameters) {
    put(PARAMETERS_KEY, parameters);
  }
  
  /**
   * Returns the list of endpoints for this service instance binding.
   * @return the list of endpoints for this service instance binding, or null, if none was set.
   */
  @SuppressWarnings("unchecked")
  public List<Endpoint> getEndpoints() {
    return (List<Endpoint>) get(ENDPOINTS_KEY);
  }
  
  /**
   * Sets the endpoints list for this service instance binding.
   * @param endpoints the endpoints list for this service instance binding.
   */
  public void setEndpoints(List<Endpoint> endpoints) {
    put(ENDPOINTS_KEY, endpoints);
  }
  
  /**
   * Returns the volume mounts list for this service instance binding.
   * @return the volume mounts list for this service instance binding, or null, if none was set.
   */
  @SuppressWarnings("unchecked")
  public List<VolumeMount> getVolumeMounts() {
    return (List<VolumeMount>) get(VOLUME_MOUNTS_KEY);
  }
  
  /**
   * Sets the volume mounts list for this service instance binding.
   * @param volumeMounts the volume mounts list for this service instance binding.
   */
  public void setVolumeMounts(List<VolumeMount> volumeMounts) {
    put(VOLUME_MOUNTS_KEY, volumeMounts);
  }
}
