package com.gu.idstore.datastore

import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.DatastoreService
import com.google.inject.Singleton


@Singleton
class DatastoreProvider {
  def getDatastore: DatastoreService = DatastoreServiceFactory.getDatastoreService
}
