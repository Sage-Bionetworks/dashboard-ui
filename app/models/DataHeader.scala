package models

/**
 * Standard set of data headers.
 */
object DataHeader extends Enumeration {
  val ID = Value("id")
  val Name = Value("name")
  val Timestamp = Value("timestamp")
  val URL = Value("url")
}
