package simple.plugin

import akka.stream.Materializer
import io.techcode.streamy.plugin.{Plugin, PluginData}

/**
  * Simple plugin implementation.
  */
class SimplePlugin(
  _materializer: Materializer,
  data: PluginData
) extends Plugin(_materializer, data) {

  override def onStart(): Unit = {
    // On start hook
  }

  override def onStop(): Unit = {
    // On stop hook
  }

}
