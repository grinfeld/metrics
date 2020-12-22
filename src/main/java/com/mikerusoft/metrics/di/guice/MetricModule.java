package com.mikerusoft.metrics.di.guice;

import com.mikerusoft.metrics.MetricStore;
import com.mikerusoft.metrics.annotations.AddMetric;
import com.mikerusoft.metrics.di.MetricFactory;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static com.google.inject.matcher.Matchers.any;

@Slf4j
public class MetricModule extends AbstractModule {

    private final Supplier<MetricStore> metricStoreSupplier;

    private static final Field instance;
    static {
        Field field = null;
        try {
            field = MetricFactory.class.getDeclaredField("instance");
            field.setAccessible(true);
        } catch (Exception e) {
            log.error("Failed to init MetricFactory", e);
        }
        instance = field;
    }

    // chosen to set factory field via Reflection, to leave this field private for external use ( weak decision :( )
    private static void initMetricFactory(MetricStore metricStore) {
        try {
            instance.set(null, metricStore);
        } catch (Exception e) {
            log.error("Failed to set metricStore in MetricFactory", e);
        }
    }

    public MetricModule(Supplier<MetricStore> metricStoreSupplier) {
        this.metricStoreSupplier = metricStoreSupplier;
    }

    @Override
    protected void configure() {
        MetricStore metrics = metricStoreSupplier.get();
        initMetricFactory(metrics);
        bindInterceptor(any(), Matchers.annotatedWith(AddMetric.class), new GuiceMetricMethodIntercepter(metrics));
        bind(MetricStore.class).toInstance(metrics);
    }
}
