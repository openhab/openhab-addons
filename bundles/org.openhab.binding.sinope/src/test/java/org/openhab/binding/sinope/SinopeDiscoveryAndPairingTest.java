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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.sinope.handler.SinopeGatewayHandler;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeAuthenticationKeyAnswer;
import org.openhab.binding.sinope.internal.core.SinopeDeviceReportAnswer;
import org.openhab.binding.sinope.internal.core.SinopePingAnswer;
import org.openhab.binding.sinope.internal.discovery.SinopeThingsDiscoveryService;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.binding.sinope.internal.util.CRC8;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

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
        byte[] frame = buildFrame((byte) 0x01, (byte) 0x11, new byte[] { 0x02, 0x00, 0x0A });
        SinopeAuthenticationKeyAnswer answer = new SinopeAuthenticationKeyAnswer(new ByteArrayInputStream(frame));

        assertEquals(2, answer.getStatus());
        assertEquals(0, answer.getApiKey().length);
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
                if (statusInfo.getStatus() == ThingStatus.ONLINE) {
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
}
