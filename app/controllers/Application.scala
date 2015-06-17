package controllers

import scala.concurrent.duration._
import java.io.{File, FileInputStream}
import java.net.URLClassLoader
import java.nio.file.Paths
import java.util.zip.ZipInputStream

import paas._
import play.api._
import play.api.mvc._
import resource._

import scala.language.postfixOps
import scala.sys.process._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  val slaves: List[String] = List(
        "ubuntu@10.0.1.188",
        "ubuntu@10.0.1.221"
  )

  def index = Action.async {
    val f = new File(s"${System.getProperty("user.home")}/paas-repo/lib/")
    f.mkdir
    val l = f.listFiles.filter(_.isFile).toList
    val data: List[(String, List[String])] = for {
      x <- l
      test = {
        managed(new URLClassLoader(Array(Paths.get(x.toString).toUri.toURL), this.getClass.getClassLoader)) and
          managed(new ZipInputStream(new FileInputStream(x)))
      } map { case (jarClassLoader, zip) =>
        val classNames = Stream.continually(zip.getNextEntry).takeWhile(_ != null)
          .filter(entry => !entry.isDirectory && entry.getName.endsWith(".class"))
          .map(entry => entry.getName.replace("/", ".").dropRight(".class".length))
          .filter(entry => classOf[Agent].isAssignableFrom(jarClassLoader.loadClass(entry)))
          .filter(entry => !jarClassLoader.loadClass(entry).isInterface)
        classNames.toList
      }
    } yield {
        x.getName -> test.opt.getOrElse(List.empty)
      }
    val indexedData = for {
      jar <- data
      result = jar._2.sorted zipWithIndex
    } yield (jar._1, result)

    val runningAgents = RunnerApplication.getListOfRunning(30 seconds)

    runningAgents.map(_.agents).map { agents =>
      Ok(views.html.index("You can send your new application here.", indexedData, agents))
    }
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val f = new File(s"${System.getProperty("user.home")}/paas-repo/lib/$filename")
      if (f.exists() && !f.isDirectory && !filename.endsWith(".jar")) {
        BadRequest("File not uploaded: " + filename)
      } else {
        picture.ref.moveTo(f, replace = true)
        slaves.map(slave => slave + ":~/paas-repo/lib/").foreach { slave =>
          copyViaSCP(f, slave, "aws-master-key.pem")
        }
        Ok("File uploaded: " + filename)
      }
      Redirect("/");
    }.getOrElse {
      Redirect(routes.Application.index()).flashing(
        "error" -> "Missing file")
    }

  }

  def launch = Action { request =>
    val req = request.body.asFormUrlEncoded
    if (req.isDefined) {
      val values = req.get.map(v => (v._1, v._2.head.toInt))
      RunnerApplication.issueActionToMaster(slaves, values)
    }
	Thread sleep 3000
    Redirect("/")
  }

  def kill = Action { req =>
    Logger.info(s"received request $req")
    val result = req.body.asFormUrlEncoded.map { form =>
	  Logger.info("form values: " + form.map(_._2.head.split(",")(0).substring(1)))
      val values = form.map(v => (v._1, v._2.head.split(",")(0).substring(1)))
	  //Logger.info("agenttokill: " + values.get("agenttokill"))
      values.get("agenttokill").map { (_, name) =>
        Logger.debug(s"about to kill agent $name")
        RunnerApplication.performStop(name)
        "success" -> "ok"
      }.getOrElse("error" -> "query not found")
    }.getOrElse("error" -> "not found")
    Redirect(routes.Application.index()).flashing(result)
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

      for (slave <- slaves) {
        val thread = new Thread(new Runnable {
          def run() {
            Process("ssh -i aws-master-key.pem " + slave + " killall java") !;
            Thread.sleep(1000)
            Process("ssh -i aws-master-key.pem " + slave + " cd paas-repo; sbt start -mem 800 < /dev/null") !
          }
        })
        thread.start()
      }
    } catch {
      case t: Throwable => Logger.error("SSH error", t)
    }
  }
}
