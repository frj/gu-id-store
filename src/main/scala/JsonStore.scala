import net.liftweb.json.JsonAST.{JArray, JObject, JValue}

class JsonStore {
  def storeJson(collectionName:String, id:String, json: JValue) = {
    //datastore.store(collectionName, id, convertJValueDotNotatedKeyValues(json))
  }

  private def convertJValueDotNotatedKeyValues(jValue: JValue, rootPath: String = ""): Seq[(String, Any)] = {
    jValue match {
      case jObject: JObject =>
        jObject.obj.flatMap { jField =>
          convertJValueDotNotatedKeyValues(jField.value, "%s%s.".format(rootPath, jField.name))
        }
      case jArray: JArray =>
        jArray.arr.zipWithIndex.flatMap { elementWithIndex =>
          convertJValueDotNotatedKeyValues(elementWithIndex._1, "%s[%d].".format(rootPath, elementWithIndex._2))
        }
      case primitive =>
        Seq((rootPath, primitive.values))
    }
  }
}
