package carads.services

import java.time.LocalDate

import carads.model._
import org.specs2.mutable._
import play.api.test._

// TODO: use Guide to obtain the configured repository instead of a fixed one
class AdvertsRepositorySpec extends Specification {

  private val repo: AdvertsRepository = new AdvertsJdbcRepository()
  private val nonPersistedForNew: AdvertForNew =
    AdvertForNew(None, "New car", Diesel, 14000)
  private val nonPersistedForUsed: AdvertForUsed =
    AdvertForUsed(None, "Used car", Gasoline, 12000, 50000, LocalDate.of(2014, 6, 21))

  private def isInIdOrder(ads: List[Advert]): Boolean =
    if (ads.isEmpty || ads.size == 1) true
    else ads.head.getId().get < ads(1).getId().get && isInIdOrder(ads.tail)

  "AdvertsRepo" should {

    "allow to clear the repo" in new WithApplication {
      repo.clear()
    }

    "allow to retrieve adverts from empty repo" in new WithApplication {
      repo.list().size must beEqualTo(0)
    }

    "allow to create adverts" in new WithApplication {
      private val persistedForNew: Advert = repo.create(nonPersistedForNew)
      private val persistedForUsed: Advert = repo.create(nonPersistedForUsed)

      persistedForNew.getId().isDefined must beTrue
      persistedForUsed.getId().isDefined must beTrue
      nonPersistedForNew.withId(persistedForNew.getId().get) must beEqualTo(persistedForNew)
      nonPersistedForUsed.withId(persistedForUsed.getId().get) must beEqualTo(persistedForUsed)
    }

    "allow to retrieve previously created adverts" in new WithApplication {
      repo.list().size must beEqualTo(2)
    }

    "retrieved adverts must be in insert (i.e id) order" in new WithApplication {
      isInIdOrder(repo.list()) must beTrue
    }

    "retrieved adverts must be equal to the ones passed to creation with id" in new WithApplication {
      private val adverts: List[Advert] = repo.list()
      private val persistedForNew: Advert = adverts(0)
      private val persistedForUsed: Advert = adverts(1)
      nonPersistedForNew.withId(persistedForNew.getId().get) must beEqualTo(persistedForNew)
      nonPersistedForUsed.withId(persistedForUsed.getId().get) must beEqualTo(persistedForUsed)
    }

  }
}

