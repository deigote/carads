package carads.services

import java.time.LocalDate

import carads.model._
import org.specs2.mutable._
import play.api.Logger
import play.api.test._

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
      Logger.info("Retrieved adverts after creation " + adverts.toString)

      private val persistedForNew: Advert = adverts(0)
      private val persistedForUsed: Advert = adverts(1)
      nonPersistedForNew.withId(persistedForNew.getId().get) must beEqualTo(persistedForNew)
      nonPersistedForUsed.withId(persistedForUsed.getId().get) must beEqualTo(persistedForUsed)
    }

    "allow to update adverts" in new WithApplication {
      private val nonUpdatedForNew: Advert = repo.create(nonPersistedForNew)
      private val nonUpdatedForUsed: Advert = repo.create(nonPersistedForUsed)

      private val nonPersistedUpdateForNew: AdvertForNew =
        AdvertForNew(None, "Newest car", Gasoline, 16000)
          .withId(nonUpdatedForNew.getId().get)
      private val nonPersistedUpdateForUsed: AdvertForUsed =
        AdvertForUsed(None, "Really used car", Diesel, 10000, 150000, LocalDate.of(2010, 5, 20))
          .withId(nonUpdatedForUsed.getId().get)

      repo.update(nonPersistedUpdateForNew) must beEqualTo(nonPersistedUpdateForNew)
      repo.update(nonPersistedUpdateForUsed) must beEqualTo(nonPersistedUpdateForUsed)

      private val adverts: List[Advert] = repo.list()
      Logger.info("Retrieved adverts after update " + adverts.toString)
      adverts(2) must beEqualTo(nonPersistedUpdateForNew)
      adverts(3) must beEqualTo(nonPersistedUpdateForUsed)
    }

    "allow to get adverts by id" in new WithApplication {
      repo.list().map(_.getId()).foreach { id =>
        val advert: Option[Advert] = repo.get(id)
        advert.isDefined must beTrue
        advert.get.getId() must beEqualTo(id)
      }
    }

    "allow to delete retrieved adverts by id" in new WithApplication {
      def deleteEachByCheckingIsDeleted(adverts: List[Advert]): Unit = {
        if (!adverts.isEmpty) {
          val idToDelete: Int = repo.list().head.getId().get
          repo.delete(idToDelete)
          val advertsAfterDeletion: List[Advert] = repo.list()
          advertsAfterDeletion.find { _.getId().get == idToDelete }.isDefined must beFalse
          deleteEachByCheckingIsDeleted(advertsAfterDeletion)
        }
      }
      deleteEachByCheckingIsDeleted(repo.list())
    }

  }
}

