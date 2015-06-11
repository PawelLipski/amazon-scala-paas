package paas

trait ReqMessage

case class TellMeSomethingMyMaster() extends ReqMessage

trait ResMessage

case class Adage(text: String) extends ResMessage

case class Launch(slaves: List[String], values: Map[String, Int])