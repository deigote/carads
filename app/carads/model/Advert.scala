package carads.model

import java.time.LocalDate

abstract class Advert(id: Int,
                      title: String,
                      fuel: Fuel,
                      price: Int,
                      mileage: Option[Int],
                      firstRegistration: Option[LocalDate])

case class AdvertForUsed(id: Int,
                         title: String,
                         fuel: Fuel,
                         price: Int,
                         mileage: Int,
                         firstRegistration: LocalDate) extends Advert(id, title, fuel, price, Some(mileage), Some(firstRegistration))

case class AdvertForNew(id: Int,
                        title: String,
                        fuel: Fuel,
                        price: Int) extends Advert(id, title, fuel, price, None, None)
