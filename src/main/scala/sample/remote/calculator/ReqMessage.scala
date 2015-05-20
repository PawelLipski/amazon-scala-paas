package sample.remote.calculator

trait ReqMessage

case class Add(nbr1: Int, nbr2: Int) extends ReqMessage

trait ResMessage

case class AddResult(nbr: Int, nbr2: Int, result: Int) extends ResMessage

