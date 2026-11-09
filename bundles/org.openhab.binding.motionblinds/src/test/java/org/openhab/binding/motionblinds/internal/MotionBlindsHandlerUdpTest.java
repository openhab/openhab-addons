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
package org.openhab.binding.motionblinds.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.motionblinds.internal.MessageSerializationTest.*;
import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

import com.google.gson.Gson;

/**
 * Tests {@link MotionBlindsHandler} with a real {@link MotionBlindsCommunicationManager} that talks UDP to a fake
 * motor on the loopback interface; only discovery and the multicast listener are stubbed.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsHandlerUdpTest {

    private static final Gson GSON = new Gson();
    private static final String MAC = "244cab4e06b4";
    private static final String KEY = "12ab345c-d67e-8f";
    private static final int REQUEST_TIMEOUT_MS = 500;
    private static final long TIMEOUT_MS = 5000;

    private final AtomicInteger writes = new AtomicInteger();
    private final CountDownLatch firstWrite = new CountDownLatch(1);
    private @NonNullByDefault({}) DatagramSocket motor;
    private @NonNullByDefault({}) Thread motorThread;
    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) MotionBlindsHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        motor = new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        motorThread = new Thread(this::runMotor, "fake-motor");
        motorThread.setDaemon(true);
        motorThread.start();

        String motorIp = InetAddress.getLoopbackAddress().getHostAddress();
        MotionBlindsCommunicationManager manager = spy(
                new MotionBlindsCommunicationManager(motor.getLocalPort(), REQUEST_TIMEOUT_MS));
        doNothing().when(manager).registerListener(anyString(), any());
        doNothing().when(manager).unregisterListener(anyString(), any());
        doAnswer(invocation -> {
            handler.onMessage(parse(GET_DEVICE_LIST_ACK), motorIp);
            return null;
        }).when(manager).sendDiscoveryRequest();

        callback = mock(ThingHandlerCallback.class);
        // like the framework, apply status updates to the thing
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));

        Thing thing = ThingBuilder.create(THING_TYPE_WIFI_MOTOR, MAC)
                .withConfiguration(
                        new Configuration(Map.of(CONFIG_MAC_ADDRESS, MAC, "key", KEY, "refreshInterval", 3600)))
                .build();
        handler = new MotionBlindsHandler(thing, manager);
        handler.setCallback(callback);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        handler.dispose();
        motor.close();
        motorThread.join(TIMEOUT_MS);
    }

    @Test
    public void unansweredWriteIsNotRetriedAfterDispose() throws InterruptedException {
        handler.initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), UpDownType.UP);
        assertTrue(firstWrite.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        // the acknowledge of the write is lost, and the Thing is disabled while the request waits for it
        handler.dispose();

        // give the remaining attempts the time they would need
        Thread.sleep(4L * REQUEST_TIMEOUT_MS);
        assertEquals(1, writes.get());
    }

    /**
     * Answers {@code GetDeviceList} and {@code ReadDevice}, and drops the acknowledges of {@code WriteDevice}.
     */
    private void runMotor() {
        byte[] buffer = new byte[4096];
        while (!motor.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                motor.receive(packet);
                String msgType = parse(
                        new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8)).msgType;
                if (MSG_GET_DEVICE_LIST.equals(msgType)) {
                    send(packet.getSocketAddress(), GET_DEVICE_LIST_ACK);
                } else if (MSG_READ_DEVICE.equals(msgType)) {
                    send(packet.getSocketAddress(), READ_DEVICE_ACK);
                } else if (MSG_WRITE_DEVICE.equals(msgType)) {
                    writes.incrementAndGet();
                    firstWrite.countDown();
                }
            } catch (SocketException e) {
                // closed
                return;
            } catch (IOException e) {
                fail(e);
            }
        }
    }

    private void send(SocketAddress target, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        motor.send(new DatagramPacket(data, data.length, target));
    }

    private static MotionBlindsMessage parse(String json) {
        return Objects.requireNonNull(GSON.fromJson(json, MotionBlindsMessage.class));
    }
}
