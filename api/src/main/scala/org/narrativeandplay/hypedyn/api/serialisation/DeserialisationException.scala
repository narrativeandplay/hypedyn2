package org.narrativeandplay.hypedyn.api.serialisation

/**
 * Exception class for deserialisation errors
 *
 * @param message The error message
 */
case class DeserialisationException(message: String) extends Exception(message)
