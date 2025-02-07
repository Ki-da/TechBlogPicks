package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.bot.Bot._
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._
import cats.implicits._
import domains.DomainError

import scala.concurrent.ExecutionContext

final case class UpdateClientInfoBody(
  clientId: Option[String],
  clientSecret: Option[String]
)

final case class UpdateClientInfoCommand(
  clientId: Option[BotClientId],
  clientSecret: Option[BotClientSecret]
)

trait UpdateClientInfoBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToUpdateClientInfoCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, UpdateClientInfoCommand]] =
    mapToValueObject[UpdateClientInfoBody, UpdateClientInfoCommand] { body =>
      (
        body.clientId.traverse(id => BotClientId.create(id).toValidatedNec),
        body.clientSecret.traverse(secret =>
          BotClientSecret.create(secret).toValidatedNec
        )
      ).mapN(UpdateClientInfoCommand.apply)
        .toEither
        .leftMap(errors =>
          BadRequestError(
            errors
              .foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
          )
        )
    }
}
