package no.nb.bikube.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Makes logger available in all classes with just calling logger()
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
