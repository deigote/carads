package carads.services

import java.time.LocalDate
import javax.inject.Singleton

import carads.model._
import play.api.libs.functional.syntax._
import play.api.libs.json._

@Singleton
class AdvertsFormatter extends Format[Advert] {

  private implicit val fuelFormatter: Format[Fuel] = new Format[Fuel] {
    override def reads(json: JsValue): JsResult[Fuel] = {
      json match {
        case JsString(fuel) => fuel match {
          case "Gasoline" => JsSuccess(Gasoline)
          case "Diesel" => JsSuccess(Diesel)
          case _ => jsFuelError("has unknown value '" + fuel + "'")
        }
        case _ => jsFuelError("is not a String.")
      }
    }
    override def writes(fuel: Fuel): JsValue = JsString(fuel.toString())

    private def jsFuelError(concreteError: String): JsError =
      JsError("property 'fuel' " + concreteError + ". Must be either '" + Gasoline.toString + "' or '" + Diesel.toString + "'")
  }

  private val commonFormatBuilder =
  (JsPath \ "id").formatNullable[Int] and
    (JsPath \ "title").format[String] and
    (JsPath \ "fuel").format[Fuel] and
    (JsPath \ "price").format[Int]

  private val adForNewFormat: Format[AdvertForNew] =
    commonFormatBuilder(AdvertForNew.apply, unlift(AdvertForNew.unapply))

  private val adForUsedFormat: Format[AdvertForUsed] = (
    commonFormatBuilder and
      (JsPath \ "mileage").format[Int] and
      (JsPath \ "firstRegistration").format[LocalDate]
  )(AdvertForUsed.apply, unlift(AdvertForUsed.unapply))

  override def writes(ad: Advert): JsValue = {
    ad match {
      case AdvertForNew(_,_,_,_) =>
        adForNewFormat.writes(ad.asInstanceOf[AdvertForNew]).as[JsObject] + ("type" -> Json.toJson("New"))
      case AdvertForUsed(_,_,_,_,_,_) =>
        adForUsedFormat.writes(ad.asInstanceOf[AdvertForUsed]).as[JsObject] + ("type" -> Json.toJson("Used"))
    }
  }

  override def reads(json: JsValue): JsResult[Advert] = {
    json match {
      case JsObject(_) => reads(json.as[JsObject])
      case _ => JsError("Advert must be an object")
    }
  }

  private def reads(json: JsObject): JsResult[Advert] = {
    val adType: JsValue = json.fields.find(field => field._1 == "type").getOrElse("type" -> JsNull)._2
    adType match {
      case JsNull => jsTypeError("is required")
      case JsString(value) => value match {
        case "Used" => adForUsedFormat.reads(json - "type")
        case "New" => adForNewFormat.reads(json - "type")
        case _ => jsTypeError("has invalid value '" + value + "'")
      }
      case _ => jsTypeError("is not a String")
    }
  }

  private def jsTypeError(concreteError: String): JsError =
    JsError("property 'type' " + concreteError + ". Must be either 'Used' or 'New'")

}
