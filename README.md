# Tasks

> Extend the order endpoint to find a specific order, by ID 

**Answer:** Implemented, added a simple `findById` method for the [OrderRepository](src/main/java/com/example/store/order/OrderRepository.java).

> Extend the customer endpoint to find customers based on a query string to match a substring of one of the words in their name

**Answer:** Implemented, added `findByNamePartialMatch` method for the [CustomerRepository](src/main/java/com/example/store/customer/CustomerRepository.java), using a native SQL query with a **LIKE** clause.

**Consideration**: A `GIN` index (using the `pg_trgm` extension) could have improved the performance. But I've not used it much, so I introduced only a simple index on the `name` column.

> Users have complained that in production the GET endpoints can get very slow. The database is unfortunately not co-located with the application server, and there's high latency between the two. Identify if there are any optimisations that can improve performance

**Answer:** I applied the following updates, both to improve efficiency and also to improve user-experience:

- No index on the DB tables for foreign key, added one for order::customer_id
- Introduced pagination (optional, so the API remains backwards compatible)
- Enabled gzip compression for responses

> Add a new endpoint /products to model products which appear in an order:
>    * A single order contains 1 or more products.
>    * A product has an ID and a description.
>    * Add a POST endpoint to create a product
>    * Add a GET endpoint to return all products, and a specific product by ID
>        * In both cases, also return a list of the order IDs which contain those products
>    * Change the orders endpoint to return a list of products contained in the order

**Answer:**

# Bonus points
> Implement a CI pipeline on the platform of your choice to build the project and deliver it as a Dockerized image

**Answer:** This has been implemented at [publish.yml](.github/workflows/publish.yml). It runs lints, unit tests and integration tests, then publishes
a docker image to [GHCR](https://github.com/zodac-personal/store/pkgs/container/store).

I have assumed all pushes will be directly to the **master** branch, and am not checking for any PRs.

# Notes on the tasks
> Assume that the project represents a production application.
Think carefully about the impact on performance when implementing your changes
The specifications of the tasks have been left deliberately vague. You will be required to exercise judgement about what to deliver - in a real world environment, you would clarify these points in refinement, but since this is a project to be completed without interaction, feel free to make assumptions - but be prepared to defend them when asked.
There's no CI pipeline associated with this project, but in reality there would be. Consider the things that you would expect that pipeline to verify before allowing your code to be promoted
Feel free to refactor the codebase if necessary. Bad choices were deliberately made when creating this project.

Fixed:
- Refactoring: I re-packaged the application to be by domain/feature, rather than by layer
- Marking API endpoints with 'consumes' and 'produces' definitions

Others:
- API path should include `/api/v1` for compatibility
- Might be a convention, but instead of the Controller returning 404, we could use a global exception handler?
- Logging for requests, at API boundary and perhaps even DB layer, could use some traceability
- DTOs are using `Long` for the ID, perhaps would be better as a UUID?
- Local dev, I would like a containerised script to run the lint/unit/IT checks as a githook before pushing, relying on host for now
- Expose the OpenAPI.yaml through the UI?
- Javadoc missing from public methods, should be some more comments/docs

----

# Original

----

# Store Application
The Store application keeps track of customers and orders in a database.


# Assumptions
This README assumes you're using a posix environment. It's possible to run this on Windows as well:
* Instead of `./gradlew` use `gradlew.bat`
* The syntax for creating the Docker container is different. You could also install PostgreSQL on bare metal if you prefer


# Prerequisites
This service assumes the presence of a postgresql 16.2 database server running on localhost:5433 (note the non-standard port)
It assumes a username and password `admin:admin` can be used.
It assumes there's already a database called `store`

You can start the PostgreSQL instance like this:
```shell
docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v postgres:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:16.2 \
  postgres -c wal_level=logical
```

# Running the application
You should be able to run the service using
```shell
./gradlew bootRun
```

The application uses Liquibase to migrate the schema. Some sample data is provided. You can create more data by reading the documentation in utils/README.md

# Data model
An order has an ID, a description, and is associated with the customer which made the order.
A customer has an ID, a name, and 0 or more orders.

# API
Two endpoints are provided:
   * /order
   * /customer

Each of them supports a POST and a GET. The data model is circular - a customer owns a number of orders, and that order necessarily refers back to the customer which owns it.
To avoid loops in the serializer, when writing out a Customer or an Order, they're mapped to CustomerDTO and OrderDTO which contain truncated versions of the dependent object - CustomerOrderDTO and OrderCustomerDTO respectively.

The API is documented in the OpenAPI file OpenAPI.yaml. Note that this spec includes part of one of the tasks below (the new /products endpoint)
