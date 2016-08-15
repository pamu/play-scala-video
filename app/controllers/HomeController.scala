package controllers

import java.io.{File, FileInputStream}
import javax.inject._

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.Logger
import play.api.http.HttpEntity
import play.api.libs.MimeTypes
import play.api.libs.iteratee.{Enumeratee, Enumerator, Iteratee}
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@Singleton
class HomeController @Inject() extends Controller {


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def media = Action { req =>

    Logger.info(req.headers.toMap.mkString("\n"))

    val file = new File("/Users/pnagarjuna/Downloads/passenger.mp4")
    val rangeHeaderOpt = req.headers.get(RANGE)
    rangeHeaderOpt.map { range =>
      val strs = range.substring("bytes=".length).split("-")
      if (strs.length == 1) {
        val start = strs.head.toLong
        val length = file.length() - 1L
        partialContentHelper(file, start, length)
      } else {
        val start = strs.head.toLong
        val length = strs.tail.head.toLong
        partialContentHelper(file, start, length)
      }
    }.getOrElse {
      Ok.sendFile(file)
    }
  }


  def partialContentHelper(file: File, start: Long, length: Long) = {
    val fis = new FileInputStream(file)
    fis.skip(start)
    val byteStringEnumerator = Enumerator.fromStream(fis).map(ByteString.fromArray).onDoneEnumerating(Try {
      Logger.warn("file closed")
      fis.close()
    })
    val mediaSource = Source.fromPublisher(Streams.enumeratorToPublisher(byteStringEnumerator))
    PartialContent.sendEntity(HttpEntity.Streamed(mediaSource, None, None)).withHeaders(
      CONTENT_TYPE -> MimeTypes.forExtension("mp4").get,
      CONTENT_LENGTH -> ((length - start) + 1).toString,
      CONTENT_RANGE -> s"bytes $start-$length/${file.length()}",
      ACCEPT_RANGES -> "bytes",
      CONNECTION -> "keep-alive"
    )
  }

  def fileUpload = Action.async(parse.multipartFormData) { req =>
    Future(Ok(""))
  }

}
