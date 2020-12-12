# Spring OSBAPI Framework - Spring Boot Starter JPA

A Spring Boot Starter implementing JPA persistence for [Spring Open Service Broker API framework](../../spring-osbapi-framework/).

This starter uses Spring Data JPA to persist any state information required by a service broker in an object-relational database using the Java Persistence APIs. It also serves as a reference implementation for the [Spring OBSAPI Framework Persistence API](../../spring-osbapi-persistence-api/).

# Usage

Add the following dependency to your `pom.xml`.

```xml
<dependency>
  <groupId>com.equalities.cloud</groupId>
  <artifactId>spring-osbapi-jpa-springboot-starter</artifactId>
  <version>${version}</version>
</dependency>
```

For detailed descriptions of configurations of the JPA persistence layer, please consult the documentation of [Spring OSBAPI Persistence JPA](../../spring-osbapi-persistence-jpa/).