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

import javax.net.ssl.SSLParameters;
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
    private static final int TAG_IQ_STANZA = 7;
    private static final int TAG_DATA_MESSAGE_STANZA = 8;

    @FunctionalInterface
    public interface PushEventCallback {
        void onPushEvent(String payloadJson, String androidConfigJson, @Nullable String imgJson);
    }

    private @Nullable SSLSocket socket;
    private @Nullable InputStream in;
    private @Nullable OutputStream out;
    private volatile boolean isRunning = false;

    private final PushEventCallback eventCallback;
    private final Consumer<Boolean> stateCallback;

    public FcmClient(PushEventCallback eventCallback, Consumer<Boolean> stateCallback) {
        this.eventCallback = eventCallback;
        this.stateCallback = stateCallback;
    }

    public void connect(String androidId, String securityToken) {
        try {
            socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(HOST, PORT);
            socket.setKeepAlive(true);

            SSLParameters sslParameters = socket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            socket.setSSLParameters(sslParameters);
            socket.startHandshake();

            in = socket.getInputStream();
            out = socket.getOutputStream();

            // Send Protocol Version (41)
            out.write(41);
            out.flush();

            LoginRequest loginRequest = LoginRequest.newBuilder().setId("chrome-" + androidId)
                    .setDomain("mcs.android.com").setUser(androidId).setResource(androidId).setAuthToken(securityToken)
                    .setDeviceId("android-" + Long.toHexString(Long.parseUnsignedLong(androidId)))
                    .setAuthService(LoginRequest.AuthService.ANDROID_ID).setNetworkType(1).setUseRmq2(true)
                    .setAdaptiveHeartbeat(false)
                    .addSetting(Setting.newBuilder().setName("new_vc").setValue("1").build()).build();

            send(TAG_LOGIN_REQUEST, loginRequest.toByteArray());
            isRunning = true;

            // heartbeat loop to keep the Google socket alive
            Thread.ofVirtual().name("ring-fcm-heartbeat").start(this::heartbeatLoop);

            // Start Virtual Thread for listening
            Thread.ofVirtual().name("ring-fcm-listener-" + androidId).start(this::listenLoop);
        } catch (IOException | NumberFormatException e) {
            logger.debug("Failed to connect to FCM Socket", e);
            disconnect();
        }
    }

    private synchronized void send(int tag, byte[] protobufData) throws IOException {
        OutputStream localOut = out;
        if (localOut == null) {
            throw new IOException("Cannot send data: OutputStream is null");
        }

        localOut.write(tag);
        writeVarInt(localOut, protobufData.length);
        localOut.write(protobufData);
        localOut.flush();
    }

    private void sendSelectiveAck(String persistentId) {
        try {
            if (persistentId.isEmpty()) {
                return;
            }

            SelectiveAck sa = SelectiveAck.newBuilder().addId(persistentId).build();
            Extension ext = Extension.newBuilder().setId(12).setData(sa.toByteString()).build();
            IqStanza iq = IqStanza.newBuilder().setType(IqStanza.IqType.SET).setId("").setExtension(ext).build();

            send(TAG_IQ_STANZA, iq.toByteArray());
            logger.debug("Sent Selective ACK receipt to Google for message: {}", persistentId);
        } catch (IOException e) {
            logger.debug("Failed to send Selective ACK", e);
        }
    }

    private void listenLoop() {
        try {
            InputStream localIn = in;
            SSLSocket localSocket = socket;

            if (localIn == null || localSocket == null) {
                return;
            }

            // Consume the server's Version byte (41) before parsing Protobuf tags
            int versionByte = localIn.read();
            if (versionByte != 41) {
                logger.debug("FCM Server reported unexpected version byte: {}", versionByte);
            }

            while (isRunning && !localSocket.isClosed()) {
                int tag = localIn.read();
                if (tag == -1) {
                    logger.debug("FCM socket closed by remote host");
                    disconnect();
                    return;
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
                                logger.debug("FCM Login failed: {}", response.getError().getMessage());
                                disconnect();
                            } else {
                                logger.debug("FCM Login Successful!");
                                send(TAG_HEARTBEAT_PING, new byte[0]);
                                stateCallback.accept(true);
                            }
                        } catch (IOException e) {
                            logger.debug(
                                    "FCM LoginResponse Parse Error! Declared Size: {}, Actual Bytes Read: {}, Raw Hex: {}",
                                    size, data.length, bytesToHex(data), e);
                            disconnect();
                        }
                    }
                    case TAG_DATA_MESSAGE_STANZA -> {
                        logger.debug("FCM Listener: Dispatching push event to handler.");
                        DataMessageStanza msg = DataMessageStanza.parseFrom(data);

                        // Send the payload to the decryptor
                        handlePushMessage(msg);

                        if (msg.hasPersistentId()) {
                            sendSelectiveAck(msg.getPersistentId());
                        }
                    }
                    case TAG_HEARTBEAT_PING -> {
                        HeartbeatAck ack = HeartbeatAck.newBuilder().build();
                        send(TAG_HEARTBEAT_ACK, ack.toByteArray());
                    }
                    case TAG_HEARTBEAT_ACK -> logger.trace("FCM Heartbeat ACK received");
                    case TAG_IQ_STANZA -> {
                        logger.trace("FCM IQ Stanza received (Ignored)");
                        // Google occasionally sends IQ stanzas for state sync.
                        // The official python client safely ignores these.
                    }
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
        logger.trace("--- INCOMING PUSH MESSAGE DEBUG ---");
        if (msg.getAppDataList().isEmpty()) {
            logger.trace("Payload contains no AppData keys.");
        } else {
            for (AppData data : msg.getAppDataList()) {
                // Print the key and the raw value so we can see exactly what Ring sent
                logger.trace("Key: [{}] | Value: {}", data.getKey(), data.getValue());
            }
        }
        logger.trace("--- END PUSH MESSAGE DEBUG ---");
        String jsonEvent = null;
        String androidConfig = "";
        String imgJson = null;

        for (AppData data : msg.getAppDataList()) {
            if ("data".equals(data.getKey())) {
                jsonEvent = data.getValue();
            } else if ("android_config".equals(data.getKey())) {
                androidConfig = data.getValue();
            } else if ("img".equals(data.getKey())) {
                imgJson = data.getValue();
            }
        }

        if (jsonEvent != null) {
            logger.debug("Successfully received Ring Event payload.");
            eventCallback.onPushEvent(jsonEvent, androidConfig, imgJson);
        } else {
            logger.debug("Push message received but contained no 'data' key.");
        }
    }

    private void heartbeatLoop() {
        while (isRunning) {
            try {
                // 60 seconds is bulletproof against all aggressive home router NATs
                Thread.sleep(60000);
                HeartbeatPing ping = HeartbeatPing.newBuilder().build();
                send(TAG_HEARTBEAT_PING, ping.toByteArray());
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
        boolean wasRunning = isRunning;
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Ignored on disconnect
        }

        // Only trigger the state callback if we were actually running
        if (wasRunning) {
            stateCallback.accept(false);
        }
    }

    // --- VarInt Helpers (Google's Base 128 Varints) ---
    private int readVarInt(InputStream is) throws IOException {
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = is.read();
            if (b == -1) {
                throw new IOException("EOF reading VarInt");
            }
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
