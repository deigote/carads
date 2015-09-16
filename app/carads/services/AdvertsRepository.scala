package carads.services

import carads.model.Advert
import com.google.inject.ImplementedBy

/**
 * Defines the operations required for storing and retrieving Advert using some kind of
 * persistent storage.
 */
// TODO: support range when listing adverts
@ImplementedBy(classOf[AdvertsJdbcRepository])
trait AdvertsRepository {

  def list(): List[Advert] = list(None)
  def list(sortBy: Option[String]): List[Advert] = list(sortBy.getOrElse("id"))
  def list(sortBy: String): List[Advert]
  def clear(): Unit
}