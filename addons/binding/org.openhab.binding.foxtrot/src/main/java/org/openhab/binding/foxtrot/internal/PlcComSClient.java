/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

    private static final long DEF_SOCKET_TIMEOUT = 500;

    /** {@link Pattern} for matching error messages: ERROR:<code> <message> */
    private final Pattern ERROR_PATTERN = Pattern.compile("(ERROR|WARNING):(\\d+) (.+)\\n");

    private String host;
    private int port;
    private long timeout;

    private SocketChannel channel = null;
    private Selector rdSelector = null;
    private SelectionKey rdKey = null;
    private Selector wrSelector = null;
    private SelectionKey wrKey = null;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(256);

    /**
     * Initialize host, port and wait timeout to 500 ms.
     *
     * @param host hostname or IP where PLCComS server runs
     * @param port port where PLCComS server listens
     */
    public PlcComSClient(String host, int port) {
        this(host, port, DEF_SOCKET_TIMEOUT);
    }

    /**
     * Initialize host, port and wait timeout.
     *
     * @param host hostname or IP where PLCComS server runs
     * @param port port where PLCComS server listens
     * @param timeout max wait time for reply from server
     */
    public PlcComSClient(String host, int port, long timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Opens communication connection (socket) to PLCComS server.
     *
     * @throws IOException if opening connection fails
     */
    public void open() throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);

        rdSelector = Selector.open();
        rdKey = channel.register(rdSelector, SelectionKey.OP_READ);
        wrSelector = Selector.open();
        wrKey = channel.register(wrSelector, SelectionKey.OP_WRITE);
    }

    /**
     * Returns if connection to PLCComS server is opened.
     *
     * @return true if connection is open, otherwise false
     */
    public boolean isOpen() {
        return channel != null && !channel.socket().isClosed();
    }

    /**
     * Closes connection to PLCComS server.
     */
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException ignored) { }
    }

    /**
     * Reads all accessible plc variable registered on PlcComS server.
     *
     * @return list of variable names (in lowercase)
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public List<String> list() throws IOException {
        String reply = sendAndReceive("list:");

        List<String> list = new ArrayList<>(200);
        if (reply != null) {
            StringTokenizer st = new StringTokenizer(reply, "\n");
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (t.contains(",") && !t.contains("__")) {
                    list.add(t.substring(t.indexOf(':') + 1, t.indexOf(',')).toLowerCase());
                }
            }
        }
        return list;
    }

    /**
     * Gets value of BOOL variable form PLCComS server as {@link Boolean}.
     *
     * @param var variable name
     * @return true if variable value is 1 or false if value is 0 or null if value is something else
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public Boolean getBool(String var) throws IOException {
        String v = get(var);

        if ("0".equals(v) || "1".equals(v)) {
            return "0".equals(v) ? Boolean.FALSE : Boolean.TRUE;
        } else {
            return null;
        }
    }

    /**
     * Gets value of number (REAL or INT) valiable from PLCComS server as {@link BigDecimal}.
     *
     * @param var variable name
     * @return value as BigDecimal or null if value is not number or empty
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public BigDecimal getNumber(String var) throws IOException {
        try {
            String v = get(var);

            return v != null ? new BigDecimal(v) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets value of string valiable from PLCComS server as {@link String}.
     *
     * @param var variable name
     * @return string value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public String get(String var) throws IOException {
        return parseGetResult(sendAndReceive(String.format("get:%1s\n", var)));
    }

    /**
     * Gets PLCComS information.
     *
     * @param info info parameter
     * @return string value
     * @throws IOException if communication with PlcComS server failed
     * @throws PlcComSEception if PLCComS server returns error or warning
     */
    public String getInfo(String info) throws IOException {
        return parseGetResult(sendAndReceive(String.format("getinfo:%1s\n", info)));
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
        sendAndReceive(String.format("set:%1s,%2s\n", var, val));
    }

    private String sendAndReceive(String req) throws IOException {
        if (!(channel != null && channel.isConnected())) {
            throw new IOException("Socket channel not connected, ensure call open first");
        }

        wrSelector.select(timeout);
        if (!wrSelector.selectedKeys().contains(wrKey)) {
            throw new IOException("Cannot write to socket!");
        }
        channel.write(ByteBuffer.wrap(req.getBytes()));

        rdSelector.select(timeout);
        if (rdSelector.selectedKeys().contains(rdKey)) {
            StringBuilder sb = new StringBuilder(256);
            while (channel.read(buffer) > 0) {
                byte[] bytes = new byte[buffer.flip().limit()];
                buffer.get(bytes).clear();
                sb.append(new String(bytes));
            }
            String reply = sb.toString();
            if (reply.startsWith("ERR") || reply.startsWith("WARN")) {
                throw handleError(reply);
            }
            return reply;
        }
        return null;
    }

    private String parseGetResult(String val) throws IOException {
        if (val == null) {
            throw new IOException("Get result is null");
        }
        if (val.contains("\n")) {
            val = val.substring(0, val.indexOf('\n'));
        }
        int i = val.contains(",") ? val.indexOf(',') + 1 : 0;
        if (i > val.length()) {
            throw new IOException("Wrong get result format, result too short");
        }
        return i < val.length() ? val.substring(i, val.length()) : null;
    }

    private PlcComSEception handleError(String errorText) {
        Matcher m = ERROR_PATTERN.matcher(errorText);
        if (m.matches()) {
            return new PlcComSEception(m.group(1), m.group(2), m.group(3));
        }
        return new PlcComSEception("ERROR", "1024", "Unknown error");
    }
}
