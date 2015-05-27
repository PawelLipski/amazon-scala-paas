import play.api._
import sys.process._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    val result = s"mkdir -p /home/ubuntu/jars/" !!;
    Logger.info(result)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}