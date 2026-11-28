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
package org.openhab.binding.sinope;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.sinope.handler.SinopeGatewayHandler;
import org.openhab.binding.sinope.handler.SinopeThermostatHandler;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginAnswer;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginRequest;
import org.openhab.binding.sinope.internal.core.SinopeAuthenticationKeyAnswer;
import org.openhab.binding.sinope.internal.core.SinopeDataReadAnswer;
import org.openhab.binding.sinope.internal.core.SinopeDeviceReportAnswer;
import org.openhab.binding.sinope.internal.core.SinopePingAnswer;
import org.openhab.binding.sinope.internal.core.appdata.SinopeRoomTempData;
import org.openhab.binding.sinope.internal.discovery.SinopeThingsDiscoveryService;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.binding.sinope.internal.util.CRC8;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Unit tests for Sinopé protocol framing, API key pairing, and device discovery.
 *
 * @author Pascal Larin - Initial contribution
 */
@NonNullByDefault
public class SinopeDiscoveryAndPairingTest {

    private static byte[] buildFrame(byte cmdHi, byte cmdLo, byte[] data) {
        int size = 2 + data.length;
        ByteBuffer bb = ByteBuffer.allocate(4 + size + 1);
        bb.put((byte) 0x55);
        bb.put((byte) 0x00);
        bb.order(ByteOrder.LITTLE_ENDIAN).putShort((short) size);
        bb.put(cmdLo);
        bb.put(cmdHi);
        bb.put(data);
        CRC8 crc = new CRC8();
        byte[] arr = bb.array();
        crc.update(arr, 0, arr.length - 1);
        arr[arr.length - 1] = (byte) crc.getValue();
        return arr;
    }

    private static byte[] readFrame(InputStream in) throws IOException {
        byte[] header = in.readNBytes(4);
        if (header.length < 4) {
            throw new EOFException("Short header");
        }
        short size = ByteBuffer.wrap(header, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        byte[] rest = in.readNBytes(size + 1);
        if (rest.length < size + 1) {
            throw new EOFException("Short frame body");
        }
        ByteBuffer bb = ByteBuffer.allocate(4 + size + 1);
        bb.put(header);
        bb.put(rest);
        return bb.array();
    }

    @Test
    public void testAuthenticationKeyAnswerParsingAndRedaction() throws IOException {
        byte[] expectedKey = new byte[] { 0x12, 0x57, 0x55, (byte) 0xD5, (byte) 0xCD, 0x4A, (byte) 0xD5, 0x33 };
        ByteBuffer data = ByteBuffer.allocate(1 + 2 + 8);
        data.put((byte) 0x00); // status OK
        data.put((byte) 0x00);
        data.put((byte) 0x00); // backoff
        data.put(ByteUtil.reverse(expectedKey)); // wire order is reversed

        byte[] frame = buildFrame((byte) 0x01, (byte) 0x11, data.array());
        SinopeAuthenticationKeyAnswer answer = new SinopeAuthenticationKeyAnswer(new ByteArrayInputStream(frame));

        assertEquals(0, answer.getStatus());
        assertArrayEquals(expectedKey, answer.getApiKey());
        assertArrayEquals(expectedKey, SinopeConfig.convert(ByteUtil.toString(answer.getApiKey()).trim()));
        assertFalse(answer.toString().contains("0x12"));
    }

    @Test
    public void testAuthenticationKeyAnswerShortFrame() throws IOException {
        // Truncated 9-byte data frame (only 6 key bytes instead of 8) must NOT pass length guard
        byte[] truncatedKeyData = new byte[] { 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 };
        byte[] frame = buildFrame((byte) 0x01, (byte) 0x11, truncatedKeyData);
        SinopeAuthenticationKeyAnswer answer = new SinopeAuthenticationKeyAnswer(new ByteArrayInputStream(frame));

        assertEquals(1, answer.getStatus());
        assertEquals(0, answer.getApiKey().length);
    }

    @Test
    public void testDeviceReportTruncatedFrameAndInvalidSize() throws IOException {
        // Truncated 3-byte data frame (only 2 device-id bytes instead of 4) must NOT fabricate an ID
        byte[] truncatedReport = buildFrame((byte) 0x01, (byte) 0x16, new byte[] { 0x00, 0x12, 0x34 });
        SinopeDeviceReportAnswer answer = new SinopeDeviceReportAnswer(new ByteArrayInputStream(truncatedReport));
        assertEquals(0, answer.getDeviceId().length);

        // Size below COMMAND_SIZE (2) must be rejected
        byte[] badSizeFrame = new byte[] { 0x55, 0x00, 0x01, 0x00, 0x00, 0x00 };
        assertThrows(IOException.class, () -> new SinopePingAnswer(new ByteArrayInputStream(badSizeFrame)));
    }

    @Test
    public void testLoginRequestToStringRedactsApiKey() {
        byte[] gatewayId = new byte[] { 0x00, 0x2F, (byte) 0xC2, (byte) 0xC2, (byte) 0xDD, (byte) 0x88, (byte) 0xAA,
                (byte) 0xAA };
        byte[] apiKey = new byte[] { 0x12, 0x57, 0x55, (byte) 0xD5, (byte) 0xCD, 0x4A, (byte) 0xD5, 0x33 };
        SinopeApiLoginRequest request = new SinopeApiLoginRequest(gatewayId, apiKey);

        String diagnostic = request.toString();
        assertThat(diagnostic, containsString("***"));
        assertFalse(diagnostic.contains("0x57"));
        assertFalse(diagnostic.contains("0xCD"));
    }

    @Test
    @Timeout(10)
    public void testDisposeUnblocksInFlightConnectPromptly() throws Exception {
        CountDownLatch acceptedLatch = new CountDownLatch(1);
        CountDownLatch releaseServer = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            Thread stalledGateway = new Thread(() -> {
                try (Socket s = server.accept()) {
                    acceptedLatch.countDown();
                    // Accept TCP but never respond to login until test releases
                    releaseServer.await(10, TimeUnit.SECONDS);
                } catch (IOException | InterruptedException ignored) {
                }
            });
            stalledGateway.setDaemon(true);
            stalledGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "stalledgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            handler.setCallback(callback);

            try {
                handler.initialize();
                assertTrue(acceptedLatch.await(5, TimeUnit.SECONDS), "Gateway should initiate TCP connection");
                long startNanos = System.nanoTime();
                handler.dispose();
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                assertTrue(elapsedMs < 3000, "dispose() should cancel in-flight connect promptly, took " + elapsedMs);
            } finally {
                releaseServer.countDown();
            }
        }
    }

    @Test
    @Timeout(15)
    public void testReinitializedPairingIgnoresStaleWorker() throws Exception {
        byte[] staleKey = new byte[] { 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11 };
        byte[] freshKey = new byte[] { 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22 };
        String expectedFreshKey = ByteUtil.toString(freshKey).trim();

        CountDownLatch staleConnected = new CountDownLatch(1);
        CountDownLatch releaseStaleReply = new CountDownLatch(1);
        CountDownLatch freshKeySaved = new CountDownLatch(1);
        AtomicReference<String> savedApiKey = new AtomicReference<>("");

        try (ServerSocket staleServer = new ServerSocket(0); ServerSocket freshServer = new ServerSocket(0)) {
            Thread staleThread = new Thread(() -> {
                try (Socket s = staleServer.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();
                    readFrame(in); // ping
                    out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                    out.flush();
                    readFrame(in); // auth key request
                    staleConnected.countDown();
                    releaseStaleReply.await(5, TimeUnit.SECONDS);
                    ByteBuffer keyData = ByteBuffer.allocate(11);
                    keyData.put((byte) 0x01).put((byte) 0x00).put((byte) 0x00).put(ByteUtil.reverse(staleKey));
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, keyData.array()));
                    out.flush();
                } catch (IOException | InterruptedException ignored) {
                }
            });
            staleThread.setDaemon(true);
            staleThread.start();

            Thread freshThread = new Thread(() -> {
                try {
                    try (Socket s1 = freshServer.accept()) {
                        InputStream in = s1.getInputStream();
                        OutputStream out = s1.getOutputStream();
                        readFrame(in); // ping
                        out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                        out.flush();
                        readFrame(in); // auth key request
                        ByteBuffer keyData = ByteBuffer.allocate(11);
                        keyData.put((byte) 0x01).put((byte) 0x00).put((byte) 0x00).put(ByteUtil.reverse(freshKey));
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, keyData.array()));
                        out.flush();
                    }
                    try (Socket s2 = freshServer.accept()) {
                        InputStream in = s2.getInputStream();
                        OutputStream out = s2.getOutputStream();
                        readFrame(in); // login
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                        out.flush();
                        in.read();
                    }
                } catch (IOException ignored) {
                }
            });
            freshThread.setDaemon(true);
            freshThread.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "reinitgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port",
                            staleServer.getLocalPort(), "gatewayId", "002f-c2c2-dd88-aaaa", "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            doAnswer(inv -> {
                Bridge updated = inv.getArgument(0);
                Object key = updated.getConfiguration().get(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY);
                if (key instanceof String s) {
                    savedApiKey.set(s);
                    freshKeySaved.countDown();
                }
                return null;
            }).when(callback).thingUpdated(any());
            handler.setCallback(callback);

            try {
                handler.initialize();
                assertTrue(staleConnected.await(5, TimeUnit.SECONDS), "Initial pairing should connect");

                // Reinitialize the same handler instance against freshServer via thingUpdated()
                Bridge updatedBridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "reinitgw")
                        .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port",
                                freshServer.getLocalPort(), "gatewayId", "002f-c2c2-dd88-aaaa", "refresh", 60)))
                        .build();
                handler.thingUpdated(updatedBridge);
                releaseStaleReply.countDown();

                assertTrue(freshKeySaved.await(10, TimeUnit.SECONDS), "Fresh pairing should save API key");
                assertEquals(expectedFreshKey, savedApiKey.get());
            } finally {
                releaseStaleReply.countDown();
                handler.dispose();
            }
        }
    }

    @Test
    public void testDeviceReportAnswerParsingAndFragmentedStream() throws IOException {
        byte[] expectedDeviceId = new byte[] { 0x00, 0x00, 0x35, (byte) 0x86 };
        ByteBuffer data = ByteBuffer.allocate(5);
        data.put((byte) 0x00);
        data.put(ByteUtil.reverse(expectedDeviceId));
        byte[] frame = buildFrame((byte) 0x01, (byte) 0x16, data.array());

        // Feed one byte per read() call to verify readFully handles TCP fragmentation
        InputStream oneByteStream = new InputStream() {
            private int idx = 0;

            @Override
            public int read() {
                return idx < frame.length ? (frame[idx++] & 0xFF) : -1;
            }

            @Override
            public int read(byte @Nullable [] b, int off, int len) {
                if (b == null || idx >= frame.length) {
                    return -1;
                }
                b[off] = frame[idx++];
                return 1;
            }
        };

        SinopeDeviceReportAnswer answer = new SinopeDeviceReportAnswer(oneByteStream);
        assertEquals(0, answer.getStatus());
        assertArrayEquals(expectedDeviceId, answer.getDeviceId());
    }

    @Test
    public void testAnswerThrowsEofOnClosedStream() {
        assertThrows(EOFException.class, () -> new SinopePingAnswer(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    @Timeout(15)
    public void testGatewayAutomaticApiKeyPairing() throws Exception {
        byte[] expectedKey = new byte[] { 0x12, 0x57, 0x55, (byte) 0xD5, (byte) 0xCD, 0x4A, (byte) 0xD5, 0x33 };
        String expectedFormattedKey = ByteUtil.toString(expectedKey).trim();
        CountDownLatch pendingLatch = new CountDownLatch(1);
        CountDownLatch configUpdatedLatch = new CountDownLatch(1);
        CountDownLatch onlineLatch = new CountDownLatch(1);
        AtomicReference<String> savedApiKey = new AtomicReference<>("");

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try {
                    // Connection 1: pairing (ping + auth key request)
                    try (Socket s1 = server.accept()) {
                        InputStream in = s1.getInputStream();
                        OutputStream out = s1.getOutputStream();

                        byte[] pingReq = readFrame(in);
                        assertEquals(0x12, pingReq[4]);
                        assertEquals(0x00, pingReq[5]);
                        out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                        out.flush();

                        byte[] authReq = readFrame(in);
                        assertEquals(0x0a, authReq[4]);
                        assertEquals(0x01, authReq[5]);

                        ByteBuffer keyData = ByteBuffer.allocate(11);
                        keyData.put((byte) 0x01); // GT150 returns 0x01 when WEB button is pressed
                        keyData.put((byte) 0x00);
                        keyData.put((byte) 0x00);
                        keyData.put(ByteUtil.reverse(expectedKey));
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, keyData.array()));
                        out.flush();
                    }

                    // Connection 2: normal API login with newly obtained key
                    try (Socket s2 = server.accept()) {
                        InputStream in = s2.getInputStream();
                        OutputStream out = s2.getOutputStream();

                        byte[] loginReq = readFrame(in);
                        assertEquals(0x10, loginReq[4]);
                        assertEquals(0x01, loginReq[5]);

                        byte[] loginData = new byte[10]; // status=0 + backoff + swRev + deviceId
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, loginData));
                        out.flush();

                        // Keep connection open briefly until test finishes
                        in.read();
                    }
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "testgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            doAnswer(inv -> {
                ThingStatusInfo statusInfo = inv.getArgument(1);
                bridge.setStatusInfo(statusInfo);
                if (statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_PENDING) {
                    pendingLatch.countDown();
                }
                if (statusInfo.getStatus() == ThingStatus.ONLINE
                        && statusInfo.getStatusDetail() == ThingStatusDetail.NONE) {
                    onlineLatch.countDown();
                }
                return null;
            }).when(callback).statusUpdated(any(), any());
            doAnswer(inv -> {
                Bridge updated = inv.getArgument(0);
                Object key = updated.getConfiguration().get(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY);
                if (key instanceof String s) {
                    savedApiKey.set(s);
                    configUpdatedLatch.countDown();
                }
                return null;
            }).when(callback).thingUpdated(any());

            handler.setCallback(callback);
            try {
                handler.initialize();
                assertTrue(pendingLatch.await(10, TimeUnit.SECONDS), "Gateway should enter CONFIGURATION_PENDING");
                assertTrue(configUpdatedLatch.await(10, TimeUnit.SECONDS), "API key should be saved to configuration");
                assertEquals(expectedFormattedKey, savedApiKey.get());
                assertTrue(onlineLatch.await(10, TimeUnit.SECONDS), "Gateway should come ONLINE after pairing");
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(15)
    public void testDeviceDiscoveryReportsThermostat() throws Exception {
        byte[] expectedDeviceId = new byte[] { 0x00, 0x00, 0x35, (byte) 0x86 };
        CountDownLatch discoveredLatch = new CountDownLatch(1);
        AtomicReference<@Nullable DiscoveryResult> discoveredResult = new AtomicReference<>();

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try (Socket s = server.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();

                    // Ping
                    readFrame(in);
                    out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                    out.flush();

                    // Login
                    readFrame(in);
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                    out.flush();

                    // Send unsolicited device report after user presses buttons on thermostat
                    ByteBuffer reportData = ByteBuffer.allocate(5);
                    reportData.put((byte) 0x00);
                    reportData.put(ByteUtil.reverse(expectedDeviceId));
                    out.write(buildFrame((byte) 0x01, (byte) 0x16, reportData.array()));
                    out.flush();

                    in.read();
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "testgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            handler.setCallback(callback);

            SinopeThingsDiscoveryService discoveryService = new SinopeThingsDiscoveryService();
            discoveryService.setThingHandler(handler);
            discoveryService.addDiscoveryListener(new DiscoveryListener() {
                @Override
                public void thingDiscovered(org.openhab.core.config.discovery.DiscoveryService source,
                        DiscoveryResult result) {
                    discoveredResult.set(result);
                    discoveredLatch.countDown();
                }

                @Override
                public void thingRemoved(org.openhab.core.config.discovery.DiscoveryService source, ThingUID thingUID) {
                }

                @Override
                public java.util.@org.eclipse.jdt.annotation.Nullable Collection<ThingUID> removeOlderResults(
                        org.openhab.core.config.discovery.DiscoveryService source, java.time.Instant timestamp,
                        java.util.@org.eclipse.jdt.annotation.Nullable Collection<org.openhab.core.thing.ThingTypeUID> thingTypeUIDs,
                        @org.eclipse.jdt.annotation.Nullable ThingUID bridgeUID) {
                    return null;
                }
            });

            try {
                discoveryService.startScan();
                assertTrue(discoveredLatch.await(10, TimeUnit.SECONDS), "Thermostat should be discovered");

                DiscoveryResult result = discoveredResult.get();
                assertNotNull(result);
                assertEquals(SinopeBindingConstants.THING_TYPE_THERMO, result.getThingTypeUID());
                assertEquals(bridge.getUID(), result.getBridgeUID());
                assertEquals("0x00 0x00 0x35 0x86",
                        result.getProperties().get(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID));
                assertEquals(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID, result.getRepresentationProperty());
                assertThat(result.getLabel(), containsString("00003586"));
            } finally {
                discoveryService.abortScan();
                handler.dispose();
            }
        }
    }

    @Test
    public void testConfigStatusAllowsBlankApiKeyForPairing() {
        Bridge bridge = BridgeBuilder
                .create(SinopeBindingConstants.THING_TYPE_GATEWAY, "testgw").withConfiguration(new Configuration(Map
                        .of("hostname", "127.0.0.1", "port", 4550, "gatewayId", "002f-c2c2-dd88-aaaa", "refresh", 60)))
                .build();

        SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
        assertTrue(handler.getConfigStatus().isEmpty());

        Bridge invalidKeyBridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "testgw2")
                .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", 4550, "gatewayId",
                        "002f-c2c2-dd88-aaaa", "apiKey", "nothex!!", "refresh", 60)))
                .build();
        SinopeGatewayHandler invalidHandler = new SinopeGatewayHandler(invalidKeyBridge);
        assertEquals(1, invalidHandler.getConfigStatus().size());
        assertEquals(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY,
                invalidHandler.getConfigStatus().iterator().next().parameterName);
    }

    @Test
    public void testTruncatedLoginAndDataAnswersThrowIOException() {
        // Login frame declaring size=2 (empty data) must fail with IOException instead of ArrayIndexOutOfBounds
        byte[] emptyLoginFrame = buildFrame((byte) 0x01, (byte) 0x11, new byte[0]);
        assertThrows(IOException.class, () -> new SinopeApiLoginAnswer(new ByteArrayInputStream(emptyLoginFrame)));

        // Truncated data answer (only 4 data bytes, less than minimum 12) must fail with IOException
        byte[] shortDataFrame = buildFrame((byte) 0x02, (byte) 0x01, new byte[] { 0x01, 0x00, 0x00, 0x00 });
        assertThrows(IOException.class,
                () -> new SinopeDataReadAnswer(new ByteArrayInputStream(shortDataFrame), new SinopeRoomTempData()));

        // Outer app-data length = 5, nested value length = 2 with 0 value bytes must fail with IOException
        byte[] nestedTruncatedData = new byte[] { 0x01, 0x00, 0x00, 0x00, // seq
                0x0A, // status
                0x00, // attempt
                0x00, // more
                0x01, 0x02, 0x03, 0x04, // srcDeviceId
                0x05, // outer app data length = 5
                0x00, 0x00, 0x02, 0x03, // dataId (4 bytes)
                0x02 // nested length = 2, but 0 bytes follow
        };
        byte[] nestedTruncatedFrame = buildFrame((byte) 0x02, (byte) 0x01, nestedTruncatedData);
        assertThrows(IOException.class, () -> new SinopeDataReadAnswer(new ByteArrayInputStream(nestedTruncatedFrame),
                new SinopeRoomTempData()));

        // Nested value length = 1 (with 1 byte present) is too short for 2-byte SinopeRoomTempData
        byte[] undersizedValueData = new byte[] { 0x01, 0x00, 0x00, 0x00, // seq
                0x0A, // status
                0x00, // attempt
                0x00, // more
                0x01, 0x02, 0x03, 0x04, // srcDeviceId
                0x06, // outer app data length = 6
                0x00, 0x00, 0x02, 0x03, // dataId (4 bytes)
                0x01, // nested length = 1 (< 2 expected)
                0x2A // single byte payload
        };
        byte[] undersizedValueFrame = buildFrame((byte) 0x02, (byte) 0x01, undersizedValueData);
        assertThrows(IOException.class, () -> new SinopeDataReadAnswer(new ByteArrayInputStream(undersizedValueFrame),
                new SinopeRoomTempData()));
    }

    @Test
    public void testThermostatInvalidDeviceIdSetsConfigurationErrorWithoutException() {
        Thing thing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "badthermo")
                .withConfiguration(new Configuration(Map.of("deviceId", "nothex!!"))).build();

        SinopeThermostatHandler handler = new SinopeThermostatHandler(thing);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        AtomicReference<@Nullable ThingStatusInfo> lastStatus = new AtomicReference<>();
        doAnswer(inv -> {
            lastStatus.set(inv.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        handler.setCallback(callback);

        assertDoesNotThrow(handler::initialize);
        ThingStatusInfo status = lastStatus.get();
        assertNotNull(status);
        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
    }

    @Test
    @Timeout(10)
    public void testConcurrentGetClientSocketSerializesSingleLogin() throws Exception {
        AtomicInteger acceptCount = new AtomicInteger();
        CountDownLatch allowLoginReply = new CountDownLatch(1);
        CountDownLatch firstAccepted = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                while (!server.isClosed()) {
                    try {
                        Socket s = server.accept();
                        int count = acceptCount.incrementAndGet();
                        if (count == 1) {
                            firstAccepted.countDown();
                            allowLoginReply.await(5, TimeUnit.SECONDS);
                        }
                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream();
                        readFrame(in); // login
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                        out.flush();
                    } catch (IOException | InterruptedException ignored) {
                        break;
                    }
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "concgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            handler.setCallback(callback);

            try {
                AtomicReference<@Nullable Socket> s1 = new AtomicReference<>();
                AtomicReference<@Nullable Socket> s2 = new AtomicReference<>();
                CountDownLatch done = new CountDownLatch(2);

                Thread t1 = new Thread(() -> {
                    try {
                        s1.set(handler.getClientSocket());
                    } catch (IOException ignored) {
                    } finally {
                        done.countDown();
                    }
                });
                Thread t2 = new Thread(() -> {
                    try {
                        firstAccepted.await(5, TimeUnit.SECONDS);
                        s2.set(handler.getClientSocket());
                    } catch (IOException | InterruptedException ignored) {
                    } finally {
                        done.countDown();
                    }
                });
                t1.start();
                t2.start();
                assertTrue(firstAccepted.await(5, TimeUnit.SECONDS));
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (t2.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }
                assertEquals(Thread.State.BLOCKED, t2.getState(), "Second caller should block waiting on connectLock");
                allowLoginReply.countDown();
                assertTrue(done.await(5, TimeUnit.SECONDS));

                assertNotNull(s1.get());
                assertSame(s1.get(), s2.get(), "Concurrent callers should reuse the single serialized connection");
                assertEquals(1, acceptCount.get(), "Gateway should receive only one TCP login connection");
            } finally {
                allowLoginReply.countDown();
                handler.dispose();
            }
        }
    }

    @Test
    public void testInvalidPortRejectedByConfigStatusAndInitialize() {
        for (int badPort : new int[] { 0, -1, 65536, 70000 }) {
            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "badport")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", badPort, "gatewayId",
                            "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33", "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            assertEquals(1, handler.getConfigStatus().size(), "Port " + badPort + " should fail config status");
            assertEquals(SinopeBindingConstants.CONFIG_PROPERTY_PORT,
                    handler.getConfigStatus().iterator().next().parameterName);

            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            AtomicReference<@Nullable ThingStatusInfo> lastStatus = new AtomicReference<>();
            doAnswer(inv -> {
                lastStatus.set(inv.getArgument(1));
                return null;
            }).when(callback).statusUpdated(any(), any());
            handler.setCallback(callback);

            assertDoesNotThrow(handler::initialize);
            ThingStatusInfo status = lastStatus.get();
            assertNotNull(status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail(),
                    "Port " + badPort + " should set CONFIGURATION_ERROR");
            handler.dispose();
        }
    }

    @Test
    @Timeout(15)
    public void testAbortScanStopsGatewaySearchAndResumesNormalRequests() throws Exception {
        CountDownLatch searchLoggedIn = new CountDownLatch(1);
        CountDownLatch searchClosed = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try {
                    // Connection 1: discovery scan (ping + login, then blocks until scan aborted)
                    try (Socket s1 = server.accept()) {
                        InputStream in = s1.getInputStream();
                        OutputStream out = s1.getOutputStream();
                        readFrame(in); // ping
                        out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                        out.flush();
                        readFrame(in); // login
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                        out.flush();
                        searchLoggedIn.countDown();
                        in.read(); // blocks until abortScan closes searchSocket
                    } finally {
                        searchClosed.countDown();
                    }

                    // Connection 2: normal client login after abortScan
                    try (Socket s2 = server.accept()) {
                        InputStream in = s2.getInputStream();
                        OutputStream out = s2.getOutputStream();
                        readFrame(in); // login
                        out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                        out.flush();
                        in.read();
                    }
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "abortgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();

            SinopeGatewayHandler handler = new SinopeGatewayHandler(bridge);
            handler.setCallback(mock(ThingHandlerCallback.class));

            SinopeThingsDiscoveryService discoveryService = new SinopeThingsDiscoveryService();
            discoveryService.setThingHandler(handler);

            try {
                discoveryService.startScan();
                assertTrue(searchLoggedIn.await(5, TimeUnit.SECONDS), "Discovery scan should log into gateway");

                discoveryService.abortScan();
                assertTrue(searchClosed.await(5, TimeUnit.SECONDS), "abortScan() should close search socket promptly");

                Socket normalSocket = handler.getClientSocket();
                assertNotNull(normalSocket);
                assertTrue(normalSocket.isConnected() && !normalSocket.isClosed());
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(10)
    public void testSetpointDecimalCommandRoundsToNearestHundredth() throws Exception {
        AtomicReference<byte @Nullable []> capturedWriteFrame = new AtomicReference<>();
        CountDownLatch writeReceived = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try (Socket s = server.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();

                    // Login
                    readFrame(in);
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                    out.flush();

                    // Data write request
                    byte[] writeReq = readFrame(in);
                    capturedWriteFrame.set(writeReq);
                    writeReceived.countDown();

                    // Reply with DATA_ANSWER (status=0x0A)
                    byte[] writeReplyData = new byte[] { 0x01, 0x00, 0x00, 0x00, // seq
                            0x0A, // status = DATA_ANSWER
                            0x00, // attempt
                            0x00, // more
                            0x01, 0x02, 0x03, 0x04, // srcDeviceId
                            0x04, // app data length
                            0x08, 0x02, 0x00, 0x00 // dataId
                    };
                    out.write(buildFrame((byte) 0x02, (byte) 0x04, writeReplyData));
                    out.flush();
                    in.read();
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "tempgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();
            SinopeGatewayHandler gatewayHandler = new SinopeGatewayHandler(bridge);
            gatewayHandler.setCallback(mock(ThingHandlerCallback.class));
            bridge.setHandler(gatewayHandler);

            Thing thermoThing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "t1")
                    .withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("deviceId", "0x00 0x00 0x35 0x86"))).build();
            SinopeThermostatHandler thermoHandler = new SinopeThermostatHandler(thermoThing) {
                @Override
                protected @Nullable Bridge getBridge() {
                    return bridge;
                }
            };
            thermoHandler.setCallback(mock(ThingHandlerCallback.class));

            try {
                thermoHandler.initialize();
                thermoHandler.handleCommand(
                        new ChannelUID(thermoThing.getUID(), SinopeBindingConstants.CHANNEL_SETTEMP),
                        new DecimalType("21.3"));

                assertTrue(writeReceived.await(5, TimeUnit.SECONDS), "Gateway should receive setpoint write request");
                byte[] frame = capturedWriteFrame.get();
                assertNotNull(frame);
                // Last 3 bytes of frame are: [tempLo, tempHi, crc8]
                short encodedTemp = ByteBuffer.wrap(frame, frame.length - 3, 2).order(ByteOrder.LITTLE_ENDIAN)
                        .getShort();
                assertEquals(2130, encodedTemp, "21.3 C must round to 2130 hundredths, not truncate to 2129");
            } finally {
                gatewayHandler.dispose();
            }
        }
    }

    @Test
    @Timeout(10)
    public void testCommandDuringDiscoveryDoesNotMarkThermostatOffline() throws Exception {
        CountDownLatch searchLoggedIn = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try (Socket s = server.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();
                    readFrame(in); // ping
                    out.write(buildFrame((byte) 0x00, (byte) 0x13, new byte[0]));
                    out.flush();
                    readFrame(in); // login
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                    out.flush();
                    searchLoggedIn.countDown();
                    in.read();
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "busygw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();
            SinopeGatewayHandler gatewayHandler = new SinopeGatewayHandler(bridge);
            gatewayHandler.setCallback(mock(ThingHandlerCallback.class));
            bridge.setHandler(gatewayHandler);

            Thing thermoThing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "t2")
                    .withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("deviceId", "0x00 0x00 0x35 0x86"))).build();
            SinopeThermostatHandler thermoHandler = new SinopeThermostatHandler(thermoThing) {
                @Override
                protected @Nullable Bridge getBridge() {
                    return bridge;
                }
            };
            ThingHandlerCallback thermoCallback = mock(ThingHandlerCallback.class);
            AtomicReference<@Nullable ThingStatusInfo> lastThermoStatus = new AtomicReference<>();
            doAnswer(inv -> {
                ThingStatusInfo info = inv.getArgument(1);
                thermoThing.setStatusInfo(info);
                lastThermoStatus.set(info);
                return null;
            }).when(thermoCallback).statusUpdated(any(), any());
            thermoHandler.setCallback(thermoCallback);

            try {
                thermoHandler.initialize();
                ThingStatusInfo initialStatus = lastThermoStatus.get();
                assertNotNull(initialStatus);
                assertEquals(ThingStatus.ONLINE, initialStatus.getStatus());

                gatewayHandler.startSearch(id -> {
                });
                assertTrue(searchLoggedIn.await(5, TimeUnit.SECONDS), "Gateway should enter discovery mode");

                thermoHandler.handleCommand(
                        new ChannelUID(thermoThing.getUID(), SinopeBindingConstants.CHANNEL_SETTEMP),
                        new DecimalType("21.5"));

                ThingStatusInfo statusAfter = lastThermoStatus.get();
                assertNotNull(statusAfter);
                assertEquals(ThingStatus.ONLINE, statusAfter.getStatus(),
                        "Command during discovery must not mark thermostat offline");
                assertEquals(ThingStatusDetail.NONE, statusAfter.getStatusDetail());
            } finally {
                gatewayHandler.stopSearch();
                gatewayHandler.dispose();
            }
        }
    }

    @Test
    @Timeout(15)
    public void testStaleCommandErrorDoesNotCloseReplacementSocketAfterReinit() throws Exception {
        CountDownLatch oldWriteBlocked = new CountDownLatch(1);
        CountDownLatch releaseOldServerClose = new CountDownLatch(1);
        CountDownLatch oldCommandDone = new CountDownLatch(1);
        CountDownLatch newOnline = new CountDownLatch(1);

        try (ServerSocket oldServer = new ServerSocket(0); ServerSocket newServer = new ServerSocket(0)) {
            Thread oldGateway = new Thread(() -> {
                try (Socket s = oldServer.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();
                    readFrame(in); // login
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                    out.flush();
                    readFrame(in); // setpoint write request from old generation
                    oldWriteBlocked.countDown();
                    releaseOldServerClose.await(5, TimeUnit.SECONDS);
                } catch (IOException | InterruptedException ignored) {
                }
            });
            oldGateway.setDaemon(true);
            oldGateway.start();

            Thread newGateway = new Thread(() -> {
                try (Socket s = newServer.accept()) {
                    InputStream in = s.getInputStream();
                    OutputStream out = s.getOutputStream();
                    readFrame(in); // login on fresh generation
                    out.write(buildFrame((byte) 0x01, (byte) 0x11, new byte[10]));
                    out.flush();
                    in.read();
                } catch (IOException ignored) {
                }
            });
            newGateway.setDaemon(true);
            newGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "reinitcmdgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port",
                            oldServer.getLocalPort(), "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey",
                            "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33", "refresh", 60)))
                    .build();

            SinopeGatewayHandler gatewayHandler = new SinopeGatewayHandler(bridge);
            AtomicReference<@Nullable ThingStatusInfo> lastGatewayStatus = new AtomicReference<>();
            ThingHandlerCallback gwCallback = mock(ThingHandlerCallback.class);
            doAnswer(inv -> {
                Bridge targetBridge = inv.getArgument(0);
                ThingStatusInfo info = inv.getArgument(1);
                targetBridge.setStatusInfo(info);
                lastGatewayStatus.set(info);
                if (info.getStatus() == ThingStatus.ONLINE && info.getStatusDetail() == ThingStatusDetail.NONE
                        && newServer.getLocalPort() == ((Number) gatewayHandler.getThing().getConfiguration()
                                .get(SinopeBindingConstants.CONFIG_PROPERTY_PORT)).intValue()) {
                    newOnline.countDown();
                }
                return null;
            }).when(gwCallback).statusUpdated(any(), any());
            gatewayHandler.setCallback(gwCallback);
            bridge.setHandler(gatewayHandler);

            Thing thermoThing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "t3")
                    .withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("deviceId", "0x00 0x00 0x35 0x86"))).build();
            SinopeThermostatHandler thermoHandler = new SinopeThermostatHandler(thermoThing) {
                @Override
                protected @Nullable Bridge getBridge() {
                    return bridge;
                }
            };
            thermoHandler.setCallback(mock(ThingHandlerCallback.class));

            try {
                thermoHandler.initialize();
                Thread oldCmdThread = new Thread(() -> {
                    try {
                        thermoHandler.handleCommand(
                                new ChannelUID(thermoThing.getUID(), SinopeBindingConstants.CHANNEL_SETTEMP),
                                new DecimalType("21.0"));
                    } finally {
                        oldCommandDone.countDown();
                    }
                });
                oldCmdThread.setDaemon(true);
                oldCmdThread.start();

                assertTrue(oldWriteBlocked.await(5, TimeUnit.SECONDS), "Old command should reach old gateway");

                Bridge updatedBridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "reinitcmdgw")
                        .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port",
                                newServer.getLocalPort(), "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey",
                                "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33", "refresh", 60)))
                        .build();
                gatewayHandler.thingUpdated(updatedBridge);
                releaseOldServerClose.countDown();

                assertTrue(newOnline.await(5, TimeUnit.SECONDS), "Reinitialized gateway should come ONLINE");
                assertTrue(oldCommandDone.await(5, TimeUnit.SECONDS), "Old command thread should exit");

                Socket replacementSocket = gatewayHandler.getClientSocket();
                assertNotNull(replacementSocket);
                assertTrue(replacementSocket.isConnected() && !replacementSocket.isClosed(),
                        "Stale command error must not close healthy replacement socket");
                ThingStatusInfo gwStatus = lastGatewayStatus.get();
                assertNotNull(gwStatus);
                assertEquals(ThingStatus.ONLINE, gwStatus.getStatus(),
                        "Stale command error must not overwrite healthy ONLINE status");
            } finally {
                releaseOldServerClose.countDown();
                gatewayHandler.dispose();
            }
        }
    }

    @Test
    public void testCommandOnMisconfiguredGatewayPreservesConfigurationError() {
        Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "misconfiggw")
                .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", 70000, "gatewayId",
                        "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33", "refresh", 60)))
                .build();
        SinopeGatewayHandler gatewayHandler = new SinopeGatewayHandler(bridge);
        AtomicReference<@Nullable ThingStatusInfo> lastGwStatus = new AtomicReference<>();
        ThingHandlerCallback gwCallback = mock(ThingHandlerCallback.class);
        doAnswer(inv -> {
            ThingStatusInfo info = inv.getArgument(1);
            bridge.setStatusInfo(info);
            lastGwStatus.set(info);
            return null;
        }).when(gwCallback).statusUpdated(any(), any());
        gatewayHandler.setCallback(gwCallback);
        bridge.setHandler(gatewayHandler);

        Thing thermoThing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "t4")
                .withBridge(bridge.getUID())
                .withConfiguration(new Configuration(Map.of("deviceId", "0x00 0x00 0x35 0x86"))).build();
        SinopeThermostatHandler thermoHandler = new SinopeThermostatHandler(thermoThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };
        AtomicReference<@Nullable ThingStatusInfo> lastThermoStatus = new AtomicReference<>();
        ThingHandlerCallback thermoCallback = mock(ThingHandlerCallback.class);
        doAnswer(inv -> {
            ThingStatusInfo info = inv.getArgument(1);
            thermoThing.setStatusInfo(info);
            lastThermoStatus.set(info);
            return null;
        }).when(thermoCallback).statusUpdated(any(), any());
        thermoHandler.setCallback(thermoCallback);

        try {
            gatewayHandler.initialize();
            thermoHandler.initialize();

            ThingStatusInfo gwStatusBefore = lastGwStatus.get();
            assertNotNull(gwStatusBefore);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, gwStatusBefore.getStatusDetail());

            thermoHandler.handleCommand(new ChannelUID(thermoThing.getUID(), SinopeBindingConstants.CHANNEL_SETTEMP),
                    new DecimalType("21.5"));

            ThingStatusInfo gwStatusAfter = lastGwStatus.get();
            assertNotNull(gwStatusAfter);
            assertEquals(gwStatusBefore.getDescription(), gwStatusAfter.getDescription(),
                    "Command on misconfigured gateway must not overwrite CONFIGURATION_ERROR");
            ThingStatusInfo thermoStatusAfter = lastThermoStatus.get();
            assertNotNull(thermoStatusAfter);
            assertEquals(ThingStatus.ONLINE, thermoStatusAfter.getStatus(),
                    "Command on inactive misconfigured gateway should be skipped without setting COMMUNICATION_ERROR");
        } finally {
            gatewayHandler.dispose();
        }
    }

    @Test
    @Timeout(10)
    public void testCommandWithEmptyDeviceIdDoesNotConnect() throws Exception {
        AtomicInteger acceptCount = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0)) {
            Thread mockGateway = new Thread(() -> {
                try (Socket ignored = server.accept()) {
                    acceptCount.incrementAndGet();
                } catch (IOException ignored) {
                }
            });
            mockGateway.setDaemon(true);
            mockGateway.start();

            Bridge bridge = BridgeBuilder.create(SinopeBindingConstants.THING_TYPE_GATEWAY, "emptyidgw")
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", server.getLocalPort(),
                            "gatewayId", "002f-c2c2-dd88-aaaa", "apiKey", "0x12 0x57 0x55 0xD5 0xCD 0x4A 0xD5 0x33",
                            "refresh", 60)))
                    .build();
            SinopeGatewayHandler gatewayHandler = new SinopeGatewayHandler(bridge);
            gatewayHandler.setCallback(mock(ThingHandlerCallback.class));
            bridge.setHandler(gatewayHandler);

            Thing thermoThing = ThingBuilder.create(SinopeBindingConstants.THING_TYPE_THERMO, "t5")
                    .withBridge(bridge.getUID()).withConfiguration(new Configuration(Map.of("deviceId", "bad-hex")))
                    .build();
            SinopeThermostatHandler thermoHandler = new SinopeThermostatHandler(thermoThing) {
                @Override
                protected @Nullable Bridge getBridge() {
                    return bridge;
                }
            };
            thermoHandler.setCallback(mock(ThingHandlerCallback.class));

            try {
                thermoHandler.initialize();
                thermoHandler.handleCommand(
                        new ChannelUID(thermoThing.getUID(), SinopeBindingConstants.CHANNEL_SETTEMP),
                        new DecimalType("21.0"));
                assertEquals(0, acceptCount.get(),
                        "Thermostat with invalid/empty deviceId must not open gateway connection");
            } finally {
                gatewayHandler.dispose();
            }
        }
    }
}
