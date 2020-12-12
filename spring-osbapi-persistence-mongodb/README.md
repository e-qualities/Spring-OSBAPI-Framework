# Spring Open Service Broker API Framework - MongoDB Persistence

This project is a reference implementation of [spring-osbapi-persistence-api](../spring-osbapi-persistence-api) using MongoDB as persistence technology.

The implementation is based on [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) and its `ReactiveMongoTemplate`.
For configuration details, please see the Spring Data MongoDB reference documentation.

# Using MongoDB Locally

At the root of this repository you can find a `scripts` folder, which contains Docker-based shell scripts that allow you to start and stop MongoDB as a Docker container.

To start MongoDB locally, proceed as follows:
1. Open a terminal window at the root of this repository
2. Execute `./scripts/startMongoDb.sh` (likewise to stop, execute `./scripts/stopMongoDb.sh`)

This will start the MongoDB database locally with default settings.

No additional Spring Boot configurations are necessary to connect from the broker implementation to the local MongoDB instance.

However, if you choose to set a different username or password (see [MongoDB on Docker Hub](https://hub.docker.com/_/mongo)) you will have to add MongoDB-specific configurations to your `application.yaml`.

See the Spring Data MongoDB documentation for details.