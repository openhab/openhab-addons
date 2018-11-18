/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * JUnit test of USBTinDevice
 *
 * @author Lubos Housa - Initial contribution
 */
public class USBTinDeviceTest {

    @Mock
    private SerialPortManager serialPortManager;

    @Mock
    private SerialPortIdentifier port;

    @Mock
    private SerialPort serialPort;

    @Mock
    private Reader input;

    @Mock
    private Writer output;

    @Mock
    private CanBusDeviceListener listener;

    @Mock
    private SerialPortEvent event;

    private USBTinDevice device;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        device = new USBTinDevice(inputStream -> input, outputStream -> output);
        device.setSerialPortManager(serialPortManager);
        device.registerCanBusDeviceListener(listener);
        device.setWatchdogStartupDelay(10000);
        device.setWatchdogDelay(10000);
    }

    @After
    public void tearDown() throws Exception {
        device.disconnect();
    }

    private CanBusDeviceStatus connect() {
        return device.connect("test", 50000);
    }

    @Test
    public void testConnectMissingPort() throws Exception {
        CanBusDeviceStatus status = connect();
        assertEquals(CanBusDeviceStatus.MISSING_PORT_ERROR, status);
    }

    private void prepSuccessConnect() throws Exception {
        when(serialPortManager.getIdentifier(anyString())).thenReturn(port);
        when(port.open(anyString(), anyInt())).thenReturn(serialPort);
    }

    @Test
    public void testConnectExceptionFromSerialOutput() throws Exception {
        prepSuccessConnect();
        doThrow(IOException.class).when(output).write(anyString());

        assertEquals(CanBusDeviceStatus.CANBUS_CONNECT_ERROR, connect());
    }

    @Test
    public void testConnectExceptionFromSerialInput() throws Exception {
        prepSuccessConnect();
        when(input.ready()).thenReturn(true).thenReturn(false);
        doThrow(IOException.class).when(input).read();

        assertEquals(CanBusDeviceStatus.CANBUS_CONNECT_ERROR, connect());
    }

    @Test
    public void testConnectOKCallsCommands() throws Exception {
        prepSuccessConnect();

        assertEquals(CanBusDeviceStatus.CONNECTED, connect());
        verify(output, times(6)).write(anyString());
    }

    @Test
    public void testCheckConnectionFailCallsListener() throws Exception {
        prepSuccessConnect();
        doNothing().doNothing().doNothing().doNothing().doNothing().doNothing().doThrow(MyIOException.class)
                .when(output).write(anyString());

        device.setWatchdogStartupDelay(0);
        device.setWatchdogDelay(2);
        assertEquals(CanBusDeviceStatus.CONNECTED, connect());

        Thread.sleep(20); // we should have disconnected by this time for sure
        verify(listener, atLeast(1)).onDeviceFatalError("error"); // matches MyIOException.getMessage
    }

    @Test
    public void testDisconnectCommandSentAllClosed() throws Exception {
        prepSuccessConnect();

        connect();
        reset(output);
        device.disconnect();
        verify(output, times(1)).write(anyString());
        assertEquals(CanBusDeviceStatus.UNITIALIZED, device.getStatus());
        verify(output, times(1)).close();
        verify(output, times(1)).close();
    }

    @Test
    public void testSendCanMessages() throws Exception {
        prepSuccessConnect();
        assertEquals(CanBusDeviceStatus.CONNECTED, connect());

        reset(output);
        device.send(CanMessage.newBuilder().id(1).withDataByte(1).build());
        device.send(CanMessage.newBuilder().id(0x89).withDataByte(0x10).withDataByte(0x20).withDataByte(0x30).build());

        verify(output, times(1)).write("t001101\r");
        verify(output, times(1)).write("t0893102030\r");
    }

    @Test
    public void testReceiveSerialErrorNotifiesListeners() throws Exception {
        when(event.getEventType()).thenReturn(SerialPortEvent.BI);
        when(event.getNewValue()).thenReturn(true);

        device.serialEvent(event);

        verify(listener, times(1)).onDeviceError(matches(".*Break interrupt.*"));
    }

    @Test
    public void testReceiveSerialValidCanMessage() throws Exception {
        prepSuccessConnect();
        device.connect("test", 50000);

        // try translating data t001101 to canMessage
        char[] d = "t001101".toCharArray();
        when(event.getEventType()).thenReturn(SerialPortEvent.DATA_AVAILABLE);
        when(event.getNewValue()).thenReturn(true);
        when(input.ready()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(true).thenReturn(false);
        when(input.read()).thenReturn((int) d[0]).thenReturn((int) d[1]).thenReturn((int) d[2]).thenReturn((int) d[3])
                .thenReturn((int) d[4]).thenReturn((int) d[5]).thenReturn((int) d[6]);

        device.serialEvent(event);

        verify(listener, times(1)).onMessage(CanMessage.newBuilder().id(0x01).withDataByte(0x01).build());
    }

    private class MyIOException extends IOException {
        private static final long serialVersionUID = 1L;

        @Override
        public String getMessage() {
            return "error";
        }
    }
}
