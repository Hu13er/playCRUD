package controllers

import javax.inject.Inject

import models._
import play.api.libs.json.JsValue
import play.api.mvc._
import views._

import scala.concurrent.{ExecutionContext, Future}

class PersonController @Inject()(repo: PersonRepository,
                                  cc: ControllerComponents
                                )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
    * The index action.
    */

  def find(id: Long, username: String, password: String) = Action.async { implicit req: Request[AnyContent] =>
    repo.find(Person(id, username, password)).map(list => Ok(s"${list}"))
  }

  def delete(id: Long, username: String, password: String) = Action.async { implicit req: Request[AnyContent] =>
    repo.delete(Person(id, username, password)).map(list => Ok(s"${list}"))
  }

  def update(id: Long, username: String, password: String) = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val (jusername, jpassword) = ((req.body \ "username"), (req.body \ "password"))
    val nusername = jusername.asOpt[String].getOrElse("")
    val npassword = jpassword.asOpt[String].getOrElse("")

    repo.update(Person(id, username, password), Person(0, nusername, npassword)).
      map(updated => Ok(s"${updated} users updated."))
  }

  def create = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val (jusername, jpassword) = ((req.body \ "username"), (req.body \ "password"))
    val username = jusername.asOpt[String].getOrElse("")
    val password = jpassword.asOpt[String].getOrElse("")

    repo.create(Person(0, username, password)).map(person => Ok(s"user created with ID: ${person.id}."))
  }
}

