package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import dbConfig.profile.api._

  private class PeopleTable(tag: Tag) extends Table[Person](tag, "people") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")

    def * = (id, username, password) <> ((Person.apply _).tupled, Person.unapply)
  }
  private val people = TableQuery[PeopleTable]

  private def query(person:Person) = {
    val qID   = if (person.id != 0)        people.filter(_.id === person.id) else people
    val qName = if (person.username != "") qID.filter   (_.username === person.username) else qID
    val qPass = if (person.password != "") qName.filter (_.password === person.password) else qName

    qPass
  }

  def find(person: Person): Future[Seq[Person]] = db.run(query(person).result)

  def delete(person:Person): Future[Int] = db.run(query(person).delete)

  def update(person: Person, to: Person): Future[Seq[Int]] = {
    val q = query(person)

    val works = Seq(
      (if (to.id != 0)
        Some(q.map(q => q.id).update(to.id))
      else None),

      (if (to.username != "")
        Some(q.map(q => q.username).update(to.username))
      else None),

      (if (to.password != "")
        Some(q.map(q => q.password).update(to.password))
      else None)
    )

    val dbio = DBIO.sequence(works.filter({
      case Some(_) => true
      case None => false
    }).flatMap(x => x))

    db.run(dbio)
  }

  def create(person: Person): Future[Person] = db.run {
    val Person(_, username, password) = person

    (people.map(p => (p.username, p.password))
      returning people.map(_.id)
      into ((nameAge, id) => Person(id, nameAge._1, nameAge._2))
    ) += (username, password)
  }

}
