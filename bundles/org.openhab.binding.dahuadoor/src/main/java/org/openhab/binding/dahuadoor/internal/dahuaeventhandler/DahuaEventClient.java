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
package org.openhab.binding.dahuadoor.internal.dahuaeventhandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link DahuaEventClient} client polls the Dahua device
 *
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaEventClient implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(DahuaEventClient.class);

    private final String host;
    private final String username;
    private final String password;
    private int id = 0; // Our Request / Response ID that must be in all requests and initiated by us
    private int sessionId = 0; // Session ID will be returned after successful login
    private String fakeIpAddr = "(null)"; // WebGUI: mask our real IP
    private String clientType = ""; // WebGUI: We do not show up in logs or online users
    private int keepAliveInterval = 60;
    private long lastKeepAlive = 0;

    private DHIPEventListener eventListener;

    private @Nullable Socket sock;
    private final Gson gson = new Gson();
    private volatile boolean execThread = true;
    private Consumer<String> errorInformer;
    private ByteBuffer residualBuffer = ByteBuffer.allocate(0); // Buffer for incomplete frames across reads

    private static final int HTTP_TIMEOUT_SECONDS = 10;
    private static final String SNAPSHOT_PATH = "/cgi-bin/snapshot.cgi";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public DahuaEventClient(String host, String username, String password, DHIPEventListener eventListener,
            Consumer<String> errorInformer) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.eventListener = eventListener;
        this.errorInformer = errorInformer;
        this.execThread = true;
        ThreadPoolManager.getPool("binding.dahuadoor").submit(this);
    }

    public void dispose() {
        this.execThread = false;
        // Close socket to unblock any pending read() operations
        final Socket localSock = sock;
        if (localSock != null) {
            try {
                localSock.close();
            } catch (IOException e) {
                logger.trace("Error closing socket during dispose: {}", e.getMessage());
            }
        }
    }

    /**
     * Requests a JPEG snapshot image from the device via HTTP.
     *
     * @return JPEG image bytes, or null if the request failed
     */
    public byte @Nullable [] requestImage() {
        try {
            SnapshotHttpResponse response = sendSnapshotRequest(null);
            if (response.statusCode == 200) {
                return response.body;
            }
            if (response.statusCode == 401) {
                String challenge = response.headers.get("www-authenticate");
                if (challenge != null) {
                    String digestHeader = createDigestAuthorizationHeader(challenge, SNAPSHOT_PATH);
                    if (digestHeader != null) {
                        SnapshotHttpResponse authResponse = sendSnapshotRequest(digestHeader);
                        if (authResponse.statusCode == 200) {
                            return authResponse.body;
                        }
                        logger.debug("Authenticated snapshot request failed with HTTP status {} from {}",
                                authResponse.statusCode, host);
                    }
                }
            }
            logger.debug("Snapshot request failed with HTTP status {} from {}", response.statusCode, host);
        } catch (Exception e) {
            logger.warn("Could not retrieve snapshot from {}", host, e);
        }
        return null;
    }

    /**
     * Opens the specified door via HTTP.
     *
     * @param doorNo door number (1 or 2)
     */
    public void openDoor(int doorNo) {
        try {
            String path = "/cgi-bin/accessControl.cgi?action=openDoor&UserID=101&Type=Remote&channel=" + doorNo;
            OpenDoorHttpResponse response = sendOpenDoorRequest(path, null);
            if (response.statusCode == 200) {
                logger.debug("Open Door Success");
            } else if (response.statusCode == 401) {
                String challenge = response.wwwAuthenticate;
                if (challenge != null) {
                    String digestHeader = createDigestAuthorizationHeader(challenge, path);
                    if (digestHeader != null) {
                        OpenDoorHttpResponse authResponse = sendOpenDoorRequest(path, digestHeader);
                        if (authResponse.statusCode == 200) {
                            logger.debug("Open Door Success (with authentication)");
                        } else {
                            logger.debug("Open door request failed with HTTP status {} for door {} on {}",
                                    authResponse.statusCode, doorNo, host);
                        }
                    }
                } else {
                    logger.debug("Open door request failed with HTTP status {} for door {} on {}", response.statusCode,
                            doorNo, host);
                }
            } else {
                logger.debug("Open door request failed with HTTP status {} for door {} on {}", response.statusCode,
                        doorNo, host);
            }
        } catch (Exception e) {
            logger.warn("Could not open door {} on {}", doorNo, host, e);
        }
    }

    private SnapshotHttpResponse sendSnapshotRequest(@Nullable String authorizationHeader) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 80), HTTP_TIMEOUT_SECONDS * 1000);
            socket.setSoTimeout(HTTP_TIMEOUT_SECONDS * 1000);
            StringBuilder request = new StringBuilder();
            request.append("GET ").append(SNAPSHOT_PATH).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(host).append("\r\n");
            request.append("Connection: close\r\n");
            request.append("Accept: */*\r\n");
            if (authorizationHeader != null) {
                request.append("Authorization: ").append(authorizationHeader).append("\r\n");
            }
            request.append("\r\n");
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();
            byte[] rawResponse = readHttpResponse(socket.getInputStream());
            return parseSnapshotResponse(rawResponse);
        }
    }

    private OpenDoorHttpResponse sendOpenDoorRequest(String path, @Nullable String authorizationHeader)
            throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 80), HTTP_TIMEOUT_SECONDS * 1000);
            socket.setSoTimeout(HTTP_TIMEOUT_SECONDS * 1000);
            StringBuilder request = new StringBuilder();
            request.append("GET ").append(path).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(host).append("\r\n");
            request.append("Connection: close\r\n");
            if (authorizationHeader != null) {
                request.append("Authorization: ").append(authorizationHeader).append("\r\n");
            }
            request.append("\r\n");
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();
            byte[] rawResponse = readHttpResponse(socket.getInputStream());
            return parseOpenDoorResponse(rawResponse);
        }
    }

    private byte[] readHttpResponse(InputStream inputStream) throws Exception {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        int state = 0;
        while (true) {
            int next = inputStream.read();
            if (next == -1) {
                break;
            }
            headerStream.write(next);
            if (state == 0 && next == '\r') {
                state = 1;
            } else if (state == 1 && next == '\n') {
                state = 2;
            } else if (state == 2 && next == '\r') {
                state = 3;
            } else if (state == 3 && next == '\n') {
                break;
            } else {
                state = 0;
            }
        }
        byte[] headerBytes = headerStream.toByteArray();
        int contentLength = extractContentLength(headerBytes);
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        responseStream.write(headerBytes);
        byte[] buffer = new byte[4096];
        if (contentLength >= 0) {
            int remaining = contentLength;
            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                responseStream.write(buffer, 0, read);
                remaining -= read;
            }
        } else {
            while (true) {
                try {
                    int read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    responseStream.write(buffer, 0, read);
                } catch (SocketTimeoutException e) {
                    logger.debug("Snapshot response read timed out without Content-Length; using partial body");
                    break;
                }
            }
        }
        return responseStream.toByteArray();
    }

    private int extractContentLength(byte[] headerBytes) {
        String headerText = new String(headerBytes, StandardCharsets.ISO_8859_1);
        for (String line : headerText.split("\\r\\n")) {
            if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
                try {
                    return Integer.parseInt(line.substring("Content-Length:".length()).trim());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private SnapshotHttpResponse parseSnapshotResponse(byte[] rawResponse) {
        int headerEnd = -1;
        for (int i = 0; i < rawResponse.length - 3; i++) {
            if (rawResponse[i] == '\r' && rawResponse[i + 1] == '\n' && rawResponse[i + 2] == '\r'
                    && rawResponse[i + 3] == '\n') {
                headerEnd = i + 4;
                break;
            }
        }
        if (headerEnd < 0) {
            return new SnapshotHttpResponse(0, Map.of(), new byte[0]);
        }
        String headerText = new String(rawResponse, 0, headerEnd, StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\\r\\n");
        int statusCode = 0;
        if (lines.length > 0) {
            String[] statusParts = lines[0].split(" ");
            if (statusParts.length > 1) {
                try {
                    statusCode = Integer.parseInt(statusParts[1]);
                } catch (NumberFormatException e) {
                    statusCode = 0;
                }
            }
        }
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                headers.put(lines[i].substring(0, colon).trim().toLowerCase(Locale.ROOT),
                        lines[i].substring(colon + 1).trim());
            }
        }
        byte[] body = Arrays.copyOfRange(rawResponse, headerEnd, rawResponse.length);
        return new SnapshotHttpResponse(statusCode, headers, body);
    }

    private OpenDoorHttpResponse parseOpenDoorResponse(byte[] rawResponse) {
        String headerText = new String(rawResponse, StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\\r\\n");
        int statusCode = 0;
        if (lines.length > 0) {
            String[] statusParts = lines[0].split(" ");
            if (statusParts.length > 1) {
                try {
                    statusCode = Integer.parseInt(statusParts[1]);
                } catch (NumberFormatException e) {
                    statusCode = 0;
                }
            }
        }
        String wwwAuthenticate = null;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].regionMatches(true, 0, "WWW-Authenticate:", 0, "WWW-Authenticate:".length())) {
                wwwAuthenticate = lines[i].substring("WWW-Authenticate:".length()).trim();
                break;
            }
        }
        return new OpenDoorHttpResponse(statusCode, wwwAuthenticate);
    }

    private @Nullable String createDigestAuthorizationHeader(String challenge, String requestPath) throws Exception {
        if (!challenge.toLowerCase(Locale.ROOT).startsWith("digest")) {
            return null;
        }
        Map<String, String> digestParams = parseDigestChallenge(challenge.substring(6).trim());
        String realm = digestParams.get("realm");
        String nonce = digestParams.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }
        String qop = digestParams.get("qop");
        if (qop != null && qop.contains(",")) {
            qop = qop.split(",")[0].trim();
        }
        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        String ha2 = md5Hex("GET:" + requestPath);
        StringBuilder auth = new StringBuilder("Digest ");
        auth.append("username=\"").append(escapeDigestValue(username)).append("\"");
        auth.append(", realm=\"").append(escapeDigestValue(realm)).append("\"");
        auth.append(", nonce=\"").append(escapeDigestValue(nonce)).append("\"");
        auth.append(", uri=\"").append(requestPath).append("\"");
        String response;
        if (qop != null && !qop.isBlank()) {
            String nonceCount = "00000001";
            String cnonce = randomHex(16);
            response = md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);
            auth.append(", qop=").append(qop);
            auth.append(", nc=").append(nonceCount);
            auth.append(", cnonce=\"").append(cnonce).append("\"");
        } else {
            response = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        }
        String opaque = digestParams.get("opaque");
        if (opaque != null) {
            auth.append(", opaque=\"").append(escapeDigestValue(opaque)).append("\"");
        }
        String algorithm = digestParams.get("algorithm");
        if (algorithm != null) {
            auth.append(", algorithm=").append(algorithm);
        }
        auth.append(", response=\"").append(response).append("\"");
        return auth.toString();
    }

    private Map<String, String> parseDigestChallenge(String challenge) {
        Map<String, String> result = new HashMap<>();
        int index = 0;
        while (index < challenge.length()) {
            while (index < challenge.length()
                    && (challenge.charAt(index) == ',' || Character.isWhitespace(challenge.charAt(index)))) {
                index++;
            }
            int equals = challenge.indexOf('=', index);
            if (equals < 0) {
                break;
            }
            String key = challenge.substring(index, equals).trim().toLowerCase(Locale.ROOT);
            index = equals + 1;
            String value;
            if (index < challenge.length() && challenge.charAt(index) == '"') {
                index++;
                int end = challenge.indexOf('"', index);
                if (end < 0) {
                    break;
                }
                value = challenge.substring(index, end);
                index = end + 1;
            } else {
                int comma = challenge.indexOf(',', index);
                if (comma < 0) {
                    value = challenge.substring(index).trim();
                    index = challenge.length();
                } else {
                    value = challenge.substring(index, comma).trim();
                    index = comma + 1;
                }
            }
            result.put(key, value);
        }
        return result;
    }

    private String md5Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.ISO_8859_1));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String randomHex(int length) {
        byte[] bytes = new byte[length / 2];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(length);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String escapeDigestValue(String value) {
        return value.replace("\\\\", "\\\\\\\\").replace("\"", "\\\"");
    }

    private record SnapshotHttpResponse(int statusCode, Map<String, String> headers, byte[] body) {
    }

    private record OpenDoorHttpResponse(int statusCode, @Nullable String wwwAuthenticate) {
    }

    public String generateMd5Hash(String dahuaRandom, String dahuaRealm, String username, String password)
            throws Exception {
        final String passwordDbHash = md5(String.join(":", username, dahuaRealm, password)).toUpperCase(Locale.ROOT);
        final String pass = String.join(":", username, dahuaRandom, passwordDbHash);
        return md5(pass).toUpperCase(Locale.ROOT);
    }

    /**
     * MD5 hash function for Dahua DHIP protocol authentication.
     * Note: MD5 is cryptographically weak, but is required by the Dahua device API
     * for digest authentication. This is a protocol limitation, not a design choice.
     */
    private String md5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void keepAlive(int delay) {
        final Socket localSock = sock;
        if (localSock == null) {
            return;
        }

        logger.trace("Started keepAlive thread");
        while (execThread) {
            Map<String, Object> queryArgs = new HashMap<>();
            queryArgs.put("method", "global.keepAlive");
            queryArgs.put("magic", "0x1234");
            queryArgs.put("params", Map.of("timeout", delay, "active", true));
            queryArgs.put("id", this.id);
            queryArgs.put("session", this.sessionId);

            try {
                send(new Gson().toJson(queryArgs));
            } catch (IOException e) {
                logger.trace("Error sending keepAlive", e);
                return;
            }
            lastKeepAlive = System.currentTimeMillis();
            boolean keepAliveReceived = false;

            while (lastKeepAlive + delay * 1000 > System.currentTimeMillis()) {
                ArrayList<String> data;
                try {
                    data = receive();
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if (jsonPacket != null) {
                            if (jsonPacket.has("result")) {
                                logger.trace("keepAlive back");
                                keepAliveReceived = true;
                            } else if ("client.notifyEventStream".equals(jsonPacket.get("method").getAsString())) {
                                eventListener.onEvent(jsonPacket);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Read timeout: no data yet, keep waiting until the keep-alive deadline.
                    logger.trace("keepAlive receive timeout, continuing to wait");
                } catch (IOException e) {
                    logger.trace("Error receiving keepAlive response", e);
                    return;
                }
            }

            if (!keepAliveReceived) {
                logger.trace("keepAlive failed");
                return;
            }
        }
    }

    public void send(String packet) throws IOException {
        byte[] payloadBytes = packet.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(32 + payloadBytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0x20000000);
        buffer.putInt(0x44484950);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(sessionId);
        buffer.putInt(id);
        buffer.putInt(payloadBytes.length);
        buffer.putInt(0);
        buffer.putInt(payloadBytes.length);
        buffer.putInt(0);

        if (buffer.position() != 32) {
            logger.trace("Binary header != 32");
            return;
        }

        id += 1;

        final Socket localSock = sock;
        if (localSock == null) {
            throw new IOException("Socket is not connected");
        }

        buffer.put(payloadBytes);
        localSock.getOutputStream().write(buffer.array());
    }

    public ArrayList<String> receive() throws IOException {
        ArrayList<String> p2pReturnData = new ArrayList<String>();
        byte[] buffer = new byte[8192];
        byte[] header = new byte[32];
        ByteBuffer bbuffer;
        int lenRecved = 1;
        int timeout = 5;

        final Socket localSock = sock;
        if (localSock == null) {
            logger.debug("Socket is not connected");
            throw new IOException("Socket is not connected");
        }

        try {
            localSock.setSoTimeout(timeout * 1000); // Set timeout in milliseconds
            InputStream input = localSock.getInputStream();
            int bytesRead = input.read(buffer);
            if (bytesRead < 0) {
                // End of stream - connection closed
                throw new IOException("Connection closed by remote host");
            }

            // Combine residual buffer with new data
            if (residualBuffer.hasRemaining()) {
                ByteBuffer combined = ByteBuffer.allocate(residualBuffer.remaining() + bytesRead);
                combined.put(residualBuffer);
                combined.put(buffer, 0, bytesRead);
                combined.flip();
                bbuffer = combined;
                residualBuffer = ByteBuffer.allocate(0); // Clear residual
            } else {
                bbuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            logger.trace("IOException in receive(): {}", e.getMessage());
            throw e;
        }

        while (bbuffer.hasRemaining()) {
            // Ensure we have enough bytes for at least the magic value
            if (bbuffer.remaining() < Long.BYTES) {
                // Save remaining bytes for next read
                residualBuffer = ByteBuffer.allocate(bbuffer.remaining());
                residualBuffer.put(bbuffer);
                residualBuffer.flip();
                break;
            }

            bbuffer.order(ByteOrder.BIG_ENDIAN);
            if (bbuffer.getLong(bbuffer.position()) == 0x2000000044484950L) {
                // Ensure we have a full header before reading it
                if (bbuffer.remaining() < 32) {
                    // Save remaining bytes for next read
                    residualBuffer = ByteBuffer.allocate(bbuffer.remaining());
                    residualBuffer.put(bbuffer);
                    residualBuffer.flip();
                    break;
                }
                bbuffer.order(ByteOrder.LITTLE_ENDIAN);
                // DHIP Protocol Header Structure (32 bytes):
                // Offset 0-7: Magic value (0x2000000044484950)
                // Offset 8-11: Session ID
                // Offset 12-15: Sequence number
                // Offset 16-19: Length of received data (lenRecved)
                // Offset 20-23: Reserved
                // Offset 24-27: Expected length for multi-part messages
                // Offset 28-31: Reserved
                int frameStart = bbuffer.position();
                lenRecved = bbuffer.getInt(frameStart + 16);
                bbuffer.get(header, 0, 32); // advances position by 32 to payload start

            } else {
                if (lenRecved > 0) {
                    // Ensure we have the full payload before reading it
                    if (bbuffer.remaining() < lenRecved) {
                        // Save from header start to preserve full frame for the next read.
                        ByteBuffer residualSlice = bbuffer.duplicate();
                        int headerStartPosition = Math.max(0, bbuffer.position() - 32);
                        residualSlice.position(headerStartPosition);
                        residualBuffer = ByteBuffer.allocate(residualSlice.remaining());
                        residualBuffer.put(residualSlice);
                        residualBuffer.flip();
                        break;
                    }
                    String p2pData = new String(bbuffer.array(), bbuffer.arrayOffset() + bbuffer.position(), lenRecved,
                            StandardCharsets.UTF_8);
                    bbuffer.position(bbuffer.position() + lenRecved);
                    p2pReturnData.add(p2pData);
                    lenRecved = 0;
                } else {
                    break;
                }
            }
        }
        return p2pReturnData;
    }

    @SuppressWarnings("unchecked")
    public boolean login() {
        logger.trace("Start login");

        Map<String, Object> queryArgs = new HashMap<>();
        queryArgs.put("id", this.id);
        queryArgs.put("magic", "0x1234");
        queryArgs.put("method", "global.login");
        queryArgs.put("params", Map.of("clientType", this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct",
                "password", "", "userName", this.username));
        queryArgs.put("session", 0);

        try {
            send(new Gson().toJson(queryArgs));
            ArrayList<String> data = receive();
            if (data.isEmpty()) {
                logger.trace("global.login [random]");
                return false;
            }
            Map<String, Object> jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData == null || !jsonData.containsKey("session") || !jsonData.containsKey("params")) {
                logger.trace("Invalid JSON response from login");
                return false;
            }
            Object sessionObj = jsonData.get("session");
            if (sessionObj instanceof Number) {
                this.sessionId = ((Number) sessionObj).intValue();
            } else {
                logger.trace("Invalid session type in response");
                return false;
            }
            Map<String, Object> params = (Map<String, Object>) jsonData.get("params");
            if (params == null) {
                logger.trace("Missing params in response");
                return false;
            }
            String random = (String) params.get("random");
            String realm = (String) params.get("realm");

            if (random == null || realm == null) {
                logger.trace("Login failed: missing random or realm");
                return false;
            }

            String hashedCredentials = generateMd5Hash(random, realm, this.username, this.password);

            queryArgs = new HashMap<>();
            queryArgs.put("id", this.id);
            queryArgs.put("magic", "0x1234");
            queryArgs.put("method", "global.login");
            queryArgs.put("session", this.sessionId);
            queryArgs.put("params", Map.of("userName", this.username, "password", hashedCredentials, "clientType",
                    this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct", "authorityType", "Default"));

            send(new Gson().toJson(queryArgs));
            data = receive();
            if (data.isEmpty()) {
                return false;
            }
            jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData != null) {
                if (Boolean.TRUE.equals(jsonData.get("result"))) {
                    logger.trace("Login success");
                    Object paramsObj = jsonData.get("params");
                    if (paramsObj instanceof Map) {
                        Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
                        Object intervalObj = paramsMap.get("keepAliveInterval");
                        if (intervalObj instanceof Number) {
                            this.keepAliveInterval = ((Number) intervalObj).intValue();
                        }
                    }
                    return true;
                }
                Object errorObj = jsonData.get("error");
                if (errorObj instanceof Map) {
                    Map<?, ?> errorMap = (Map<?, ?>) errorObj;
                    Object code = errorMap.get("code");
                    Object message = errorMap.get("message");
                    logger.trace("Login failed: {} {}", code, message);
                } else {
                    logger.trace("Login failed: {}", jsonData);
                }
            } else {
                logger.trace("Login failed: empty or invalid JSON response");
            }
        } catch (Exception e) {
            logger.trace("Login error: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        boolean error = false;
        int loginTries = 0;
        while (execThread) {
            if (error) {
                try {
                    for (int i = 0; i < 12 && execThread; i++) {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    logger.debug("Thread interrupted during error wait", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            error = true;
            try {
                Socket localSock = new Socket();
                localSock.connect(new InetSocketAddress(host, 5000), 5000); // 5s connect timeout
                sock = localSock;
                localSock.setSoTimeout(5000); // Set timeout to 5 seconds
                error = false;

                if (!login()) {
                    loginTries++;
                    if (loginTries > 4) {
                        errorInformer.accept("Login failed. Verify the host configuration and credentials.");
                        execThread = false;
                    }
                    try {
                        Socket sockToClose = sock;
                        if (sockToClose != null) {
                            sockToClose.close();
                        }
                    } catch (IOException e) {
                        logger.trace("Error closing socket after login failure", e);
                    }
                    continue;
                }

                Map<String, Object> queryArgs = new HashMap<>();
                queryArgs.put("id", this.id);
                queryArgs.put("magic", "0x1234");
                queryArgs.put("method", "eventManager.attach");
                queryArgs.put("params", Map.of("codes", new String[] { "All" }));
                queryArgs.put("session", this.sessionId);

                send(new Gson().toJson(queryArgs));
                ArrayList<String> data = receive();
                JsonObject jsonData = data.isEmpty() ? null : gson.fromJson(data.get(0), JsonObject.class);
                if (jsonData == null || !jsonData.has("result")) {
                    logger.trace("Failure eventManager.attach");
                    try {
                        Socket sockToClose = sock;
                        if (sockToClose != null) {
                            sockToClose.close();
                        }
                    } catch (IOException e) {
                        logger.trace("Error closing socket after attach failure", e);
                    }
                    continue;
                } else {
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if (jsonPacket != null && jsonPacket.has("method")) {
                            String method = jsonPacket.get("method").getAsString();
                            if ("client.notifyEventStream".equals(method)) {
                                eventListener.onEvent(jsonPacket);
                            }
                        }
                    }
                }
                keepAlive(this.keepAliveInterval);
                logger.trace("Failure no keep alive received");
            } catch (Exception e) {
                logger.trace("Socket open failed", e);
            }
        }
        try {
            Socket sockToClose = sock;
            if (sockToClose != null) {
                sockToClose.close();
            }
        } catch (Exception e) {
            logger.trace("Error while closing socket", e);
        }
    }
}
