package com.mikerusoft.metrics.functions;

@FunctionalInterface
public interface CallExecution<V> {
    V execute();
}
