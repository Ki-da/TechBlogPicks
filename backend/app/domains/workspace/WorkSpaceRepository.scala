package domains.workspace

import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceTemporaryOauthCode}
import domains.bot.Bot.{BotClientId, BotClientSecret, BotId}

import scala.concurrent.Future

trait WorkSpaceRepository {
  def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: BotClientId,
    clientSecret: BotClientSecret
  ): Future[Option[WorkSpace]]

  def add(model: WorkSpace): Future[Unit]
  def update(model: WorkSpace): Future[Unit]
  def find(id: WorkSpaceId): Future[Option[WorkSpace]]
  def find(id: WorkSpaceId, botId: BotId): Future[Option[WorkSpace]]
}
