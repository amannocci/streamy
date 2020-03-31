# Create a plugin

## Lifecycle

### State Events

There are three categories of state events:
* `Loading`: When plugin is loading, before the onStart hook is started.
* `Running`: When plugin is running, after the onStart hook has been fired.
* `Stopping`: When plugins is stopping, before the onStop hook is fired.
* `Stopped`: When plugins is stopped, after the onStop hook has been fired.