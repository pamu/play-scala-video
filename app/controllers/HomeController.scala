package controllers

import common._
import java.io.{File, FileInputStream}
import javax.inject._

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.apache.commons.io.{FileUtils, FilenameUtils}
import play.api.Logger
import play.api.http.HttpEntity
import play.api.libs.MimeTypes
import play.api.libs.iteratee.Enumerator
import play.api.libs.streams.Streams
import play.api.mvc._
import services.models.MediaId
import services.service_modules.MediaLocatorService

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class HomeController @Inject()(mediaLocationService: MediaLocatorService) extends Controller {


  def index = Action {
    Ok(views.html.index("Media Service"))
  }

  def media(id: String) = Action { req =>

    Logger.info(req.headers.toMap.mkString("\n"))

    mediaLocationService.getPath(MediaId(id)).map { path =>
      val file = path.toFile
      val rangeHeaderOpt = req.headers.get(RANGE)
      rangeHeaderOpt.map { range =>
        val strs = range.substring("bytes=".length).split("-")
        if (strs.length == 1) {
          val start = strs.head.toLong
          val length = file.length() - 1L
          partialContentHelper(file, start, length, Some(1024 * 16))
        } else {
          val start = strs.head.toLong
          val length = strs.tail.head.toLong
          partialContentHelper(file, start, length)
        }
      }.getOrElse {
        Ok.sendFile(file)
      }
    }.getOrElse {
      InternalServerError
    }

  }


  def partialContentHelper(file: File, start: Long, length: Long, optChunkSize: Option[Int] = None) = {

    val fis = new FileInputStream(file)
    fis.skip(start)

    val byteStringEnumerator = optChunkSize.map { chunkSize =>
      Enumerator.fromStream(fis, chunkSize)
    }.getOrElse {
      Enumerator.fromStream(fis)
    }.map(ByteString.fromArray).onDoneEnumerating {
      Try {
        Logger.warn("file closed")
        fis.close()
      }
    }

    val mediaSource = Source.fromPublisher(Streams.enumeratorToPublisher(byteStringEnumerator))

    val partialContent = PartialContent.sendEntity(HttpEntity.Streamed(mediaSource, None, None)).withHeaders(
      CONTENT_TYPE -> MimeTypes.forExtension(FilenameUtils.getExtension(file.getName).trim).get,
      CONTENT_LENGTH -> ((length - start) + 1).toString,
      CONTENT_RANGE -> s"bytes $start-$length/${file.length()}",
      ACCEPT_RANGES -> "bytes",
      CONNECTION -> "keep-alive")

    Logger.warn(partialContent.header.headers.mkString("\n"))

    partialContent
  }

  def fileUpload = Action.async(parse.multipartFormData) { req =>
    Future {
      req.body.files.map { fileData =>
        Future {
          scala.concurrent.blocking {
            Try {
              FileUtils.moveFile(fileData.ref.file, new File(MediaFolder, fileData.filename))
            } match {
              case Failure(exception) => Logger.info(s"""exception ocurred ${exception.getStackTrace.mkString("\n")}""")
              case Success(value) => Logger.info(s"""moving files successful.""")
            }
          }
        }
      }
    }.map { result =>
      Ok("success")
    }.recover { case throwable =>
      Ok("failure")
    }
  }

}
