package com.gu.idstore.datastore

import org.scalatest.{OneInstancePerTest, path}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import com.google.appengine.api.datastore._
import java.util.ConcurrentModificationException


class DatastoreTest extends path.FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  val datastoreProvider = mock[DatastoreProvider]
  val datastoreService = mock[DatastoreService]
  val txn = mock[Transaction]

  val collectionKind = "collection"
  val collectionName = "testCollection"
  val dataKind = "dataKind"
  val dataId = "testData"
  val collectionEntity = new Entity(collectionKind, collectionName)
  val dataEntity = new Entity(dataKind, dataId, collectionEntity.getKey)
  val data = List(("test", "data"), ("test2", 123))
  val populatedEntity = dataEntity.clone()
  populatedEntity.setProperty("test", "data")
  populatedEntity.setProperty("test2", 123)

  when(datastoreProvider.getDatastore) thenReturn(datastoreService)
  when(datastoreService.beginTransaction()) thenReturn(txn)

  val datastore = new Datastore(datastoreProvider)

  "The save method" - {
    "when given an existing record" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenReturn(dataEntity)

      "should start a transaction" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).beginTransaction()
      }

      "should lookup the collection" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, collectionEntity.getKey)
      }

      "should lookup the data entity" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, dataEntity.getKey)
      }

      "should update the entity" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).put(txn, populatedEntity) // TODO custom matcher would be good here to check data
      }

      "if the write succeeds" - {
        "should commit the transaction" in {
          datastore.save(collectionName, dataId, data)
          verify(datastoreService).beginTransaction()
        }
      }

      "if the write fails" - {
        when(datastoreService.put(Matchers.any[Transaction], Matchers.any[Entity]))
          .thenThrow(ConcurrentModificationException)

        "should rollback the transaction" in {
          datastore.save(collectionName, dataId, data)
          verify(txn).rollback()
        }
      }
    }

    "when given a record that does not exist" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenThrow(EntityNotFoundException)

      "should create the entity" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).put(txn, populatedEntity) // TODO custom matcher would be good here to check data
      }
    }

    "when given a collection that does not exist (and thus also implicitly a record that does not exist)" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenThrow(EntityNotFoundException)
      when(datastoreService.get(txn, dataEntity.getKey)) thenThrow(EntityNotFoundException)

      "should create the collection" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).put(txn, collectionEntity)
      }

      "should then create the entity" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).put(txn, populatedEntity) // TODO custom matcher would be good here to check data
      }
    }
  }

  "The get method" in {
    "when given a record that exists" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenReturn(dataEntity)

      "should check the collection exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, collectionEntity.getKey)
      }

      "should check the data entity exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, dataEntity.getKey)
      }

      "should return the entity" in {
        val entity = datastore.get(collectionName, dataId)
        entity.isEmpty should equal(false)
        entity.get.getKey should equal(dataEntity.getKey)
      }
    }

    "when give a record that does not exist" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenThrow(EntityNotFoundException)

      "should check the collection exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, collectionEntity.getKey)
      }

      "should check the data entity exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, dataEntity.getKey)
      }

      "should return None" in {
        val entity = datastore.get(collectionName, dataId)
        entity.isEmpty should equal(true)
      }
    }

    "when give a collection that does not exist" - {
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenThrow(EntityNotFoundException)

      "should check the collection exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, collectionEntity.getKey)
      }

      "should not check the data entity exists" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService, never).get(txn, dataEntity.getKey)
      }

      "should return None" in {
        val entity = datastore.get(collectionName, dataId)
        entity.isEmpty should equal(true)
      }
    }
  }
}
