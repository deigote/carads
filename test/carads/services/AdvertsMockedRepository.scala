package carads.services

import carads.model.{AdvertForUsed, AdvertForNew, Advert}
import play.api.Logger

class AdvertsMockedRepository extends AdvertsRepository {

  private var autoIncId: Int = 1

  override def list(sortBy: String): List[Advert] = {
    Logger.info("Invoked list")
    List()
  }

  override def update(advert: Advert): Advert = {
    advert
  }

  override def get(id: Int): Option[Advert] = {
    None
  }

  override def clear(): Unit = {}

  override def delete(id: Int): Unit = {}

  override def create(advert: AdvertForNew): Advert = {
    Logger.info("INVOKED create " + advert)
    autoIncId = autoIncId + 1
    advert.withId(autoIncId)
  }

  override def create(advert: AdvertForUsed): Advert = {
    Logger.info("INVOKED create " + advert)
    autoIncId = autoIncId + 1
    advert.withId(autoIncId)
  }

}
