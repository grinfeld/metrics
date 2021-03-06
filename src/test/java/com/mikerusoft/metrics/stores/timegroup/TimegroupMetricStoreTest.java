package com.mikerusoft.metrics.stores.timegroup;

import com.mikerusoft.metrics.functions.Execution;
import com.mikerusoft.metrics.timegroup.TimegroupMetricStore;
import com.timgroup.statsd.StatsDClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimegroupMetricStoreTest {

    @Nested
    class CounterTest {

        @Test
        void whenExceptionThrownInIncreaseCounter_expectedNothing() {
            StatsDClient mock = mock(StatsDClient.class);

            doThrow(new NullPointerException()).when(mock).count(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);
            Assertions.assertDoesNotThrow(() -> timegroupMetricStore.increaseCounter("abc", true));
        }

        @Test
        void whenIncreaseCounterReturnsResultValueOfCallable_expectedValueOfCallable() {
            StatsDClient mock = mock(StatsDClient.class);

            doNothing().when(mock).count(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);
            String result = timegroupMetricStore.increaseCounter("abc", () -> "blablabla");

            Assertions.assertEquals("blablabla", result );
        }

        @Test
        void whenCallableThrowsExceptionInIncreaseCounter_expectedToThrowSameException() {
            StatsDClient mock = mock(StatsDClient.class);

            doNothing().when(mock).count(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);

            assertThrows(IllegalArgumentException.class, () -> timegroupMetricStore.increaseCounter("abc", new Execution() {
                @Override
                public void execute() {
                    throw new IllegalArgumentException();
                }
            }));
        }
    }

    @Nested
    class TimerTest {
        @Test
        void whenExceptionThrownInIncreaseCounter_expectedNothing() {
            StatsDClient mock = mock(StatsDClient.class);

            doThrow(new NullPointerException()).when(mock).recordExecutionTime(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);
            Assertions.assertDoesNotThrow(() -> timegroupMetricStore.recordTime("abc", true, System.currentTimeMillis()));
        }

        @Test
        void whenIncreaseCounterReturnsResultValueOfCallable_expectedValueOfCallable() {
            StatsDClient mock = mock(StatsDClient.class);

            doNothing().when(mock).recordExecutionTime(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);
            String result = timegroupMetricStore.recordTime("abc", () -> "blablabla");

            Assertions.assertEquals("blablabla", result );
        }

        @Test
        void whenCallableThrowsExceptionInIncreaseCounter_expectedToThrowSameException() {
            StatsDClient mock = mock(StatsDClient.class);

            doNothing().when(mock).recordExecutionTime(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);

            assertThrows(IllegalArgumentException.class, () -> timegroupMetricStore.recordTime("abc", new Execution() {
                @Override
                public void execute() {
                    throw new IllegalArgumentException();
                }
            }));
        }
    }

    @Nested
    class GaugeTest {
        @Test
        void whenExceptionThrownInIncreaseCounter_expectedNothing() {
            StatsDClient mock = mock(StatsDClient.class);

            doThrow(new NullPointerException()).when(mock).gauge(anyString(),anyLong());
            TimegroupMetricStore timegroupMetricStore = new TimegroupMetricStore(mock);

            Assertions.assertDoesNotThrow(() -> timegroupMetricStore.gauge("abc", 1D,true));
        }
    }
}
