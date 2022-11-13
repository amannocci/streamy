# Administration

## Process Management

### Starting

#### Flags

| Flag        | Description                 |
| ----------- | --------------------------- |
| `--dry-run` | Run streamy in dry-run mode |

#### Daemonizing

Streamy does not directly offer a way to daemonize the Streamy process. It's recommend that you use a utility like [Systemd](https://systemd.io/) to daemonize and manage your processes. Streamy provides a [streamy-core.service](https://github.com/amannocci/streamy/blob/master/core/src/templates/systemloader/systemd/start-template) file for Systemd.

### Stopping

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

Streamy provide some internal events to monitor it self.  
They can be catch using the [Akka EventBus](https://doc.akka.io/docs/akka/current/event-bus.html#classic-event-bus) and then use in any way.  
For example, you can aggregate events and deduce [garbage collector pressure](https://github.com/amannocci/streamy/blob/master/core/src/main/scala/io/techcode/streamy/util/monitor/GarbageCollectorMonitor.scala).


## Tuning

Streamy is written in Scala and therefore rely on the JVM runtime. By default, Streamy will attempt take full advantage of all system resources without any adjustments. Nevertheless, in some case it's recommend to set heap size and dispatcher size.