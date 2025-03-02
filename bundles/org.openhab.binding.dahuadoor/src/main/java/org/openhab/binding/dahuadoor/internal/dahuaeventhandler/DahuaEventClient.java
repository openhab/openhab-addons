package org.openhab.binding.dahuadoor.internal.dahuaeventhandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * The {@link DauhaEventClient} client polls the Dahua device
 *
 *
 * @author Sven Schad - Initial contribution
 */

public class DahuaEventClient implements Runnable {

    Logger logger;

    private String host;
    private String username;
    private String password;
    private int ID = 0; // Our Request / Response ID that must be in all requests and initiated by us
    private int SessionID = 0; // Session ID will be returned after successful login
    private String FakeIPaddr = "(null)"; // WebGUI: mask our real IP
    private String clientType = ""; // WebGUI: We do not show up in logs or online users
    private int keepAliveInterval = 60;
    private long lastKeepAlive = 0;
    private static boolean debug = true;

    private DHIPEventListener eventListener;
    private Future<?> connectionThread;

    private Socket sock;
    private final Gson gson = new Gson();
    private boolean execThread = true;
    private Consumer<String> errorInformer;

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

    public String Gen_md5_hash(String Dahua_random, String Dahua_realm, String username, String password)
            throws Exception {
        String PWDDB_HASH = md5(username + ":" + Dahua_realm + ":" + password).toUpperCase();
        String PASS = username + ":" + Dahua_random + ":" + PWDDB_HASH;
        return md5(PASS).toUpperCase();
    }

    private String md5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void KeepAlive(int delay) {
        logger.trace("Started keepAlive thread");
        while (execThread) {
            Map<String, Object> queryArgs = new HashMap<>();
            queryArgs.put("method", "global.keepAlive");
            queryArgs.put("magic", "0x1234");
            queryArgs.put("params", Map.of("timeout", delay, "active", true));
            queryArgs.put("id", this.ID);
            queryArgs.put("session", this.SessionID);

            try {
                Send(new Gson().toJson(queryArgs));
            } catch (IOException e) {
                logger.trace("Error sending keepAlive: " + e.getMessage());
            }
            lastKeepAlive = System.currentTimeMillis();
            boolean keepAliveReceived = false;

            while (lastKeepAlive + delay * 1000 > System.currentTimeMillis()) {
                ArrayList<String> data;
                try {
                    data = Receive();
                    if (data != null) {
                        for (String packet : data) {
                            JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                            if (jsonPacket.has("result")) {
                                if (debug) {
                                    logger.trace("keepAlive back");
                                }
                                keepAliveReceived = true;
                            } else if ("client.notifyEventStream".equals(jsonPacket.get("method").getAsString())) {
                                eventListener.EventHandler(jsonPacket);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.trace("Error receiving keepAlive response: " + e.getMessage());
                }
            }

            if (!keepAliveReceived) {
                logger.trace("keepAlive failed");
                return;
            }
        }
    }

    public void Send(String packet) throws IOException {
        if (packet == null) {
            packet = "";
        }

        ByteBuffer buffer = ByteBuffer.allocate(32 + packet.length());
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0x20000000);
        buffer.putInt(0x44484950);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(SessionID);
        buffer.putInt(ID);
        buffer.putInt(packet.length());
        buffer.putInt(0);
        buffer.putInt(packet.length());
        buffer.putInt(0);

        if (buffer.position() != 32) {
            logger.trace("Binary header != 32");
            return;
        }

        ID += 1;

        try {
            buffer.put(packet.getBytes());
            /*
             * buffer.flip();
             * byte[] byteArray = new byte[buffer.remaining()];
             * buffer.get(byteArray);
             * String result = new String(byteArray, StandardCharsets.UTF_8);
             */
            String result2 = new String(buffer.array(), StandardCharsets.UTF_8);

            // logger.trace("Sending:"+result);
            sock.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }

    private String pack(int value) {

        ByteBuffer buffer = ByteBuffer.allocate(4).putInt(value);
        String teststring = new String(buffer.array(), 0, 4); // buffer.array().toString();
        return teststring;
    }

    private int unpackShort(byte[] bytes, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int myInt = buffer.getShort();
        return myInt;
    }

    public ArrayList<String> Receive() throws IOException {

        ArrayList<String> p2pReturnData = new ArrayList<String>();
        byte[] buffer = new byte[8192];
        byte[] header = new byte[32];
        ByteBuffer bbuffer;
        int lenRecved = 1;
        int lenExpect = 1;
        int timeout = 5;

        try {
            sock.setSoTimeout(timeout * 1000); // Set timeout in milliseconds
            InputStream input = sock.getInputStream();
            int bytesRead = input.read(buffer);
            bbuffer = ByteBuffer.wrap(buffer).limit(bytesRead);
            // // logger.trace("Buffer: {}",HexFormat.of().formatHex(buffer));
            // logger.trace("Bytes read:{}",bytesRead);
        } catch (IOException e) {
            return new ArrayList<>();
        }

        while (bbuffer.hasRemaining()) {
            bbuffer.order(ByteOrder.BIG_ENDIAN);
            if (bbuffer.getLong(0) == 0x2000000044484950L) {
                bbuffer.order(ByteOrder.LITTLE_ENDIAN);
                lenRecved = bbuffer.getShort(16);
                lenExpect = bbuffer.getShort(24);
                bbuffer.get(header, 0, 32);
                bbuffer = bbuffer.position(32).slice(); // cut bbuffer by 32 Bytes
                // logger.trace("HEADER+ eventData.get(" logger.trace("LEN
                // rec {} , LEN exp {}",lenRecved,lenExpect);
                // logger.trace("Header: {}",HexFormat.of().formatHex(header));

            } else {
                if (lenRecved > 0) {
                    String p2pData = new String(bbuffer.array(), bbuffer.arrayOffset(), lenRecved);
                    bbuffer = bbuffer.position(lenRecved).slice(); // cut bbuffer
                    // logger.trace("Data: {}",p2pData);
                    p2pReturnData.add(p2pData);
                    lenRecved = 0;
                } else {
                    break;
                }
            }
        }
        return p2pReturnData;
    }

    public boolean Login() {
        logger.trace("Start login");

        Map<String, Object> queryArgs = new HashMap<>();
        queryArgs.put("id", 10000);
        queryArgs.put("magic", "0x1234");
        queryArgs.put("method", "global.login");
        queryArgs.put("params", Map.of("clientType", this.clientType, "ipAddr", this.FakeIPaddr, "loginType", "Direct",
                "password", "", "userName", this.username));
        queryArgs.put("session", 0);

        try {
            Send(new Gson().toJson(queryArgs));
            ArrayList<String> data = Receive();
            if (data == null) {
                logger.trace("global.login [random]");
                return false;
            }
            Map<String, Object> jsonData = new Gson().fromJson(data.get(0), Map.class);
            this.SessionID = ((Double) jsonData.get("session")).intValue();
            Map<String, Object> params = (Map<String, Object>) jsonData.get("params");
            String RANDOM = (String) params.get("random");
            String REALM = (String) params.get("realm");

            String RANDOM_HASH = Gen_md5_hash(RANDOM, REALM, this.username, this.password);

            queryArgs = new HashMap<>();
            queryArgs.put("id", 10000);
            queryArgs.put("magic", "0x1234");
            queryArgs.put("method", "global.login");
            queryArgs.put("session", this.SessionID);
            queryArgs.put("params", Map.of("userName", this.username, "password", RANDOM_HASH, "clientType",
                    this.clientType, "ipAddr", this.FakeIPaddr, "loginType", "Direct", "authorityType", "Default"));

            Send(new Gson().toJson(queryArgs));
            data = Receive();
            if (data == null) {
                return false;
            }
            jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData.containsKey("result") && (boolean) jsonData.get("result")) {
                logger.trace("Login success");
                this.keepAliveInterval = ((Double) ((Map<String, Object>) jsonData.get("params"))
                        .get("keepAliveInterval")).intValue();
                return true;
            }
            logger.trace("Login failed: " + ((Map<String, Object>) jsonData.get("error")).get("code") + " "
                    + ((Map<String, Object>) jsonData.get("error")).get("message"));
        } catch (Exception e) {
            logger.trace("Login error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {

        logger = LoggerFactory.getLogger(DahuaEventClient.class);

        boolean error = false;
        int loginTries = 0;
        while (execThread) {
            if (error) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            error = true;
            try {

                sock = new Socket(host, 5000);
                sock.setSoTimeout(5000); // Set timeout to 5 seconds
                error = false;

                if (!Login()) {
                    loginTries++;
                    if (loginTries > 4) {
                        errorInformer.accept("can't login, check host setting and credentials");
                        execThread = false;
                    }
                    continue;
                }

                Map<String, Object> queryArgs = new HashMap<>();
                queryArgs.put("id", this.ID);
                queryArgs.put("magic", "0x1234");
                queryArgs.put("method", "eventManager.attach");
                queryArgs.put("params", Map.of("codes", new String[] { "All" }));
                queryArgs.put("session", this.SessionID);

                Send(new Gson().toJson(queryArgs));
                ArrayList<String> data = Receive();
                if (data.isEmpty() || !gson.fromJson(data.get(0), JsonObject.class).has("result")) {
                    logger.trace("Failure eventManager.attach");
                    continue;
                } else {
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if ("client.notifyEventStream".equals(jsonPacket.get("method"))) {
                            eventListener.EventHandler(jsonPacket);
                        }
                    }
                }
                KeepAlive(this.keepAliveInterval);
                logger.trace("Failure no keep alive received");
            } catch (Exception e) {
                logger.trace("Socket open failed: " + e.getMessage());
            }
        }
        try {
            sock.close();
        } catch (Exception e) {
        }
    }

}
