package simple.plugin

import io.techcode.streamy.plugin.Plugin

/**
  * Simple plugin implementation.
  */
class SimplePlugin(
  data: Plugin.Data
) extends Plugin(data) {

  override def onStart(): Unit = {
    // On start hook
    log.info("Hello world")
  }

}
