package carads.controllers

import carads.model.{AdvertForNew, Advert}
import carads.services.{AdvertsFormatter, AdvertsJdbcRepository, AdvertsRepository}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class AdvertsRestSpec extends Specification {

  private val repo: AdvertsRepository = new AdvertsJdbcRepository()
  private implicit val formatter = new AdvertsFormatter()

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

    "send not found without body when getting a non existent advert" in new WithApplication {
      private val resp = route(FakeRequest(GET, "/advert/" + 30403)).get
      status(resp) must equalTo(NOT_FOUND)
      contentAsString(resp).length must beEqualTo(0)
    }

    "send OK along with a valid body when getting a existent advert" in new WithApplication {
      private val advert: Advert = repo.list().head
      private val resp = route(FakeRequest(GET, "/advert/" + advert.getId().get)).get
      status(resp) must equalTo(OK)
      contentAsJson(resp).validate[Advert].get must beEqualTo(advert)
    }

    "send unsupported media type when updating with wrong media type" in new WithApplication {
      private val resp = route(FakeRequest(PUT, "/advert/" + 30403)).get
      status(resp) must equalTo(UNSUPPORTED_MEDIA_TYPE)
    }

    "send not found without body when updating a non existent advert" in new WithApplication {
      private val advertForNew: Advert = repo.list().find { _.isInstanceOf[AdvertForNew] }.get
      private val updatedAdvertForNew: Advert = AdvertForNew(
        None, "Updated title", advertForNew.getFuel(), advertForNew.getPrice() * 2
      )
      private val resp = route(
        FakeRequest(PUT, "/advert/" + 30403)
          .withHeaders(("Content-Type", "application/json"))
          .withJsonBody(Json.toJson(updatedAdvertForNew))
      ).get
      status(resp) must equalTo(NOT_FOUND)
      contentAsString(resp).length must beEqualTo(0)
    }

    "send OK along with a valid body when updating a existent advert" in new WithApplication {
      private val advertForNew: Advert = repo.list().find { _.isInstanceOf[AdvertForNew] }.get
      private val updatedAdvertForNew: Advert = AdvertForNew(
        advertForNew.getId(), "Updated title", advertForNew.getFuel(), advertForNew.getPrice() * 2
      )
      private val resp = route(
        FakeRequest(PUT, "/advert/" + advertForNew.getId().get)
          .withHeaders(("Content-Type", "application/json"))
          .withJsonBody(Json.toJson(updatedAdvertForNew))
      ).get
      status(resp) must equalTo(OK)
      contentAsJson(resp).validate[Advert].get must beEqualTo(updatedAdvertForNew)
    }

    "send no content after deleting an existent advert, and not found afterwards for the same" in new WithApplication {
      private val advert: Advert = repo.list().head
      private val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/advert/" + advert.getId().get)
      private val firstResp = route(request).get
      private val secondResp = route(request).get

      status(firstResp) must equalTo(NO_CONTENT)
      contentAsString(firstResp).length must beEqualTo(0)

      status(secondResp) must equalTo(NOT_FOUND)
      contentAsString(firstResp).length must beEqualTo(0)
    }

  }
}
