# Administration

## Process Management

### Starting

TODO

### Stopping

TODO

#### Graceful Shutdown

Streamy is designed to gracefully shutdown withing around 30 seconds when a `SIGTERM` process signal is received.
The shutdown process is as follow :

1. Stop accepting new data for all sources.
2. Flush any events in process.
3. Gracefully close any resources with a 10 seconds timeout (Configurable: `streamy.lifecycle.graceful-timeout`).
4. Shutdown any remaining resources with a 15 seconds timeout (Configurable: `streamy.lifecycle.shutdown-timeout`).
5. Exit the process with an exit code.

#### Force Killing

If Streamy is forcefully killed there is potential for losing any in-flight data.
To mitigate this we recommend using an on-disk buffers, avoiding forceful shutdowns whenever possible
or designing an architecture that handle such cases by reprocessing.

## Monitoring

TODO

## Tuning

Streamy is written in Scala and therefore rely the JVM runtime. By default, Streamy will attempt take full advantage of all system resources without any adjustments. Nevertheless in some case it's recommend to set heap size and dispatcher size.