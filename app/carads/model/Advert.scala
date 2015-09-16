package carads.model

import java.time.LocalDate

// TODO: investigate Scala more and rethink model - no auto-getters for constructor params in abstract classes!
abstract class Advert(id: Option[Int],
                      title: String,
                      fuel: Fuel,
                      price: Int,
                      mileage: Option[Int],
                      firstRegistration: Option[LocalDate]) {

  def withId(id: Int): Advert
  def getId(): Option[Int] = id
  def getTitle(): String = title
  def getFuel(): Fuel = fuel
  def getPrice(): Int = price
  def getMileage(): Option[Int] = mileage
  def getFirstRegistration(): Option[LocalDate] = firstRegistration
}

case class AdvertForUsed(id: Option[Int],
                         title: String,
                         fuel: Fuel,
                         price: Int,
                         mileage: Int,
                         firstRegistration: LocalDate) extends Advert(id, title, fuel, price, Some(mileage), Some(firstRegistration)) {

  override def withId(id: Int): AdvertForUsed =
    AdvertForUsed(Some(id), title, fuel, price, mileage, firstRegistration)

}

case class AdvertForNew(id: Option[Int],
                        title: String,
                        fuel: Fuel,
                        price: Int) extends Advert(id, title, fuel, price, None, None) {
  override def withId(id: Int): AdvertForNew =
    AdvertForNew(Some(id), title, fuel, price)
}
