package services.dtos

import java.nio.file.Path

case class FilePathDTO(filePath: Path) {
  val file = filePath.toFile
}
