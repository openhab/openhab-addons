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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a Firebase Cloud Messaging (FCM) Client
 *
 * @author Paul Smedley - Initial contribution
 *
 */
@NonNullByDefault
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

    private @Nullable SSLSocket socket;
    private @Nullable InputStream in;
    private @Nullable OutputStream out;
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
            // Inside FcmClient.connect()...

            out.write(41);
            out.flush();

            LoginRequest loginRequest = LoginRequest.newBuilder().setId("chrome-" + androidId) // <-- Ensure this is
                                                                                               // unique!
                    .setDomain("mcs.android.com").setUser(androidId).setResource(androidId).setAuthToken(securityToken)
                    .setDeviceId("android-" + Long.toHexString(Long.parseLong(androidId))).build();

            send(TAG_LOGIN_REQUEST, loginRequest.toByteArray());
            isRunning = true;

            // RESTORED: The critical heartbeat loop to keep the Google socket alive
            Thread.ofVirtual().name("ring-fcm-heartbeat").start(this::heartbeatLoop);

            // Start Virtual Thread for listening
            Thread.ofVirtual().name("ring-fcm-listener-" + androidId).start(this::listenLoop);
        } catch (IOException | NumberFormatException e) {
            logger.error("Failed to connect to FCM Socket", e);
            stateCallback.accept(false);
        }
    }

    private void send(int tag, byte[] protobufData) throws IOException {
        OutputStream localOut = out;
        if (localOut == null) {
            throw new IOException("Cannot send data: OutputStream is null");
        }

        localOut.write(tag);
        writeVarInt(localOut, protobufData.length);
        localOut.write(protobufData);
        localOut.flush();
    }

    private void listenLoop() {
        try {
            InputStream localIn = in;
            SSLSocket localSocket = socket;

            if (localIn == null || localSocket == null) {
                return;
            }

            // NEW: Consume the server's Version byte (41) before parsing Protobuf tags
            int versionByte = localIn.read();
            if (versionByte != 41) {
                logger.debug("FCM Server reported unexpected version byte: {}", versionByte);
            }

            while (isRunning && !localSocket.isClosed()) {
                int tag = localIn.read();
                if (tag == -1) {
                    break;
                }

                int size = readVarInt(localIn);
                byte[] data = localIn.readNBytes(size);

                switch (tag) {
                    case TAG_LOGIN_RESPONSE -> {
                        // Check if the server closed the socket before sending the full payload
                        if (data.length < size) {
                            logger.warn("FCM Payload truncated! Expected {} bytes but got {}", size, data.length);
                        }

                        try {
                            LoginResponse response = LoginResponse.parseFrom(data);
                            if (response.hasError()) {
                                logger.error("FCM Login failed: {}", response.getError().getMessage());
                                disconnect();
                            } else {
                                logger.debug("FCM Login Successful!");
                                send(TAG_HEARTBEAT_PING, new byte[0]);
                                stateCallback.accept(true);
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "FCM LoginResponse Parse Error! Declared Size: {}, Actual Bytes Read: {}, Raw Hex: {}",
                                    size, data.length, bytesToHex(data), e);
                            disconnect();
                        }
                    }
                    case TAG_DATA_MESSAGE_STANZA -> {
                        logger.debug("FCM Listener: Dispatching push event to handler.");
                        DataMessageStanza msg = DataMessageStanza.parseFrom(data);
                        handlePushMessage(msg);
                    }
                    case TAG_HEARTBEAT_PING -> send(TAG_HEARTBEAT_ACK, new byte[0]);
                    case TAG_HEARTBEAT_ACK -> logger.trace("FCM Heartbeat ACK received");
                    default -> {
                        logger.warn("FCM Unknown Tag Received: {} | Data length: {} | Raw Hex: {}", tag, data.length,
                                bytesToHex(data));
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("FCM Listener connection lost", e);
            disconnect();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private void handlePushMessage(DataMessageStanza msg) {
        // Log the entire raw protobuf structure so we can see exactly what Ring sends
        logger.debug("FCM Raw Stanza Received: \n{}", msg.toString());

        for (AppData data : msg.getAppDataList()) {
            String key = data.getKey();
            String value = data.getValue();

            // Log every individual key-value pair for deep inspection
            logger.debug("FCM AppData - Key: [{}] | Value: [{}]", key, value);

            // Ring typically puts the actual event JSON inside a key named "ding"
            if ("ding".equals(key) || "payload".equals(key) || "data".equals(key)) {
                eventCallback.accept(value);
            }
        }
    }

    private void heartbeatLoop() {
        while (isRunning) {
            try {
                Thread.sleep(300000); // 5 minutes
                send(TAG_HEARTBEAT_PING, new byte[0]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                disconnect();
                return;
            } catch (IOException e) {
                disconnect();
                return;
            }
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Ignored on disconnect
        }
        stateCallback.accept(false);
    }

    // --- VarInt Helpers (Google's Base 128 Varints) ---
    private int readVarInt(InputStream is) throws IOException {
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = is.read();
            if (b == -1)
                throw new IOException("EOF reading VarInt");
            result |= (b & 0x7f) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    private void writeVarInt(OutputStream os, int value) throws IOException {
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
