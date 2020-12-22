package com.mikerusoft.metrics.di;

import com.mikerusoft.metrics.MetricStore;

import java.time.Duration;
import java.util.function.Supplier;

public class MetricFactory {

    private MetricFactory() {}

    // we need this for use-case when we want to use metrics in class which hasn't been
    // bound via Guice (note: this module SHOULD exists in application in any case and registered in Guice, since it initialized during Guice configure state)
    // for example, classes that created with "new"
    public static MetricStore getMetricStore() {
        return instance;
    }
    // don't do it final !!!
    private static MetricStore instance = new MetricStore() {
        @Override
        public void increaseCounter(String name, Boolean error, long value) {
            // do nothing
        }

        @Override
        public void recordTime(String name, Boolean error, Duration duration) {
            // do nothing
        }

        @Override
        public void gauge(String name, Double value, Boolean error) {
            // do nothing
        }
    };
}
