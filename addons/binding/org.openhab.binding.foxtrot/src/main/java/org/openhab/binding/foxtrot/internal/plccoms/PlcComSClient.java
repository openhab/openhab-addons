/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.plccoms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlcComS client.
 *
 * @author Radovan Sninsky
 * @since 2015-08-26 21:20
 */
@SuppressWarnings("unused")
public class PlcComSClient {

    private final Logger logger = LoggerFactory.getLogger(PlcComSClient.class);

    /** {@link Pattern} for matching error messages: ERROR:<code> <message> */
    private final Pattern ERROR_PATTERN = Pattern.compile("(ERROR|WARNING):(\\d+) (.+)");
    /** {@link Pattern} for matching value reply (diff, get) messages: <op>:<variable>,<value> */
    private final Pattern REPLY_PATTERN = Pattern.compile("(\\w+):([\\w.]+),(.+)");
    /** {@link Pattern} for matching get info messages: GETINFO:<info_key>,<value> */
    private final Pattern GI_PATTERN = Pattern.compile("(\\w+),(.+)");

    private String host;
    private int port;

    private Socket socket = null;
    private Writer socketWriter = null;
    private BufferedReader socketReader = null;

    private Map<String, String> plcInfos = new HashMap<>(20);

    /**
     * Initialize host, port and wait timeout to 500 ms.
     *
     * @param host hostname or IP where PLCComS server runs
     * @param port port where PLCComS server listens
     */
    public PlcComSClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Opens communication connection (socket) to PLCComS server.
     *
     * @throws IOException if opening connection fails
     */
    public void open() throws IOException {
        logger.trace("Connecting to PlcComS ...");
        socket = new Socket(host, port);
        socketWriter = new OutputStreamWriter(socket.getOutputStream());
        socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        readPlcComSInfo();
    }

    private void readPlcComSInfo() throws IOException {
        send("getinfo:\n");

        sleep(300);

        while (socketReader.ready()) {
            String line = socketReader.readLine();
            if (line != null && line.startsWith("GETINFO") && line.contains(",")) {
                Matcher m = GI_PATTERN.matcher(line);
                if (m.matches()) {
                    plcInfos.put(m.group(1).toLowerCase(), m.group(2));
                }
            }
        }
    }

    /**
     * Returns if connection to PLCComS server is opened.
     *
     * @return true if connection is open, otherwise false
     */
    public boolean isOpen() {
        return !socket.isClosed();
    }

    /**
     * Closes connection to PLCComS server.
     */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socketWriter.close();
                socketReader.close();
                socket.close();
            }
        } catch (IOException ignored) {
        } finally {
            logger.trace("Connection to PlcComS closed");
        }
    }

    public Map<String, String> getInfos() {
        return plcInfos;
    }

    public void doGet(String var) throws IOException {
        send(String.format("get:%1s\n", var));
    }

    /**
     * Sets new boolean value to variable.
     *
     * @param var variable name
     * @param val new value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public void set(String var, boolean val) throws IOException {
        set(var, Boolean.toString(val));
    }

    /**
     * Sets new integer value to variable.
     *
     * @param var variable name
     * @param val new value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public void set(String var, int val) throws IOException {
        set(var, Integer.toString(val));
    }

    /**
     * Sets new double value to variable.
     *
     * @param var variable name
     * @param val new value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public void set(String var, double val) throws IOException {
        set(var, Double.toString(val));
    }

    /**
     * Sets new BigDecimal value to variable.
     *
     * @param var variable name
     * @param val new value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public void set(String var, BigDecimal val) throws IOException {
        if (val != null) {
            set(var, val.toPlainString());
        }
    }

    /**
     * Sets new string value to variable.
     *
     * @param var variable name
     * @param val new value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public void set(String var, String val) throws IOException {
        send(String.format("set:%1s,%2s\n", var, val));
    }

    public void enable(String var) throws IOException {
        enable(var, null);
    }

    public void enable(String var, BigDecimal delta) throws IOException {
        if (delta == null) {
            send(String.format("en:%1s\n", var));
        } else {
            send(delta.scale() == 0 ? String.format("en:%1s %d\n", var, delta.intValue()) :
                    String.format(Locale.US, "en:%1s %." + delta.scale() + "f\n", var, delta));
        }
    }

    public void disable(String variable) throws IOException {
        send(String.format("di:%1s\n", variable));
    }

    public PlcComSReply receive(int timeout) throws IOException {
        long maxTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < maxTime) {
            PlcComSReply r = receive();
            if (r != null) {
                return r;
            }
            sleep(100);
        }

        return null;
    }

    private void send(String text) throws IOException {
        logger.trace("Sending -> PlcComS: '{}'", text);
        socketWriter.write(text);
        socketWriter.flush();
    }

    private PlcComSReply receive() throws IOException {
        if (!isOpen()) {
            throw new ConnectException("Socket closed");
        }
        if (socketReader.ready()) {
            String reply = socketReader.readLine();
            if (reply != null) {
                logger.trace("Received <- PlcComS: '{}'", reply);
                if (reply.startsWith("ERR") || reply.startsWith("WARN")) {
                    Matcher m = ERROR_PATTERN.matcher(reply);
                    throw m.matches() ? new PlcComSEception(m.group(1), m.group(2), m.group(3)) :
                            new PlcComSEception("ERROR", "1024", "Unknown error");
                }
                Matcher m = REPLY_PATTERN.matcher(reply);
                return m.matches() ? new PlcComSReply(m.group(1), m.group(2), m.group(3)) : new PlcComSReply("UNK", null, reply);
            }
        }

        return null;
    }

    private void sleep(long milis) {
        try { Thread.sleep(milis); } catch (InterruptedException ignored) { }
    }
}
