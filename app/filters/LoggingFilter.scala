package filters

import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoggingFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    Logger.info(s"""logging: ${rh.toString()}""")
    f(rh)
  }

}
