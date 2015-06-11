import play.api._
import sys.process._
import paas.RunnerApplication
import scala.language.postfixOps
import scala.collection.JavaConversions._
import java.net.NetworkInterface

object Global extends GlobalSettings {

  val masterIP = "10.0.1.1"//"10.0.0.240"
  
  override def onStart(app: Application) {
    Logger.info("Application has started")
    
    val ips = 
      for(interface <- NetworkInterface.getNetworkInterfaces();
    	address <- interface.getInetAddresses()) 
      yield address.getHostAddress()
    
    if(ips.exists(ip => ip == masterIP))
	  RunnerApplication.main(Array("Master"))
    else 
      RunnerApplication.main(Array("Slave", masterIP))
    
    	  
    val str = s"mkdir -p ${System.getProperty("user.home")}/paas-repo/lib/"
    if((Process(str) !) != 0)
      Logger.info("Error while creating lib directory")
  }

  override def onStop(app: Application) {
    RunnerApplication.stop
    Logger.info("Application shutdown...")
  }

}
