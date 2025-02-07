package adapters.controllers.post

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import cats.implicits._
import domains.post.Post.PostId
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

final case class DeletePostsBody(ids: Seq[Long])

final case class DeletePostsCommand(ids: Seq[PostId])

trait DeletePostsBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToDeleteCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, DeletePostsCommand]] =
    mapToValueObject[DeletePostsBody, DeletePostsCommand] { body =>
      body.ids
        .map(PostId.create)
        .sequence
        .map(DeletePostsCommand)
        .leftMap(error => BadRequestError(error.errorMessage))
    }
}
