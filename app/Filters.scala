import com.google.inject.{Inject, Singleton}
import filters.LoggingFilter
import play.api.http.DefaultHttpFilters

@Singleton
class Filters @Inject()(loggingFilter: LoggingFilter) extends DefaultHttpFilters {}
