package org.narrativeandplay.hypedyn.serialisation.serialisers

/**
 * Exception class for deserialisation errors
 *
 * @param message The error message
 */
case class DeserialisationException(message: String) extends Exception(message)
