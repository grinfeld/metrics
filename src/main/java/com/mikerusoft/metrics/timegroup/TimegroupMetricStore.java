package com.mikerusoft.metrics.timegroup;

import com.mikerusoft.metrics.MetricStore;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class TimegroupMetricStore implements MetricStore {

    public static final String DELIMITER = ".";
    private final StatsDClient client;

    public TimegroupMetricStore(StatsDClient client) {
        this.client = client;
    }

    @Override
    public void increaseCounter(String name, Boolean error, long value) {
        if (clientNotExists())
            return;

        try {
            client.count(buildName(name, error), value);
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    @Override
    public void recordTime(String name, Boolean error, Duration duration) {
        if (clientNotExists())
            return;
        if (duration == null)
            return;

        try {
            client.recordExecutionTime(buildName(name, error), duration.toMillis());
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    @Override
    public void gauge(String name, Double value, Boolean error) {
        if (clientNotExists())
            return;
        try {
            client.gauge(buildName(name, error), value);
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    private String buildName(String name, Boolean error) {
        return this.buildName(name, error, DELIMITER);
    }

    private boolean clientNotExists() {
        if (client != null)
            return false;

        log.trace("No client  - check configuration");
        return true;
    }
}
