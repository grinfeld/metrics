package com.mikerusoft.metrics.di.guice;

import com.mikerusoft.metrics.MetricIntercepter;
import com.mikerusoft.metrics.MetricStore;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class GuiceMetricMethodIntercepter implements MethodInterceptor {

    private MetricIntercepter metrics;

    @Inject
    public GuiceMetricMethodIntercepter(MetricStore metrics) {
        this.metrics = new MetricIntercepter(metrics);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return this.metrics.invoke(invocation.getThis(), invocation.getMethod(), invocation.getArguments(), invocation::proceed);
    }

}
