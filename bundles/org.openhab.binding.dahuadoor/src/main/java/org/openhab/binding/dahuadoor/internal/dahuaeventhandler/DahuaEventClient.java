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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private String host;
    private String username;
    private String password;
    private int id = 0; // Our Request / Response ID that must be in all requests and initiated by us
    private int sessionId = 0; // Session ID will be returned after successful login
    private String fakeIpAddr = "(null)"; // WebGUI: mask our real IP
    private String clientType = ""; // WebGUI: We do not show up in logs or online users
    private int keepAliveInterval = 60;
    private long lastKeepAlive = 0;

    private DHIPEventListener eventListener;

    private @Nullable Socket sock;
    private final Gson gson = new Gson();
    private boolean execThread = true;
    private Consumer<String> errorInformer;
    private ByteBuffer residualBuffer = ByteBuffer.allocate(0); // Buffer for incomplete frames across reads

    public DahuaEventClient(String host, String username, String password, DHIPEventListener eventListener,
            ScheduledExecutorService scheduler, Consumer<String> errorInformer) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.eventListener = eventListener;
        this.errorInformer = errorInformer;
        this.execThread = true;
        scheduler.submit(this);
    }

    public void dispose() {
        this.execThread = false;
    }

    public String genMd5Hash(String Dahua_random, String Dahua_realm, String username, String password)
            throws Exception {
        String PWDDB_HASH = md5(username + ":" + Dahua_realm + ":" + password).toUpperCase();
        String PASS = username + ":" + Dahua_random + ":" + PWDDB_HASH;
        return md5(PASS).toUpperCase();
    }

    /**
     * MD5 hash function for Dahua DHIP protocol authentication.
     * Note: MD5 is cryptographically weak, but is required by the Dahua device API
     * for digest authentication. This is a protocol limitation, not a design choice.
     */
    private String md5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void keepAlive(int delay) {
        final Logger localLogger = logger;
        if (localLogger == null) {
            return;
        }
        final Socket localSock = sock;
        if (localSock == null) {
            return;
        }

        localLogger.trace("Started keepAlive thread");
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
                logger.trace("Error sending keepAlive: {}", e.getMessage());
            }
            lastKeepAlive = System.currentTimeMillis();
            boolean keepAliveReceived = false;

            while (lastKeepAlive + delay * 1000 > System.currentTimeMillis()) {
                ArrayList<String> data;
                try {
                    data = receive();
                    if (data != null) {
                        for (String packet : data) {
                            JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                            if (jsonPacket.has("result")) {
                                logger.trace("keepAlive back");
                                keepAliveReceived = true;
                            } else if ("client.notifyEventStream".equals(jsonPacket.get("method").getAsString())) {
                                eventListener.eventHandler(jsonPacket);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.trace("Error receiving keepAlive response: {}", e.getMessage());
                }
            }

            if (!keepAliveReceived) {
                logger.trace("keepAlive failed");
                return;
            }
        }
    }

    public void send(String packet) throws IOException {
        if (packet == null) {
            packet = "";
        }

        ByteBuffer buffer = ByteBuffer.allocate(32 + packet.length());
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0x20000000);
        buffer.putInt(0x44484950);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(sessionId);
        buffer.putInt(id);
        buffer.putInt(packet.length());
        buffer.putInt(0);
        buffer.putInt(packet.length());
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

        buffer.put(packet.getBytes());
        localSock.getOutputStream().write(buffer.array());
    }

    public ArrayList<String> receive() throws IOException {

        ArrayList<String> p2pReturnData = new ArrayList<String>();
        byte[] buffer = new byte[8192];
        byte[] header = new byte[32];
        ByteBuffer bbuffer;
        int lenRecved = 1;
        int lenExpect = 1;
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
                // Length fields are 32-bit in the header
                lenRecved = bbuffer.getInt(16);
                lenExpect = bbuffer.getInt(24);
                bbuffer.get(header, 0, 32);
                bbuffer = bbuffer.position(32).slice(); // cut bbuffer by 32 Bytes

            } else {
                if (lenRecved > 0) {
                    // Ensure we have the full payload before reading it
                    if (bbuffer.remaining() < lenRecved) {
                        // Save remaining bytes for next read - include current position
                        bbuffer.position(bbuffer.position() - 32); // Go back to include header
                        residualBuffer = ByteBuffer.allocate(bbuffer.remaining());
                        residualBuffer.put(bbuffer);
                        residualBuffer.flip();
                        break;
                    }
                    String p2pData = new String(bbuffer.array(), bbuffer.arrayOffset() + bbuffer.position(), lenRecved);
                    bbuffer = bbuffer.position(bbuffer.position() + lenRecved).slice(); // cut bbuffer
                    p2pReturnData.add(p2pData);
                    lenRecved = 0;
                } else {
                    break;
                }
            }
        }
        return p2pReturnData;
    }

    public boolean login() {
        logger.trace("Start login");

        Map<String, Object> queryArgs = new HashMap<>();
        queryArgs.put("id", 10000);
        queryArgs.put("magic", "0x1234");
        queryArgs.put("method", "global.login");
        queryArgs.put("params", Map.of("clientType", this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct",
                "password", "", "userName", this.username));
        queryArgs.put("session", 0);

        try {
            send(new Gson().toJson(queryArgs));
            ArrayList<String> data = receive();
            if (data == null) {
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

            String RANDOM_HASH = genMd5Hash(random, realm, this.username, this.password);

            queryArgs = new HashMap<>();
            queryArgs.put("id", 10000);
            queryArgs.put("magic", "0x1234");
            queryArgs.put("method", "global.login");
            queryArgs.put("session", this.sessionId);
            queryArgs.put("params", Map.of("userName", this.username, "password", RANDOM_HASH, "clientType",
                    this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct", "authorityType", "Default"));

            send(new Gson().toJson(queryArgs));
            data = receive();
            if (data == null) {
                return false;
            }
            jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData != null && jsonData.containsKey("result") && (boolean) jsonData.get("result")) {
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
            logger.trace("Login failed: {} {}", ((Map<String, Object>) jsonData.get("error")).get("code"),
                    ((Map<String, Object>) jsonData.get("error")).get("message"));
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
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    final Logger localLogger = logger;
                    if (localLogger != null) {
                        localLogger.debug("Thread interrupted during error wait", e);
                    }
                }
            }
            error = true;
            try {

                sock = new Socket(host, 5000);
                sock.setSoTimeout(5000); // Set timeout to 5 seconds
                error = false;

                if (!login()) {
                    loginTries++;
                    if (loginTries > 4) {
                        errorInformer.accept("can't login, check host setting and credentials");
                        execThread = false;
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
                if (data.isEmpty() || !gson.fromJson(data.get(0), JsonObject.class).has("result")) {
                    logger.trace("Failure eventManager.attach");
                    continue;
                } else {
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if (jsonPacket != null && jsonPacket.has("method")) {
                            String method = jsonPacket.get("method").getAsString();
                            if ("client.notifyEventStream".equals(method)) {
                                eventListener.eventHandler(jsonPacket);
                            }
                        }
                    }
                }
                keepAlive(this.keepAliveInterval);
                logger.trace("Failure no keep alive received");
            } catch (Exception e) {
                logger.trace("Socket open failed: {}", e.getMessage());
            }
        }
        try {
            if (sock != null) {
                sock.close();
            }
        } catch (Exception e) {
            final Logger localLogger = logger;
            if (localLogger != null) {
                localLogger.trace("Error while closing socket", e);
            }
        }
    }
}
