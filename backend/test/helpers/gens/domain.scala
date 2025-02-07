package helpers.gens

import domains.workspace.WorkSpace
import domains.post.Post._
import domains.workspace.WorkSpace._
import domains.bot.Bot
import domains.bot.Bot._
import domains.post.Post.PostId
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._
import domain._
import domains.post.Post
import cats.syntax.option._

object domain extends DomainGen

trait DomainGen extends WorkSpaceGen with BotGen with PostGen

trait WorkSpaceGen {
  val accessTokenGen: Gen[WorkSpaceToken] =
    stringRefinedNonEmptyGen.map(WorkSpaceToken(_))

  val temporaryOauthCodeGen: Gen[WorkSpaceTemporaryOauthCode] =
    stringRefinedNonEmptyGen.map(WorkSpaceTemporaryOauthCode(_))

  val workSpaceIdGen: Gen[WorkSpaceId] =
    stringRefinedNonEmptyGen.map(WorkSpaceId(_))

  val workSpaceGen: Gen[WorkSpace] = for {
    id                 <- workSpaceIdGen
    accessTokens       <- Gen.listOf(accessTokenGen)
    temporaryOauthCode <- Gen.option(temporaryOauthCodeGen)
    botIds             <- Gen.listOf(botIdGen)
  } yield WorkSpace(id, accessTokens, temporaryOauthCode, botIds)

  val newWorkSpaceGen: Gen[WorkSpace] = for {
    workSpace   <- workSpaceGen
    accessToken <- accessTokenGen
    botId       <- botIdGen
  } yield workSpace.copy(tokens = Seq(accessToken), botIds = Seq(botId))
}

trait PostGen {
  val postIdGen: Gen[PostId] = longRefinedPositiveGen.map(PostId(_))

  val postUrlGen: Gen[PostUrl] = stringRefinedUrlGen.map(PostUrl(_))

  val postTitleGen: Gen[PostTitle] = stringRefinedNonEmptyGen.map(PostTitle(_))

  val postAuthorGen: Gen[PostAuthor] =
    stringRefinedNonEmptyGen.map(PostAuthor(_))

  val postPostedAtGen: Gen[PostPostedAt] =
    longRefinedPositiveGen.map(PostPostedAt(_))

  val postGen: Gen[Post] = for {
    id       <- postIdGen
    url      <- postUrlGen
    title    <- postTitleGen
    author   <- postAuthorGen
    postedAt <- postPostedAtGen
  } yield Post(id.some, url, title, author, postedAt)
}

trait BotGen {

  val botIdGen: Gen[BotId] = stringRefinedNonEmptyGen.map(BotId(_))

  val botNameGen: Gen[BotName] = stringRefinedNonEmptyGen.map(BotName(_))

  val accessTokensGen: Gen[Seq[WorkSpaceToken]] =
    Gen.listOf(domain.accessTokenGen)

  val botClientIdGen: Gen[BotClientId] =
    stringRefinedNonEmptyGen.map(BotClientId(_))

  val botClientSecretGen: Gen[BotClientSecret] =
    stringRefinedNonEmptyGen.map(BotClientSecret(_))

  val botGen: Gen[Bot] = for {
    botId        <- botIdGen
    botName      <- botNameGen
    accessTokens <- accessTokensGen
    posts        <- Gen.listOf(postIdGen)
    clientId     <- Gen.option(botClientIdGen)
    clientSecret <- Gen.option(botClientSecretGen)
  } yield Bot(botId, botName, accessTokens, posts, clientId, clientSecret)

  val nonOptionBotGen: Gen[Bot] =
    botGen.suchThat(bot => bot.clientId.isDefined && bot.clientSecret.isDefined)
}
