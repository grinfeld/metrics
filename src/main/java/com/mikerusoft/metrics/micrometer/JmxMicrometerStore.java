package com.mikerusoft.metrics.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmxMicrometerStore extends MicrometerStore {

    public JmxMicrometerStore(String prefix, boolean disableHost) {
        super(prefix, new JmxMeterRegistry(new JmxConfig() {
            @Override
            public String prefix() {
                return prefix;
            }

            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public void requireValid() throws ValidationException {
                // do nothing
            }
        }, Clock.SYSTEM), disableHost);
    }
}
