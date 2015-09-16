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
