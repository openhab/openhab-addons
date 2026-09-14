/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smartmeter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.MeterDevice;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.MeterValueListener;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.io.transport.serial.SerialPortManager;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class TestMeterReading {

    private static final Duration OUTER_RETRY_DELAY = Duration.ofSeconds(2);

    private RetrySuppressingExecutor createRetrySuppressingExecutor(int threadCount) {
        return new RetrySuppressingExecutor(threadCount, OUTER_RETRY_DELAY);
    }

    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_PERIOD = Duration.ofMillis(100);

    @AfterEach
    public void resetRxJavaPlugins() {
        RxJavaPlugins.reset();
    }

    @Test
    public void testContinuousReading() {
        final int executionCount = 5;
        MockMeterReaderConnector connector = getMockedConnector(false, () -> new Object());
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        CountDownLatch valuesChanged = new CountDownLatch(executionCount);
        RetrySuppressingExecutor executorService = createRetrySuppressingExecutor(1);

        doAnswer(invocation -> {
            valuesChanged.countDown();
            return null;
        }).when(changeListener).valueChanged(any());
        meter.addValueChangeListener(changeListener);

        Disposable disposable = meter.readValues(EVENT_TIMEOUT.toMillis(), executorService, READ_PERIOD);

        try {
            await(valuesChanged, "Did not receive the expected meter values");
            executorService.suppressNextRetry();
        } finally {
            dispose(disposable, executorService);
        }

        verify(changeListener, atLeast(executionCount)).valueChanged(any());
        verify(changeListener, never()).errorOccurred(any());
    }

    @Test
    public void testRetryHandling() {
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            throw new IllegalArgumentException();
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        CountDownLatch readingError = new CountDownLatch(1);
        RetrySuppressingExecutor executorService = createRetrySuppressingExecutor(1);

        doAnswer(invocation -> {
            executorService.suppressNextRetry();
            readingError.countDown();
            return null;
        }).when(changeListener).errorOccurred(any());
        meter.addValueChangeListener(changeListener);

        Disposable disposable = meter.readValues(EVENT_TIMEOUT.toMillis(), executorService, READ_PERIOD);

        try {
            await(readingError, "Did not receive the expected reading error");
            executorService.awaitRetrySuppressed(EVENT_TIMEOUT);
        } finally {
            dispose(disposable, executorService);
        }

        verify(changeListener, times(1)).errorOccurred(any());
        verify(connector, times(ConnectorBase.NUMBER_OF_RETRIES)).retryHook(anyInt());
    }

    @Test
    public void testTimeoutHandling() {
        final int timeout = 1000;
        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch releaseRead = new CountDownLatch(1);
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            readStarted.countDown();
            awaitUninterruptibly(releaseRead);
            return new Object();
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        CountDownLatch timeoutOccurred = new CountDownLatch(1);
        RetrySuppressingExecutor executorService = createRetrySuppressingExecutor(2);

        doAnswer(invocation -> {
            executorService.suppressNextRetry();
            timeoutOccurred.countDown();
            return null;
        }).when(changeListener).errorOccurred(any());
        meter.addValueChangeListener(changeListener);

        Disposable disposable = meter.readValues(timeout, executorService, Duration.ZERO);

        try {
            await(readStarted, "The meter read did not start");
            await(timeoutOccurred, "The meter read did not time out");
            executorService.awaitRetrySuppressed(EVENT_TIMEOUT);
        } finally {
            releaseRead.countDown();
            dispose(disposable, executorService);
        }

        verify(changeListener, times(1)).errorOccurred(any(TimeoutException.class));
    }

    @Test
    public void shouldNotReportToFallbackException() {
        final int timeout = 1000;
        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch releaseRead = new CountDownLatch(1);
        RetrySuppressingExecutor executorService = createRetrySuppressingExecutor(2);
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            executorService.markSourceTask();
            readStarted.countDown();
            awaitUninterruptibly(releaseRead);
            throw new UncheckedIOException(new IOException("simulated read failure"));
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        @SuppressWarnings("unchecked")
        Consumer<Throwable> errorHandler = mock(Consumer.class);
        RxJavaPlugins.setErrorHandler(errorHandler);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        CountDownLatch timeoutOccurred = new CountDownLatch(1);
        doAnswer(invocation -> {
            executorService.suppressNextRetry();
            timeoutOccurred.countDown();
            return null;
        }).when(changeListener).errorOccurred(any());
        meter.addValueChangeListener(changeListener);

        Disposable disposable = meter.readValues(timeout, executorService, Duration.ZERO);

        try {
            await(readStarted, "The meter read did not start");
            await(timeoutOccurred, "The meter read did not time out");
            executorService.awaitRetrySuppressed(EVENT_TIMEOUT);
            releaseRead.countDown();
            executorService.awaitSourceTaskFinished(EVENT_TIMEOUT);
        } finally {
            releaseRead.countDown();
            dispose(disposable, executorService);
        }

        verify(changeListener, times(1)).errorOccurred(any(TimeoutException.class));
        verifyNoInteractions(errorHandler);
    }

    MockMeterReaderConnector getMockedConnector(boolean applyRetry, Supplier<Object> readNextSupplier) {
        return new MockMeterReaderConnector("Test port", applyRetry, readNextSupplier);
    }

    MeterDevice<Object> getMeterDevice(ConnectorBase<Object> connector) {
        return new MeterDevice<>(() -> mock(SerialPortManager.class), "id", "port", null, 9600, 0, ProtocolMode.SML) {

            @Override
            protected IMeterReaderConnector<Object> createConnector(
                    Supplier<SerialPortManager> serialPortManagerSupplier, String serialPort, int baudrate,
                    int baudrateChangeDelay, ProtocolMode protocolMode) {
                return connector;
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            protected <Q extends Quantity<Q>> void populateValueCache(Object smlFile) {
                addObisCache(new MeterValue("123", "333", null));
            }
        };
    }

    private void await(CountDownLatch latch, String failureMessage) {
        try {
            assertTrue(latch.await(EVENT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS), failureMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for an asynchronous test event", e);
        }
    }

    private void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        while (true) {
            try {
                latch.await();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void dispose(Disposable disposable, ScheduledExecutorService executorService) {
        disposable.dispose();
        executorService.shutdownNow();
        try {
            assertTrue(executorService.awaitTermination(EVENT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS),
                    "Executor did not terminate");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while terminating the executor", e);
        }
    }
}
