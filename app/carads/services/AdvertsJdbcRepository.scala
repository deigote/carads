package carads.services

import java.sql.{PreparedStatement, ResultSet}
import java.time.{Instant, LocalDate, ZoneId}
import javax.inject.{Inject, Singleton}
import carads.model._
import play.api.Play.current
import play.api.db._

// TODO: move from raw JDBC to Slick, Anorm or any other Play abstraction. JDBC is so 20st century :)
// TODO: move type constants to a better way - enum, case objs?
// TODO: investigate a more functional way to define the exit condition in RS consumption
@Singleton
class AdvertsJdbcRepository @Inject()() extends AdvertsRepository {

  override def clear(): Unit = {
    DB.withConnection() { conn =>
      conn.prepareStatement("delete from advert").execute()
    }
  }

  override def list(sortBy: String): List[Advert] = {
    DB.withConnection() { conn =>
      val prepareStatement: PreparedStatement =
        conn.prepareStatement("select id, title, fuel, price, mileage, firstRegistration from advert order by ?")
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
        resultSet.getInt("id"),
        resultSet.getString("title"),
        fuelFrom(resultSet.getString("fuel")),
        resultSet.getInt("price"),
        resultSet.getInt("mileage"),
        toLocalDate(resultSet.getDate("firstRegistration").getTime())
      )
    else if (advertType == "New")
      AdvertForNew(
        resultSet.getInt("id"),
        resultSet.getString("title"),
        fuelFrom(resultSet.getString("fuel")),
        resultSet.getInt("price")
      )
    else throw new IllegalStateException("Database does not correspond to current app: found " + advertType + " for type column")
  }


  private def toLocalDate(dateInMillis: Long): LocalDate =
    Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

  private def fuelFrom(fuel: String): Fuel =
    if (fuel == Gasoline.toString()) Gasoline
    else if (fuel == Diesel.toString()) Diesel
    else throw new IllegalStateException("Database does not correspond to current app: found " + fuel + " for fuel column")

}
