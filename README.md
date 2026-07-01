# WSO2 APIM MultiAnalytics

This customization allows APIM to send metrics to both ELK and Moesif.

> Note: The MoesifCounterMetric internally calls builder.build() to extract the attribute map and enqueue it (it doesn't perform a type check on the builder). However, if that contract changes in a future version of APIM, the separate try/catch block in CompositeCounterMetric.incrementCount prevents a failure in one publisher from crashing the other.

## To use the customization, follow the instructions below

1. Compile the source code.

```bash
mvn clean package
```

2. Copy the JAR to the APIM classpath

```bash
cp target/composite-analytics-reporter-1.0.0.jar \
   $APIM_HOME/repository/components/lib/
```

3. Restart APIM

```bash
sh $APIM_HOME/bin/api-manager.sh restart
```

4. Check the logs at startup:

   INFO CompositeMetricReporter - CompositeMetricReporter initialized — publishing to ELK and Moesif simultaneously


## Configurations

Add these configs in gateways:

```yaml
[apim.analytics]
enable = true

[apim.analytics.properties]
"publisher.reporter.class" = "com.wso2.apim.analytics.CompositeMetricReporter"
moesifKey = "eyJ***"
moesif_base_url = "https://api.moesif.net"
```
In the log4j2.properties these configs should appear:

```properties
appender.APIM_METRICS_APPENDER.type = RollingFile
appender.APIM_METRICS_APPENDER.name = APIM_METRICS_APPENDER
appender.APIM_METRICS_APPENDER.fileName = ${sys:carbon.home}/repository/logs/apim_metrics.log
appender.APIM_METRICS_APPENDER.filePattern = ${sys:carbon.home}/repository/logs/apim_metrics-%d{MM-dd-yyyy}-%i.log
appender.APIM_METRICS_APPENDER.layout.type = PatternLayout
appender.APIM_METRICS_APPENDER.layout.pattern = %d{HH:mm:ss,SSS} [%X{ip}-%X{host}] [%t] %5p %c{1} %m%n
appender.APIM_METRICS_APPENDER.policies.type = Policies
appender.APIM_METRICS_APPENDER.policies.time.type = TimeBasedTriggeringPolicy
appender.APIM_METRICS_APPENDER.policies.time.interval = 1
appender.APIM_METRICS_APPENDER.policies.time.modulate = true
appender.APIM_METRICS_APPENDER.policies.size.type = SizeBasedTriggeringPolicy
appender.APIM_METRICS_APPENDER.policies.size.size=1000MB
appender.APIM_METRICS_APPENDER.strategy.type = DefaultRolloverStrategy
appender.APIM_METRICS_APPENDER.strategy.max = 10

appenders=APIM_METRICS_APPENDER, ....

logger.reporter.name = org.wso2.am.analytics.publisher.reporter.elk
logger.reporter.level = INFO
logger.reporter.appenderRef.APIM_METRICS_APPENDER.ref = APIM_METRICS_APPENDER
logger.reporter.additivity = false

loggers = reporter, ....
```

ELK installation guide:
<https://apim.docs.wso2.com/en/latest/monitoring/api-analytics/on-prem/elk-installation-guide/>
