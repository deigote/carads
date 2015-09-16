package carads.model

import java.time.LocalDate

abstract class Advert(id: Option[Int],
                      title: String,
                      fuel: Fuel,
                      price: Int,
                      mileage: Option[Int],
                      firstRegistration: Option[LocalDate]) {
  def withId(id: Int): Advert
  def getId(): Option[Int] = id
}

case class AdvertForUsed(id: Option[Int],
                         title: String,
                         fuel: Fuel,
                         price: Int,
                         mileage: Int,
                         firstRegistration: LocalDate) extends Advert(id, title, fuel, price, Some(mileage), Some(firstRegistration)) {

  override def withId(id: Int): Advert =
    AdvertForUsed(Some(id), title, fuel, price, mileage, firstRegistration)

}

case class AdvertForNew(id: Option[Int],
                        title: String,
                        fuel: Fuel,
                        price: Int) extends Advert(id, title, fuel, price, None, None) {
  override def withId(id: Int): Advert =
    AdvertForNew(Some(id), title, fuel, price)
}
