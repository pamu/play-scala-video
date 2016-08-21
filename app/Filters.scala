import com.google.inject.{Inject, Singleton}
import filters.LoggingFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

@Singleton
class Filters @Inject()(loggingFilter: LoggingFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = {
    Seq(loggingFilter)
  }
}
