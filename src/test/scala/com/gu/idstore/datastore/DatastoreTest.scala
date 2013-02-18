package com.gu.idstore.datastore

import org.scalatest.{OneInstancePerTest, path}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.google.appengine.api.datastore.{Entity, KeyFactory, Transaction, DatastoreService}


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

  when(datastoreProvider.getDatastore) thenReturn(datastoreService)
  when(datastoreService.beginTransaction()) thenReturn(txn)

  val datastore = new Datastore(datastoreProvider)

  "The save method" - {
    "when given an existing record" - {
      val data = List(("test", "data"), ("test2", 123))
      when(datastoreService.get(txn, collectionEntity.getKey)) thenReturn(collectionEntity)
      when(datastoreService.get(txn, dataEntity.getKey)) thenReturn(dataEntity)

      "should lookup the collection" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, collectionEntity.getKey)
      }

      "should lookup the data entity" in {
        datastore.save(collectionName, dataId, data)
        verify(datastoreService).get(txn, dataEntity.getKey)
      }
    }
  }
}
