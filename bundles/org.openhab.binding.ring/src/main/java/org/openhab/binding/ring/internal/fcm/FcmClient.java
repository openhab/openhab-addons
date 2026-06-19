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

package org.openhab.binding.ring.internal.fcm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a Firebase Cloud Messaging (FCM) Client
 *
 * @author Paul Smedley - Initial contribution
 *
 */
public class FcmClient {
    private static final Logger logger = LoggerFactory.getLogger(FcmClient.class);
    private static final String HOST = "mtalk.google.com";
    private static final int PORT = 5228;

    // MCS Tags
    private static final int TAG_HEARTBEAT_PING = 0;
    private static final int TAG_HEARTBEAT_ACK = 1;
    private static final int TAG_LOGIN_REQUEST = 2;
    private static final int TAG_LOGIN_RESPONSE = 3;
    private static final int TAG_DATA_MESSAGE_STANZA = 8;

    private SSLSocket socket;
    private InputStream in;
    private OutputStream out;
    private volatile boolean isRunning = false;

    private final Consumer<String> eventCallback;
    private final Consumer<Boolean> stateCallback;

    public FcmClient(Consumer<String> eventCallback, Consumer<Boolean> stateCallback) {
        this.eventCallback = eventCallback;
        this.stateCallback = stateCallback;
    }

    public void connect(String androidId, String securityToken) {
        try {
            socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(HOST, PORT);
            socket.setKeepAlive(true);
            in = socket.getInputStream();
            out = socket.getOutputStream();

            // Send Protocol Version (41)
            out.write(41);

            // Build Login Request
            LoginRequest loginRequest = LoginRequest.newBuilder().setId("chrome-").setDomain("mcs.android.com")
                    .setUser(androidId).setResource(androidId).setAuthToken(securityToken)
                    .setDeviceId("android-" + Long.toHexString(Long.parseLong(androidId))).build();

            send(TAG_LOGIN_REQUEST, loginRequest.toByteArray());
            isRunning = true;

            // Start Virtual Thread for listening
            Thread.ofVirtual().name("ring-fcm-listener").start(this::listenLoop);

            // Start Virtual Thread for Heartbeats (every 5 mins)
            Thread.ofVirtual().name("ring-fcm-heartbeat").start(this::heartbeatLoop);

        } catch (Exception e) {
            logger.error("Failed to connect to FCM Socket", e);
        }
    }

    private void send(int tag, byte[] protobufData) throws Exception {
        out.write(tag);
        writeVarInt(out, protobufData.length);
        out.write(protobufData);
        out.flush();
    }

    private void listenLoop() {
        try {
            while (isRunning && !socket.isClosed()) {
                int tag = in.read();
                if (tag == -1)
                    break;

                int size = readVarInt(in);
                byte[] data = in.readNBytes(size);

                switch (tag) {
                    case TAG_LOGIN_RESPONSE -> {
                        LoginResponse response = LoginResponse.parseFrom(data);
                        logger.debug("FCM Login Response: {}", response.getId());
                    }
                    case TAG_DATA_MESSAGE_STANZA -> {
                        DataMessageStanza msg = DataMessageStanza.parseFrom(data);
                        handlePushMessage(msg);
                    }
                    case TAG_HEARTBEAT_PING -> send(TAG_HEARTBEAT_ACK, new byte[0]);
                    case TAG_HEARTBEAT_ACK -> logger.trace("FCM Heartbeat ACK received");
                }
            }
        } catch (Exception e) {
            logger.warn("FCM Listener connection lost", e);
            disconnect();
        }
    }

    private void handlePushMessage(DataMessageStanza msg) {
        // Ring packs the actual event payload JSON into the AppData key/value pairs
        for (AppData data : msg.getAppDataList()) {
            if ("payload".equals(data.getKey()) || "data".equals(data.getKey())) {
                eventCallback.accept(data.getValue());
            }
        }
    }

    private void heartbeatLoop() {
        while (isRunning) {
            try {
                Thread.sleep(300000); // 5 minutes
                send(TAG_HEARTBEAT_PING, new byte[0]);
            } catch (Exception e) {
                disconnect();
            }
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null)
                socket.close();
        } catch (Exception ignored) {
        }
    }

    // --- VarInt Helpers (Google's Base 128 Varints) ---
    private int readVarInt(InputStream is) throws Exception {
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = is.read();
            if (b == -1)
                throw new Exception("EOF reading VarInt");
            result |= (b & 0x7f) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    private void writeVarInt(OutputStream os, int value) throws Exception {
        while (true) {
            if ((value & ~0x7F) == 0) {
                os.write(value);
                return;
            } else {
                os.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }
}
