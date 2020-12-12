# Spring Service Broker Framework

This repository aggregates various projects that constitute a framework to more easily create service brokers following the Open Service Broker API specifications using Spring Boot.

The framework presented here, is based on the well-known [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) project, which provides a Spring Boot implementation of the [Open Service Broker API specification](https://www.openservicebrokerapi.org/). The latter constitutes a significant cornerstone in the integration of services into various Cloud platforms, ranging from Cloud Foundry to Kubernetes.

The Open Service Broker API specification defines a REST API to `create`, `delete`, `update` and `read` service instances, as well as `create`, `delete` and `read` service instance _bindings_.

The REST API specification supports both synchronous (i.e. blocking) requests from Cloud controllers as well as asynchronous (i.e. non-blocking) requests to create, update or delete service instances and bindings. Especially asynchronous operations, which are often used for long-running create / delete / update processes, require that broker developers keep an operation's state. That state is subsequently polled by a Cloud platform controller to periodically check if the asynchronous operation has already finished and whether it succeeded or failed.

[Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) provides the interfaces to implement the REST APIs but it does not provide an implementation for persisting and managing asynchronous operation and service instance (binding) state. 
That is where Spring Open Service Broker API (OSBAPI) Framework takes over.

Spring OSBAPI Framework extends [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) by adding the following features:

- Default implementation for the handling of `create`, `delete`, `update` of service instances.
- Default implementation for handling of `create` and `delete` of service instance bindings.
- Default implementations for `getLastOpertation()` of both `ServiceInstanceService` and `ServiceInstanceBindingService` to support asynchronous operations.
- Definition of a domain-specific persistence model and API to store state related to service instances, service instance bindings and operations to create / delete / update them. These APIs are implemented by concrete persistence layer implementations available for MongoDB, JPA and in-memory persistence.

As a result, we allow developers of service brokers to focus solely on the business logic of their brokers without having to deal with the intricacies of keeping operation state or persisting instance and binding state information.

At the same time, we keep the framework generic to allow for the 90% case of service broker implementations.
The remaining 10% of broker scenarios can always fall back to Spring Cloud Open Service Broker APIs directly.

By doing so, we aim to make service broker implementation easier, more efficient and less error prone.

# Contents of the Repository

The main components of this repository are:

* [`spring-osbapi-framework`](./spring-osbapi-framework/) - Default implementations of the Open Service Broker APIs, implementing also the persistence and handling of state for (mostly asynchronous) operations.

* [`spring-osbapi-persistence-api`](./spring-osbapi-persistence-api/) - Persistence model and APIs of `spring-osbapi-framework`. This is the persistence layer that `spring-osbapi-framework` interfaces with. Several reference implementations are provided in this repository, and applications are free to provide their own ones, if they need or want to.

* [`spring-osbapi-persistence-*`](./README.md) - Various reference implementations of `spring-osbapi-persistence-api`. Currently an in-memory (`* = inmemory`), MongoDB (`* = mongodb`) and JPA-based (`* = jpa`) implementation exists.

* [`spring-osbapi-*-springboot-starter`](./spring-osbapi-starters/) - Various Spring Boot starters to easily get started with `spring-osbapi-framework`. The starters are specific to a persistence technology, so developers can pick and choose whatever fits them best.

Additional to the components above, the following folders exist:
* [`scripts`](./scripts/) - a folder holding scripts that can be used to locally test applications using `spring-osbapi-framework` by bringing up Docker-based MongoDB  or PostgreSQL database instances.

* [`postman-collections`](./postman-collections/) - here you can find a request collection for the popular Postman tool. The requests correspond to the HTTP requests a Cloud Controller will send, and as they were specified by the Open Service Broker API specification.

# Component-specific Documentation

As this repository follows a modular approach, please consult the documentation of the individual components for details.
