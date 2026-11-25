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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Implements the legacy TP-Link/Kasa XOR protocol used by devices such as early HS200 and HS220 switches.
 *
 * @author Lee Ballard - Initial contribution
 * @author Christopher Smith - Add child outlet support for Kasa power strips
 */
@NonNullByDefault
public class KasaXorProtocol extends KasaIotProtocol {
    private static final int PORT = 9999;
    private static final int INITIAL_KEY = 0xAB;
    private static final int SOCKET_TIMEOUT_MILLIS = 5000;
    private static final int MAX_RESPONSE_LENGTH = 1024 * 1024;

    private final String ipAddress;
    private final int port;
    private @Nullable Socket activeSocket;

    public KasaXorProtocol(TapoDeviceConnector connector, String ipAddress, boolean dimmer) {
        this(connector, ipAddress, dimmer, PORT);
    }

    KasaXorProtocol(TapoDeviceConnector connector, String ipAddress, boolean dimmer, int port) {
        super(connector, dimmer);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    protected void disconnect() {
        Socket socket;
        synchronized (sessionLock) {
            socket = activeSocket;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // The connection is already being discarded.
            }
        }
    }

    @Override
    protected String sendCommand(String command, long requestSession) throws IOException, TapoErrorHandler {
        Socket socket = new Socket();
        synchronized (sessionLock) {
            requireActiveSessionLocked(requestSession);
            activeSocket = socket;
        }
        try (socket) {
            socket.connect(new InetSocketAddress(ipAddress, port), SOCKET_TIMEOUT_MILLIS);
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    DataInputStream input = new DataInputStream(socket.getInputStream())) {
                byte[] encrypted = encrypt(command);
                output.writeInt(encrypted.length);
                output.write(encrypted);
                output.flush();

                int responseLength = input.readInt();
                if (responseLength <= 0 || responseLength > MAX_RESPONSE_LENGTH) {
                    throw new IOException("Invalid Kasa response length: " + responseLength);
                }
                byte[] response = new byte[responseLength];
                input.readFully(response);
                return decrypt(response);
            }
        } finally {
            synchronized (sessionLock) {
                activeSocket = null;
            }
        }
    }

    static byte[] encrypt(String value) {
        byte[] plainText = value.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[plainText.length];
        int key = INITIAL_KEY;
        for (int i = 0; i < plainText.length; i++) {
            encrypted[i] = (byte) (plainText[i] ^ key);
            key = Byte.toUnsignedInt(encrypted[i]);
        }
        return encrypted;
    }

    static String decrypt(byte[] encrypted) {
        byte[] plainText = new byte[encrypted.length];
        int key = INITIAL_KEY;
        for (int i = 0; i < encrypted.length; i++) {
            int nextKey = Byte.toUnsignedInt(encrypted[i]);
            plainText[i] = (byte) (nextKey ^ key);
            key = nextKey;
        }
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
