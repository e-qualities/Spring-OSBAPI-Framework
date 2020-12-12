# Spring Open Service Broker API Framework - JPA Persistence

This project is a reference implementation of [spring-osbapi-persistence-api](../spring-osbapi-persistence-api) based on [Spring Data JPA](https://spring.io/projects/spring-data-jpa).

The implementation was tested with PostgreSQL as its underlying persistence technology. However, using JPA, other object-relational databases can be used with this persistence implementation as well. For details please consulte the Spring Data JPA reference documentation.

In the following we will show how to connect to a PostgreSQL database.

# Using PostgreSQL Locally

At the root of this repository you can find a `scripts` folder, which contains Docker-based shell scripts that allow you to start and stop PostgreSQL as a Docker container.

To start PostgreSQL locally, proceed as follows:
1. Open a terminal window at the root of this repository
2. Execute `./scripts/startPostgreSQL.sh` (likewise to stop, execute `./scripts/stopPostgreSQL.sh`)

This will start the PostgreSQL database configured with user name `postgres`, password `test1234` and database `postgres`.

In your `application.yaml` you need to specify these connection parameters for Spring Boot to be able to access your PostgreSQL instance:

```yaml
spring:
  jpa:  
    hibernate:  
      # options: 
      # - validate:     validates the schema but makes no changes
      # - update:       updates the schema
      # - create:       creates the schema, destroying any previous data
      # - create-drop:  like create, but also drops the schema when the session closes (useful for testing)
      ddl-auto: create-drop 
    database-platform: org.hibernate.dialect.PostgreSQLDialect  
  datasource:  
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: test1234
```

Note, that the cconfiguration above uses the `ddl-auto: create-drop`. This is only useful for testing, as it will result in a database drop, i.e. purging all data, as soon as your application is stopped.
For production scenarios, use `ddl-auto: create` or `ddl-auto: update`.

# Using PostgreSQL in Cloud Foundry

When running on CF, you need a PostgreSQL service instance bound to your application.
In the following, we assume that the name of the service instance is given by an environment variable called `postgres-instance-name`.

One way to set such an environment variable is to use `manifest.yml`. In case you want to hardcode the service instance name, you can do so, too, of course.

```yaml
spring:
  jpa:  
    hibernate:  
      # options: 
      # - validate:     validates the schema but makes no changes
      # - update:       updates the schema
      # - create:       creates the schema, destroying any previous data
      # - create-drop:  like create, but also drops the schema when the session closes (useful for testing)
      ddl-auto: create-drop 
    database-platform: org.hibernate.dialect.PostgreSQLDialect  
  datasource:  
    url: jdbc:${vcap.services.${postgres-instance-name}.credentials.uri}  
    username: ${vcap.services.${postgres-instance-name}.credentials.username}  
    password: ${vcap.services.${postgres-instance-name}.credentials.password}
```

# Custom Service Instance and Binding Data

The [Spring OSBAPI Framework Persistence API](../../spring-osbapi-persistence-api/) defines `ServiceInstanceEntity` and `ServiceInstanceBindingEntity` which both provide a `data` property of type `ServiceInstanceInfo` and `ServiceInstanceBindingInfo`, respectively.

Both `data` objects, are subclasses of Java's `HashMap<String, Object>`, and allow developers to persist custom objects to store whatever state is required in the context of the created service instance or binding.

The JPA persistence layer provided by this project serializes those custom objects into JSON before persisting them in an object relational database as an SQL large object (Lob). Serialization and deserialization is performed using [Jackson Data Bind](https://github.com/FasterXML/jackson-databind).

For developers, that means that your custom objects need to be serializable to JSON - which is the case for most of the objects you will create, anyway.

However, there can be corner cases, in which you might experience serialzation errors. Examples for such corner cases are the use of unmodifiable types, e.g. `Collections.unmodifiableList()`, `List.of()`, etc.. 
These will fail to deserialized as a result of Jackson not being able to instantiate unmodifiable types.

Generally, the use of unmodifiable types should be avoided, when they are part of data structures that should be serialized. In error cases, please always consult the Jackson Data Bind documentation and make sure your data can be properly serialized and deserialized to and from JSON.