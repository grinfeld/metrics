package com.mikerusoft.metrics;

import com.mikerusoft.metrics.functions.CallExecution;
import com.mikerusoft.metrics.functions.Execution;
import com.mikerusoft.metrics.utils.Utils;

import java.time.Duration;

public interface MetricStore {
    String SUCCESS_PREFIX = "succ";
    String ERROR_PREFIX = "err";

    default void increaseCounter(String name, Execution runnable) {
        increaseCounter(name, () -> {
            runnable.execute();
            return null;
        });
    }

    default void increaseCounter(String name, Boolean error) {
        increaseCounter(name, error, 1L);
    }

    void increaseCounter(String name, Boolean error, long value);

    default void increaseCounter(String name) {
        increaseCounter(name, (Boolean) null);
    }

    default <V> V increaseCounter(String name, CallExecution<V> callable) {
        try {
            V call = callable.execute();
            increaseCounter(name, false);
            return call;
        } catch (Throwable t) {
            increaseCounter(name, true);
            return Utils.rethrowRuntime(t);
        }
    }

    default void recordTime(String name, Execution runnable) {
        recordTime(name, () -> {
            runnable.execute();
            return null;
        });
    }

    default void recordTime(String name, Boolean error, Long start) {
        recordTime(name, error, Duration.ofMillis(System.currentTimeMillis() - start));
    }

    void recordTime(String name, Boolean error, Duration duration);

    default void recordTime(String name, Long start) {
        recordTime(name, null, start);
    }

    default <V> V recordTime(String name, CallExecution<V> callable) {
        long start = System.currentTimeMillis();
        try {
            V call = callable.execute();
            recordTime(name, false, start);
            return call;
        } catch (Throwable t) {
            recordTime(name, true, start);
            return Utils.rethrowRuntime(t);
        }
    }

    default void gauge(String name, Double value) {
        gauge(name, value, (Boolean)null);
    }

    void gauge(String name, Double value, Boolean error);

    default <V> V gauge(String name, Double value, CallExecution<V> callable) {
        try {
            V call = callable.execute();
            gauge(name, value, false);
            return call;
        } catch (Throwable t) {
            gauge(name, value, true);
            return Utils.rethrowRuntime(t);
        }
    }

    default void gauge(String name, Double value, Execution runnable) {
        gauge(name, value, () -> {
            runnable.execute();
            return null;
        });
    }

    default void removeGauge(String name, Boolean error) {
        // do nothing
    }

    default String buildName(String name, Boolean error, String delimiter) {
        StringBuilder sb = new StringBuilder(name);
        if (error != null) {
            if (error) sb.append(delimiter).append(ERROR_PREFIX);
            else sb.append(delimiter).append(SUCCESS_PREFIX);
        }
        return sb.toString();
    }
}
