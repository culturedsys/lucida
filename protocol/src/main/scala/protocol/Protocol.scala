package protocol

import java.nio.charset.StandardCharsets
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.apache.commons.io.IOUtils
import play.api.http.HttpEntity
import play.api.mvc.MultipartFormData.FilePart
import play.core.formatters

/**
  * Functions dealing with formatting data for transmission between the coordinator and other
  * components. Placed here so they can be shared between components.
  */
object Protocol {
  /**
    * Packs a number of files into an HttpEntity for streaming.
    *
    * @param files tuples of (key, name, contentType, data) representing the files to put in
    *              the multipart data.
    */
  def filesToMultipart(files: (String, String, Option[String], Array[Byte])*):
      HttpEntity.Streamed = {
    val boundary = formatters.Multipart.randomBoundary()
    val formatter = formatters.Multipart.format(boundary, StandardCharsets.US_ASCII, 65535)

    val parts = files.map{
      case (key, name, contentType, data) =>
        FilePart(key, name, contentType, Source.single(ByteString.fromArray(data)))
    }

    HttpEntity.Streamed(Source(parts.toList).via(formatter),
      None, Some(s"multipart/form-data; boundary=$boundary"))
  }

  /**
    * Convert multipart/form-data to a sequence of byte arrays representing the files in the
    * multipart data.
    */
  def multipartToFiles(multipart: Array[Byte], contentType: String): Seq[(String, Array[Byte])] = {
    val mp = new MimeMultipart(new ByteArrayDataSource(multipart, contentType))

    (0 until mp.getCount).map { i =>
      val part = mp.getBodyPart(i)
      val data = IOUtils.toByteArray(part.getInputStream)
      (part.getFileName , data)
    }
  }
}
