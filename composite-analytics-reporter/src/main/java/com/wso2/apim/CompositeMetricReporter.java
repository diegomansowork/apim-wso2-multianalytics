package com.wso2.apim.analytics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.AbstractMetricReporter;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricReporter;
import org.wso2.am.analytics.publisher.reporter.MetricReporterFactory;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.TimerMetric;

import java.util.HashMap;
import java.util.Map;

public class CompositeMetricReporter extends AbstractMetricReporter {

    private static final Logger log = LogManager.getLogger(CompositeMetricReporter.class);

    private final MetricReporter elkReporter;
    private final MetricReporter moesifReporter;

    public CompositeMetricReporter(Map<String, String> properties) throws MetricCreationException {
        super(properties);

        MetricReporterFactory factory = MetricReporterFactory.getInstance();

        this.elkReporter = factory.createLogMetricReporter(properties);

        Map<String, String> moesifProps = new HashMap<>(properties);
        moesifProps.put("type", "moesif");
        this.moesifReporter = factory.createMoesifMetricReporter(moesifProps);

        log.info("CompositeMetricReporter initialized — publishing to ELK and Moesif simultaneously");
    }

    @Override
    protected void validateConfigProperties(Map<String, String> properties) throws MetricCreationException {
        if (properties.get("moesifKey") == null || properties.get("moesifKey").isEmpty()) {
            throw new MetricCreationException(
                "CompositeMetricReporter requires 'moesifKey' in [apim.analytics.properties]");
        }
    }

    @Override
    protected CounterMetric createCounter(String name, MetricSchema schema) throws MetricCreationException {
        CounterMetric elkCounter    = elkReporter.createCounterMetric(name, schema);
        CounterMetric moesifCounter = moesifReporter.createCounterMetric(name, schema);
        return new CompositeCounterMetric(name, schema, elkCounter, moesifCounter);
    }

    @Override
    protected TimerMetric createTimer(String name) {
        return null;
    }
}