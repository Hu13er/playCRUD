package models

import play.api.libs.json._

case class Person(id: Long = 0, username: String = "", password: String = "")

object Person {
  implicit val personFormat = Json.format[Person]
}
