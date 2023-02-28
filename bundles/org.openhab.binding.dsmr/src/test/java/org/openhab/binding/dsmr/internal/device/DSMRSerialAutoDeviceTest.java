/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice.DeviceState;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * Test class for {@link DSMRSerialAutoDevice}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class DSMRSerialAutoDeviceTest {

    private static final String DUMMY_PORTNAME = "/dev/dummy-serial";
    private static final String TELEGRAM_NAME = "dsmr_50";

    private @NonNullByDefault({}) @Mock SerialPortIdentifier mockIdentifier;
    private @NonNullByDefault({}) @Mock ScheduledExecutorService scheduler;
    private @NonNullByDefault({}) @Mock SerialPort mockSerialPort;

    private final SerialPortManager serialPortManager = new SerialPortManager() {
        @Override
        public @Nullable SerialPortIdentifier getIdentifier(final String name) {
            assertEquals(DUMMY_PORTNAME, name, "Expect the passed serial port name");
            return mockIdentifier;
        }

        @Override
        public Stream<SerialPortIdentifier> getIdentifiers() {
            return Stream.empty();
        }
    };
    private @NonNullByDefault({}) SerialPortEventListener serialPortEventListener;

    @BeforeEach
    public void setUp() throws PortInUseException, TooManyListenersException {
        doAnswer(a -> {
            serialPortEventListener = a.getArgument(0);
            return null;
        }).when(mockSerialPort).addEventListener(any());
    }

    @Test
    public void testHandlingDataAndRestart() throws IOException, PortInUseException {
        mockValidSerialPort();
        final AtomicReference<@Nullable P1Telegram> telegramRef = new AtomicReference<>(null);
        final P1TelegramListener listener = new P1TelegramListener() {
            @Override
            public void telegramReceived(final P1Telegram telegram) {
                telegramRef.set(telegram);
            }

            @Override
            public void onError(final DSMRErrorStatus errorStatus, final String message) {
                fail("No error status expected" + errorStatus);
            }
        };
        try (InputStream inputStream = new ByteArrayInputStream(TelegramReaderUtil.readRawTelegram(TELEGRAM_NAME))) {
            when(mockSerialPort.getInputStream()).thenReturn(inputStream);
            final DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener,
                    new DSMRTelegramListener(), scheduler, 1);
            device.start();
            assertSame(DeviceState.DISCOVER_SETTINGS, device.getState(), "Expect to be starting discovery state");
            serialPortEventListener
                    .serialEvent(new MockSerialPortEvent(mockSerialPort, SerialPortEvent.BI, false, true));
            assertSame(DeviceState.DISCOVER_SETTINGS, device.getState(), "Expect to be still in discovery state");
            serialPortEventListener
                    .serialEvent(new MockSerialPortEvent(mockSerialPort, SerialPortEvent.DATA_AVAILABLE, false, true));
            assertSame(DeviceState.NORMAL, device.getState(), "Expect to be in normal state");
            device.restart();
            assertSame(DeviceState.NORMAL, device.getState(), "Expect not to start rediscovery when in normal state");
            device.stop();
        }
        assertNotNull(telegramRef.get(), "Expected to have read a telegram");
    }

    @Test
    public void testHandleError() throws IOException, PortInUseException {
        final AtomicReference<@Nullable DSMRErrorStatus> eventRef = new AtomicReference<>(null);
        final P1TelegramListener listener = new P1TelegramListener() {
            @Override
            public void telegramReceived(final P1Telegram telegram) {
                fail("No telegram expected:" + telegram);
            }

            @Override
            public void onError(final DSMRErrorStatus errorStatus, final String message) {
                eventRef.set(errorStatus);
            }
        };
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            when(mockSerialPort.getInputStream()).thenReturn(inputStream);
            // Trigger device to go into error stage with port in use.
            doThrow(new PortInUseException(new Exception())).when(mockIdentifier)
                    .open(eq(DSMRBindingConstants.DSMR_PORT_NAME), anyInt());
            DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener,
                    new DSMRTelegramListener(), scheduler, 1);
            device.start();
            assertSame(DSMRErrorStatus.PORT_IN_USE, eventRef.get(), "Expected an error");
            assertSame(DeviceState.ERROR, device.getState(), "Expect to be in error state");
            // Trigger device to restart
            mockValidSerialPort();
            device.restart();
            assertSame(DeviceState.DISCOVER_SETTINGS, device.getState(), "Expect to be starting discovery state");
            // Trigger device to go into error stage with port doesn't exist.
            mockIdentifier = null;
            device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener, new DSMRTelegramListener(),
                    scheduler, 1);
            device.start();
            assertSame(DSMRErrorStatus.PORT_DONT_EXISTS, eventRef.get(), "Expected an error");
            assertSame(DeviceState.ERROR, device.getState(), "Expect to be in error state");
        }
    }

    private void mockValidSerialPort() throws PortInUseException {
        doReturn(mockSerialPort).when(mockIdentifier).open(anyString(), anyInt());
    }

    /**
     * Mock class implementing {@link SerialPortEvent}.
     */
    private static class MockSerialPortEvent implements SerialPortEvent {
        private final int eventType;
        private final boolean newValue;

        public MockSerialPortEvent(final SerialPort mockSerialPort, final int eventType, final boolean oldValue,
                final boolean newValue) {
            this.eventType = eventType;
            this.newValue = newValue;
        }

        @Override
        public int getEventType() {
            return eventType;
        }

        @Override
        public boolean getNewValue() {
            return newValue;
        }
    }
}
