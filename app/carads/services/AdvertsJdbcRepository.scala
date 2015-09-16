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
    DB.withConnection { conn =>
      conn.prepareStatement("delete from advert").execute()
    }
  }

  override def create(advert: AdvertForNew): Advert = {
    withAdvertCreation(
      advert,
      "insert into advert (type, title, fuel, price) values (?, ?, ?, ?)",
      { insert: PreparedStatement =>
        insert.setString(1, "New")
        insert.setString(2, advert.title)
        insert.setString(3, advert.fuel.toString())
        insert.setInt(4, advert.price)
        insert
      }
    )
  }

  override def create(advert: AdvertForUsed): Advert = {
    withAdvertCreation(
    advert,
    "insert into advert (type, title, fuel, price, mileage, firstRegistration) values (?, ?, ?, ?, ?, ?)",
    { insert: PreparedStatement =>
      insert.setString(1, "Used")
      insert.setString(2, advert.title)
      insert.setString(3, advert.fuel.toString())
      insert.setInt(4, advert.price)
      insert.setInt(5, advert.mileage)
      insert.setDate(6, new Date(toMillis(advert.firstRegistration)))
      insert
    }
    )
  }

  override def update(advert: Advert): Advert = {
    assert(advert.getId().isDefined)
    DB.withConnection() { conn =>
      val update: PreparedStatement = conn.prepareStatement(
        "update advert set title = ?, fuel = ?, price = ?, mileage = ?, firstRegistration = ? where id = ?"
      )
      update.setString(1, advert.getTitle())
      update.setString(2, advert.getFuel().toString())
      update.setInt(3, advert.getPrice())
      advert.getMileage() match {
        case Some(mileage) => update.setInt(4, mileage)
        case None => update.setNull(4, Types.INTEGER)
      }
      advert.getFirstRegistration() match {
        case Some(date) => update.setDate(5, new Date(toMillis(date)))
        case None => update.setNull(5, Types.DATE)
      }
      update.setInt(6, advert.getId().get)
      if (update.executeUpdate() == 0) throw new SQLException("Advert update failed: no rows affected")
      else return advert
    }
  }

  private def withAdvertCreation(advertToCreate: Advert,
                                 insertSql: String,
                                 paramsSetter: PreparedStatement => PreparedStatement): Advert = {
    DB.withConnection() { conn =>
      val insert: PreparedStatement =
        paramsSetter(conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
      val affectedRows: Int = insert.executeUpdate()
      val generatedKeys: ResultSet = insert.getGeneratedKeys()
      if (affectedRows == 0) throw new SQLException("Advert creation failed: no rows affected")
      else if (!generatedKeys.next()) throw new SQLException("Advert creation failed: no ID returned")
      else return advertToCreate.withId(generatedKeys.getInt(1))
    }
  }

  override def get(id: Int): Option[Advert] = {
    DB.withConnection() { conn =>
      val get: PreparedStatement =
        conn.prepareStatement("select id, type, title, fuel, price, mileage, firstRegistration from advert where id = ?")
      get.setInt(1, id)
      val adverts = consumeResultSet(get.executeQuery())
      if (adverts.size > 1) throw new IllegalStateException("Database does not correspond to current app: found more than one advert with id " + id)
      else if (adverts.size == 1) Some(adverts.head)
      else None
    }
  }

  override def delete(id: Int): Unit = {
    DB.withConnection() { conn =>
      val delete: PreparedStatement = conn.prepareStatement("delete from advert where id = ?")
      delete.setInt(1, id)
      if (delete.executeUpdate() == 0) throw new SQLException("Advert deletion failed: no rows affected")
    }
  }

  override def list(sortBy: String): List[Advert] = {
    DB.withConnection() { conn =>
      val list: PreparedStatement =
        conn.prepareStatement("select id, type, title, fuel, price, mileage, firstRegistration from advert order by ?")
      list.setString(1, sortBy)
      consumeResultSet(list.executeQuery())
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
