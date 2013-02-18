package com.gu.idstore.DataStore

import com.google.appengine.api.datastore._


class DataStoreService {
  type KeyValuePair = List[(String, Any)]

  val datastore = DatastoreServiceFactory.getDatastoreService
  val collectionKind = "collection"
  val dataKind = "userData"

  private def runInTransaction(code: (Transaction) => Any) = {
    val txn = datastore.beginTransaction()
    try {
      code(txn)
    } finally {
      if (txn.isActive) txn.rollback()
    }
  }

  private def getCollection(collectionName: String): Option[Entity] = {
    try {
      Some(datastore.get(new Key(collectionKind, collectionName)))
    } catch {
      case e: EntityNotFoundException => None
    }
  }

  private def getOrCreateCollection(collectionName: String, txn: Transaction): Entity = {
    getCollection(collectionName) match {
      case Some(_) => _
      case None => {
        val collection = new Entity(collectionKind, collectionName)
        datastore.put(txn, collection)
        collection
      }
    }
  }

  private def getEntity(collection: Entity, id: String): Either[Entity, Entity] = {
    try {
      Right(datastore.get(new Key(dataKind, collection.getKey, id)))
    } catch {
      case e: EntityNotFoundException => Left(new Entity(dataKind, id, collection.getKey))
    }
  }

  private def mergeData(entity: Entity, data: KeyValuePair): Entity = {
    data.foreach({
      case (name, value) => entity.setProperty(name, value)
    })
    entity
  }

  def save(collectionName: String, id: String, data: KeyValuePair) = {
    runInTransaction({(txn) =>
      val collection = getOrCreateCollection(collectionName, txn)
      val entity = getEntity(collection, id) match {
        case Left(_) => _
        case Right(_) => _
      }
      val data = mergeData(entity, data)
      datastore.put(txn, data)
    })
  }

  def get(collectionName: String, id: String): Option[Entity] = {
    getCollection(collectionName) match {
      case None => None
      case Some(collection) => getEntity(collection, id) match {
          case Left(_) => None
          case Right(entity) => Some(entity)
        }
    }
  }
}
