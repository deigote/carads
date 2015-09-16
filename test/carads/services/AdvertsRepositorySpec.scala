package carads.services

import org.specs2.mutable._
import play.api.test._

// TODO: use Guide to obtain the configured repository instead of a fixed one
class AdvertsRepositorySpec extends Specification {

  private val repo: AdvertsRepository = new AdvertsJdbcRepository()


  "AdvertsRepo" should {

    "allow to clear the repo" in new WithApplication {
      repo.clear()
    }

    "allow to retrieve adverts from empty repo" in new WithApplication {
      repo.list().size must beEqualTo(0)
    }
  }
}

