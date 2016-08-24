package services.service_modules

import java.nio.file.Path

import com.google.inject.{ImplementedBy, Singleton}
import services.models.MediaId

import scala.util.Try

@ImplementedBy(classOf[MediaLocatorServiceImpl])
trait MediaLocatorService {
  def getPath(id: MediaId): Try[Path]
}

@Singleton
class MediaLocatorServiceImpl extends MediaLocatorService {
  override def getPath(id: MediaId): Try[Path] = ???
}
