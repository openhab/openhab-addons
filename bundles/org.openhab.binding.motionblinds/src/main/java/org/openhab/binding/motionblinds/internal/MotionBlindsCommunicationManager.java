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
package org.openhab.binding.motionblinds.internal;

import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link MotionBlindsCommunicationManager} is shared by all motors. It
 * <ul>
 * <li>sends unicast requests to a motor on port 32100 and waits for the acknowledge on the same socket,</li>
 * <li>listens for multicast pushes ({@code Report}, {@code Heartbeat}) on 238.0.0.18:32101 and dispatches them to
 * the handler registered for the MAC address in the message,</li>
 * <li>sends a multicast {@code GetDeviceList} for discovery; the acknowledges arrive on the multicast socket.</li>
 * </ul>
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
@Component(service = MotionBlindsCommunicationManager.class)
public class MotionBlindsCommunicationManager {

    private static final int REQUEST_TIMEOUT_MS = 3000;
    private static final int REQUEST_ATTEMPTS = 3;
    private static final int RECEIVE_BUFFER_SIZE = 4096;

    private final Logger logger = LoggerFactory.getLogger(MotionBlindsCommunicationManager.class);
    private final Gson gson = new Gson();

    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();
    private final AtomicReference<@Nullable DiscoveryListener> discoveryListener = new AtomicReference<>();

    private final Object listenerLock = new Object();
    private @Nullable MulticastSocket multicastSocket;
    private @Nullable Thread listenerThread;

    /**
     * Receives messages pushed by a motor.
     */
    public interface MessageListener {
        void onMessage(MotionBlindsMessage message, String sourceIp);
    }

    /**
     * Receives {@code GetDeviceListAck} messages during discovery.
     */
    public interface DiscoveryListener {
        void onDeviceFound(MotionBlindsMessage message, String sourceIp);
    }

    @Deactivate
    public void deactivate() {
        listeners.clear();
        discoveryListener.set(null);
        stopListener();
    }

    public void registerListener(String mac, MessageListener listener) {
        listeners.put(normalizeMac(mac), listener);
        startListener();
    }

    public void unregisterListener(String mac, MessageListener listener) {
        listeners.remove(normalizeMac(mac), listener);
        stopListenerIfUnused();
    }

    /**
     * Send a request to a motor and wait for its acknowledge.
     *
     * @throws IOException if the motor did not answer after all attempts or the answer could not be parsed
     */
    public MotionBlindsMessage sendRequest(String ipAddress, MotionBlindsRequest request) throws IOException {
        String json = gson.toJson(request);
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(ipAddress);
        if (logger.isTraceEnabled()) {
            logger.trace("Sending to {}: {}", ipAddress, hideTokens(json));
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(REQUEST_TIMEOUT_MS);
            byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
            for (int attempt = 1;; attempt++) {
                socket.send(new DatagramPacket(payload, payload.length, address, UDP_PORT_SEND));
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    if (attempt >= REQUEST_ATTEMPTS) {
                        throw new IOException("No response from " + ipAddress + " after " + attempt + " attempts");
                    }
                    logger.debug("Timeout waiting for response from {} (attempt {})", ipAddress, attempt);
                    continue;
                }
                String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                if (logger.isTraceEnabled()) {
                    logger.trace("Received from {}: {}", ipAddress, hideTokens(response));
                }
                MotionBlindsMessage message = parse(response);
                if (message == null) {
                    throw new IOException("Invalid response from " + ipAddress);
                }
                return message;
            }
        }
    }

    /**
     * Send a multicast {@code GetDeviceList}. Acknowledges are passed to the given listener until
     * {@link #stopDiscovery} is called.
     */
    public void startDiscovery(DiscoveryListener listener) {
        discoveryListener.set(listener);
        sendDiscoveryRequest();
    }

    public void stopDiscovery(DiscoveryListener listener) {
        discoveryListener.compareAndSet(listener, null);
        stopListenerIfUnused();
    }

    /**
     * Send a multicast {@code GetDeviceList} on all suitable network interfaces. Every motor answers with a
     * {@code GetDeviceListAck}, which is passed to the discovery listener and to the listener registered for the MAC
     * address of the motor. Handlers use this to find the IP address of their motor.
     */
    public void sendDiscoveryRequest() {
        startListener();

        byte[] payload = gson.toJson(new MotionBlindsRequest(MSG_GET_DEVICE_LIST)).getBytes(StandardCharsets.UTF_8);
        synchronized (listenerLock) {
            MulticastSocket socket = multicastSocket;
            if (socket == null) {
                logger.debug("Multicast socket is not available, discovery skipped");
                return;
            }
            for (NetworkInterface networkInterface : getMulticastInterfaces()) {
                try {
                    socket.setNetworkInterface(networkInterface);
                    socket.send(new DatagramPacket(payload, payload.length, InetAddress.getByName(MULTICAST_ADDRESS),
                            UDP_PORT_SEND));
                    logger.trace("Sent discovery request on {}", networkInterface.getDisplayName());
                } catch (IOException e) {
                    logger.debug("Failed to send discovery request on {}: {}", networkInterface.getDisplayName(),
                            e.getMessage());
                }
            }
        }
    }

    private void startListener() {
        synchronized (listenerLock) {
            if (listenerThread != null) {
                return;
            }
            MulticastSocket socket;
            try {
                socket = new MulticastSocket(null);
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(UDP_PORT_RECEIVE));
            } catch (IOException e) {
                logger.warn("Unable to listen on UDP port {}: {}", UDP_PORT_RECEIVE, e.getMessage());
                return;
            }
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, UDP_PORT_RECEIVE);
            for (NetworkInterface networkInterface : getMulticastInterfaces()) {
                try {
                    socket.joinGroup(group, networkInterface);
                    logger.debug("Joined multicast group {} on {}", MULTICAST_ADDRESS,
                            networkInterface.getDisplayName());
                } catch (IOException e) {
                    logger.debug("Unable to join multicast group on {}: {}", networkInterface.getDisplayName(),
                            e.getMessage());
                }
            }
            Thread thread = new Thread(() -> receiveLoop(socket), "OH-binding-" + BINDING_ID);
            thread.setDaemon(true);
            multicastSocket = socket;
            listenerThread = thread;
            thread.start();
        }
    }

    private void stopListenerIfUnused() {
        if (listeners.isEmpty() && discoveryListener.get() == null) {
            stopListener();
        }
    }

    private void stopListener() {
        synchronized (listenerLock) {
            MulticastSocket socket = multicastSocket;
            multicastSocket = null;
            listenerThread = null;
            if (socket != null) {
                // unblocks receive() in the listener thread, which then terminates
                socket.close();
            }
        }
    }

    private void receiveLoop(MulticastSocket socket) {
        byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
        logger.debug("Multicast listener started");
        while (!socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    logger.debug("Error receiving multicast message: {}", e.getMessage());
                }
                continue;
            }
            String sourceIp = packet.getAddress().getHostAddress();
            String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            if (logger.isTraceEnabled()) {
                logger.trace("Multicast from {}: {}", sourceIp, hideTokens(json));
            }
            try {
                dispatch(json, sourceIp);
            } catch (RuntimeException e) {
                logger.warn("Error processing message from {}: {}", sourceIp, e.getMessage(), e);
            }
        }
        logger.debug("Multicast listener stopped");
    }

    private void dispatch(String json, String sourceIp) {
        MotionBlindsMessage message = parse(json);
        if (message == null) {
            logger.debug("Ignoring invalid message from {}", sourceIp);
            return;
        }
        String mac = message.mac;
        if (mac == null) {
            // our own discovery request looped back, or an unknown message
            return;
        }

        DiscoveryListener discovery = discoveryListener.get();
        if (discovery != null && MSG_GET_DEVICE_LIST_ACK.equals(message.msgType)) {
            discovery.onDeviceFound(message, sourceIp);
        }

        MessageListener listener = listeners.get(normalizeMac(mac));
        if (listener != null) {
            listener.onMessage(message, sourceIp);
        }
    }

    private @Nullable MotionBlindsMessage parse(String json) {
        try {
            return gson.fromJson(json, MotionBlindsMessage.class);
        } catch (JsonParseException e) {
            logger.debug("Unable to parse message '{}': {}", hideTokens(json), e.getMessage());
            return null;
        }
    }

    private List<NetworkInterface> getMulticastInterfaces() {
        List<NetworkInterface> result = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isPointToPoint()
                            && networkInterface.supportsMulticast()
                            && Collections.list(networkInterface.getInetAddresses()).stream()
                                    .anyMatch(Inet4Address.class::isInstance)) {
                        result.add(networkInterface);
                    }
                } catch (SocketException e) {
                    // ignore this interface
                }
            }
        } catch (SocketException e) {
            logger.debug("Unable to list network interfaces: {}", e.getMessage());
        }
        return result;
    }

    static String normalizeMac(String mac) {
        return mac.replaceAll("[^0-9A-Fa-f]", "").toLowerCase(Locale.ROOT);
    }

    static String hideTokens(String json) {
        return json.replaceAll("(\"(?:token|AccessToken)\"\\s*:\\s*\")[^\"]*", "$1***");
    }
}
