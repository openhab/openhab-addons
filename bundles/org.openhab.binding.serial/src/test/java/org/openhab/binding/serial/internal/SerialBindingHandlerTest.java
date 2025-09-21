/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
@NonNullByDefault
public class SerialBindingHandlerTest {

    private @Nullable MockupBridgeHandler handler;
    private @Nullable SerialBridgeConfiguration config;
    private @Nullable ByteBufferInputStream bin;
    private @Nullable ByteArrayOutputStream bos;
    private final ThingHandlerCallback callbackMock = Mockito.mock(ThingHandlerCallback.class);
    private final Bridge bridgeMock = Mockito.mock(Bridge.class);
    private final SerialPortIdentifier mockIdentifier = Mockito.mock(SerialPortIdentifier.class);
    private final SerialPort mockSerialPort = Mockito.mock(SerialPort.class);

    private final SerialPortManager serialPortManager = new SerialPortManager() {
        @Override
        public @NonNull SerialPortIdentifier getIdentifier(final String name) {
            assertEquals("/dev/dummy-serial", name, "Expect the passed serial port name");
            return mockIdentifier;
        }

        @Override
        public @NonNull Stream<SerialPortIdentifier> getIdentifiers() {
            Stream<SerialPortIdentifier> stream = Stream.empty();
            assertNotNull(stream);
            return stream;
        }
    };

    @BeforeEach
    @SuppressWarnings("null")
    public void setUp() throws PortInUseException, IOException {
        SerialBridgeConfiguration config = new SerialBridgeConfiguration();
        config.serialPort = "/dev/dummy-serial";
        this.config = config;
        this.handler = new MockupBridgeHandler(bridgeMock, serialPortManager, config);
        this.handler.setCallback(callbackMock);
        this.bin = new ByteBufferInputStream();
        this.bos = new ByteArrayOutputStream();
        resetMock();
    }

    @AfterEach
    public void tearDown() {
        MockupBridgeHandler handler = this.handler;
        this.handler = null;
        if (handler != null) {
            handler.dispose();
        }
    }

    private void resetMock() throws IOException, PortInUseException {
        reset(callbackMock, bridgeMock, mockSerialPort, mockIdentifier);
        doReturn(new ThingUID("serial:serialBridge:test")).when(bridgeMock).getUID();
        doReturn(mockSerialPort).when(mockIdentifier).open(anyString(), anyInt());
        doReturn(bin).when(mockSerialPort).getInputStream();
        doReturn(bos).when(mockSerialPort).getOutputStream();
    }

    @Test
    public void initializeShouldCallTheCallback() throws PortInUseException, IOException {
        MockupBridgeHandler handler = this.handler;
        ThingHandlerCallback callbackMock = this.callbackMock;
        Bridge bridgeMock = this.bridgeMock;
        SerialBridgeConfiguration config = this.config;
        assertNotNull(handler);
        assertNotNull(callbackMock);
        assertNotNull(bridgeMock);
        assertNotNull(config);

        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        resetMock();
        config.charset = "UTF-8";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        resetMock();
        config.charset = "foobar";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        config.charset = "HEX";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        config.eolPattern = "Invalid(Pattern";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(callbackMock).statusUpdated(eq(bridgeMock),
                argThat(arg -> arg.getStatusDetail().equals(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR)));

        resetMock();
        config.eolPattern = "ValidPattern";
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
    }

    @Test
    void testWriteUtf8String() {
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteArrayOutputStream bos = this.bos;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bos);

        config.charset = "UTF-8";
        handler.initialize();
        handler.writeString("föö");
        assertEquals("föö", bos.toString(StandardCharsets.UTF_8));
    }

    @Test
    void testWriteLatin1String() {
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteArrayOutputStream bos = this.bos;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bos);

        config.charset = "ISO-8859-1";
        handler.initialize();
        handler.writeString("föö");
        assertEquals("föö", bos.toString(StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteHexString() {
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteArrayOutputStream bos = this.bos;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bos);

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
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteBufferInputStream bin = this.bin;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bin);

        config.charset = "UTF-8";
        handler.initialize();
        bin.appendString("föö", StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("föö", sb.toString());
    }

    @Test
    void testReadLatin1String() {
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteBufferInputStream bin = this.bin;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bin);

        config.charset = "ISO-8859-1";
        handler.initialize();
        bin.appendString("föö", StandardCharsets.ISO_8859_1);

        StringBuilder sb = new StringBuilder();
        handler.receiveAndProcess(sb, false);
        assertEquals("föö", sb.toString());
    }

    @Test
    void testReadHexString() {
        MockupBridgeHandler handler = this.handler;
        SerialBridgeConfiguration config = this.config;
        ByteArrayOutputStream bos = this.bos;
        ByteBufferInputStream bin = this.bin;
        assertNotNull(config);
        assertNotNull(handler);
        assertNotNull(bos);
        assertNotNull(bin);

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
