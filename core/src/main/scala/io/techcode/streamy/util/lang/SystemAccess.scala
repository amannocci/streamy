package io.techcode.streamy.util.lang

import java.lang.management._
import java.lang.reflect.Method
import java.util

import io.techcode.streamy.util.lang.SystemAccess.Platform.Platform

/**
  * Provide system access by reflection.
  */
object SystemAccess {

  // Supported platform
  private[lang] object Platform extends Enumeration {
    type Platform = Value
    val Windows, MacOS, Linux = Value
  }

  // Detect platform
  private val platform: Platform = detectPlatform(System.getProperty("os.name").toLowerCase)

  // OS access
  val OsBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean
  val RuntimeBean: RuntimeMXBean = ManagementFactory.getRuntimeMXBean
  val MemoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean
  val ThreadBean: ThreadMXBean = ManagementFactory.getThreadMXBean
  val ClassLoadingBean: ClassLoadingMXBean = ManagementFactory.getClassLoadingMXBean
  val GarbageCollectorsBean: util.List[GarbageCollectorMXBean] = ManagementFactory.getGarbageCollectorMXBeans

  // MXBeans
  private val genericMxBean: String = "com.sun.management.OperatingSystemMXBean"
  private val unixMxBean: String = "com.sun.management.UnixOperatingSystemMXBean"

  /**
    * Returns a given method of the OperatingSystemMXBean, or [[None]] if the method is unavailable.
    *
    * @param methodName method name to access.
    * @return reflected method or [[None]].
    */
  def getMethod(methodName: String): Option[Method] = reflectMethod(genericMxBean, methodName)

  /**
    * Returns a given method of the UnixOperatingSystemMXBean, or [[None]] if the method is unavailable.
    *
    * @param methodName method name to access.
    * @return reflected method or [[None]].
    */
  def getUnixMethod(methodName: String): Option[Method] = reflectMethod(unixMxBean, methodName)

  /**
    * Returns true if the detected platform is Windows.
    *
    * @return true if the detected platform is Windows, otherwise false.
    */
  def isWindows: Boolean = platform == Platform.Windows

  /**
    * Returns true if the detected platform is MacOS.
    *
    * @return true if the detected platform is MacOS, otherwise false.
    */
  def isMacOS: Boolean = platform == Platform.MacOS

  /**
    * Returns true if the detected platform is Linux or Unix.
    *
    * @return true if the detected platform is Linux or Unix, otherwise false.
    */
  def isLinux: Boolean = platform == Platform.Linux

  /**
    * Attempt to detect correctly platform.
    *
    * @param name name of the operating system.
    * @return platform type.
    */
  private[lang] def detectPlatform(name: String): Platform = name match {
    case x if x.contains("win") => Platform.Windows
    case x if x.contains("mac") => Platform.MacOS
    case _ => Platform.Linux
  }

  /**
    * Access to a method by reflection.
    *
    * @param className  class name where the method is.
    * @param methodName method name to access.
    * @return reflected method if accessible or [[None]].
    */
  private def reflectMethod(className: String, methodName: String): Option[Method] = {
    try {
      Some(Class.forName(className).getMethod(methodName))
    } catch {
      // Not available
      case _: Exception => None
    }
  }

}
