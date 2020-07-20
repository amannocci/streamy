package io.techcode.streamy.event

/**
  * All monitor events.
  */
object MonitorEvent {

  /**
    * Represent a process monitor event.
    * This event is fired when a monitoring check has been done.
    */
  case class Process(
    timestamp: Long,
    openFileDescriptors: Long,
    maxFileDescriptors: Long,
    cpuPercent: Short,
    cpuTotal: Long,
    memTotalVirtual: Long
  )

  /**
    * Represent a os monitor event.
    * This event is fired when a monitoring check has been done.
    */
  case class Os(
    timestamp: Long,
    cpuPercent: Short,
    cpuLoadAverage: Array[Double],
    memFree: Long,
    memAvailable: Long,
    memTotal: Long,
    swapFree: Long,
    swapTotal: Long
  )

  /**
    * Represent a jvm monitor event.
    * This event is fired when a monitoring check has been done.
    */
  case class Jvm(
    timestamp: Long,
    uptime: Long,
    memHeapUsed: Long,
    memHeapCommitted: Long,
    memHeapMax: Long,
    memNonHeapCommitted: Long,
    memNonHeapUsed: Long,
    thread: Int,
    threadPeak: Int,
    classLoaded: Long,
    classLoadedTotal: Long,
    classUnloaded: Long,
    bufferPools: Seq[Jvm.BufferPool],
    garbageCollectors: Seq[Jvm.GarbageCollector]
  )

  /**
    * Represent a garbage collector overhead monitor event.
    * This event is fired when a monitoring check has been done.
    */
  case class GarbageCollectorOverhead(
    timestamp: Long,
    time: Long,
    elapsed: Long,
    percent: Short
  )

  /**
    * Jvm event extensions.
    */
  object Jvm {

    // Buffer poll data
    case class BufferPool(
      name: String,
      count: Long,
      totalCapacity: Long,
      memUsed: Long
    )

    // Garbage collector data
    case class GarbageCollector(
      name: String,
      collectionCount: Long,
      collectionTime: Long
    )

  }

}
