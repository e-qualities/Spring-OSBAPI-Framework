# Spring OSBAPI Framework Sample Application

This sample shows an implementation of a service broker written with [Spring OSBAPI Framework](../../spring-osbapi-framework/). In contrast to the [`plain-osb-sample-application`](../plain-osb-sample-application/), this sample does not use Spring Cloud Open Service Broker directly, but the [Spring OSBAPI Framework](../../spring-osbapi-framework/) which was built on top. 

As a result this sample application does not have worry about persisting and handling asynchronous operation state or service instance and binding-specific data. All that is taken care of by the Spring OSBAPI Framework.Persistence implementations exist for MongoDB and JPA (e.g. PostgreSQL) as well as an in-memory variant intended mainly for testing and development purposes.

Note, however, that this application still is a full Spring Cloud-based app. In other words, you still have full access to the APIs provided by Spring Cloud Open Service Broker.

# Building Project

1. Open a terminal and change directory to the root of this repository.
2. On the root of the repository, execute `mvn clean install`.

This will build the entire framework, including the sample applications.

# Running Locally

Before running locally, you first need to decide which persistence technology you want to use.
Spring OSBAPI Framework comes with 3 different persistence layer implementations:
1. Mongo DB
2. JPA (e.g. PostgreSQL)
3. In-Memory (not intended for production purposes)
   
You select the persistence layer by including the respective Spring Boot starter in the sample's `pom.xml`:

```xml
    <dependency>
      <groupId>com.equalities.cloud</groupId>
      <artifactId>spring-osbapi-jpa-springboot-starter</artifactId>
      <version>${revision}</version>
      <optional>false</optional>
    </dependency>
    
    <!-- 
    <dependency>
      <groupId>com.equalities.cloud</groupId>
      <artifactId>spring-osbapi-mongodb-springboot-starter</artifactId>
      <version>${revision}</version>
    </dependency>
    -->

    <!-- 
    <dependency>
      <groupId>com.equalities.cloud</groupId>
      <artifactId>spring-osbapi-inmemory-springboot-starter</artifactId>
      <version>${revision}</version>
    </dependency>
    -->
```
By default, the JPA persistence layer is active, but you can choose another simply by commenting the JPA starter and uncommenting any of the other two.

Once you have decided for a peristence layer, you can run the application as follows:

1. Open `application-default.yaml` and locate the following section:
   ```yaml
   spring:
    profiles:
      active:
      - default
      - postgresql
   ```
   Depending on the persistence layer, you chose, make sure that the active profile is either `postgresql`, `mongodb` or `inmemory`. Make sure the `default` is also active!
2. Build the application as described in [Building Project](#building-project).
3. If you chose MongoDB or JPA as the persistence layer, you need to start the respective database locally, first. You can do that using the shell scripts in the `scripts` folder, but you will need to have Docker installed and running for that to work.  
   For MongoDB, execute `./scripts/startMongoDb.sh`.  
   For PostgreSQL, use `./scripts/startPostgreSQL.sh`.  
   Likewise, there are scripts to stop the databases again.
4. Finally, **from the root of this repository**, run the broker application using:
   ```shell 
   mvn spring-boot:run -f ./samples/osb-framework-sample-application/pom.xml
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

2020-11-10 16:17:47.028  INFO 29630 --- [           main] c.s.cloud.osbsample.OsbTestApplication   : The following profiles are active: postgresql
2020-11-10 16:17:47.618  INFO 29630 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2020-11-10 16:17:47.835  INFO 29630 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 206ms. Found 4 JPA repository interfaces.
2020-11-10 16:17:48.200  INFO 29630 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=efb201f8-0a47-39d5-94b1-e7998e9043f3
2020-11-10 16:17:48.553  INFO 29630 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9002 (http)
2020-11-10 16:17:48.562  INFO 29630 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2020-11-10 16:17:48.563  INFO 29630 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.38]
2020-11-10 16:17:48.676  INFO 29630 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2020-11-10 16:17:48.676  INFO 29630 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1626 ms
2020-11-10 16:17:48.966  INFO 29630 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2020-11-10 16:17:49.062  INFO 29630 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 5.4.21.Final
2020-11-10 16:17:49.306  INFO 29630 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
2020-11-10 16:17:49.452  INFO 29630 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2020-11-10 16:17:49.600  INFO 29630 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2020-11-10 16:17:49.623  INFO 29630 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.PostgreSQLDialect
2020-11-10 16:17:50.514  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2020-11-10 16:17:50.515  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "jpaservice_instance_binding_entity" does not exist, skipping
2020-11-10 16:17:50.517  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2020-11-10 16:17:50.517  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "jpaservice_instance_binding_operation_entity" does not exist, skipping
2020-11-10 16:17:50.519  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2020-11-10 16:17:50.519  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "jpaservice_instance_entity" does not exist, skipping
2020-11-10 16:17:50.520  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2020-11-10 16:17:50.520  WARN 29630 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "jpaservice_instance_operation_entity" does not exist, skipping
2020-11-10 16:17:50.569  INFO 29630 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
2020-11-10 16:17:50.576  INFO 29630 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2020-11-10 16:17:50.956  WARN 29630 --- [           main] o.s.c.n.a.ArchaiusAutoConfiguration      : No spring.application.name found, defaulting to 'application'
2020-11-10 16:17:50.961  WARN 29630 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
2020-11-10 16:17:50.961  INFO 29630 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
2020-11-10 16:17:50.969  WARN 29630 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
2020-11-10 16:17:50.969  INFO 29630 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
2020-11-10 16:17:51.061  WARN 29630 --- [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2020-11-10 16:17:51.236  INFO 29630 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2020-11-10 16:17:51.693  INFO 29630 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoint(s) beneath base path '/actuator'
2020-11-10 16:17:51.765  INFO 29630 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9002 (http) with context path ''
2020-11-10 16:17:51.787  INFO 29630 --- [           main] c.s.cloud.osbsample.OsbTestApplication   : Started OsbTestApplication in 5.869 seconds (JVM running for 11.63)
2020-11-10 16:17:54.211  INFO 29630 --- [on(3)-127.0.0.1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2020-11-10 16:17:54.211  INFO 29630 --- [on(3)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2020-11-10 16:17:54.219  INFO 29630 --- [on(3)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 8 ms
```

Once up and running, you can use the [Postman](https://www.postman.com/downloads/) collection in folder `postman-collections` to send requests to the broker application as will be done by a Cloud platform controller.

# Deploying to Cloud Foundry

Your service broker is just a Spring Boot application that exposes the Open Service Broker API REST endpoints.
In this sample we show how to deploy the broker to Cloud Foundry and using PostgreSQL as the persistence. As a result, a PostgreSQL service instance needs to be created.

To deploy to Cloud Foundry proceed as follows:
1. Check out `manifest-variables.yml` and adjust the settings to your deployment landscape. These variables are used in `manifest.yml` during deployment, and in `services-manifest.yml` for service instance creation.
2. Create the PostgresSQL service instance using  
   `cf create-service-push --vars-file manifest-variables.yml --no-push --push-as-subprocess`
3. Push the broker to the cloud using `cf push --vars-file manifest-variables.yml`

Note that Step 2. requires the [CF Create-Service-Push plugin](https://github.com/dawu415/CF-CLI-Create-Service-Push-Plugin).

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

## Broker Implementation

Using Spring OSBAPI Framework, service developers write a service broker by subclassing the following two abstract classes:
* `DefaultServiceInstanceService`, see class `OsbTestServiceInstanceService` for how this can be done.
* `DefaultServiceInstanceBindingService`, see class `OsbTestServiceInstanceBindingService` for how this can be done.

As is the case for plain Spring Cloud Open Service Broker implementations, developers need to deal with creation, deletion and update of service instances as well as creation and deletion of service instance bindings to applications.

To that end, the following abstract methods have been defined that need to be implemented by service broker developers:

For Service Instances:
* ```java 
  Mono<ServiceInstanceInfo> addServiceInstance(CreateServiceInstanceRequest request)
  ```
* ```java 
  Mono<Void> removeServiceInstance(DeleteServiceInstanceRequest request, ServiceInstanceInfo instanceInfo)
  ```
* ```java 
  Mono<ServiceInstanceInfo> changeServiceInstance(UpdateServiceInstanceRequest request, ServiceInstanceInfo instanceInfo)
  ```

For Service Instance Bindings:
* ```java
  Mono<ServiceInstanceBindingInfo> addServiceInstanceBinding(CreateServiceInstanceBindingRequest request, ServiceInstanceInfo serviceInstanceInfo)
  ```
* ```java 
  Mono<Void> removeServiceInstanceBinding(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingInfo bindingInfo)
  ```

Note, that using `Mono<?>` structures of the popular [Project Reactor](https://projectreactor.io/docs/core/release/reference/) framework allows developers to create fully asynchronous implementations, if they need to. The framework will handle even long-running tasks gracefully, if the Cloud platform itself supports asynchronous service brokers.

The Open Service Broker REST APIs were defined to support both synchronous, i.e. blocking requests from Cloud Controllers as well as asynchronous (mostly long-running) requests to create, update or delete service instances and bindings. Especially the latter require that broker developers keep an asynchronous operation state, which is frequently polled by a Cloud Controller to check, if instance and binding creation / deletion / update has succeeded or not.

Spring OSBAPI Framework handles all that for you. In fact, Spring OSBAPI Framework comes with several persistence implementations that can be exchanged at compile time. Peristence implementations for MongoDB and JPA (e.g. PostgreSQL) exist as well as an in-memory variant mainly intended for testing purposes.

# Configuring Services Metadata & Plans

Apart from dealing with service instance and binding creation, service brokers also play a crucial part in service discoverability. They do so by providing REST endpoints that allow Cloud platform controllers to query catalog information about the services managed by a broker along with their respective service plans.

That catalog information is usually requested from the broker via REST calls and displayed in human-readable form in a service marketplace in the respective Cloud platform.

With Spring Cloud Open Service Broker, maintaining that catalog information is as easy as editing [`application.yml`](./src/main/resources/application.yaml) and this is, of course, available in Spring OSBAPI Framework as well.

Here is an example of what that might look like:

```yaml
spring:
  cloud:
    openservicebroker:
      catalog:
        services:
        # Service A
        - id: com.equalities.sample.serviceA
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
          - id: com.equalities.sample.serviceA.small
            name: small
            description: A small-sized service version
          - id: com.equalities.sample.serviceA.medium
            name: medium
            description: A medium-sized service version
          - id: com.equalities.sample.serviceA.large
            name: large
            description: A large-sized service version
```
Above, only a single service is declared as part of the catalog data. In fact, however, a single service broker can be responsible for several services, and hence, more than one services can be declared in the catalog data. The sample project shows that, so feel free to inspect `application.yaml` if you are curious.

Also note, that both the service as well as its plans require `id`s that uniquely distinguish them from potential other services that might be registered to the Cloud platform.
In the example above, a human readable form following a reverse domain name pattern was used.
Of course, you could also use GUIDs, however, human-readable IDs should be preferred.

Finally, note the `image-url-resource` property. Here a URL or the name of an image resource can be specified that will be used to show an icon / logo for the service in the market place. Spring Cloud allows you to package image resources inside your broker's `.jar` file and thus makes it easy to provide your service broker with a visually appealing, branded catalog entry.

# Scheduled State Cleanup
Spring OSBAPI Framework persists state not only about service instances and service bindings, but also about operations related to their creation, deletion or update.

While this operation state is required, especially to service asynchronous requests from the Cloud Controller, its relevance is usually short-lived. Once an operation has succeeded (or failed) and the Cloud Controller has polled its state and reacted accordingly, the operation's state is not really required any more - at most when some error analysis is required.

Hence, in order to not overload the persistent storage with continously accumulating operation state, Spring OSBAPI Framework offers a scheduled removal of such state from persistence. In that case a simple scheduled task is executing at defined intervals and removes succeeded, failed or all operations that exceed a certain, configurable age.

To enable scheduled cleanup tasks, broker developers can use the following configurations in their `application.yaml`:

```yaml
com:
  equalities:
    osbapi:
      service-bindings:
        operations:
          cleanup-age:      "10d" # the age an operation should have to qualify for scheduled removal / clean up. Format: 1s / 2h / 3d
          cleanup-all:       "0 0 0 */10 * *"  # CRON pattern to schedule removal of all qualifying operations.
          cleanup-succeeded: "0 0 0 */10 * *"  # CRON pattern to schedule removal of succeeded, qualifying operations.
          cleanup-failed:    "0 0 0 */10 * *"  # CRON pattern to schedule removal of failed, qualifying operations.
      service-instances:        
        operations:
          cleanup-age:      "10d" # the age an operation should have to qualify for scheduled removal / clean up. Format: 1s / 2h / 3d
          cleanup-all:       "0 0 0 */10 * *"  # CRON pattern to schedule removal of all qualifying operations.
          cleanup-succeeded: "0 0 0 */10 * *"  # CRON pattern to schedule removal of succeeded, qualifying operations.
          cleanup-failed:    "0 0 0 */10 * *"  # CRON pattern to schedule removal of failed, qualifying operations.
```

As depicted above, the broker developers can specify a CRON job pattern for **either** all, succeeded or failed operations to schedule removal. To qualify operations for cleanup a `cleanup-age` should be specified, which is a `java.time.Duration` that an operation's `createdAt` timestamp is checked against. If an operation's age exceeds the configured duration it will be removed.

If no CRON patterns are defined in `application.yaml`, scheduled cleanup will be disabled.

For an explanation and examples of CRON job patterns, have a look at these resources:
* [Spring CronExpression](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html)
* [CronTab Manual](https://www.manpagez.com/man/5/crontab/)

# Service Broker Security

When a service broker is registered to a Cloud platform, you usually have to provide credentials.
For example, if you register a broker to Cloud Foundry, you do so as follows:

```shell
cf create-service-broker my-service-broker <some username> <some password> <URL of your deployed broker application> --space-scoped
```

Note the `username` and `password` specified in the CLI call. These credentials are used by CF's Cloud Controller to authenticate itself to your broker application, when it calls its REST APIs.

The Spring Cloud Service Broker library does not provide a security configuration as part of the framework. The main reason is, that Cloud Platforms do not use a uniform authentication mechanism between the platform's Cloud controller and the registered service brokers. Some use basic authentication, others prefer OAuth 2.0. Authentication to service brokers is also not part of the Open Service Broker API specs.

However, Spring Cloud list examples how to secure your service broker endpoints with Spring Security. Please consult the [Spring Cloud Service Broker - Service Broker Security](https://docs.spring.io/spring-cloud-open-service-broker/docs/3.1.2.RELEASE/reference/html5/#service-broker-security) section of the documentation and follow what is described there.