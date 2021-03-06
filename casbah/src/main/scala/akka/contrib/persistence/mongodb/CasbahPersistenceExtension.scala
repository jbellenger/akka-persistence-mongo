package akka.contrib.persistence.mongodb

import akka.actor.ActorSystem
import com.mongodb.casbah.Imports._
import akka.persistence.PersistentRepr
import scala.concurrent.Future
import akka.serialization.SerializationExtension
import com.mongodb.ServerAddress
import com.mongodb.casbah.WriteConcern
import scala.collection.immutable.{ Seq => ISeq }
import scala.concurrent.ExecutionContext
import akka.persistence.SelectedSnapshot
import scala.language.implicitConversions

object CasbahPersistenceDriver {
  implicit def convertListOfStringsToListOfServerAddresses(strings: List[String]): List[ServerAddress] =
    strings.map { url =>
    val Array(host, port) = url.split(":")
    new ServerAddress(host, port.toInt)
    }.toList

}

trait CasbahPersistenceDriver extends MongoPersistenceDriver with MongoPersistenceBase {
  import CasbahPersistenceDriver._
  
  // Collection type
  type C = MongoCollection

  private[this] lazy val db = MongoClient(mongoUrl)(mongoDbName)
  
  private[mongodb] def collection(name: String) = db(name)
}

class CasbahMongoDriver(val actorSystem: ActorSystem) extends CasbahPersistenceDriver

class CasbahPersistenceExtension(val actorSystem: ActorSystem) extends MongoPersistenceExtension {
  private[this] lazy val driver = new CasbahMongoDriver(actorSystem)
  private[this] lazy val _journaler = new CasbahPersistenceJournaller(driver)
  private[this] lazy val _snapshotter = new CasbahPersistenceSnapshotter(driver)
  
  override def journaler = _journaler
  override def snapshotter = _snapshotter
}
