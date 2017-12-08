package org.narrativeandplay.hypedyn.api.serialisation

object Serialiser {
  /**
   * Object <-> Ast Serialisers
   */

  /**
   * Serialises a serialisable object
   *
   * @param t The object to serialise
   * @param serialiser The typeclass instance implementing Serialisable for the type T
   * @tparam T The type of the object to serialise
   * @return The serialised form of the object
   */
  def serialise[T](t: T)(implicit serialiser: Serialisable[_ >: T]) = serialiser.serialise(t)

  /**
   * Deserialises a serialisable object
   *
   * @param data The serialised data to deserialise
   * @param deserialiser The typeclass instance implementing Serialisable for the type T
   * @tparam T The type of the object to deserialise
   * @return The deserialised object
   */
  def deserialise[T](data: AstElement)(implicit deserialiser: Serialisable[T]) = deserialiser.deserialise(data)

  /**
   * Ast <-> String Serialisers
   */

  /**
   * Transforms serialised data to a string
   *
   * @param data The data to transform
   * @return The string form of the data
   */
  def render(data: AstElement) = JsonRenderer serialise data

  /**
   * Transforms a string into serialised data
   *
   * @param string The string to transform
   * @return The serialised data form of the string
   */
  def parse(string: String) = JsonRenderer deserialise string

  /**
   * Object that transforms AST data into JSON strings
   */
  private[this] object JsonRenderer {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    import org.json4s.native.JsonMethods.{render => jsonRender, parse => jsonParse}
    /**
     * Transforms an AST element into a JSON string
     *
     * @param data The AST element to transform
     * @return A JSON string
     */
    def serialise(data: AstElement) = pretty(jsonRender(AstElementToJValue(data)))

    /**
     * Transforms a JSON string into an AST element
     *
     * @param data The JSON string to transform
     * @return An AST element
     */
    def deserialise(data: String): AstElement = jValueToAstElement(jsonParse(data))

    private def AstElementToJValue(elem: AstElement): JValue = elem match {
      case AstInteger(i) => JInt(i)
      case AstFloat(f) => JDouble(f)
      case AstString(s) => JString(s)
      case AstBoolean(b) => JBool(b)
      case AstDecimal(d) => JDecimal(d)
      case AstNothing => JNothing
      case AstNull => JNull
      case AstList(ls @ _*) => JArray((ls map AstElementToJValue).toList)
      case AstMap(hs @ _*) =>
        JObject((hs map { case (k, v) =>
          k -> AstElementToJValue(v)
        }).toList)
    }

    private def jValueToAstElement(jValue: JValue): AstElement = jValue match {
      case JInt(i) => AstInteger(i)
      case JLong(l) => AstInteger(l)
      case JDouble(f) => AstFloat(f)
      case JString(s) => AstString(s)
      case JBool(b) => AstBoolean(b)
      case JDecimal(d) => AstDecimal(d)
      case JNothing => AstNothing
      case JNull => AstNull
      case JArray(a) => AstList(a map jValueToAstElement: _*)
      case JSet(vs) => AstList(vs.toList map jValueToAstElement: _*)
      case JObject(o) =>
        AstMap(o map { case (k, v) =>
          k -> jValueToAstElement(v)
        }: _*)
    }
  }
}
