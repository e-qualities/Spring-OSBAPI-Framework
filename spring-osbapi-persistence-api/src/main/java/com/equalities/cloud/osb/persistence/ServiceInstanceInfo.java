package com.equalities.cloud.osb.persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * A context class carrying state data to be persisted when a Service Instance was
 * created. Applications can use this structure to provide a key-value map of context
 * data that it wants to be stored alongside generic service instance information.
 * <p>
 * Out of the generic information an application can store here, the following properties
 * take a special role in that they are sent back to the Cloud Platform controller, if the
 * respective callback response permits it:
 * <ul>
 * <li> dashboardUrl - the dashboard URL, if the service provides any dashboard.
 * <li> parameters   - the some service-instance-specific parameters.
 * </ul>
 *
 */
public class ServiceInstanceInfo extends HashMap<String, Object> {
  private static final long serialVersionUID    = -3030800158419790344L;
  private static final String DASHBOARD_URL_KEY = "com_equalities_cloud_osb_DashboardUrl";
  private static final String PARAMETERS_KEY    = "com_equalities_cloud_osb_Parameters";
  
  /**
   * Creates a new instance.
   */
  public ServiceInstanceInfo() {}
  
  /**
   * Returns the dashboard URL of the service instance, if one was set. 
   * @return the dashboard instance URL if set. Null otherwise.
   */
  public String getDashboardUrl() {
    return (String) get(DASHBOARD_URL_KEY);
  }
  
  /**
   * Sets the service instance dashboard URL.
   * @param dashboardURL the dashboard URL of the service instance.
   */
  public void setDashboardURL(String dashboardURL) {
    put(DASHBOARD_URL_KEY, dashboardURL);
  }
  
  /**
   * Returns the parameter map for this service instance.
   * @return the parameter map for this service instance, or null, if none was set.
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getParameters() {
    return (Map<String, Object>) get(PARAMETERS_KEY);
  }
  
  /**
   * Sets the parameter map for this service instance.
   * @param parameters the parameters for this service instance.
   */
  public void setParameters(Map<String, Object> parameters) {
    put(PARAMETERS_KEY, parameters);
  }
}
