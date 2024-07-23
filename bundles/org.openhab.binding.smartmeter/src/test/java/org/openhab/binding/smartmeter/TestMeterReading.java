/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

    @Test
    public void testContinousReading() throws Exception {
        final Duration period = Duration.ofSeconds(1);
        final int executionCount = 5;
        MockMeterReaderConnector connector = getMockedConnector(false, () -> new Object());
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        long executionTime = period.toMillis() * executionCount;
        Disposable disposable = meter.readValues(executionTime, Executors.newScheduledThreadPool(1), period);
        try {
            verify(changeListener, after(executionTime + period.toMillis() / 2 + 50).never()).errorOccurred(any());
            verify(changeListener, times(executionCount)).valueChanged(any());
        } finally {
            disposable.dispose();
        }
    }

    @Test
    public void testRetryHandling() {
        final Duration period = Duration.ofSeconds(1);
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            throw new IllegalArgumentException();
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        Disposable disposable = meter.readValues(5000, Executors.newScheduledThreadPool(1), period);
        try {
            verify(changeListener, after(
                    period.toMillis() + 2 * period.toMillis() * ConnectorBase.NUMBER_OF_RETRIES + period.toMillis() / 2)
                    .times(1)).errorOccurred(any());
            verify(connector, times(ConnectorBase.NUMBER_OF_RETRIES)).retryHook(ArgumentMatchers.anyInt());
        } finally {
            disposable.dispose();
        }
    }

    @Test
    public void testTimeoutHandling() {
        final Duration period = Duration.ofSeconds(2);
        final int timeout = 5000;
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
            }
            return new Object();
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        Disposable disposable = meter.readValues(timeout / 2, Executors.newScheduledThreadPool(2), period);
        try {
            verify(changeListener, timeout(timeout)).errorOccurred(any(TimeoutException.class));
        } finally {
            disposable.dispose();
        }
    }

    @Test
    public void shouldNotReportToFallbackException() {
        final Duration period = Duration.ofSeconds(2);
        final int timeout = 5000;
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
            }
            throw new RuntimeException(new IOException("fucked up"));
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        @SuppressWarnings("unchecked")
        Consumer<Throwable> errorHandler = mock(Consumer.class);
        RxJavaPlugins.setErrorHandler(errorHandler);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        Disposable disposable = meter.readValues(timeout / 2, Executors.newScheduledThreadPool(2), period);
        try {
            verify(changeListener, timeout(timeout)).errorOccurred(any(TimeoutException.class));
            verifyNoMoreInteractions(errorHandler);
        } finally {
            disposable.dispose();
        }
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
}
