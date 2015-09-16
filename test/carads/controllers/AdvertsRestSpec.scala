package carads.controllers

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.Logger
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class AdvertsRestSpec extends Specification {

  val invalidBodies = List(
    "{invalid: json}",
    """{ "id": "string"}""",
    """{ "id": 1, "fuel": "Gas"}"""
  )

  val validBodies = List(
    JsObject(Map(
      "type" -> JsString("New"),
      "title" -> JsString("a title"),
      "price" -> JsNumber(14000),
      "fuel" -> JsString("Gasoline")
    )),
    JsObject(Map(
      "type" -> JsString("Used"),
      "title" -> JsString("a different title"),
      "price" -> JsNumber(12000),
      "fuel" -> JsString("Diesel"),
      "mileage" -> JsNumber(28000),
      "firstRegistration" -> JsString("2014-03-20")
    ))
  )

  // TODO: Investigate how the following can be used. Without it, interactions between controller and repo cannot be tested
  /*
  val application = new GuiceApplicationBuilder()
    .overrides(bind[AdvertsRepository].to[AdvertsMockedRepository])
    .build
  */

  "Application" should {

    "send unsupported media type when creating an advert with no JSON content type" in new WithApplication() {
      val resp = route(FakeRequest(POST, "/advert")).get
      status(resp) must equalTo(UNSUPPORTED_MEDIA_TYPE)
    }

    "send bad request when creating an advert with no JSON body" in new WithApplication {
      val resp = route(
        FakeRequest(POST, "/advert")
          .withHeaders(("Content-Type", "application/json"))
      ).get
      status(resp) must equalTo(BAD_REQUEST)
    }

    "send bad request when creating an advert with invalid JSON body" in new WithApplication {
      invalidBodies.foreach { invalidBody =>
        val resp = route(
          FakeRequest(POST, "/advert")
            .withHeaders(("Content-Type", "application/json"))
            .withTextBody(invalidBody)
        ).get
        Logger.info("Received after invalid JSON body: " + contentAsString(resp))
        status(resp) must equalTo(BAD_REQUEST)
      }
    }

    "send created along with the created advert when creating an advert with valid JSON body" in new WithApplication {
      validBodies.foreach { validBody =>
        val resp = route(
          FakeRequest(POST, "/advert")
            .withHeaders(("Content-Type", "application/json"))
            .withJsonBody(validBody)
        ).get
        Logger.info("Received after valid JSON body: " + contentAsJson(resp))
        status(resp) must equalTo(CREATED)
        val response: JsObject = Json.parse(contentAsString(resp)).as[JsObject]
        response - "id" must beEqualTo(validBody)
      }
    }
  }
}
