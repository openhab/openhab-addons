/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice.DeviceState;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;

/**
 * Test class for {@link DSMRSerialAutoDevice}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class DSMRSerialAutoDeviceTest {

    private static final String DUMMY_PORTNAME = "/dev/dummy-serial";
    private static final String TELEGRAM_NAME = "dsmr_50";

    @Mock
    private SerialPortIdentifier mockIdentifier;
    @Mock
    private ScheduledExecutorService scheduler;
    @Mock
    private SerialPort mockSerialPort;

    private SerialPortManager serialPortManager = new SerialPortManager() {
        @Override
        public SerialPortIdentifier getIdentifier(String name) {
            assertEquals("Expect the passed serial port name", DUMMY_PORTNAME, name);
            return mockIdentifier;
        }

        @Override
        public @NonNull Stream<@NonNull SerialPortIdentifier> getIdentifiers() {
            return Stream.empty();
        }
    };
    private SerialPortEventListener serialPortEventListener;

    @Before
    public void setUp() throws PortInUseException, TooManyListenersException {
        initMocks(this);
        doAnswer(a -> {
            serialPortEventListener = a.getArgument(0);
            return null;
        }).when(mockSerialPort).addEventListener(any());
    }

    @Test
    public void testHandlingDataAndRestart() throws IOException, PortInUseException {
        mockValidSerialPort();
        AtomicReference<P1Telegram> telegramRef = new AtomicReference<>(null);
        DSMREventListener listener = new DSMREventListener() {
            @Override
            public void handleTelegramReceived(@NonNull P1Telegram telegram) {
                telegramRef.set(telegram);
            }

            @Override
            public void handleErrorEvent(@NonNull DSMRConnectorErrorEvent connectorErrorEvent) {
                fail("No handleErrorEvent Expected" + connectorErrorEvent);
            }
        };
        try (InputStream inputStream = new ByteArrayInputStream(TelegramReaderUtil.readRawTelegram(TELEGRAM_NAME))) {
            when(mockSerialPort.getInputStream()).thenReturn(inputStream);
            DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener,
                    new DSMRTelegramListener(), scheduler, 1);
            device.start();
            assertSame("Expect to be starting discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
            serialPortEventListener
                    .serialEvent(new MockSerialPortEvent(mockSerialPort, SerialPortEvent.BI, false, true));
            assertSame("Expect to be still in discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
            serialPortEventListener
                    .serialEvent(new MockSerialPortEvent(mockSerialPort, SerialPortEvent.DATA_AVAILABLE, false, true));
            assertSame("Expect to be in normal state", DeviceState.NORMAL, device.getState());
            device.restart();
            assertSame("Expect not to start rediscovery when in normal state", DeviceState.NORMAL, device.getState());
            device.stop();
        }
        assertNotNull("Expected to have read a telegram", telegramRef.get());
    }

    @Test
    public void testHandleError() throws IOException, PortInUseException {
        AtomicReference<DSMRConnectorErrorEvent> eventRef = new AtomicReference<>(null);
        DSMREventListener listener = new DSMREventListener() {
            @Override
            public void handleTelegramReceived(@NonNull P1Telegram telegram) {
                fail("No telegram expected:" + telegram);
            }

            @Override
            public void handleErrorEvent(@NonNull DSMRConnectorErrorEvent connectorErrorEvent) {
                eventRef.set(connectorErrorEvent);
            }
        };
        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            when(mockSerialPort.getInputStream()).thenReturn(inputStream);
            // Trigger device to go into error stage with port in use.
            doAnswer(a -> {
                throw new PortInUseException();
            }).when(mockIdentifier).open(eq(DSMRBindingConstants.DSMR_PORT_NAME), anyInt());
            DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener,
                    new DSMRTelegramListener(), scheduler, 1);
            device.start();
            assertSame("Expected an error", DSMRConnectorErrorEvent.IN_USE, eventRef.get());
            assertSame("Expect to be in error state", DeviceState.ERROR, device.getState());
            // Trigger device to restart
            mockValidSerialPort();
            device.restart();
            assertSame("Expect to be starting discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
            // Trigger device to go into error stage with port doesn't exist.
            mockIdentifier = null;
            device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener, new DSMRTelegramListener(),
                    scheduler, 1);
            device.start();
            assertSame("Expected an error", DSMRConnectorErrorEvent.DONT_EXISTS, eventRef.get());
            assertSame("Expect to be in error state", DeviceState.ERROR, device.getState());
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

        public MockSerialPortEvent(SerialPort mockSerialPort, int eventType, boolean oldValue, boolean newValue) {
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
