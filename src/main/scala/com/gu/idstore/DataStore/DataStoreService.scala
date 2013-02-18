package com.gu.idstore.DataStore

import com.google.appengine.api.datastore._
import com.google.appengine.api.datastore.Query.{FilterPredicate, CompositeFilterOperator}
import scala.collection.JavaConversions._
import scala.Some


class DataStoreService {
  type KeyValuePair = List[(String, Any)]

  val datastore = DatastoreServiceFactory.getDatastoreService
  val collectionKind = "collection"
  val dataKind = "userData"

  private def getCollection(collectionName: String, txn: Transaction = null): Option[Entity] = {
    try {
      Some(datastore.get(txn, KeyFactory.createKey(collectionKind, collectionName)))
    } catch {
      case e: EntityNotFoundException => None
    }
  }

  private def getOrCreateCollection(collectionName: String, txn: Transaction = null): Entity = {
    getCollection(collectionName, txn) match {
      case Some(collection) => collection
      case None => {
        val collection = new Entity(collectionKind, collectionName)
        datastore.put(txn, collection)
        collection
      }
    }
  }

  private def getEntity(collection: Entity, id: String, txn: Transaction = null): Either[Entity, Entity] = {
    try {
      Right(datastore.get(txn, KeyFactory.createKey(collection.getKey, dataKind, id)))
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
    val txn = datastore.beginTransaction()
    try {
      val collection = getOrCreateCollection(collectionName, txn)
      val entity = getEntity(collection, id) match {
        case Left(entity) => entity
        case Right(entity) => entity
      }
      val mergedEntity = mergeData(entity, data)
      datastore.put(txn, mergedEntity)

      txn.commit()
    } finally {
      if (txn.isActive) txn.rollback()
    }
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

  def find(collectionName: String, filters: Seq[FilterPredicate]): List[Entity] = {
    getCollection(collectionName) match {
      case None => Nil
      case Some(collection) => {
        val query = new Query(dataKind, collection.getKey)
        filters match {
          case Nil => query
          case head :: Nil => query.setFilter(head)
          case xs => query.setFilter(CompositeFilterOperator.and(xs))
        }
        val prepared = datastore.prepare(query)
        // TODO use asIterable to optimize large queries
        prepared.asList(FetchOptions.Builder.withDefaults()).toList
      }
    }
  }
}
