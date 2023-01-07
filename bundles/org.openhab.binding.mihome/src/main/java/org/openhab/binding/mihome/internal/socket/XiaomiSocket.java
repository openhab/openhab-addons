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
package org.openhab.binding.mihome.internal.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mihome.internal.discovery.XiaomiBridgeDiscoveryService;
import org.openhab.binding.mihome.internal.handler.XiaomiBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Takes care of the communication with MiHome devices.
 *
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - JavaDoc, refactored, reviewed
 *
 */
@NonNullByDefault
public abstract class XiaomiSocket {

    static final String MCAST_ADDR = "224.0.0.50";

    private static final int BUFFER_LENGTH = 1024;

    private final Logger logger = LoggerFactory.getLogger(XiaomiSocket.class);

    private final DatagramPacket datagramPacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
    private final Set<XiaomiSocketListener> listeners = new CopyOnWriteArraySet<>();

    private final int port;
    private @Nullable DatagramSocket socket;
    private final Thread socketReceiveThread = new Thread(this::receiveData);

    /**
     * Sets up a {@link XiaomiSocket} with the MiHome multicast address and a random port
     *
     * @param owner identifies the socket owner
     */
    public XiaomiSocket(String owner) {
        this(0, owner);
    }

    /**
     * Sets up a {@link XiaomiSocket} with the MiHome multicast address and a specific port
     *
     * @param port the socket will be bound to this port
     * @param owner identifies the socket owner
     */
    public XiaomiSocket(int port, String owner) {
        this.port = port;
        socketReceiveThread.setName("XiaomiSocketReceiveThread(" + port + ", " + owner + ")");
    }

    public void initialize() {
        setupSocket();
        if (!socketReceiveThread.isAlive()) {
            logger.trace("Starting receiver thread {}", socketReceiveThread);
            socketReceiveThread.start();
        }
    }

    protected abstract void setupSocket();

    /**
     * Interrupts the {@link ReceiverThread} and closes the {@link XiaomiSocket}.
     */
    private void closeSocket() {
        synchronized (XiaomiSocket.class) {
            logger.debug("Interrupting receiver thread {}", socketReceiveThread);
            socketReceiveThread.interrupt();

            DatagramSocket socket = this.socket;
            if (socket != null) {
                logger.debug("Closing socket {}", socket);
                socket.close();
                this.socket = null;
            }
        }
    }

    /**
     * Registers a {@link XiaomiSocketListener} to be called back, when data is received.
     * If no {@link XiaomiSocket} exists, when the method is called, it is being set up.
     *
     * @param listener {@link XiaomiSocketListener} to be called back
     */
    public synchronized void registerListener(XiaomiSocketListener listener) {
        if (listeners.add(listener)) {
            logger.trace("Added socket listener {}", listener);
        }

        DatagramSocket socket = this.socket;
        if (socket == null) {
            initialize();
        }
    }

    /**
     * Unregisters a {@link XiaomiSocketListener}. If there are no listeners left,
     * the {@link XiaomiSocket} is being closed.
     *
     * @param listener {@link XiaomiSocketListener} to be unregistered
     */
    public synchronized void unregisterListener(XiaomiSocketListener listener) {
        if (listeners.remove(listener)) {
            logger.trace("Removed socket listener {}", listener);
        }

        if (listeners.isEmpty()) {
            closeSocket();
        }
    }

    /**
     * Sends a message through the {@link XiaomiSocket} to a specific address and port
     *
     * @param message the message to be sent
     * @param address the message destination address
     * @param port the message destination port
     */
    public void sendMessage(String message, InetAddress address, int port) {
        DatagramSocket socket = this.socket;
        if (socket == null) {
            logger.error("Error while sending message (socket is null)");
            return;
        }

        try {
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            logger.trace("Sending message: {} to {}:{}", message, address, port);
            socket.send(sendPacket);
        } catch (IOException e) {
            logger.error("Error while sending message", e);
        }
    }

    /**
     * @return the port number of this {@link XiaomiSocket}
     */
    public int getPort() {
        return port;
    }

    protected @Nullable DatagramSocket getSocket() {
        return socket;
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * This method is the main method of the receiver thread for the {@link XiaomiBridgeSocket}.
     * If the socket has data, it parses the data to a json object and calls all
     * {@link XiaomiSocketListener} and passes the data to them.
     */
    private void receiveData() {
        DatagramSocket socket = this.socket;
        if (socket == null) {
            logger.error("Failed to receive data (socket is null)");
            return;
        }

        Thread currentThread = Thread.currentThread();
        int localPort = socket.getLocalPort();

        try {
            while (!currentThread.isInterrupted()) {
                logger.trace("Thread {} waiting for data on port {}", currentThread.getName(), localPort);
                socket.receive(datagramPacket);
                InetAddress address = datagramPacket.getAddress();
                logger.debug("Received Datagram from {}:{} on port {}", address.getHostAddress(),
                        datagramPacket.getPort(), localPort);
                String sentence = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                JsonObject message = JsonParser.parseString(sentence).getAsJsonObject();
                notifyListeners(message, address);
                logger.trace("Data received and notified {} listeners", listeners.size());
            }
        } catch (IOException e) {
            if (!currentThread.isInterrupted()) {
                logger.error("Error while receiving", e);
            } else {
                logger.trace("Receiver thread was interrupted");
            }
        }
        logger.debug("Receiver thread ended");
    }

    /**
     * Notifies all {@link XiaomiSocketListener} on the parent {@link XiaomiSocket}. First checks for any matching
     * {@link XiaomiBridgeHandler}, before passing to any {@link XiaomiBridgeDiscoveryService}.
     *
     * @param message the data message as {@link JsonObject}
     * @param address the address from which the message was received
     */
    private void notifyListeners(JsonObject message, InetAddress address) {
        for (XiaomiSocketListener listener : listeners) {
            if (listener instanceof XiaomiBridgeHandler) {
                if (((XiaomiBridgeHandler) listener).getHost().equals(address)) {
                    listener.onDataReceived(message);
                }
            } else if (listener instanceof XiaomiBridgeDiscoveryService) {
                listener.onDataReceived(message);
            }
        }
    }
}
