# Setup

## Installation

### Containers

### Package Managers

### Operating Systems

### Manual

## Logging

Logging is performed asynchronously using [logback](http://logback.qos.ch/) and to stdout by [default](https://github.com/amannocci/streamy/blob/master/core/src/main/resources/logback.xml).  
If you want to log in a file you can create a `logback.xml` configuration in `${STREAMY_HOME}/conf`.

This configuration provide a rolling logging in file based on date and size.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/streamy.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/streamy.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>128MB</maxFileSize>
            <maxHistory>3</maxHistory>
            <totalSizeCap>512MB</totalSizeCap>
        </rollingPolicy>

        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="io.techcode.streamy.util.logging.JsonLayout">
                <timestamp>timestamp</timestamp>
                <level>level</level>
                <thread>thread</thread>
                <stacktrace>stacktrace</stacktrace>
                <message>message</message>
            </layout>
        </encoder>
    </appender>

    <!-- Async appenders -->
    <appender name="async-file" class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold>
        <queueSize>8192</queueSize>
        <neverBlock>true</neverBlock>
        <appender-ref ref="file"/>
    </appender>

    <!-- Root -->
    <root level="info">
        <appender-ref ref="async-file"/>
    </root>

    <!-- Shutdown Hook -->
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>
</configuration>
```

You have to load the configuration by adding the following directive `-Dlogback.configurationFile=conf/logback.xml` in `${STREAMY_HOME}/conf/application.ini`.  
You can configure [logback](http://logback.qos.ch/) in the way you want.