package com.mikerusoft.metrics.di.guice.timegroup;

import com.mikerusoft.metrics.MetricStore;
import com.mikerusoft.metrics.timegroup.TimegroupMetricStore;
import com.mikerusoft.metrics.utils.Utils;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class StatsdProvider {

    private static final String DEF_STATSD_HOST = "localhost";
    static final Integer DEF_STATSD_PORT = 8125;

    public static Supplier<MetricStore> getMetricStore(Config config) {
        return () -> {
            if (getOrDefault(config, "monitoring.statsd.enabled", false)) {
                String machineName = Utils.getDashesHostname();
                log.info("Starting with metrics on machine '{}' with prefix '{}', host '{}' and port '{}'",
                        machineName, getOrDefault(config, "monitoring.prefix", ""),
                        getOrDefault(config, "monitoring.statsd.host", DEF_STATSD_HOST),
                        getOrDefault(config, "monitoring.statsd.port", DEF_STATSD_PORT)
                );
                return new TimegroupMetricStore(
                        new NonBlockingStatsDClient(
                                machineName + "." + getOrDefault(config, "monitoring.prefix", ""),
                                getOrDefault(config, "monitoring.statsd.host", DEF_STATSD_HOST),
                                getOrDefault(config, "monitoring.statsd.port", DEF_STATSD_PORT)
                        )
                );
            } else {
                return new TimegroupMetricStore(new NoOpStatsDClient());
            }
        };
    }

    private static <T> T getOrDefault(Config config, String name, T def) {
        if (config.hasPathOrNull(name)) {
            return (T) config.getAnyRef(name);
        }
        return def;
    }
}

