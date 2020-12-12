# Spring Open Service Broker API Framework

This project provides a framework for the implementation of Spring Boot-based Cloud service brokers that adhere to the [Open Service Broker API specifications](https://www.openservicebrokerapi.org/).

The framework is based on the [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) project which provides the programmatic interfaces for the implementation of the Open Service Broker API specifications.

Using Spring Cloud Open Service Broker, service developers usually write a service broker by implementing the following two interfaces:
* `ServiceInstanceService` - to deal with the creation / deletion / update of service instances.
* `ServiceInstanceBindingService` - to deal with the creation / deletion of service instance bindings to applications.

Both service instances and bindings are created via requests issued by a Cloud platform's controller. To that end, every service broker provides a well-defined REST API for a Cloud platform controller to interact with.

The REST API is defined to support both synchronous (i.e. blocking) requests from Cloud controllers as well as asynchronous (mostly long-running) requests to create, update or delete service instances and bindings. Especially the latter require that broker developers keep an asynchronous operation state, which is frequently polled by the Cloud controller to check, if instance and / or binding creation / deletion / update has succeeded or not.

[Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) provides the interfaces to implement the REST APIs but it does not provide an implementation for persisting and managing asynchronous operation or service instance and binding state. 
That is where Spring Open Service Broker API (OSBAPI) Framework takes over.

Spring OSBAPI Framework extends [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) by adding the following features:

- Default implementation for the handling of `create`, `delete`, `update` of service instances.
- Default implementation for handling of `create` and `delete` of service instance bindings.
- Default implementations for `getLastOpertation()` of both `ServiceInstanceService` and `ServiceInstanceBindingService` to support asynchronous operations.
- Definition of a domain-specific persistence model and API to store state related to service instances, service instance bindings and operations to create / delete / update them. These APIs are implemented by concrete persistence layer implementations for MongoDB, JPA and in-memory peristence.

As a result we allow developers of service brokers to focus solely on the business logic of their brokers without having to deal with the intricacies of keeping operation state or persisting instance and binding state information.

At the same time we keep the framework generic to allow for the 90% case of service broker implementations.
The remaining 10% of broker scenarios can always fall back to Spring Cloud Open Service Broker APIs directly.

# Usage

To use Spring OSBAPI Framework, add the following dependencies to your `pom.xml`:

* For use with in-memory persistence:
  ```xml
  <dependency>
    <groupId>com.equalities.cloud</groupId>
    <artifactId>spring-osbapi-inmemory-springboot-starter</artifactId>
    <version>${revision}</version>
  </dependency>
  ```
* For use with MongoDB persistence:
  ```xml
  <dependency>
    <groupId>com.equalities.cloud</groupId>
    <artifactId>spring-osbapi-mongodb-springboot-starter</artifactId>
    <version>${revision}</version>
  </dependency>
  ```
* For use with JPA-based persistence:
  ```xml
  <dependency>
    <groupId>com.equalities.cloud</groupId>
    <artifactId>spring-osbapi-jpa-springboot-starter</artifactId>
    <version>${revision}</version>
  </dependency>
  ```

Each respective starter will add Spring OSBAPI Framework, the corresponding persistence layer implementation as well as auto-configurations to your your project. Auto-configurations will make sure that all required Spring Beans will be exposed accordingly.

❗Note that depending on the persistence technology chosen, you might have to configure connectivity properties in your broker's `application.yaml` file(s). Please consult the documentation of the respective starter / persistence implementation for details.

# Implementing Service Brokers with Spring OSBAPI Framework

Using Spring OSBAPI Framework, service developers write a service broker by subclassing the following two abstract classes:

* `DefaultServiceInstanceService` - provides default implementations and hooks to handle creation, deletion and update of service instances.
* `DefaultServiceInstanceBindingService` - provides default implementations and hooks to handle creation and deletion of service instance bindings.

Additional to extending the classes above, broker developers need to annotate their subclasses with the `@Service` annotation from Spring Boot, for Spring Cloud Open Service Broker to pick up the implementations as beans:

```java
@Service
public class MyServiceInstanceService extends DefaultServiceInstanceService {
 //...
}

@Service
public class MyServiceInstanceBindingService extends DefaultServiceInstanceBindingService {
 //...
}
```

As is the case for plain Spring Cloud Open Service Broker implementations, developers need to code the service-specific business logic that deals with creation, deletion and update of service instances and bindings to applications.

To that end, the following abstract methods have been defined that need to be implemented by service broker developers:

For Service Instances, `DefaultServiceInstanceService` provides the following abstract methods:
* ```java 
  Mono<ServiceInstanceInfo> addServiceInstance(CreateServiceInstanceRequest request)
  ```
* ```java 
  Mono<Void> removeServiceInstance(DeleteServiceInstanceRequest request, ServiceInstanceInfo instanceInfo)
  ```
* ```java 
  Mono<ServiceInstanceInfo> changeServiceInstance(UpdateServiceInstanceRequest request, ServiceInstanceInfo instanceInfo)
  ```

For Service Instance Bindings, `DefaultServiceInstanceBindingService` provides the following abstract methods:
* ```java
  Mono<ServiceInstanceBindingInfo> addServiceInstanceBinding(CreateServiceInstanceBindingRequest request, ServiceInstanceInfo serviceInstanceInfo)
  ```
* ```java 
  Mono<Void> removeServiceInstanceBinding(DeleteServiceInstanceBindingRequest request, ServiceInstanceBindingInfo bindingInfo)
  ```

Note, that we are using `Mono<?>` structures (aka promises) of the popular [Project Reactor](https://projectreactor.io/docs/core/release/reference/) framework. By doing so, we allow developers to create fully asynchronous service broker implementations, if they need to. 

The framework will handle even long-running tasks gracefully, and will favor asynchronous operations over synchronous ones if the Cloud platform itself supports it. Broker developers can influence that, however, by using any of the `setCreateServiceInstanceAsync(boolean)`, `setDeleteServiceInstanceAsync(boolean)` and `setUpdateServiceInstanceAsync(boolean)` as well as `setCreateServiceInstanceBindingAsync(boolean)` and `setDeleteServiceInstanceBindingAsync(boolean)` methods respectively.

For sample broker implementations have a look at the [samples](../samples) folder.

# Managing Persistence

Spring OSBAPI Framework defines a common persistence model and API that allows developers to define their own persistence layers. The framework comes with 3 reference implementations, based on [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb), [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and an in-memory `java.util.HashMap` (mainly intended for debugging and testing).

Broker developers can easily select the proper combination of Spring OSBAPI Framework and the required persistence technology by adding one of the Spring Boot [starters](../spring-osbapi-starters) provided with this framework.

# Configuring Services Metadata & Plans

Apart from dealing with service instance and binding creation, service brokers also play a crucial part in service discovery. They do so by providing REST endpoints that allow Cloud platform controllers to query catalog information about the services managed by a broker along with their respective service plans.

That catalog information is then displayed in human-readable form in a service marketplace of the respective Cloud platform.

With Spring Cloud Open Service Broker, developers can maintaining such catalog information in `application.yml`. This is, of course, available in Spring OSBAPI Framework as well.

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
In the configuration above, only a single service is declared as part of the catalog data. However, a single service broker can be responsible for several services, and hence, a list of any number of services can be declared in the catalog data. The [osb-framework-sample-application](../samples/osb-framework-sample-application/) project shows that, so feel free to inspect its `application.yaml` if you are curious.

Also note, that both the service as well as its plans require `id`s that uniquely distinguish them from potential other services that might be registered to the respective Cloud platform.
In the example above, a human readable form following a reverse domain name pattern was used.
Of course you could also use GUIDs, human-readable IDs should be preferred, however.

Finally, note the `image-url-resource` property. Here a URL or the name of an image resource can be specified that will be used to show an icon / logo for the service in the market place. Spring Cloud allows you to package image resources inside your broker's `.jar` file and thus makes it easy to provide your service broker with a visually appealing, branded catalog entry.

# Additional Configuration Parameters

Apart from the configurations provided by Spring Cloud Open Service Broker, Spring OSBAPI Framework provides the following configurations:

| Configuration Key | Type | Description | Default Value |
|-------------------|------|-------------|---------------|
|com.equalities.osbapi.service-bindings.force-delete-unknown| boolean | flag to forcefully remove a service binding instance whose ID cannot be found in the broker's data storage | false |
|com.equalities.osbapi.service-instances.force-delete-unknown| boolean | flag to forcefully remove a service instance whose ID cannot be found in the broker's data storage | false |

These configurations can be useful during development, when the broker's underlying data store is frequently purged, e.g. as a result to the broker being undeployed or re-deployed. In such cases, developers easily end up with inconsistent state, where a service instance was created by a previous broker version, but (after redeployment) can now no longer be found in the (purged) data store. Under normal circumstances this will lead to an error, and the service instance will cannot be deleted anymore. To override this, ignore any unknown service instance IDs and simply signal successful deletion to the Cloud platform contoller, you can enable the configuration flags listed above.

Broker developers use these configurations in their `application.yaml` like this:

```yaml
com:
  equalities:
    osbapi:
      service-bindings:
        # Use with care. Requests to delete unknown bindings (i.e. whose ID is not known) will be removed without error.
        force-delete-unknown: true 
      service-instances:
        # Use with care. Requests to delete unknown instances (i.e. whose ID is not known) will be removed without error.
        force-delete-unknown: true 
```

❗ **Note:** Although not likely to cause any harm, it is not recommended to use these configurations during production.

# Dealing with Broker State

As mentioned in the synopsis of Spring OSBAPI Framework, the framework handles the state required to provide asynchronous broker operations. It also manages any state that required for book keeping of created service instances and bindings.

Generally, that state management is handled transparently for broker developers. However, there may be cases when broker developers want to keep their own, additional state related to a service instance of binding.

To support such scenarios, and to avoid that broker developers need to manage their own state stores additionally, the Spring OSBAPI Framework APIs provide two generic classes that can be used to persist custom state.
These classes are `ServiceInstanceInfo` and `ServiceInstanceBindingInfo`.

Both `ServiceInstanceInfo` and `ServiceInstanceBindingInfo` are subclasses of `java.util.HashMap` and support the key-value storage of arbitrary objects. `ServiceInstanceInfo` and `ServiceInstanceBindingInfo` are both persisted as part of the state information related to a newly created (or updated) service instance and binding, respectively.

Broker developers return a new instance of `ServiceInstanceInfo` from the implementation of `addServiceInstance(CreateServiceInstanceRequest)` and can use it to store custom state information as they see fit. Likewise a new instance of `ServiceInstanceBindingInfo` is returned from `addServiceInstanceBinding(CreateServiceInstanceBindingRequest)`. Spring OSBAPI Framework will make sure that subsequent calls to `removeServiceInstance()`, `updateServiceInstance()` or `removeServiceInstanceBinding()` will get the created objects injected.

Since it is not uncommon for state created during service instance creation to be required when a service _binding_ is created the `addServiceInstanceBinding(CreateServiceInstanceBindingRequest, ServiceInstanceInfo)` method of class `DefaultServiceInstanceBindingService` also gets the `ServiceInstanceInfo` object injected.
A scenario, when this can be useful, is when during service instance creation, credentials have been generated or retrieved that need to be returned later, as a result of the instance being bound to an application.

❗ Although storage of custom state information is handled conveniently in Spring OSBAPI Framework, service broker implementations should use it with care, and for lightweight state information only. 
Depending on the underlying persistence technology used, state information may be serialized before it is persisted. Therefore, the amount of data stored as custom state should be limited to a bare minimum.

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