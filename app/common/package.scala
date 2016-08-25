import java.io.File

import play.api.Logger

import scala.util.Try

package object common {
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  val MediaFolder = new File(s"""${sys.env.get("HOME").get}/media_folder""")
  if (! MediaFolder.exists()) {
    Try(MediaFolder.mkdir()).map { result =>
      if (result) {
        Logger.info("media folder created")
      } else {
        Logger.info("media folder creation failed.")
      }
    }.recover { case th =>
      th.printStackTrace()
      Logger.info("media folder creation failed.")
    }
  }
}
