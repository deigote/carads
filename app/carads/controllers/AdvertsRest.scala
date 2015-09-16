package carads.controllers

import java.util.NoSuchElementException
import javax.inject.Inject

import carads.model.Advert
import carads.services.{AdvertsFormatter, AdvertsRepository}
import play.api.Logger
import play.api.mvc.{Controller, Action}
import play.api.libs.json.{JsError, JsSuccess, Json}

// TODO: check if when using the repo (which is most likely a blocking operation - JDBC) actions must be async
class AdvertsRest @Inject()(advertsRepo: AdvertsRepository) (implicit advertsFormatter: AdvertsFormatter) extends Controller {

  def create = Action(parse.json) { request =>
    request.body.validate[Advert] match {
      case (success: JsSuccess[Advert]) =>
        Logger.info("Creating " + success.get)
        Created(Json.toJson(advertsRepo.create(success.get)))
      case (error: JsError) =>
        BadRequest(JsError.toJson(error))
    }
  }

  def show(id: Int) = Action {
    advertsRepo.get(id) match {
      case Some(advert) => Ok(Json.toJson(advert))
      case None => NotFound("")
    }
  }

  def update(id: Int) = Action(parse.json) { request =>
    request.body.validate[Advert] match {
      case (success: JsSuccess[Advert]) =>
        try {
          Logger.info("Updating " + success.get)
          Ok(Json.toJson(advertsRepo.update(success.get.withId(id))))
        } catch {
          case noElement: NoSuchElementException => NotFound("")
          case other: Throwable => throw other
        }
      case (error: JsError) =>
        BadRequest(JsError.toJson(error))
    }
  }

}
