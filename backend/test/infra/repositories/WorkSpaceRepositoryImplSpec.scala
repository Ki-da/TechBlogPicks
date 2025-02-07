package infra.repositories

import domains.bot.Bot.BotId
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import helpers.traits.RepositorySpec
import play.api.libs.json.Json
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import eu.timepit.refined.auto._
import infra.repositoryimpl.WorkSpaceRepositoryImpl
import infra.dto.Tables._

class WorkSpaceRepositoryImplSpec
    extends RepositorySpec[WorkSpaceRepositoryImpl]

class WorkSpaceRepositoryImplSuccessSpec extends WorkSpaceRepositoryImplSpec {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/oauth.v2.access") =>
      Action(Ok(Json.obj("access_token" -> "mock access token")))
    case ("GET", str: String)
        if str.matches("https://slack.com/api/team.info") =>
      Action(Ok(Json.obj("team" -> Json.obj("id" -> "teamId"))))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  "find" when {
    "succeed" should {
      "get work space" in {
        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
          (code, clientId, clientSecret) =>
            val result =
              repository.find(code, clientId, clientSecret).futureValue

            assert(
              result === Some(
                WorkSpace(
                  WorkSpaceId("teamId"),
                  Seq(WorkSpaceToken("mock access token")),
                  Some(code),
                  Seq()
                )
              )
            )
        }
      }
    }
  }

  "add" when {
    "succeed" should {
      "add new data" in {
        forAll(newWorkSpaceGen) { newModel =>
          repository.add(newModel).futureValue

          val result = db.run(WorkSpaces.result).futureValue

          val expected = for {
            token <- newModel.tokens
            botId <- newModel.botIds
          } yield WorkSpacesRow(token.value.value, botId.value.value, newModel.id.value.value)

          assert(result === expected)

          db.run(WorkSpaces.delete).futureValue
        }
      }
    }
  }

  "update" when {
    "succeed" should {
      "delete data" in {
        val beforeAction = DBIO.seq(
          WorkSpaces.forceInsertAll(
            Seq(
              WorkSpacesRow("token1", "bot1", "team1"),
              WorkSpacesRow("token2", "bot2", "team2"),
              WorkSpacesRow("token3", "bot3", "team1")
            )
          )
        )

        val deleteAction = WorkSpaces.delete

        db.run(beforeAction.transactionally).futureValue

        val params     = WorkSpace(
          WorkSpaceId("team1"),
          Seq(WorkSpaceToken("token2"), WorkSpaceToken("token3")),
          None,
          Seq(BotId("bot2"), BotId("bot3"))
        )
        repository.update(params)
        val workSpaces = db.run(WorkSpaces.result).futureValue

        assert(workSpaces.length === 2)
        assert(workSpaces.head.token === "token2")

        db.run(deleteAction).futureValue

      }
    }
  }
}

class WorkSpaceRepositoryImplFailSpec
    extends RepositorySpec[WorkSpaceRepository] {
  "find" when {
    "failed" should {
      "None returned" in {
        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
          (code, clientId, clientSecret) =>
            val result = repository.find(code, clientId, clientSecret)

            whenReady(result, timeout(Span(1, Seconds))) { e =>
              assert(e === None)
            }
        }
      }
    }
  }
}
