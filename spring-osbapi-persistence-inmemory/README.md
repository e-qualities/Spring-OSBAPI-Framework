# Spring Open Service Broker API Framework - In Memory Persistence

This project is a reference implementation of [spring-osbapi-persistence-api](../spring-osbapi-persistence-api).

It implements persistence based on a simple `HashMap` that only persists in application memory.
The implementation is mainly intended as a reference implementation of the persistence APIs and for testing and debugging purposes.

❗**Warning:** This implementation is not intended for production purposes. It is not recommended to ship this with your product unless you know what you are doing.