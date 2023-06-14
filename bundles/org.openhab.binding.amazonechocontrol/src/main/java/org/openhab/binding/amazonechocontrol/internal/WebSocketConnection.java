/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPushCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WebSocketConnection} encapsulate the Web Socket connection to the amazon server.
 * The code is based on
 * https://github.com/Apollon77/alexa-remote/blob/master/alexa-wsmqtt.js
 *
 * @author Michael Geramb - Initial contribution
 * @author Ingo Fischer - (https://github.com/Apollon77/alexa-remote/blob/master/alexa-wsmqtt.js)
 */
@NonNullByDefault
public class WebSocketConnection {
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);
    private final Gson gson = new Gson();
    private final WebSocketClient webSocketClient;
    private final IWebSocketCommandHandler webSocketCommandHandler;
    private final AmazonEchoControlWebSocket amazonEchoControlWebSocket;

    private @Nullable Session session;
    private @Nullable Timer pingTimer;
    private @Nullable Timer pongTimeoutTimer;
    private @Nullable Future<?> sessionFuture;

    private boolean closed;

    public WebSocketConnection(String amazonSite, List<HttpCookie> sessionCookies,
            IWebSocketCommandHandler webSocketCommandHandler) throws IOException {
        this.webSocketCommandHandler = webSocketCommandHandler;
        amazonEchoControlWebSocket = new AmazonEchoControlWebSocket();
        HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
        webSocketClient = new WebSocketClient(httpClient);
        try {
            String host;
            if ("amazon.com".equalsIgnoreCase(amazonSite)) {
                host = "dp-gw-na-js." + amazonSite;
            } else {
                host = "dp-gw-na." + amazonSite;
            }

            String deviceSerial = "";
            List<HttpCookie> cookiesForWs = new ArrayList<>();
            for (HttpCookie cookie : sessionCookies) {
                if (cookie.getName().equals("ubid-acbde")) {
                    deviceSerial = cookie.getValue();
                }
                // Clone the cookie without the security attribute, because the web socket implementation ignore secure
                // cookies
                String value = cookie.getValue().replaceAll("^\"|\"$", "");
                HttpCookie cookieForWs = new HttpCookie(cookie.getName(), value);
                cookiesForWs.add(cookieForWs);
            }
            deviceSerial += "-" + new Date().getTime();
            URI uri;

            uri = new URI("wss://" + host + "/?x-amz-device-type=ALEGCNGL9K0HM&x-amz-device-serial=" + deviceSerial);

            try {
                webSocketClient.start();
            } catch (Exception e) {
                logger.warn("Web socket start failed", e);
                throw new IOException("Web socket start failed");
            }

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Host", host);
            request.setHeader("Origin", "alexa." + amazonSite);
            request.setCookies(cookiesForWs);

            initPongTimeoutTimer();

            sessionFuture = webSocketClient.connect(amazonEchoControlWebSocket, uri, request);
        } catch (URISyntaxException e) {
            logger.debug("Initialize web socket failed", e);
        }
    }

    private void setSession(Session session) {
        this.session = session;
        logger.debug("Web Socket session started");
        Timer pingTimer = new Timer();
        this.pingTimer = pingTimer;
        pingTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                amazonEchoControlWebSocket.sendPing();
            }
        }, 180000, 180000);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        Timer pingTimer = this.pingTimer;
        if (pingTimer != null) {
            pingTimer.cancel();
        }
        clearPongTimeoutTimer();
        Session session = this.session;
        this.session = null;
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.debug("Closing session failed", e);
            }
        }
        logger.trace("Connect future = {}", sessionFuture);
        final Future<?> sessionFuture = this.sessionFuture;
        if (sessionFuture != null && !sessionFuture.isDone()) {
            sessionFuture.cancel(true);
        }
        try {
            webSocketClient.stop();
        } catch (InterruptedException e) {
            // Just ignore
        } catch (Exception e) {
            logger.debug("Stopping websocket failed", e);
        }
        webSocketClient.destroy();
    }

    void clearPongTimeoutTimer() {
        Timer pongTimeoutTimer = this.pongTimeoutTimer;
        this.pongTimeoutTimer = null;
        if (pongTimeoutTimer != null) {
            logger.trace("Cancelling pong timeout");
            pongTimeoutTimer.cancel();
        }
    }

    void initPongTimeoutTimer() {
        clearPongTimeoutTimer();
        Timer pongTimeoutTimer = new Timer();
        this.pongTimeoutTimer = pongTimeoutTimer;
        logger.trace("Scheduling pong timeout");
        pongTimeoutTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                logger.trace("Pong timeout reached. Closing connection.");
                close();
            }
        }, 60000);
    }

    @WebSocket(maxTextMessageSize = 64 * 1024, maxBinaryMessageSize = 64 * 1024)
    public class AmazonEchoControlWebSocket {
        int msgCounter = -1;
        int messageId;

        AmazonEchoControlWebSocket() {
            this.messageId = ThreadLocalRandom.current().nextInt(0, Short.MAX_VALUE);
        }

        void sendMessage(String message) {
            sendMessage(message.getBytes(StandardCharsets.UTF_8));
        }

        void sendMessageHex(String message) {
            sendMessage(hexStringToByteArray(message));
        }

        void sendMessage(byte[] buffer) {
            try {
                logger.debug("Send message with length {}", buffer.length);
                Session session = WebSocketConnection.this.session;
                if (session != null) {
                    session.getRemote().sendBytes(ByteBuffer.wrap(buffer));
                }
            } catch (IOException e) {
                logger.debug("Send message failed", e);
                WebSocketConnection.this.close();
            }
        }

        byte[] hexStringToByteArray(String str) {
            byte[] bytes = new byte[str.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                String strValue = str.substring(2 * i, 2 * i + 2);
                bytes[i] = (byte) Integer.parseInt(strValue, 16);
            }
            return bytes;
        }

        long readHex(byte[] data, int index, int length) {
            String str = readString(data, index, length);
            if (str.startsWith("0x")) {
                str = str.substring(2);
            }
            return Long.parseLong(str, 16);
        }

        String readString(byte[] data, int index, int length) {
            return new String(data, index, length, StandardCharsets.UTF_8);
        }

        class Message {
            String service = "";
            Content content = new Content();
            String contentTune = "";
            String messageType = "";
            long channel;
            long checksum;
            long messageId;
            String moreFlag = "";
            long seq;
        }

        class Content {
            String messageType = "";
            String protocolVersion = "";
            String connectionUUID = "";
            long established;
            long timestampINI;
            long timestampACK;
            String subMessageType = "";
            long channel;
            String destinationIdentityUrn = "";
            String deviceIdentityUrn = "";
            @Nullable
            String payload;
            byte[] payloadData = new byte[0];
            @Nullable
            JsonPushCommand pushCommand;
        }

        Message parseIncomingMessage(byte[] data) {
            int idx = 0;
            Message message = new Message();
            message.service = readString(data, data.length - 4, 4);

            if (message.service.equals("TUNE")) {
                message.checksum = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                int contentLength = (int) readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.contentTune = readString(data, idx, contentLength - 4 - idx);
            } else if (message.service.equals("FABE")) {
                message.messageType = readString(data, idx, 3);
                idx += 4;
                message.channel = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.messageId = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.moreFlag = readString(data, idx, 1);
                idx += 2; // 1 + delimiter;
                message.seq = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.checksum = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;

                // currently not used: long contentLength = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;

                message.content.messageType = readString(data, idx, 3);
                idx += 4;

                if (message.channel == 0x361) { // GW_HANDSHAKE_CHANNEL
                    if (message.content.messageType.equals("ACK")) {
                        int length = (int) readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.protocolVersion = readString(data, idx, length);
                        idx += length + 1;
                        length = (int) readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.connectionUUID = readString(data, idx, length);
                        idx += length + 1;
                        message.content.established = readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.timestampINI = readHex(data, idx, 18);
                        idx += 19; // 18 + delimiter;
                        message.content.timestampACK = readHex(data, idx, 18);
                        idx += 19; // 18 + delimiter;
                    }
                } else if (message.channel == 0x362) { // GW_CHANNEL
                    if (message.content.messageType.equals("GWM")) {
                        message.content.subMessageType = readString(data, idx, 3);
                        idx += 4;
                        message.content.channel = readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;

                        if (message.content.channel == 0xb479) { // DEE_WEBSITE_MESSAGING
                            int length = (int) readHex(data, idx, 10);
                            idx += 11; // 10 + delimiter;
                            message.content.destinationIdentityUrn = readString(data, idx, length);
                            idx += length + 1;

                            length = (int) readHex(data, idx, 10);
                            idx += 11; // 10 + delimiter;
                            String idData = readString(data, idx, length);
                            idx += length + 1;

                            String[] idDataElements = idData.split(" ", 2);
                            message.content.deviceIdentityUrn = idDataElements[0];
                            String payload = null;
                            if (idDataElements.length == 2) {
                                payload = idDataElements[1];
                            }
                            if (payload == null) {
                                payload = readString(data, idx, data.length - 4 - idx);
                            }
                            if (!payload.isEmpty()) {
                                try {
                                    message.content.pushCommand = gson.fromJson(payload, JsonPushCommand.class);
                                } catch (JsonSyntaxException e) {
                                    logger.info("Parsing json failed, illegal JSON: {}", payload, e);
                                }
                            }
                            message.content.payload = payload;
                        }
                    }
                } else if (message.channel == 0x65) { // CHANNEL_FOR_HEARTBEAT
                    idx -= 1; // no delimiter!
                    message.content.payloadData = Arrays.copyOfRange(data, idx, data.length - 4);
                }
            }
            return message;
        }

        @OnWebSocketConnect
        public void onWebSocketConnect(@Nullable Session session) {
            if (session != null) {
                this.msgCounter = -1;
                setSession(session);
                sendMessage("0x99d4f71a 0x0000001d A:HTUNE");
            } else {
                logger.debug("Web Socket connect without session");
            }
        }

        @OnWebSocketMessage
        public void onWebSocketBinary(byte @Nullable [] data, int offset, int len) {
            if (data == null) {
                return;
            }
            this.msgCounter++;
            if (this.msgCounter == 0) {
                sendMessage(
                        "0xa6f6a951 0x0000009c {\"protocolName\":\"A:H\",\"parameters\":{\"AlphaProtocolHandler.receiveWindowSize\":\"16\",\"AlphaProtocolHandler.maxFragmentSize\":\"16000\"}}TUNE");
                sendMessage(encodeGWHandshake());
            } else if (this.msgCounter == 1) {
                sendMessage(encodeGWRegister());
                sendPing();
            } else {
                byte[] buffer = data;
                if (offset > 0 || len != buffer.length) {
                    buffer = Arrays.copyOfRange(data, offset, offset + len);
                }
                try {
                    Message message = parseIncomingMessage(buffer);
                    if (message.service.equals("FABE") && message.content.messageType.equals("PON")
                            && message.content.payloadData.length > 0) {
                        logger.debug("Pong received");
                        WebSocketConnection.this.clearPongTimeoutTimer();
                        return;
                    } else {
                        JsonPushCommand pushCommand = message.content.pushCommand;
                        logger.debug("Message received: {}", message.content.payload);
                        if (pushCommand != null) {
                            webSocketCommandHandler.webSocketCommandReceived(pushCommand);
                        }
                        return;
                    }
                } catch (Exception e) {
                    logger.debug("Handling of push notification failed", e);
                }
            }
        }

        @OnWebSocketMessage
        public void onWebSocketText(@Nullable String message) {
            logger.trace("Received text message: '{}'", message);
        }

        @OnWebSocketClose
        public void onWebSocketClose(int code, @Nullable String reason) {
            logger.info("Web Socket close {}. Reason: {}", code, reason);
            WebSocketConnection.this.close();
        }

        @OnWebSocketError
        public void onWebSocketError(@Nullable Throwable error) {
            logger.info("Web Socket error", error);
            if (!closed) {
                WebSocketConnection.this.close();
            }
        }

        public void sendPing() {
            logger.debug("Send Ping");
            WebSocketConnection.this.initPongTimeoutTimer();
            sendMessage(encodePing());
        }

        String encodeNumber(long val) {
            return encodeNumber(val, 8);
        }

        String encodeNumber(long val, int len) {
            String str = Long.toHexString(val);
            if (str.length() > len) {
                str = str.substring(str.length() - len);
            }
            while (str.length() < len) {
                str = '0' + str;
            }
            return "0x" + str;
        }

        long computeBits(long input, long len) {
            long lenCounter = len;
            long value;
            for (value = toUnsignedInt(input); 0 != lenCounter && 0 != value;) {
                value = (long) Math.floor(value / 2);
                lenCounter--;
            }
            return value;
        }

        long toUnsignedInt(long value) {
            long result = value;
            if (0 > value) {
                result = 4294967295L + value + 1;
            }
            return result;
        }

        int computeChecksum(byte[] data, int exclusionStart, int exclusionEnd) {
            if (exclusionEnd < exclusionStart) {
                return 0;
            }
            long overflow;
            long sum;
            int index;
            for (overflow = 0, sum = 0, index = 0; index < data.length; index++) {
                if (index != exclusionStart) {
                    sum += toUnsignedInt((data[index] & 0xFF) << ((index & 3 ^ 3) << 3));
                    overflow += computeBits(sum, 32);
                    sum = toUnsignedInt((int) sum & (int) 4294967295L);
                } else {
                    index = exclusionEnd - 1;
                }
            }
            while (overflow != 0) {
                sum += overflow;
                overflow = computeBits(sum, 32);
                sum = (int) sum & (int) 4294967295L;
            }
            long value = toUnsignedInt(sum);
            return (int) value;
        }

        byte[] encodeGWHandshake() {
            // pubrelBuf = new Buffer('MSG 0x00000361 0x0e414e45 f 0x00000001 0xd7c62f29 0x0000009b INI 0x00000003 1.0
            // 0x00000024 ff1c4525-c036-4942-bf6c-a098755ac82f 0x00000164d106ce6b END FABE');
            this.messageId++;
            String msg = "MSG 0x00000361 "; // Message-type and Channel = GW_HANDSHAKE_CHANNEL;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x0000009b "; // length content
            msg += "INI 0x00000003 1.0 0x00000024 "; // content part 1
            msg += UUID.randomUUID().toString();
            msg += ' ';
            msg += this.encodeNumber(new Date().getTime(), 16);
            msg += " END FABE";
            // msg = "MSG 0x00000361 0x0e414e45 f 0x00000001 0xd7c62f29 0x0000009b INI 0x00000003 1.0 0x00000024
            // ff1c4525-c036-4942-bf6c-a098755ac82f 0x00000164d106ce6b END FABE";
            byte[] completeBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);
            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);

            return completeBuffer;
        }

        byte[] encodeGWRegister() {
            // pubrelBuf = new Buffer('MSG 0x00000362 0x0e414e46 f 0x00000001 0xf904b9f5 0x00000109 GWM MSG 0x0000b479
            // 0x0000003b urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041
            // urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService
            // {"command":"REGISTER_CONNECTION"}FABE');
            this.messageId++;
            String msg = "MSG 0x00000362 "; // Message-type and Channel = GW_CHANNEL;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x00000109 "; // length content
            msg += "GWM MSG 0x0000b479 0x0000003b urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041 urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService {\"command\":\"REGISTER_CONNECTION\"}FABE";

            byte[] completeBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);

            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);

            String test = readString(completeBuffer, 0, completeBuffer.length);
            test.toString();
            return completeBuffer;
        }

        void encode(byte[] data, long b, int offset, int len) {
            for (int index = 0; index < len; index++) {
                data[index + offset] = (byte) (b >> 8 * (len - 1 - index) & 255);
            }
        }

        byte[] encodePing() {
            // MSG 0x00000065 0x0e414e47 f 0x00000001 0xbc2fbb5f 0x00000062
            this.messageId++;
            String msg = "MSG 0x00000065 "; // Message-type and Channel = CHANNEL_FOR_HEARTBEAT;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x00000062 "; // length content

            byte[] completeBuffer = new byte[0x62];
            byte[] startBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            System.arraycopy(startBuffer, 0, completeBuffer, 0, startBuffer.length);

            byte[] header = "PIN".getBytes(StandardCharsets.US_ASCII);
            byte[] payload = "Regular".getBytes(StandardCharsets.US_ASCII); // g = h.length
            byte[] bufferPing = new byte[header.length + 4 + 8 + 4 + 2 * payload.length];
            int idx = 0;
            System.arraycopy(header, 0, bufferPing, 0, header.length);
            idx += header.length;
            encode(bufferPing, 0, idx, 4);
            idx += 4;
            encode(bufferPing, new Date().getTime(), idx, 8);
            idx += 8;
            encode(bufferPing, payload.length, idx, 4);
            idx += 4;

            for (int q = 0; q < payload.length; q++) {
                bufferPing[idx + q * 2] = (byte) 0;
                bufferPing[idx + q * 2 + 1] = payload[q];
            }
            System.arraycopy(bufferPing, 0, completeBuffer, startBuffer.length, bufferPing.length);

            byte[] buf2End = "FABE".getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(buf2End, 0, completeBuffer, startBuffer.length + bufferPing.length, buf2End.length);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);
            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);
            return completeBuffer;
        }
    }
}
