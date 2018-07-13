/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice.DeviceState;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.connector.SerialPortManager;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * Test class for {@link DSMRSerialAutoDevice}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class DSMRSerialAutoDeviceTest {

    private static final String DUMMY_PORTNAME = "/dev/dummy-serial";
    private static final String TELEGRAM_NAME = "dsmr_50";

    @Mock
    private CommPortIdentifier mockIdentifier;
    @Mock
    private ScheduledExecutorService scheduler;
    @Mock
    private RXTXPort mockSerialPort;

    private SerialPortManager serialPortManager = new SerialPortManager() {
        @Override
        public CommPortIdentifier getIdentifier(String name) throws NoSuchPortException {
            assertEquals("Expect the passed serial port name", DUMMY_PORTNAME, name);
            return mockIdentifier;
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
                    scheduler, 1);
            device.start();
            assertSame("Expect to be starting discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
            serialPortEventListener.serialEvent(new SerialPortEvent(mockSerialPort, SerialPortEvent.BI, false, true));
            assertSame("Expect to be still in discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
            serialPortEventListener
                    .serialEvent(new SerialPortEvent(mockSerialPort, SerialPortEvent.DATA_AVAILABLE, false, true));
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
            // Trigger device to go into error stage.
            doAnswer(a -> {
                throw new NoSuchPortException();
            }).when(mockIdentifier).open(eq(DSMRBindingConstants.DSMR_PORT_NAME), anyInt());
            DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, DUMMY_PORTNAME, listener,
                    scheduler, 1);
            device.start();
            assertSame("Expected an error", DSMRConnectorErrorEvent.DONT_EXISTS, eventRef.get());
            assertSame("Expect to be in error state", DeviceState.ERROR, device.getState());
            // Trigger device to restart
            mockValidSerialPort();
            device.restart();
            assertSame("Expect to be starting discovery state", DeviceState.DISCOVER_SETTINGS, device.getState());
        }
    }

    private void mockValidSerialPort() throws PortInUseException {
        doReturn(mockSerialPort).when(mockIdentifier).open(anyString(), anyInt());
    }
}
