package carads.services

import java.time.LocalDate

import carads.model._
import org.specs2.mutable._
import play.api.Logger
import play.api.libs.json._
import play.api.test._

class AdvertsFormatterSpec extends Specification {

  private val formatter: AdvertsFormatter = new AdvertsFormatter()

  private val commonValidInput = Map(
    "id" -> JsNumber(1),
    "title" -> JsString("a title"),
    "price" -> JsNumber(14000)
  )
  private val commonInvalidInput = Map(
    "id" -> JsString("New"),
    "type" -> JsString("WhateverType"),
    "title" -> JsNumber(1),
    "fuel" -> JsString("WhateverFuel"),
    "price" -> JsString("Gasoline")
  )

  private val validInputForAdForNew = commonValidInput ++ Map(
    "type" -> JsString("New"), "fuel" -> JsString("Gasoline")
  )
  private val invalidInputForAdForNew = commonInvalidInput
  private val newAdFields = validInputForAdForNew.keySet
  private val validJsonForAdForNew = JsObject(validInputForAdForNew)
  
  private val validInputForAdForUsed = commonValidInput ++ Map(
    "type" -> JsString("Used"),
    "fuel" -> JsString("Diesel"),
    "mileage" -> JsNumber(28000),
    "firstRegistration" -> JsString("2014-03-20")
  )
  private val invalidInputForAdForUsed = commonInvalidInput ++ Map(
    "mileage" -> JsString("2014-03-20"),
    "firstRegistration" -> JsString("chachi")
  )
  private val usedAdFields = validInputForAdForUsed.keySet
  private val validJsonForAdForUsed = JsObject(validInputForAdForUsed)
  
  private def parseValue(value: JsValue): Option[Advert] =
    formatter.reads(value) match {
      case JsError(error) =>
        Logger.info("Error parsing: " + error.toString())
        return None
      case JsSuccess(ad, _) =>
        Logger.info("Successful parsing: " + ad.toString)
        return Some(ad)
    }

  private def parseResultFails(value: JsValue): Boolean =
    parseValue(value) match {
      case None => true
      case Some(_) => false
    }

  "AdvertsFormatter" should {

    "fail when reading something that is not an object" in new WithApplication {
      parseResultFails(JsString("ad")) must beTrue
    }

    newAdFields.foreach { prop =>
      "fail when reading a new advert that misses the mandatory field " + prop in new WithApplication {
        parseResultFails(validJsonForAdForNew - prop) must beTrue
      }
    }

    usedAdFields.foreach { prop =>
      "fail when reading a used advert that misses the mandatory field " + prop in new WithApplication {
        parseResultFails(validJsonForAdForUsed - prop) must beTrue
      }
    }

    invalidInputForAdForNew.foreach { propWithInvalidValue =>
      "fail when reading a new advert with the property " + propWithInvalidValue + ", whose type is wrong" in new WithApplication {
        parseResultFails(
          validJsonForAdForNew - propWithInvalidValue._1 + propWithInvalidValue
        ) must beTrue
      }
    }

    invalidInputForAdForUsed.foreach { propWithInvalidValue =>
      "fail when reading a used advert with the property " + propWithInvalidValue + ", whose type is wrong" in new WithApplication {
        parseResultFails(
          validJsonForAdForUsed - propWithInvalidValue._1 + propWithInvalidValue
        ) must beTrue
      }
    }

    "succeed when reading a valid new advert" in new WithApplication {
      private val maybeAdvert: Option[Advert] = parseValue(validJsonForAdForNew)
      maybeAdvert.isDefined must beTrue
      maybeAdvert.getOrElse(null).isInstanceOf[AdvertForNew] must beTrue
      private val advert: AdvertForNew = maybeAdvert.getOrElse(null).asInstanceOf[AdvertForNew]
      advert.id must equalTo(1)
      advert.title must equalTo("a title")
      advert.fuel must equalTo(Gasoline)
      advert.price must equalTo(14000)
    }

    "succeed when reading a valid used advert" in new WithApplication {
      private val maybeAdvert: Option[Advert] = parseValue(validJsonForAdForUsed)
      maybeAdvert.isDefined must beTrue
      maybeAdvert.getOrElse(null).isInstanceOf[AdvertForUsed] must beTrue
      private val advert: AdvertForUsed = maybeAdvert.getOrElse(null).asInstanceOf[AdvertForUsed]
      advert.id must equalTo(1)
      advert.title must equalTo("a title")
      advert.fuel must equalTo(Diesel)
      advert.price must equalTo(14000)
      advert.firstRegistration must equalTo(LocalDate.of(2014, 3, 20))
      advert.mileage must equalTo(28000)
    }

    "succeed when reading a valid advert with extra fields" in new WithApplication {
      parseResultFails(validJsonForAdForNew + ("something", JsString("else"))) must beFalse
    }
  }

}
