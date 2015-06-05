package paas

trait ReqMessage

case class TellMeSomethingMyMaster() extends ReqMessage

trait ResMessage

case class Adage(text: String) extends ResMessage

