package controllers

import java.io.{File, FileInputStream}
import java.net.URLClassLoader
import java.util.zip.ZipInputStream
import play.api._
import play.api.mvc._
import scala.sys.process._
import scala.language.postfixOps
import resource._
import paas._
import java.nio.file.Paths
import java.net.URL

object Application extends Controller {
  
  val slaves = List(
    "ubuntu@10.0.1.188",
    "ubuntu@10.0.1.221"
  )

  def index = Action {
    val f = new File(s"${System.getProperty("user.home")}/paas-repo/lib/")
    f.mkdir
    val l = f.listFiles.filter(_.isFile).toList
    val data: List[(String, List[String])] = for {
      x <- l
      test = {
         managed(new URLClassLoader(Array(Paths.get(x.toString()).toUri().toURL()), this.getClass.getClassLoader)) and
         managed(new ZipInputStream(new FileInputStream(x)))
      } map { case(jarClassLoader, zip) =>
        val classNames = Stream.continually(zip.getNextEntry).takeWhile(_ != null)
        .filter(entry => !entry.isDirectory && entry.getName.endsWith(".class"))
        .map(entry => entry.getName.replace("/", ".").dropRight(".class".length))
        .filter(entry => classOf[Agent].isAssignableFrom(jarClassLoader.loadClass(entry)))
        .filter(entry => !jarClassLoader.loadClass(entry).isInterface())
        classNames.toList
      }   
    } yield {
      if(test.opt.isDefined)
        x.getName -> test.opt.get
      else {
        x.getName -> List()
      }    	
    }
    val indexedData = for {
      jar <- data
      result = jar._2.sorted zipWithIndex
    } yield (jar._1, result)
    
    Ok(views.html.index("You can send your new application here.", indexedData))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      val f = new File(s"${System.getProperty("user.home")}/paas-repo/lib/$filename")
      if (f.exists() && !f.isDirectory() && !filename.endsWith(".jar")) {
        BadRequest("File not uploaded: " + filename)
      } else {
        picture.ref.moveTo(f, true)
        slaves.map(slave => slave+":~/paas-repo/lib/").foreach { slave =>
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
  
  def launch = Action { request =>
    val req = request.body.asFormUrlEncoded
    if(req.isDefined) {
      val values = req.get.map(v => (v._1, v._2(0).toInt))
      RunnerApplication.issueActionToMaster(slaves, values)
    }
    Redirect("/");
  }

  def copyViaSCP(input: File, targetDirectory: String, authKeypath: String) = {
    val targetFile = targetDirectory match {
      case withSlash if withSlash.endsWith("/") => s"$targetDirectory${input.getName}"
      case withoutSlash => s"$targetDirectory/${input.getName}"
    }
    try {
      val scpStr = s"scp -i $authKeypath ${input.getCanonicalPath} $targetFile"
      Logger.info(scpStr)
      Process(scpStr) !
    } catch {
      case t: Throwable => Logger.error("SCP error", t)
    }
  }
}