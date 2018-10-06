package simple.plugin

import io.techcode.streamy.plugin.{Plugin, PluginData}

/**
  * Simple plugin implementation.
  */
class SimplePlugin(
  data: PluginData
) extends Plugin(data) {

  override def onStart(): Unit = {
    // On start hook
    log.info("Hello world")
  }

  override def onStop(): Unit = {
    // On stop hook
  }

}
