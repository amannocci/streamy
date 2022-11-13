# What is streamy ?

## Principles

* `Unified`: Streamy provide a way to decouple source and sink.
* `High performance`: Streamy can handle a large amount of events by seconds.
* `Reliability`: Streamy's primary design goal is reliability and simplicity.
* `Reactive`: Streamy is based on asynchronous non-blocking stream processing with backpressure.

## Who should use Streamy ?

* You SHOULD use Streamy to replace Logstash, FluentD, Hindsight, Heka, or similar tools.
* You SHOULD use Streamy as an Agent or an ETL.
* You SHOULD NOT use Streamy to replace Kafka. Streamy is designed to work with Kafka !
* You SHOULD NOT use Streamy as an heavy distributed system.

## How It Works

A Streamy application is in fact an Akka based application. If you used Akka before, 
you will notice the familiar development experience when you are writing or developing 
custom plugins. 

## Features

* Powerful and extensible plugin system.
* Painless event processing.
* Provide back-pressure end to end.
* Use industry standards and is interoperable with Reactive Streams.


## Why Not ...?

### Logstash

TODO

### FluendD

TODO

### Rsyslog

TODO

### Hindsight

TODO

### Vector

TODO