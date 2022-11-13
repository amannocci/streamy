# Create a plugin

## Plugin Lifecycle

Prior to any states that make the plugin visible, the plugin loader determines if all dependencies are present.

### State Events

There are three categories of state events:
* `Loading`: When plugin is loading, before the onStart hook is started.
* `Running`: When plugin is running, after the onStart hook has been fired.
* `Stopping`: When plugins is stopping, before the onStop hook is fired.
* `Stopped`: When plugins is stopped, after the onStop hook has been fired.

## Logging & Debugging

There are a few logging frameworks available for use in Scala. Logging is preferable to printing to stdout or stderr with `println()` for a number of reasons:

* Logged messages are labeled with a source name, making it easier to figure out where the logged messages are coming from.
* Logged messages have a severity level which allow for simple filtering.
* The available logger frameworks allow you to enable or disable messages from certain sources.

Streamy uses `org.slf4j.Logger`.

### Getting a Logger

The logging extension is used to provide logging system.
It is accessible directly in a plugin.

```scala
class MyPlugin(data: PluginData) extends Plugin(data) {
  def onStart(): Unit = {
    log.info("Hello world !")
  }
}
```

If you need logging in another place, simply use the logging extension. 

```scala
class MyComponent() {
  def log()(implicit system: ActorSystem): Unit = {
    Logging(system).info("Hello world !")
  }
}
```

### Emitting messages

Emitting a message with your logger is very simple.

> The following example assumes that the getter method for your logger is named `log`, as shown in the previous section. This may differ for you depending on what you named your getter method.

```scala
log.info("string")
log.debug("string")
log.warn("string")
log.error("string")
```

## Configuration