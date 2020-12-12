# Spring Cloud Open Service Broker Sample

This sample shows a skeleton implementation of a service broker written with Spring Cloud Open Service Broker.

# Building Project

1. Open a terminal and change directory to the root of this repository.
2. On the root of the repository, execute `mvn clean install`.

This will build the entire framework, including the sample applications.

# Running Locally

To run the sample broker locally, simply build the application as described above.
Then from a terminal window at the root of the repository execute:

 ```shell 
 mvn spring-boot:run -f ./samples/plain-osb-sample-application/pom.xml
 ```

As a result, you should see output similar to this one:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.4.RELEASE)

2020-11-10 15:33:36.047  INFO 28004 --- [           main] c.s.c.osbsample.OsbSampleApplication     : No active profile set, falling back to default profiles: default
2020-11-10 15:33:36.470  INFO 28004 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=7df21517-41fc-3f45-aa73-91639c8c657e
2020-11-10 15:33:36.645  INFO 28004 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9001 (http)
2020-11-10 15:33:36.651  INFO 28004 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2020-11-10 15:33:36.651  INFO 28004 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.38]
2020-11-10 15:33:36.714  INFO 28004 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2020-11-10 15:33:36.714  INFO 28004 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 658 ms
2020-11-10 15:33:36.824  WARN 28004 --- [           main] o.s.c.n.a.ArchaiusAutoConfiguration      : No spring.application.name found, defaulting to 'application'
2020-11-10 15:33:36.825  WARN 28004 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
2020-11-10 15:33:36.825  INFO 28004 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
2020-11-10 15:33:36.827  WARN 28004 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
2020-11-10 15:33:36.828  INFO 28004 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
2020-11-10 15:33:37.037  INFO 28004 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2020-11-10 15:33:37.247  INFO 28004 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoint(s) beneath base path '/actuator'
2020-11-10 15:33:37.281  INFO 28004 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9001 (http) with context path ''
2020-11-10 15:33:37.298  INFO 28004 --- [           main] c.s.c.osbsample.OsbSampleApplication     : Started OsbSampleApplication in 3.027 seconds (JVM running for 3.299)
```

Once up and running, you can use the [Postman](https://www.postman.com/downloads/) collection in folder `postman-collections` to send requests to the broker application as will be done by a Cloud platform controller.

# Deploying to Cloud Foundry

Your service broker is just a Spring Boot application that exposes the Open Service Broker API REST endpoints.
To deploy it proceed as follows:

1. Check out `manifest-variables.yml` and adjust the settings to your deployment landscape. These variables are used in `manifest.yml` during deployment.
2. `cf push --vars-file manifest-variables.yml`

# Registering the Service Broker 

To make your service broker available to the Cloud Foundry platform, you need to register it.  
This can be done on a global scale, or scoped to your development space.

To register your broker in your CF space proceed as follows:

1. Open a terminal with CF CLI installed
2. Login to your CF account and space using `cf login`. You might want to confirm which account and space you are targeting using `cf target`.
3. Make sure you have deployed your broker as described in [Deploying to Cloud Foundry](#deploying-to-cloud-foundry).
4. Register your broker using `cf create-service-broker sample-service-broker <some username> <some password> <URL of your deployed broker application> --space-scoped`, e.g. `cf create-service-broker sample-service-broker username password https://osb-sample.cfapps.eu10.hana.ondemand.com --space-scoped` 

Note the `--space-scoped` flag which is used to register the broker only within your space. Also note that the `username` and `password` you pass will be used by Cloud Controller to authenticate itself against your broker application. 

❗WARNING: Do **not** register the broker URL with a trailing slash (`/`). If you register e.g. `https://osb-sample.cfapps.eu10.hana.ondemand.com/` (note the trailing slash) Cloud Foundry's Cloud Controller may not call your broker's REST endpoints properly. In fact, what you might see is Cloud Controller requests of the form `GET //v2/service_instances/...`, where the path starts with two slashes (`//`) which causes the broker implementation to return a `404 Not Found` error.

# Broker Implementation

Using Spring Cloud Open Service Broker, service developers usually write a service broker by implementing the following two interfaces:
* `ServiceInstanceService`, see class `OSBSampleServiceInstanceService` for how this can be done.
* `ServiceInstanceBindingService`, see class `OSBSampleServiceInstanceBindingService` for how this can be done.

In the implementation, developers need to deal with creation, deletion and update of service instances as well as creation and deletion of service instance bindings to applications.

Both instance and instance bindings are created based on requests that are issued by the respective Cloud platform's Cloud controller. In other words, every service broker provides a well-defined REST API for a Cloud Controller to interact with.

Spring Cloud Open Service Broker provides the programmatic framework behind that REST API.

The REST API was defined to support both synchronous, i.e. blocking requests from Cloud Controllers as well as asynchronous (mostly long-running) requests to create, update or delete service instances and bindings. Especially the latter require that broker developers keep an asynchronous operation state, which is frequently polled by a Cloud Controller to check, if instance and binding creation / deletion / update has succeeded or not.

Spring Cloud Open Service Broker does not provide an implementations for persisting and managing this state as of today.

# Configuring Services Metadata & Plans

Apart from dealing with service instance and binding creation, service brokers also play a crucial part in service discoverability. They do so by providing REST endpoints that allow Cloud platform controllers to query catalog information about the services managed by a broker along with their respective service plans.

That catalog information is usually requested from the broker via REST calls and displayed in human-readable form in a service marketplace in the respective Cloud platform.

With Spring Cloud Open Service Broker, maintaining that catalog information is as easy as editing [`application.yml`](./src/main/resources/application.yaml). 

Here is an example of what that might look like:

```yaml
spring:
  cloud:
    openservicebroker:
      catalog:
        services:
        
        # Service A
        - id: 6311aa10-6577-48d2-8553-2812e4ebb247
          name: Service-A
          description: A simple service A.
          bindable: true
          plan-updateable: false
          instances-retrievable: true
          bindings-retrievable: true
          metadata:
            display-name:       Service A
            documentation-url:  https://en.wikipedia.org/wiki/Documentation
            support-url:        https://en.wikipedia.org/wiki/Support
            long-description:   A long description of Service A.
            image-url-resource: serviceA.png
          tags:
          - service-a
          - sample-service
          - cloud-foundry
          plans:
          - id: 401beae4-4e37-40eb-a9d4-fffbbbe6bb27
            name: small
            description: A small-sized service version
          - id: 9eb91c55-b01e-4baa-807d-c10227c75daf
            name: medium
            description: A medium-sized service version
          - id: 644a92d2-bc8a-4663-aa93-b56afe947bad
            name: large
            description: A large-sized service version
```
Above, only a single service is declared as part of the catalog data. In fact, however, a single service broker can be responsible for several services, and hence, more than one services can be declared in the catalog data.

Also note, that both the service as well as its plans require IDs that uniquely distinguish them from potential other services that might be registered to the Cloud platform.
In the example above, GUIDs are used as IDs, but more human readable versions, e.g. following a reverse domain name pattern, could be used. For example, the ID `com.equalities.cloud.MyService` might be equally unique and is much easier to read.

Finally, note the `image-url-resource` property. Here a URL or the name of an image resource can be specified that will be used to show an icon / logo for the service in the market place. Spring Cloud allows you to package image resources inside your broker's `.jar` file and thus makes it easy to provide your service broker with a visually appealing, branded catalog entry.

# Service Broker Security

When a service broker is registered to a Cloud platform, you usually have to provide credentials.
For example, if you register a broker to Cloud Foundry, you do so as follows:

```shell
cf create-service-broker my-service-broker <some username> <some password> <URL of your deployed broker application> --space-scoped
```

Note the `username` and `password` specified in the CLI call. These credentials are used by CF's Cloud Controller to authenticate itself to your broker application, when it calls its REST APIs.

The Spring Cloud Service Broker library does not provide a security configuration as part of the framework. The main reason is, that Cloud Platforms do not use a uniform authentication mechanism between the platform's Cloud controller and the registered service brokers. Some use basic authentication, others prefer OAuth 2.0. Authentication to service brokers is also not part of the Open Service Broker API specs.

However, Spring Cloud list examples how to secure your service broker endpoints with Spring Security. Please consult the [Spring Cloud Service Broker - Service Broker Security](https://docs.spring.io/spring-cloud-open-service-broker/docs/3.1.2.RELEASE/reference/html5/#service-broker-security) section of the documentation and follow what is described there.