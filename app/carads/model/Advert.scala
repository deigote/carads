package carads.model

import java.time.LocalDate

abstract class Advert(id: Option[Int],
                      title: String,
                      fuel: Fuel,
                      price: Int,
                      mileage: Option[Int],
                      firstRegistration: Option[LocalDate])

case class AdvertForUsed(id: Option[Int],
                         title: String,
                         fuel: Fuel,
                         price: Int,
                         mileage: Int,
                         firstRegistration: LocalDate) extends Advert(id, title, fuel, price, Some(mileage), Some(firstRegistration))

case class AdvertForNew(id: Option[Int],
                        title: String,
                        fuel: Fuel,
                        price: Int) extends Advert(id, title, fuel, price, None, None)
