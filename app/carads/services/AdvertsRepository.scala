package carads.services

import carads.model.{AdvertForUsed, AdvertForNew, Advert}
import com.google.inject.ImplementedBy

/**
 * Defines the operations required for storing and retrieving Advert using some kind of
 * persistent storage.
 */
@ImplementedBy(classOf[AdvertsJdbcRepository])
trait AdvertsRepository {

  def list(): List[Advert] = list(None)
  def list(sortBy: Option[String]): List[Advert] = list(sortBy.getOrElse("id"))
  def list(sortBy: String): List[Advert]

  def clear(): Unit

  def create(advert: Advert): Advert = advert match {
    case AdvertForNew(_,_,_,_) => create(advert.asInstanceOf[AdvertForNew])
    case AdvertForUsed(_,_,_,_,_,_) => create(advert.asInstanceOf[AdvertForUsed])
  }
  def create(advert: AdvertForNew): Advert
  def create(advert: AdvertForUsed): Advert

  def update(advert: Advert): Advert

  def get(maybeId: Option[Int]): Option[Advert] = maybeId match {
    case Some(id) => get(id)
    case None => None
  }
  def get(id: Int): Option[Advert]

  def delete(id: Int)
}
