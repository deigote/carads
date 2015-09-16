package carads.controllers

import javax.inject.Inject

import carads.model.Advert
import carads.services.{AdvertsFormatter, AdvertsRepository}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller, Result}

// TODO: check if when using the repo (which is most likely a blocking operation - JDBC) actions must be async
// TODO: investigate a better way of dealing with common exceptions
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
        withCommonExceptionsMapping {
          Logger.info("Updating " + success.get)
          Ok(Json.toJson(advertsRepo.update(success.get.withId(id))))
        }
      case (error: JsError) =>
        BadRequest(JsError.toJson(error))
    }
  }

  def delete(id: Int) = Action {
    withCommonExceptionsMapping {
      Logger.info("Deleting " + id)
      advertsRepo.delete(id)
      NoContent
    }
  }

  def list() = Action { request =>
    Ok(Json.toJson(advertsRepo.list(request.getQueryString("sortBy"))))
  }

  private def withCommonExceptionsMapping(block: => Result): Result = {
    try {
      block
    } catch {
      case noElement: scala.NoSuchElementException => NotFound("")
      case other: Throwable => throw other
    }
  }

}
