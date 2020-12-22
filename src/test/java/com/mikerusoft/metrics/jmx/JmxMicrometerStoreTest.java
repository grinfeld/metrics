package com.mikerusoft.metrics.jmx;

import com.mikerusoft.metrics.micrometer.JmxMicrometerStore;
import com.mikerusoft.metrics.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JmxMicrometerStoreTest {

    public static final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();

    @Data
    @AllArgsConstructor
    public static class Pair<K,V> {
        K key;
        V value;
    }

    private static final String host = Utils.getDashesHostname();

    @Test
    void whenIncreaseCounter_expectedMicrometerMetricWithCounterEquals1() throws Exception {
        JmxMicrometerStore metrics = new JmxMicrometerStore("test", false);
        metrics.increaseCounter("counter");

        ObjectName objectName = new ObjectName("metrics:name=test_counter.host." + host);

        MBeanInfo mBeanInfo = platform.getMBeanInfo(objectName);
        Map<String, Object> result = Arrays.stream(mBeanInfo.getAttributes())
                .map(t -> new Pair<>(t.getName(), getAttribute(objectName, t)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> k1));


        assertThat(result).isNotNull().hasSize(6);
        assertThat(result.get("Count")).isNotNull().isEqualTo(1L);

        platform.unregisterMBean(objectName);
    }

    @Test
    void whenReportGaugeIs10_expectedMicrometerMetricWithGaugeEquals10() throws Exception {
        JmxMicrometerStore metrics = new JmxMicrometerStore("test", false);
        metrics.gauge("gauge", 10D);

        ObjectName objectName = new ObjectName("metrics:name=test_gauge.host." + host);

        MBeanInfo mBeanInfo = platform.getMBeanInfo(objectName);
        Map<String, Object> result = Arrays.stream(mBeanInfo.getAttributes())
                .map(t -> new Pair<>(t.getName(), getAttribute(objectName, t)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> k1));


        assertGauge(result, 10D);

        platform.unregisterMBean(objectName);
    }

    @Test
    void whenReport2ValuesForGauge_expectedMicrometerMetricWithLastGaugeReturned() throws Exception {
        JmxMicrometerStore metrics = new JmxMicrometerStore("test", false);
        metrics.gauge("gauge", 10D);

        ObjectName objectName = new ObjectName("metrics:name=test_gauge.host." + host);

        MBeanInfo mBeanInfo = platform.getMBeanInfo(objectName);
        Map<String, Object> result = Arrays.stream(mBeanInfo.getAttributes())
                .map(t -> new Pair<>(t.getName(), getAttribute(objectName, t)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> k1));
        assertGauge(result, 10D);

        metrics.gauge("gauge", 2D);
        mBeanInfo = platform.getMBeanInfo(objectName);
        result = Arrays.stream(mBeanInfo.getAttributes())
                .map(t -> new Pair<>(t.getName(), getAttribute(objectName, t)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> k1));


        assertGauge(result, 2D);

        platform.unregisterMBean(objectName);
    }

    private static void assertGauge(Map<String, Object> result, double v) {
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get("Number")).isNotNull().isEqualTo(v);
        assertThat(result.get("Value")).isNotNull().isEqualTo(v);
    }

    @Test
    void whenReportTime_expectedMicrometerTime() throws Exception {
        long now = System.currentTimeMillis();
        long startTime = now - TimeUnit.SECONDS.toMillis(10L);
        JmxMicrometerStore metrics = new JmxMicrometerStore("test", false);
        metrics.recordTime("timer", null, Duration.ofMillis(now - startTime));

        ObjectName objectName = new ObjectName("metrics:name=test_timer.host." + host);

        MBeanInfo mBeanInfo = platform.getMBeanInfo(objectName);
        Map<String, Object> result = Arrays.stream(mBeanInfo.getAttributes())
                .map(t -> new Pair<>(t.getName(), getAttribute(objectName, t)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> k1));

        assertThat(result).isNotNull().hasSize(17);
        assertThat(result.get("DurationUnit")).isNotNull().isEqualTo("milliseconds");
        assertThat(result.get("Min")).isNotNull().isEqualTo(10000D);
        assertThat(result.get("Mean")).isNotNull().isEqualTo(10000D);
        assertThat(result.get("Max")).isNotNull().isEqualTo(10000D);
        assertThat(result.get("Count")).isNotNull().isEqualTo(1L);

        platform.unregisterMBean(objectName);
    }

    private static Object getAttribute(ObjectName objectName, MBeanAttributeInfo t) {
        try {
            return platform.getAttribute(objectName, t.getName());
        } catch (Exception e) {
            return Utils.rethrowRuntime(e);
        }
    }
}