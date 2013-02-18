package com.gu.idstore

import com.gu.idstore.datastore.Datastore
import com.google.inject.Inject
import net.liftweb.json.JsonAST.{JString, JArray, JObject, JValue}

class JsonStore @Inject()(dataStore: Datastore) {


  def getJson(s: String, s1: String): Option[JValue] = {
    val json : List[(String, Any)] = Nil
    Some(JString("123123"))
    //convertDotNotatedKeyValuesToJValue(json)
  }

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

//  def convertDotNotatedKeyValuesToJValue(keyValues: List[(String, Any)]): JValue = {
//    keyValues
//      .map { keyValue => (keyValue._1.split('.'), keyValue._2)
//
//  }
//
}
