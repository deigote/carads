# Car Ads - A REST repo for storing car adverts

### About Git

To make writing commit messages easier, please read the first line prepended with *After this commit...*, e.g. for a message as *Adverts repo allows creation* should be read as *After this commit, adverts repo allows creation*. 

Naturally this only applies to the title, not the extended description, if any.

### Configuring the project

##### Scala, SBT, Activator

The project has been created using TypeSafe Activator with version _sbt launcher version 0.13.8_. It uses Play 2.4 and Scala 2.11.

##### MySQL

For the first version of the project, a JDBC-based storage has been chosen. MySQL seems to have good support (attempts to use SQLite were not very promising). Configuring it involves:

 1. Install MySQL 5.6
 2. Run the script `setup.sql` with the MySQL root user: `mysql -u root -p$MYSQL_ROOT_PWD < setup.sql`

It will create a user `carads` with access to the database `carads`, and then source the script `schema.sql`, which creates the required tables.

Note: Being completely new to Scala and Play, choosing something I knew for the storage seemed like the best choice. Nevertheless, the application design allows to easily switch the persistent repository implementation.

### Model

##### Advert

Abstract class with the following parameters:

* **id** (_required_): **int** or **guid**, choose whatever is more convenient for you;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_
* **fuel** (_required_): **fuel**
* **price** (_required_): **integer**
* **new** (_required_): **boolean**, indicates if car is new or used
* **mileage** (_optional_): **integer**
* **first registration** (_optional_): **date** without time.

The following classes extend it:

* **AdvertForUsed**: forces to pass a **mileage** and **first registration** in addition to the *Advert* required fields.
* **AdvertForNew**: doesn't allow a **mileage** and **first registration** to be passed, initialising them as *None* instead.

Scala seems to have multiple choices when it comes to model something like this. When in doubt, favor *composition over inheritance*, except for real hierarchies. This one seems to be one.

Without having further experience with the language, seems to me that the chosen approach is the most flexible one - a top level abstract class that has all the properties, wrapped by an *Option* when required, extended by case classes that forces the option properties to be present or not.

This way, most of the code can deal with the top level class without worrying about its implementation details. 

##### Fuel

Abstract class with no parameters and the following case objects extending it:

* **Gasoline**
* **Diesel**

Note: Both *enumeration* and *case classes* where considered for Fuel. Seems that [enumeration has more problems](http://underscore.io/blog/posts/2014/09/03/enumerations.html), but case classes with no parameters are deprecated. Using *case objects* seems like [a good compromise](http://www.quora.com/Whats-the-difference-between-case-class-and-case-object-in-Scala) instead.

Switching between one or the other doesn't look very painful though (see [this SO answer](http://stackoverflow.com/questions/1898932/case-objects-vs-enumerations-in-scala#answer-1899887) for some *case class ala enum* helper methods).

### REST interface

The REST API is based on the resource advert, available as as the root of the context path (`BASE_URL/advert`). It uses the appropriate HTTP verbs depending on the operation type and its safeness and idempotency. Thus, `GET` is used for show and list, whereas `POST` is reserved for creations, `PUT` for updates and `DELETE` for deletions.

When an `id` needs to be specified, it must be appended to the resource URL. Adverts are represented as `JSON` both in responses and request bodies (`PATCH` is not implemented, so updates must fully include the resource). Other parameters, such as `sortBy` to specify a sorting criteria when listing, must be included in the query string (other possibilities such as headers seemed less appropriate).

##### Swagger spec

The REST API is described in detail using  [Swagger](http://swagger.io). The source in YAML format is part of the project with the name `swagger.yaml`. It can be examined in a developer's friendly GUI using the [Swagger UI](http://petstore.swagger.io/?url=https://carads-repo.herokuapp.com/assets/swagger.yaml), or copy-pasted to the [Swagger editor](http://editor.swagger.io/) for assisted edition.

### Tests

##### Unit / Integration

Due to the limited amount of time, tests are not as comprehensive as they should. There are 3 parts being tested:

 - JSON parsing and serialisation (through `AdvertsFormatterSpec`).
 - Adverts storage and retrieval (through `AdvertsRepositorySpec`).
 - REST API (through `AdvertsRestSpec`).

The first two are unit tests in the sense that they don't interact with other parts of the application - they only do so with third party components like JDBC drivers or JSON libs, which we assume correct.

The latter is more an integration test, though it focuses on the HTTP part (correct response bodies and status, etc). Still, it uses the previous 2 components and depends on them being correct. Mocking them would be more correct, but due to the lack of experience in the framework it has being discarded for the time being.

##### Functional

A next step would be to create pure functional tests. An idea for that would be to create a separate project that leverages the [Swagger ability to generate client-side code](http://swagger.io/swagger-codegen/).

This external project would use that client code to operate against a test environment. That way, both the application and the Swagger spec would be tested, ensuring one corresponds to the other accurately.

One clear benefit is that the tests would have to be truly functional. In addition to that, the technology could be other than the one used for the server (Gradle + Spock + generated client using Retrofit is my current favourite).

Another benefit would be that the client could be distributed to API consumers with the certainty that it works as expected.

##### Postman

A [Postman](https://www.getpostman.com/) collection (with the corresponding environment) is available in the `postman` directory. Is not really a test, but more a utility for the developer to quickly test the existing APIs. 

Still, if the whole collection is ran in the defined order ([newman](https://github.com/postmanlabs/newman) is ideal for that) against a clean database it tests some aspects of the API, and invokes all existing operations

### Deployment

##### Preproduction

A deployed version is available in [Car Ads on Heroku](https://carads-repo.herokuapp.com), using the free tier (only suitable for pet projects). Only Git is needed for the deployment, and of course, the appropriate Heroku permissions, MySQL ad-on and config variables.