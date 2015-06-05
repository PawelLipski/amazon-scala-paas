package controllers

import java.io.{File, FileInputStream}
import java.util.zip.ZipInputStream
import play.api._
import play.api.mvc._
import scala.sys.process._
import paas._
import scala.language.postfixOps

object Application extends Controller {

  val slaves = List(
    "ubuntu@10.0.1.188:/home/ubuntu/jars/",
    "ubuntu@10.0.1.221:/home/ubuntu/jars/"
  )


  def index = Action {

    val f = new java.io.File(s"${System.getProperty("user.home")}/jars/")
    f.mkdir
    val l = f.listFiles.filter(_.isFile).toList
    val data: List[(String, List[String])] = for {
      x <- l
      zip = new ZipInputStream(new FileInputStream(x))
      classNames = Stream.continually(zip.getNextEntry).takeWhile(_ != null)
        .filter(entry => !entry.isDirectory && entry.getName.endsWith(".class"))
        .map(entry => entry.getName.replace("/", ".").dropRight(".class".length))
    } yield x.getName -> classNames.toList
    Ok(views.html.index("You can send your new application here.", data))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      val f = new File(s"${System.getProperty("user.home")}/jars/$filename")
      if (f.exists() && !f.isDirectory() && !filename.endsWith(".jar")) {
        BadRequest("File not uploaded: " + filename)
      } else {
        picture.ref.moveTo(f, true)
        slaves.foreach { slave =>
          copyViaSCP(f, slave, "aws-master-key.pem")
        }
        Ok("File uploaded: " + filename)
      }
      Redirect("/");
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }

  }


  def copyViaSCP(input: File, targetDirectory: String, authKeypath: String) = {
    val targetFile = targetDirectory match {
      case withSlash if withSlash.endsWith("/") => s"t$targetDirectory${input.getName}"
      case withoutSlash => s"t$targetDirectory/${input.getName}"
    }
    try {
      val scpStr = s"scp -i $authKeypath ${input.getCanonicalPath} $targetFile"
      val output: String = Process(scpStr).!!
      Logger.info(output)
    } catch {
      case t: Throwable => println(t)
    }
  }
}
