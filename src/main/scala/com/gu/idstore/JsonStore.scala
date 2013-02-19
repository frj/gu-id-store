package com.gu.idstore

import com.gu.idstore.datastore.Datastore
import com.google.inject.Inject
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JArray


class JsonStore @Inject()(dataStore: Datastore) {

  def getJson(collectionName: String, entityId: String): Option[JValue] = {
    dataStore.get(collectionName, entityId).map(convertDotNotatedKeyValuesToJValue(_))
  }

  def getPublicJson(s: String, s1: String): Option[JValue] = {
    getJson(s, s1).map {
      _ match {
        case jObject:JObject =>
          pair2jvalue("public" -> jObject \ "public")
        case _ => JNothing
      }
    }
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

  def convertDotNotatedKeyValuesToJValue(keyValues: List[(String, Any)]): JValue = {
    keyValues match {
      case List(("", primitive)) => primitiveToJValue(primitive)
      case objectFields =>
        objectFields.groupBy(keyValue => keyValue._1.takeWhile(_ != '.'))
          .foldLeft(JObject(Nil)) { (jObject, values) =>
            jObject ~ (values._1 -> convertDotNotatedKeyValuesToJValue(
              values._2.map(
                subObjectKeyValues => (subObjectKeyValues._1.dropWhile(_ != '.').drop(1), subObjectKeyValues._2)
              )
            ))
          }
    }
  }

  def primitiveToJValue(primitive: Any): JValue = {
    primitive match {
      case integer: Integer => JInt(BigInt(integer))
      case double: Double => JDouble(double)
      case string: String => JString(string)
      case boolean: Boolean => JBool(boolean)
    }
  }
}
