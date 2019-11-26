# LogDNA Appender for Log4j2

LogDNA is a hosted logging platform: https://logdna.com

This small library provides an appender for [Log4j2](https://logging.apache.org/log4j/2.x/) (a popular logging subsystem for the JVM). 

The appender pushes log entries to LogDNA via HTTPS

## How To
This is what a log4j2.xml might look like:

    <?xml version="1.0" encoding="UTF-8"?>
    <Configuration>
        <Appenders>
            <LogDNAAppender name="LogDNA">
                <token>{Your ingest token}</token>
                <appName>LogDNA-Log4j2-Test</appName>
                <includeStacktrace>true</includeStacktrace>
                <sendMDC>true</sendMDC>
                <PatternLayout pattern="%date %level method: %class{1}.%method (%file:%line) - %message%n"/>
            </LogDNAAppender>
            <Console name="STDOUT" target="SYSTEM_OUT">
                <PatternLayout pattern="%date %level method: %class{1}.%method (%file:%line) - %message%n"/>
            </Console>
        </Appenders>
        <Loggers>
            <Root level="all">
                <AppenderRef ref="STDOUT" level="debug"/>
                <AppenderRef ref="LogDNA" level="debug"/>
            </Root>
        </Loggers>
    </Configuration>

## Configure
* `<appName>LogDNA-Log4j2-Test</appName>` set this for good log management in LogDNA
* `<ingestKey>{Your ingest token}</ingestKey>` signup to LogDNA and find this in your account profile
* `<includeStacktrace>true</includeStacktrace>` this library can send multiline stacktraces
* `<sendMDC>true</sendMDC>` copies over Log4j2's Context Data as LogDNA Metadata which are then indexed and searchable.
    
## More Info

* The log line displays the thread, the logger (class) and the message
* LogDNA's metadata is populated with the logger as an indexable/searchable property.
* The HTTP Transport is done by the very lightweight [DavidWebb](https://github.com/hgoebl/DavidWebb) REST Library and so doesn't introduce bulky dependencies

## Using the MDC for Meta Data

The combination of Context Data and LogDNA's metadata support is pretty powerful and means you can correlate web-requests, or a userId, or something else that happens in a thread in your application.  

For example, doing this _anywhere_ in your application...

	ThreadContext.put("customerId", "C-1");
	ThreadContext.put("requestId", "R-1001");

... means that you can then go and search for say, **that** customer in logDNA like this:

	meta.customerId:"C-1"
