Metrics
=============================

This package contains metrics (statsd) dependency and Guice annotation to use with Guice DI

Before any start, the easiest way to use metrics - it's via the singleton: ``MetricFactory.getMetricStore()``

### Why

1. Because we can
1. Where is possible - move metrics code outside of method business logic
1. Make all metrics to be in the same form (same prefix, same suffixes - error, succ) and so on
1. Make metrics to work with lambdas and streams
1. Failures in metrics should never cause failure in running business logic code, so we can to enforce it when everybody uses the same package and such behavior enforced inside package implementation
1. easy (almost) to change metric implementation without affecting code 
1. easy (almost) to change DI framework 

### How to

The most of the dependencies are in scope `provided`, means you need to add relevant dependencies into your project directly. 
The reasons are 
1. to avoid dependency collisions, if you already have such dependencies
2. to allow using specific implementation (statsd, jmx, prometheus) without embedding non-relevant dependencies into the project

For example, to use guice, you are required to add inside your own pom (gradle,...) 

        <dependency>
            <groupId>com.typesafe</groupId>
            <artifactId>config</artifactId>
            <version>${typesafe.config.version}</version>
        </dependency>
        <dependency>
            <groupId>com.google.inject</groupId>
            <artifactId>guice</artifactId>
            <version>${guice.version}</version>
        </dependency>

For statsd you need to add to your dependencies: 

        <dependency>
            <groupId>com.timgroup</groupId>
            <artifactId>java-statsd-client</artifactId>
            <version>${statsd-client.version}</version>
        </dependency>

For jmx you need to add to your dependencies:

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-jmx</artifactId>
            <version>${micrometer.version}</version>
        </dependency>

For prometheus you need to add to your dependencies:

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <version>${micrometer.version}</version>
        </dependency>

* In case of prometheus - you can define port for exposing web end-point for metric scraping

There is small difference in naming between statsd and jmx. statsd uses point `.` as delimiter, and jmx `_` underscore.

This library uses `slf4j`, so you need to include one of slf4j binders (for log4j or logback). For example, for logback:

        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>

The **statsd** implementation uses following configuration 

```
monitoring {
  statsd {
    enabled = true
    host = localhost
    port = 8125
  }
}
```

By default, it's **NOT** ``enabled``

* Works only by using Guice DI.
* Annotations are limited to only 1 type of binding
```
bind(SomeInterface.class).to(SomeClass.class);

bind(SomeClass.class);
```
* and it DOESN'T work for `@Provides` method and binding via existed instance `bind(SomeInterface.class).toInstance(new SomeClass());` - in such case use  `MetricFactory.getMetricStore()` inside your code -> annotations won't work
  
* Note: spring and dropwizard for example have their own annotations for metrics, so no need to implement any annotation

You should add dependency to pom and ``MetricModule`` to your ``Guice.createInjector(....)`` (or other Guice ways to add modules)

The main prefix for metrics is defined in configuration file

Example:

    monitoring {
        .....
        .....
        prefix = "prefix"
        .....
        .....
    }

Adding Guice MetricsModule with statsd to project:
        
        Config config = ConfigFactory.load();
        Injector injector = Guice.createInjector(
            ..........,
            new MetricModule(() -> StatsdProvider.getMetricStore(config)),
            ..........
        );

or jmx

        Injector injector = Guice.createInjector(
            ..........,
            new MetricModule(() -> new JmxMicrometerStore("prefix")),
            ..........
        );

or prometheus


        Injector injector = Guice.createInjector(
            ..........,
            new MetricModule(() -> new PrometheusMicrometerStore("prefix", true, 8080)),
            ..........
        );

There are 2 main scenarios when we want to record metrics:

1. Recording metrics for some method (from the beginning to the end of method) - the preferable way is to use annotation ``@AddMetric``  
1. Recording metrics for specific line (or few) of code or for objects created NOT by Guice (directly or 3rd party)

* Framework, automatically, adds **prefix** and **type** of metric (currently, only **timer** and **counter** are supported) to metrics path

#### Annotations

* Annotations works only on ``public`` or ``package protected`` methods
* Annotations works only with methods in objects created via Guice (if you used ``new`` keyword in the middle of your code for creating object - it won't work)

There are 5 parameters:

1. name (_String_ - **Required**) - name of metrics to appear in statsd/graphite/grafana
1. recordTime (_boolean_ - **Optional**) - defines if to record time for method annotated with ``@AddMetric``
1. countSuccess (_boolean_ - **Optional**) - defines if to count success executions for method annotated with ``@AddMetric``
1. countException (_boolean_ - **Optional**) - defines if to count failure (exception is thrown in method) executions for method annotated with ``@AddMetric``
1. withParams (_array_ of ``@Param``) - array of parameters to be resolved in order to extract some additional suffix (see explanation later in README)

* **Note:** if method has **catch** block without rethrowing exception, current AOP mechanism (based on Guice) won't add **error** metrics

```java
public class SomeClass {

    //.......
    //.......

    @AddMetric(name="mymetric", recordTime=true, countException=true)
    public void foo() {
        //.......
        //.......
        //.......
    }
}
```
    
* All success timers and counters are sent with suffix "succ"  
* In case of timers, we are not able to define different timers with errors, so in case of success we'll see ``...timers.someName.succ`` and if the same method failed with exception it will be ``...timers.someName.err``

##### Resolving method parameters

In some case we want to add suffix, by some parameter or even to resolve parameter (usually, POJO) by calling getter on it

**1.** adding method parameter value for adding to metric path 

```java
public class SomeClass {

    //.......
    //.......

    @AddMetric(name="mymetric", countSuccess=true, withParams = @Param(name="sectionId"))
    public void foo(@MetricParam(name="sectionId") String sectionId) { // let's assume sectionId=12345678
        //.......
        //.......
        //.......
    }
}
``` 

metrics name will be as following: ``...count.mymetric.12345678.succ``

**2.** resolving method parameter value for adding to metric path

```java
public class UploadEvent {
    private Integer sectionId;

    //........
    //........
    //........

    public Integer getSectionId() {
        return sectionId;
    }

    //........
    //........
    //........
}

public class SomeClass {

    //.......
    //.......
    
    @AddMetric(name="mymetric", countSuccess=true, withParams = @Param(name="sectionId", expr="getSectionId"))
    public void foo(@MetricParam(name="sectionId") UploadEvent uploadEvent) { // let's assume uploadEvent.getSectionId() returns 12345678
        //.......
        //.......
        //.......
    }
}
```

metrics name will be as following: ``...count.mymetric.12345678.succ``

**3.** resolving method parameter value for adding to metric path by calling method on result parameter

```java
public class UploadEvent {
    private Section section;

    //........
    //........
    //........

    public Section getSection() {
        return section;
    }

    //........
    //........
    //........
    
    public class Section {
        private Integer id;
        public Integer getId() {
            return id;
        }
    }
}
public class SomeClass {

    //.......
    //.......
    
    @AddMetric(name="mymetric", countSuccess=true, withParams = @Param(name="sectionId", expr="getSection#getId"))
    public void foo(@MetricParam(name="sectionId") UploadEvent uploadEvent) { // let's assume uploadEvent.getSectionId() returns 12345678
        //.......
        //.......
        //.......
    }
}
```

metrics name will be as following: ``...count.mymetric.12345678.succ``

_Notes:_
* **expr** should always be method without any argument parameters (a.k.a simple getter)
* **Never!!!!!**, but **Never!!!!** use/call methods for resolving parameters for metrics with time consumed operations (going to DB, another service, disk and etc). Use only simple POJOs getter calls, since it could affect method execution time !!!!!

#### Calling to metrics directly

* Reminder: it's still works only for projects with use of Guice

Use ``MetricFactory.getMetricStore()`` to get MetricStore reference (it's singleton) and created during MetricModule initialization

You get the ``MetricStore`` interface with following signature:

```java
public interface MetricStore {
    void increaseCounter(String name, Execution runnable) ;
    void increaseCounter(String name, Boolean error) ;
    void increaseCounter(String name) ;
    <V> V increaseCounter(String name, CallExecution<V> callable);
    void recordTime(String name, Execution runnable);
    void recordTime(String name, Boolean error, Long start);
    <V> V recordTime(String name, CallExecution<V> callable);
    void gauge(String name, Supplier<Double> supplier, Boolean error);
    void removeGauge(String name, Boolean error);
}
```

Usage: 

``increaseCounter`` or ``recordTime`` could be called directly or wrap lambda by metric (counter or recordTime) 

```java
public class Some {

    public void foo1() {
        MetricFactory.getMetricStore().increaseCounter("myCounter", false);
    }

    public void foo2() {
        MetricFactory.getMetricStore().recordTime("singleLine.write", 
            () -> System.out.println("Doing some work") 
        );
    }
}
```
