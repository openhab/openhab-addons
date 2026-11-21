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
import static org.openhab.binding.motionblinds.internal.MessageSerializationTest.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsRequest;

import com.google.gson.Gson;

/**
 * Tests that {@link MotionBlindsCommunicationManager} only accepts responses of the requested motor.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsCommunicationManagerTest {

    private static final Gson GSON = new Gson();
    private static final String MAC = "244cab4e06b4";
    private static final String HEARTBEAT_OF_MOTOR = HEARTBEAT.replace("244cab4e062c", MAC);
    private static final String READ_ACK_OF_OTHER_MOTOR = READ_DEVICE_ACK.replace(MAC, "244cab4e0f44");
    private static final int TIMEOUT_MS = 300;

    private @NonNullByDefault({}) DatagramSocket motor;
    private @NonNullByDefault({}) DatagramSocket stranger;
    private @NonNullByDefault({}) MotionBlindsCommunicationManager manager;

    @BeforeEach
    public void setUp() throws IOException {
        motor = new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        motor.setSoTimeout(5000);
        stranger = new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        manager = new MotionBlindsCommunicationManager(motor.getLocalPort(), TIMEOUT_MS);
    }

    @AfterEach
    public void tearDown() {
        motor.close();
        stranger.close();
    }

    @Test
    public void isResponseAcceptsAcknowledgeOfRequestedMotor() throws IOException {
        InetAddress motorAddress = InetAddress.getByName("192.168.0.21");
        assertTrue(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress, parse(READ_DEVICE_ACK),
                "ReadDeviceAck", MAC));
        assertTrue(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress, parse(READ_DEVICE_ACK),
                "ReadDeviceAck", "24:4C:AB:4E:06:B4"));
        // error replies are passed to the handler
        assertTrue(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress,
                parse(ACCESS_TOKEN_ERROR.replace("WriteDeviceAck", "ReadDeviceAck")), "ReadDeviceAck", MAC));
    }

    @Test
    public void isResponseRejectsOtherPackets() throws IOException {
        InetAddress motorAddress = InetAddress.getByName("192.168.0.21");
        InetAddress otherAddress = InetAddress.getByName("192.168.0.99");
        assertFalse(MotionBlindsCommunicationManager.isResponse(motorAddress, otherAddress, parse(READ_DEVICE_ACK),
                "ReadDeviceAck", MAC));
        assertFalse(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress, parse(READ_DEVICE_ACK),
                "WriteDeviceAck", MAC));
        assertFalse(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress, parse(HEARTBEAT_OF_MOTOR),
                "ReadDeviceAck", MAC));
        assertFalse(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress,
                parse(READ_ACK_OF_OTHER_MOTOR), "ReadDeviceAck", MAC));
        assertFalse(MotionBlindsCommunicationManager.isResponse(motorAddress, motorAddress,
                parse("{\"msgType\":\"ReadDeviceAck\"}"), "ReadDeviceAck", MAC));
    }

    @Test
    public void strayPacketsAreDiscarded() throws Exception {
        CompletableFuture<MotionBlindsMessage> response = sendReadDevice();
        SocketAddress client = receiveRequest();
        send(stranger, client, "not json");
        send(stranger, client, HEARTBEAT_OF_MOTOR);
        send(motor, client, READ_ACK_OF_OTHER_MOTOR);
        send(motor, client, READ_DEVICE_ACK);

        MotionBlindsMessage message = response.get(5, TimeUnit.SECONDS);
        assertEquals("ReadDeviceAck", message.msgType);
        assertEquals(MAC, message.mac);
    }

    @Test
    public void onlyStrayPacketsTimeOut() throws Exception {
        CompletableFuture<MotionBlindsMessage> response = sendReadDevice();
        // answer every attempt with packets that must be discarded
        for (int attempt = 0; attempt < 3; attempt++) {
            SocketAddress client = receiveRequest();
            send(stranger, client, READ_DEVICE_ACK.replace("ReadDeviceAck", "Report"));
            send(motor, client, READ_ACK_OF_OTHER_MOTOR);
        }

        ExecutionException e = assertThrows(ExecutionException.class, () -> response.get(5, TimeUnit.SECONDS));
        assertInstanceOf(UncheckedIOException.class, e.getCause());
    }

    private CompletableFuture<MotionBlindsMessage> sendReadDevice() {
        MotionBlindsRequest request = new MotionBlindsRequest("ReadDevice", MAC, "22000002",
                "4D098FF506E81A40DB823D3F60333214", null);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return manager.sendRequest(InetAddress.getLoopbackAddress().getHostAddress(), request, MAC);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private SocketAddress receiveRequest() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        motor.receive(packet);
        assertTrue(new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8)
                .contains("\"msgType\":\"ReadDevice\""));
        return packet.getSocketAddress();
    }

    private static void send(DatagramSocket socket, SocketAddress target, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(data, data.length, target));
    }

    private static MotionBlindsMessage parse(String json) {
        return Objects.requireNonNull(GSON.fromJson(json, MotionBlindsMessage.class));
    }
}
