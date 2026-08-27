# Usage

The application can be run using the provided [docker-compose.yml](./docker-compose.yml) file, using the following command:

```bash
docker compose down && docker compose up --build -d
```

The application has started when you can see the following message:

> Tomcat started on port 8080 (http) with context path '/'

You can use the following links to check the API endpoints.

| API Endpoint                         | Link                                                |
|--------------------------------------|-----------------------------------------------------|
| Get All Orders                       | http://127.0.0.1:8888/order                         |
| Get All Orders (paginated)           | http://127.0.0.1:8888/order?page=0&size=5           |
| Get Order By ID                      | http://127.0.0.1:8888/order/1                       |
|                                      |                                                     |
| Get All Customers                    | http://127.0.0.1:8888/customer                      |
| Get All Customers (paginated)        | http://127.0.0.1:8888/customer?page=0&size=5        |
| Search Customers By Name             | http://127.0.0.1:8888/customer?name=don             |
| Search Customers By Name (paginated) | http://127.0.0.1:8888/customer?name=m&page=0&size=5 |
|                                      |                                                     |
| Get All Products                     | http://127.0.0.1:8888/product                       |
| Get All Products (paginated)         | http://127.0.0.1:8888/product?page=0&size=5         |
| Get Product By ID                    | http://127.0.0.1:8888/product/1                     |
|                                      |                                                     |
| Get Status                           | http://127.0.0.1:8888/status                        |

# Tasks

> Extend the order endpoint to find a specific order, by ID 

**Answer:** Implemented, added a simple `findById` method for the [OrderRepository](src/main/java/com/example/store/order/OrderRepository.java).

> Extend the customer endpoint to find customers based on a query string to match a substring of one of the words in their name

**Answer:** Implemented, added `findByNamePartialMatch` method for the [CustomerRepository](src/main/java/com/example/store/customer/CustomerRepository.java), using a native SQL query with a **LIKE** clause.

**Consideration**: A `GIN` index (using the `pg_trgm` extension) could have improved the performance. But I've not used it much, so I introduced only a simple index on the `name` column.

> Users have complained that in production the GET endpoints can get very slow. The database is unfortunately not co-located with the application server, and there's high latency between the two. Identify if there are any optimisations that can improve performance

**Answer:** I applied the following updates, both to improve efficiency and also to improve user-experience:

- There was no foreign key index on the DB tables, added one for `order::customer_id`
- Introduced pagination (optional, so the API remains backwards compatible)
- Enabled gzip compression for server responses

Other considerations:

- ETags could have been added, but that's an optimisation for client -> server, but not server -> DB, so I didn't add this
- A cache like redis/valkey could have been introduced to cache repeat requests, but without profiling (are repeat requests common? Is pagination sufficient?) I left this for now
- Dunno how the mapper classes work, would investigate that further if I had time to see if there are unnecessary DB calls being made

> Add a new endpoint /products to model products which appear in an order:
>    * A single order contains 1 or more products.
>    * A product has an ID and a description.
>    * Add a POST endpoint to create a product
>    * Add a GET endpoint to return all products, and a specific product by ID
>        * In both cases, also return a list of the order IDs which contain those products
>    * Change the orders endpoint to return a list of products contained in the order

**Answer:**

- Taking the values from the pre-populated data for orders, such as below, I created [products.sql](src/main/resources/db/changelog/products.sql):
```json
{
  "id": 1,
  "name": "Muriel Donnelly",
  "orders": [
    {
      "description": "Awesome Concrete Shirt",
      "id": 158
    }
  ]
}
```
- A new package `product` was created for the new feature, with the standard DTO/Mapper/Repository/Service/Controller structure
- An order can have "1 or more" products, so a join table was also created
    - Added a [ProductMapper](src/main/java/com/example/store/product/ProductMapper.java) to map associated Orders to IDs for the JSON response
- Added GET `/`, GET `/{id}`, and POST endpoints
    - Included pagination to match Order/Customer

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
    - Added a *Service class for each feature to abstract business logic away from API/DB logic
- Built a distroless, nonroot docker image. Using multiple stages to minimise size and attack surface
    - Could have used an Alpine Linux image instead, but Debian is easier to `docker exec` into for me to debug, and the size difference was minimal (140 -> 130 MB)
    - Alpine did have fewer vulnerabilities, but it requires some explicit image tuning to remove libs, but possible if security is more important than a complex Dockerfile
    - The docker-compose.yml file is also updated to remove unnecessary privileges and the containers are in their own docker network
- Marking API endpoints with 'consumes' and 'produces' definitions

Others:

- API path should include `/api/v1` for compatibility
- Might be a company convention, but instead of the Service returning 404, we could use a global exception handler?
- Logging for requests, at API boundary and perhaps even DB layer, could use some traceability
- DTOs are using `Long` for the ID, perhaps would be better as a UUID?
- Better way instead of OrderCustomerDTO/CustomerOrderDTO? And the Mapper classes
- Other lints (PMD, CheckStyle, ErrorProne, etc.)
- Local dev, I would like a containerised script to run the lint/unit/IT checks as a githook before pushing, relying on host for now
- Expose the OpenAPI.yaml?
- Javadoc missing from public methods/classes, should be some more comments/docs
- No UI/landing page or error pages, only JSON endpoints
- Could have made a GitHub release on each publish.yml run, including a changelog

----

# Original

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
