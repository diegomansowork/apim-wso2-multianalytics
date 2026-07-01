package com.wso2.apim.analytics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;

public class CompositeCounterMetric implements CounterMetric {

    private static final Logger log = LogManager.getLogger(CompositeCounterMetric.class);

    private final String name;
    private final MetricSchema schema;
    private final CounterMetric elkCounter;
    private final CounterMetric moesifCounter;

    public CompositeCounterMetric(String name, MetricSchema schema,
                                   CounterMetric elkCounter, CounterMetric moesifCounter) {
        this.name = name;
        this.schema = schema;
        this.elkCounter = elkCounter;
        this.moesifCounter = moesifCounter;
    }

    @Override
    public int incrementCount(MetricEventBuilder builder) throws MetricReportingException {
        // ELK: serializa el evento como JSON a través de log4j2 (apim_metrics.log → Filebeat → Logstash)
        try {
            elkCounter.incrementCount(builder);
        } catch (Exception e) {
            log.error("Failed to publish event to ELK", e);
        }

        // Moesif: envía el evento a la EventQueue de Moesif para publicación HTTP asíncrona
        try {
            moesifCounter.incrementCount(builder);
        } catch (Exception e) {
            log.error("Failed to publish event to Moesif", e);
        }

        return 0;
    }

    @Override
    public String getName() { return name; }

    @Override
    public MetricSchema getSchema() { return schema; }

    // El gateway usa este builder para rellenar los campos del evento.
    // Retornamos el builder de ELK: es el más genérico y acepta todos los campos
    // estándar que el gateway produce (API name, response code, latency, etc.).
    // MoesifCounterMetric.incrementCount llama internamente builder.build() para
    // obtener el mapa de atributos — funciona con cualquier MetricEventBuilder.
    @Override
    public MetricEventBuilder getEventBuilder() {
        return elkCounter.getEventBuilder();
    }
}