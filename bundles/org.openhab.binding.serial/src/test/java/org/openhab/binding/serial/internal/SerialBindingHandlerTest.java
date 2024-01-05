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
package org.openhab.binding.serial.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.serial.internal.handler.SerialBridgeConfiguration;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Implements some tests for the CommonBridgeHandler
 *
 * @author Roland Tapken - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class SerialBindingHandlerTest {

    private MockupBridgeHandler handler;

    private SerialBridgeConfiguration config;

    private @Mock ThingHandlerCallback callbackMock;

    ByteBufferInputStream bin;

    ByteArrayOutputStream bos;

    private @Mock Bridge bridgeMock;

    private @Mock SerialPortIdentifier mockIdentifier;

    private @Mock SerialPort mockSerialPort;

    private final SerialPortManager serialPortManager = new SerialPortManager() {
        @Override
        public @Nullable SerialPortIdentifier getIdentifier(final String name) {
            assertEquals("/dev/dummy-serial", name, "Expect the passed serial port name");
            return mockIdentifier;
        }

        @Override
        public Stream<SerialPortIdentifier> getIdentifiers() {
            return Stream.empty();
        }
    };

    @BeforeEach
    public void setUp() throws Throwable {
        this.config = new SerialBridgeConfiguration();
        this.config.serialPort = "/dev/dummy-serial";
        this.handler = new MockupBridgeHandler(bridgeMock, serialPortManager, config);
        this.handler.setCallback(callbackMock);
        this.bin = new ByteBufferInputStream();
        this.bos = new ByteArrayOutputStream();
        resetMock();
    }

    @AfterEach
    public void tearDown() {
        this.handler.dispose();
        this.handler = null;
    }

    private void resetMock() throws IOException, PortInUseException {
        reset(callbackMock, bridgeMock, mockSerialPort, mockIdentifier);
        doReturn(new ThingUID("serial:serialBridge:test")).when(bridgeMock).getUID();
        doReturn(mockSerialPort).when(mockIdentifier).open(anyString(), anyInt());
        doReturn(bin).when(mockSerialPort).getInputStream();
        doReturn(bos).when(mockSerialPort).getOutputStream();
    }

    @Test
    public void initializeShouldCallTheCallback() throws Throwable {
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        resetMock();
        this.config.charset = "UTF-8";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        resetMock();
        this.config.charset = "foobar";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        this.config.charset = "HEX";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        this.config.eolPattern = "Invalid(Pattern";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        this.config.eolPattern = "ValidPattern";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
    }

    @Test
    void testWriteUtf8String() {
        config.charset = "UTF-8";
        handler.initialize();
        handler.writeString("föö");
        assertEquals("föö", bos.toString(StandardCharsets.UTF_8));
    }

    @Test
    void testWriteLatin1String() {
        config.charset = "ISO-8859-1";
        handler.initialize();
        handler.writeString("föö");
        assertEquals("föö", bos.toString(StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteHexString() {
        config.charset = "HEX";
        config.eolPattern = "\\b0A";
        handler.initialize();

        byte[] bytes = { 'T', 'E', 'S', 'T' };
        String str = HexFormat.of().withDelimiter(" ").formatHex(bytes);
        assertEquals(str, "54 45 53 54");
        handler.writeString(str);
        assertArrayEquals(bytes, bos.toByteArray());
    }

    @Test
    void testReadUtf8String() {
        config.charset = "UTF-8";
        handler.initialize();
        bin.appendString("föö", StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("föö", sb.toString());
    }

    @Test
    void testReadLatin1String() {
        config.charset = "ISO-8859-1";
        handler.initialize();
        bin.appendString("föö", StandardCharsets.ISO_8859_1);

        StringBuilder sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("föö", sb.toString());
    }

    @Test
    void testReadHexString() {
        config.charset = "HEX";
        config.eolPattern = "\\b0A";
        handler.initialize();
        bin.appendBuffer(new byte[] { 'T', 'E', 'S', 'T' });

        StringBuilder sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("54 45 53 54", sb.toString());

        // Test with newlines
        bin.appendBuffer(new byte[] { 'T', 'E', 'S', 'T', '1', '\n', 'T', 'E', 'S', 'T', '2' });
        sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("54 45 53 54 31 0A\n54 45 53 54 32", sb.toString());
    }
}
