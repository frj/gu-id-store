package com.gu.idstore

import com.gu.idstore.datastore.Datastore
import com.google.inject.Inject
import net.liftweb.json.JsonAST.{JArray, JObject, JValue}

class JsonStore @Inject()(dataStore: Datastore) {
  def storeJson(collectionName:String, id:String, json: JValue) = {
    dataStore.save(collectionName, id, convertJValueDotNotatedKeyValues(json))
  }

  private def convertJValueDotNotatedKeyValues(jValue: JValue, rootPath: String = ""): List[(String, Any)] = {
    jValue match {
      case jObject: JObject =>
        jObject.obj.map{ jField =>
          convertJValueDotNotatedKeyValues(jField.value, "%s%s.".format(rootPath, jField.name))
        }.flatten
      case jArray: JArray =>
        jArray.arr.zipWithIndex.flatMap { elementWithIndex =>
          convertJValueDotNotatedKeyValues(elementWithIndex._1, "%s[%d].".format(rootPath, elementWithIndex._2))
        }
      case primitive =>
        List((rootPath, primitive.values))
    }
  }
}
