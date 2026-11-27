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
package org.openhab.binding.miio.internal.transport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.thing.ThingStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the status reported for a directly connected device, using a fake device on the loopback interface.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
@Timeout(30)
public class MiIoAsyncCommunicationStatusTest {

    private static final String DEVICE_ID = "2864434397";
    private static final byte[] TOKEN = new byte[16];

    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) MiIoMessageListener listener;
    private @Nullable DatagramSocket deviceSocket;
    private String deviceHost = "127.0.0.1";
    private @Nullable Thread deviceThread;
    private @Nullable MiIoAsyncCommunication communication;

    @BeforeEach
    public void setUp() throws IOException {
        // the binding always talks to the fixed miio port, so the fake device needs that port on the loopback address
        final DatagramSocket socket = bindDevicePort();
        if (socket == null) {
            Assumptions.abort("Port " + MiIoBindingConstants.PORT + " is not available on the loopback address");
            return;
        }
        deviceSocket = socket;
        deviceHost = socket.getLocalAddress().getHostAddress();
        Thread thread = new Thread(() -> runFakeDevice(socket), "miio-fake-device");
        thread.setDaemon(true);
        thread.start();
        deviceThread = thread;
    }

    private static @Nullable DatagramSocket bindDevicePort() {
        try {
            DatagramSocket socket = new DatagramSocket(null);
            try {
                socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), MiIoBindingConstants.PORT));
                return socket;
            } catch (IOException e) {
                socket.close();
                return null;
            }
        } catch (SocketException e) {
            return null;
        }
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        MiIoAsyncCommunication communication = this.communication;
        if (communication != null) {
            communication.close();
        }
        DatagramSocket socket = deviceSocket;
        if (socket != null) {
            socket.close();
        }
        Thread thread = deviceThread;
        if (thread != null) {
            thread.join(5000);
        }
    }

    /**
     * Answers each request with an encrypted reply carrying the id of the request.
     */
    private void runFakeDevice(DatagramSocket socket) {
        byte[] deviceIdBytes = Utils.hexStringToByteArray(Utils.getHexId(DEVICE_ID));
        while (!socket.isClosed()) {
            try {
                DatagramPacket request = new DatagramPacket(new byte[2048], 2048);
                socket.receive(request);
                byte[] raw = Arrays.copyOfRange(request.getData(), 0, request.getLength());
                byte[] payload = new byte[0];
                if (raw.length > 32) {
                    String command = new String(MiIoCrypto.decrypt(new Message(raw).getData(), TOKEN),
                            StandardCharsets.UTF_8).trim();
                    int id = JsonParser.parseString(command).getAsJsonObject().get("id").getAsInt();
                    payload = MiIoCrypto.encrypt(
                            ("{\"id\":" + id + ",\"result\":[\"ok\"]}").getBytes(StandardCharsets.UTF_8), TOKEN);
                }
                byte[] reply = Message.createMsgData(payload, TOKEN, deviceIdBytes,
                        (int) Instant.now().getEpochSecond());
                socket.send(new DatagramPacket(reply, reply.length, request.getSocketAddress()));
            } catch (Exception e) {
                // socket closed at the end of the test
            }
        }
    }

    private MiIoAsyncCommunication createCommunication(boolean cloudCommunication) {
        MiIoAsyncCommunication communication = new MiIoAsyncCommunication(deviceHost, TOKEN, DEVICE_ID, 1, 5000,
                cloudConnector, cloudCommunication);
        communication.getListeners().add(listener);
        this.communication = communication;
        return communication;
    }

    private MiIoSendCommand command(int id) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("method", "get_properties");
        json.add("params", JsonParser.parseString("[]"));
        return new MiIoSendCommand(id, MiIoCommand.GET_PROPERTIES, json, "", "");
    }

    @Test
    public void firstReplyFromDeviceSetsOnlineOnce() {
        MiIoAsyncCommunication communication = createCommunication(false);

        MiIoSendCommand first = communication.sendMiIoSendCommand(command(2));
        MiIoSendCommand second = communication.sendMiIoSendCommand(command(3));

        assertFalse(first.isError(), first.getResponse().toString());
        assertFalse(second.isError(), second.getResponse().toString());
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onStatusUpdated(ThingStatus.OFFLINE);
        inOrder.verify(listener).onStatusUpdated(ThingStatus.ONLINE);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void pingFollowedByReplySetsOnline() throws IOException {
        MiIoAsyncCommunication communication = createCommunication(false);

        assertNotNull(communication.sendPing(deviceHost));
        MiIoSendCommand reply = communication.sendMiIoSendCommand(command(2));

        assertFalse(reply.isError(), reply.getResponse().toString());
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onStatusUpdated(ThingStatus.OFFLINE);
        inOrder.verify(listener).onStatusUpdated(ThingStatus.ONLINE);
        verifyNoMoreInteractions(listener);
    }
}
