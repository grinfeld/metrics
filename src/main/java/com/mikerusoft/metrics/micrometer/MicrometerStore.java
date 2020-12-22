package com.mikerusoft.metrics.micrometer;

import com.mikerusoft.metrics.MetricStore;
import com.mikerusoft.metrics.utils.Utils;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
public abstract class MicrometerStore implements MetricStore {

    public static final String DELIMITER = "_";
    private final String prefix;
    private final String host;
    protected final MeterRegistry registry;

    public MicrometerStore(String prefix, MeterRegistry registry, boolean disableHost) {
        this.registry = registry;
        this.host = disableHost ? null : Utils.getDashesHostname();
        this.prefix = prefix;
    }

    @Override
    public void increaseCounter(String name, Boolean error, long value) {
        try {
            registry.counter(buildName(name, error), hostsTag()).increment(value);
        } catch (Exception e) {
            log.trace("Failed to report counter", e);
        }
    }

    @Override
    public void recordTime(String name, Boolean error, Duration duration) {
        if (duration == null)
            return;
        try {
            registry.timer(buildName(name, error), hostsTag()).record(duration);
        } catch (Exception e) {
            log.trace("Failed to report record time", e);
        }
    }

    @Override
    public void gauge(String name, Double value, Boolean error) {
        try {
            Iterable<Tag> tags = hostsTag();
            String finalName = buildName(name, error);
            // so... micrometer encapsulates its inner structure with package level scope
            // in case of Gauge, we want always to get the latest value, so seems, currently
            // there is only one way to do it -> create gauge -> get its Id and remove it from stored gauges
            // maybe more "clean code" solution, to use some ValueHolder and inner Map with such holders
            // and update this holder value with the last. If someone interested to do so - good luck :)
            try {
                Gauge gauge = Gauge.builder(finalName, () -> value).tags(tags).register(registry);
                registry.remove(gauge.getId());
            } catch (Exception ignore) {
                // ignore
            }
            registry.gauge(finalName, tags, value);
        } catch (Exception e) {
            log.trace("Failed to gauge", e);
        }
    }

    public String buildName(String name, Boolean error) {
        String metricName = buildName(name, error, DELIMITER);
        if (!Utils.isEmptyTrimmed(prefix))
            metricName = prefix + DELIMITER + metricName;
        return metricName;
    }

    private Iterable<Tag> hostsTag() {
        return host == null ? new ArrayList<>(0) : Collections.singletonList(Tag.of("host", host));
    }
}
