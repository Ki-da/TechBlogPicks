package helpers.traits

import com.typesafe.config.ConfigFactory
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder

trait HasApplication {
  val builder: GuiceApplicationBuilder = GuiceApplicationBuilder(configuration =
    Configuration(ConfigFactory.load("application.test.conf"))
  )

  val app: Application = builder.build()
}
