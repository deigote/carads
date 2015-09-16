package carads.services

import java.sql._
import java.time.{Instant, LocalDate, ZoneId}
import javax.inject.{Inject, Singleton}
import carads.model._
import play.api.Play.current
import play.api.db._

// TODO: move from raw JDBC to Slick, Anorm or any other Play abstraction. JDBC is so 20st century :)
// TODO: move type constants to a better way - enum, case objs?
// TODO: investigate a more functional way to define the exit condition in RS consumption
// TODO: ensure translations between LocalDate and java.sql.Date are correct
@Singleton
class AdvertsJdbcRepository @Inject()() extends AdvertsRepository {

  override def clear(): Unit = {
    DB.withConnection() { conn =>
      conn.prepareStatement("delete from advert").execute()
    }
  }

  override def create(advert: AdvertForNew): Advert = {
    withAdvertCreation(
      advert,
      "insert into advert (type, title, fuel, price) values (?, ?, ?, ?)",
      { insertStatement: PreparedStatement =>
        insertStatement.setString(1, "New")
        insertStatement.setString(2, advert.title)
        insertStatement.setString(3, advert.fuel.toString())
        insertStatement.setInt(4, advert.price)
        insertStatement
      }
    )
  }

  override def create(advert: AdvertForUsed): Advert = {
    withAdvertCreation(
    advert,
    "insert into advert (type, title, fuel, price, mileage, firstRegistration) values (?, ?, ?, ?, ?, ?)",
    { insertStatement: PreparedStatement =>
      insertStatement.setString(1, "Used")
      insertStatement.setString(2, advert.title)
      insertStatement.setString(3, advert.fuel.toString())
      insertStatement.setInt(4, advert.price)
      insertStatement.setInt(5, advert.mileage)
      insertStatement.setDate(6, new Date(toMillis(advert.firstRegistration)))
      insertStatement
    }
    )
  }

  private def withAdvertCreation(advertToCreate: Advert,
                                 insertSql: String,
                                 paramsSetter: PreparedStatement => PreparedStatement): Advert = {
    DB.withConnection() { conn =>
      val prepareStatement: PreparedStatement =
        paramsSetter(conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
      val affectedRows: Int = prepareStatement.executeUpdate()
      val generatedKeys: ResultSet = prepareStatement.getGeneratedKeys()
      if (affectedRows == 0) throw new SQLException("Advert creation failed: no rows affected")
      else if (!generatedKeys.next()) throw new SQLException("Advert creation failed: no ID returned")
      else return advertToCreate.withId(generatedKeys.getInt(1))
    }
  }

  override def list(sortBy: String): List[Advert] = {
    DB.withConnection() { conn =>
      val prepareStatement: PreparedStatement =
        conn.prepareStatement("select id, type, title, fuel, price, mileage, firstRegistration from advert order by ?")
      prepareStatement.setString(1, sortBy)
      consumeResultSet(prepareStatement.executeQuery())
    }
  }

  private def consumeResultSet(resultSet: ResultSet): List[Advert] =
    if (!resultSet.next()) List()
    else createAdvertForCurrentRow(resultSet)::consumeResultSet(resultSet)

  private def createAdvertForCurrentRow(resultSet: ResultSet): Advert = {
    val advertType: String = resultSet.getString("type")
    if (advertType == "Used")
      AdvertForUsed(
        Some(resultSet.getInt("id")),
        resultSet.getString("title"),
        fuelFrom(resultSet.getString("fuel")),
        resultSet.getInt("price"),
        resultSet.getInt("mileage"),
        toLocalDate(resultSet.getDate("firstRegistration").getTime())
      )
    else if (advertType == "New")
      AdvertForNew(
        Some(resultSet.getInt("id")),
        resultSet.getString("title"),
        fuelFrom(resultSet.getString("fuel")),
        resultSet.getInt("price")
      )
    else throw new IllegalStateException("Database does not correspond to current app: found " + advertType + " for type column")
  }

  private def toLocalDate(dateInMillis: Long): LocalDate =
    Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

  private def toMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

  private def fuelFrom(fuel: String): Fuel =
    if (fuel == Gasoline.toString()) Gasoline
    else if (fuel == Diesel.toString()) Diesel
    else throw new IllegalStateException("Database does not correspond to current app: found " + fuel + " for fuel column")

}
