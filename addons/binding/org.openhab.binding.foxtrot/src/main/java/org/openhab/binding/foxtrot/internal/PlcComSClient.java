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

/**
 * PlcComS client.
 *
 * @author Radovan Sninsky
 * @since 2015-08-26 21:20
 */
@SuppressWarnings("unused")
public class PlcComSClient {

    private static final long DEF_SOCKET_TIMEOUT = 500;

    private String host;
    private int port;
    private long timeout;

    private SocketChannel channel = null;
    private Selector rdSelector = null;
    private SelectionKey rdKey = null;
    private Selector wrSelector = null;
    private SelectionKey wrKey = null;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(256);

    public PlcComSClient(String host, int port) {
        this(host, port, DEF_SOCKET_TIMEOUT);
    }

    private PlcComSClient(String host, int port, long timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public void open() throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);

        rdSelector = Selector.open();
        rdKey = channel.register(rdSelector, SelectionKey.OP_READ);
        wrSelector = Selector.open();
        wrKey = channel.register(wrSelector, SelectionKey.OP_WRITE);
    }

    public boolean isOpen() {
        return channel != null && channel.isConnected();
    }

    public void close() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /**
     * Reads all accessible plc variable registered on PlcComS server.
     *
     * @return list of variable names (in lowercase)
     * @throws IOException if communication with PlcComS server failed
     */
    public List<String> list() throws IOException {
        assertChannel();
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

    public Boolean getBool(String var) throws IOException {
        assertChannel();
        return !Boolean.valueOf(get(var));
    }

    public Long getLong(String var) throws IOException {
        assertChannel();
        try {
            return Long.valueOf(get(var));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal getReal(String var) throws IOException {
        assertChannel();
        try {
            String v = get(var);
            return v != null ? new BigDecimal(v) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String get(String var) throws IOException {
        assertChannel();
        return parseGetResult(sendAndReceive(String.format("get:%1s\n", var)));
    }

    public String getInfo(String var) throws IOException {
        assertChannel();
        return parseGetResult(sendAndReceive(String.format("getinfo:%1s\n", var)));
    }

    public void set(String var, boolean val) throws IOException {
        assertChannel();
        set(var, Boolean.toString(val));
    }

    public void set(String var, int val) throws IOException {
        assertChannel();
        set(var, Integer.toString(val));
    }

    public void set(String var, double val) throws IOException {
        assertChannel();
        set(var, Double.toString(val));
    }

    @SuppressWarnings("WeakerAccess")
    public void set(String var, String val) throws IOException {
        assertChannel();
        sendAndReceive(String.format("set:%1s,%2s\n", var, val));
    }

    private void assertChannel() {
        assert channel != null && channel.isConnected() :
                "Socket channel not connected, ensure call open method before calling other methods";
    }

    private String sendAndReceive(String req) throws IOException {
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
            if (reply.startsWith("ERROR")) {
                throw new IOException("PLCComS " + reply);
            }
            return reply;
        }
        return null;
    }

    private String parseGetResult(String val) throws IOException {
        if (val == null) {
            return null;
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
}
