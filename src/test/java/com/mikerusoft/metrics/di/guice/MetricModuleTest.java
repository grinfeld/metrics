package com.mikerusoft.metrics.di.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mikerusoft.metrics.MetricStore;
import com.mikerusoft.metrics.di.MetricFactory;
import com.mikerusoft.metrics.timegroup.TimegroupMetricStore;
import com.timgroup.statsd.NoOpStatsDClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class MetricModuleTest {

    @Test
    void whenMetricStoreCreatedViaGuice_expectedTheSameInstanceInGuiceAndMetricFactory() {
        TimegroupMetricStore store = spy(new TimegroupMetricStore(new NoOpStatsDClient()));
        Injector injector = Guice.createInjector(new MetricModule(() -> store));
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        MetricFactory.getMetricStore().increaseCounter("me");
        verify(store, times(1)).increaseCounter(capture.capture());
        assertEquals("me", capture.getValue());
        assertThat(MetricFactory.getMetricStore()).isSameAs(injector.getInstance(MetricStore.class));
    }

}