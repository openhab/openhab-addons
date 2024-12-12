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
package org.openhab.binding.govee.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.govee.internal.model.DiscoveryResponse;
import org.openhab.binding.govee.internal.model.GenericGoveeRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link CommunicationManager} is a thread that handles the answers of all devices.
 * Therefore it needs to apply the information to the right thing.
 *
 * Discovery uses the same response code, so we must not refresh the status during discovery.
 *
 * @author Stefan Höhn - Initial contribution
 * @author Danny Baumann - Thread-Safe design refactoring
 * @author Andrew Fiddian-Green - Extensive refactoring
 */
@NonNullByDefault
@Component(service = CommunicationManager.class)
public class CommunicationManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
    private final Gson gson = new Gson();

    // list of Thing handler listeners that will receive state notifications
    private final Map<String, GoveeHandler> thingHandlerListeners = new ConcurrentHashMap<>();

    private @Nullable GoveeDiscoveryListener discoveryListener;
    private @Nullable Thread serverThread;
    private @Nullable DatagramSocket serverSocket;
    private final Object serverLock = new Object();

    private static final String DISCOVERY_MULTICAST_ADDRESS = "239.255.255.250";
    private static final int DISCOVERY_PORT = 4001;
    private static final int RESPONSE_PORT = 4002;
    private static final int REQUEST_PORT = 4003;

    public static final int SCAN_TIMEOUT_SEC = 5;

    private static final String DISCOVER_REQUEST = "{\"msg\": {\"cmd\": \"scan\", \"data\": {\"account_topic\": \"reserve\"}}}";

    private static final InetSocketAddress DISCOVERY_SOCKET_ADDRESS = new InetSocketAddress(DISCOVERY_MULTICAST_ADDRESS,
            DISCOVERY_PORT);

    public interface GoveeDiscoveryListener {
        void onDiscoveryResponse(DiscoveryResponse discoveryResponse);
    }

    @Activate
    public CommunicationManager() {
    }

    @Deactivate
    public void deactivate() {
        thingHandlerListeners.clear();
        discoveryListener = null;
        listenerCountDecreased();
    }

    /**
     * Thing handlers register themselves to receive state updates when they are initalized.
     */
    public void registerHandler(GoveeHandler handler) {
        thingHandlerListeners.put(ipAddressFrom(handler.getHostname()), handler);
        listenerCountIncreased();
    }

    /**
     * Thing handlers unregister themselves when they are destroyed.
     */
    public void unregisterHandler(GoveeHandler handler) {
        thingHandlerListeners.remove(ipAddressFrom(handler.getHostname()));
        listenerCountDecreased();
    }

    /**
     * Send a unicast command request to the device.
     */
    public void sendRequest(GoveeHandler handler, GenericGoveeRequest request) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setReuseAddress(true);
            String message = gson.toJson(request);
            byte[] data = message.getBytes();
            String hostname = handler.getHostname();
            InetAddress address = InetAddress.getByName(hostname);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, REQUEST_PORT);
            socket.send(packet);
            logger.trace("Sent request to {} on {} with content = {}", handler.getThing().getUID(),
                    address.getHostAddress(), message);
        }
    }

    /**
     * Send discovery multicast pings on any ipv4 address bound to any network interface in the given list and
     * then sleep for sufficient time until responses may have been received.
     */
    public void runDiscoveryForInterfaces(List<NetworkInterface> interfaces, GoveeDiscoveryListener listener) {
        try {
            discoveryListener = listener;
            listenerCountIncreased();
            Instant sleepUntil = Instant.now().plusSeconds(SCAN_TIMEOUT_SEC);

            interfaces.parallelStream() // send on all interfaces in parallel
                    .forEach(interFace -> Collections.list(interFace.getInetAddresses()).stream()
                            .filter(address -> address instanceof Inet4Address).map(address -> address.getHostAddress())
                            .forEach(ipv4Address -> sendPing(interFace, ipv4Address)));

            Duration sleepDuration = Duration.between(Instant.now(), sleepUntil);
            if (!sleepDuration.isNegative()) {
                try {
                    Thread.sleep(sleepDuration.toMillis());
                } catch (InterruptedException e) {
                    // just return
                }
            }
        } finally {
            discoveryListener = null;
            listenerCountDecreased();
        }
    }

    /**
     * This is a {@link Runnable} 'run()' method which gets executed on the server thread.
     */
    @Override
    public synchronized void run() {
        while (!Thread.currentThread().isInterrupted()) {
            logger.trace("Server thread started");
            try (DatagramSocket socket = new DatagramSocket(RESPONSE_PORT)) {
                serverSocket = socket;
                socket.setReuseAddress(true);
                byte[] buffer = new byte[1024];

                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        if (Thread.currentThread().isInterrupted()) {
                            return; // terminate thread
                        } else {
                            logger.debug("Unexpected receive exception {}", e.getMessage());
                            break; // recycle socket and retry
                        }
                    }

                    String notification = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    String ipAddress = packet.getAddress().toString().replace("/", "");
                    logger.trace("Received notification from {} with content = {}", ipAddress, notification);

                    GoveeHandler handler = thingHandlerListeners.get(ipAddress);
                    boolean isStatusNotification = notification.contains("devStatus");
                    GoveeDiscoveryListener discoveryListener = this.discoveryListener;

                    /*
                     * a Thing handler exists and we have a status notification, so notify the Thing handler
                     * listener
                     */
                    if (handler != null && isStatusNotification) {
                        logger.debug("Sending state notification to {} on {}", handler.getThing().getUID(), ipAddress);
                        handler.handleIncomingStatus(notification);
                        continue;
                    }

                    /*
                     * a Thing handler does not exist, we do not have a status notification, but there is a
                     * discoveryListener, so notify the discovery listener
                     */
                    if (handler == null && !isStatusNotification && discoveryListener != null) {
                        try {
                            DiscoveryResponse response = gson.fromJson(notification, DiscoveryResponse.class);
                            if (response != null) {
                                logger.debug("Notifying potential new Thing discovered on {}", ipAddress);
                                discoveryListener.onDiscoveryResponse(response);
                            }
                        } catch (JsonParseException e) {
                            logger.debug("Failed to parse discovery notification", e);
                        }
                        continue;
                    }

                    /*
                     * none of the above conditions apply so log it
                     */
                    logger.warn(
                            "Unrecognised notification for ipAddress:{}, handler:{}, stateNotification:{}, discoveryListener:{}",
                            ipAddress, handler, isStatusNotification, discoveryListener);
                } // {while}
            } catch (SocketException e) {
                logger.debug("Unexpected socket exception {}", e.getMessage());
            } finally {
                serverSocket = null;
                serverThread = null;
                logger.trace("Server thread finished");
            }
        } // {while}
    }

    /**
     * Get the resolved IP address from the given host name.
     */
    private static String ipAddressFrom(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
        }
        return host;
    }

    /**
     * Call this after one or more listeners have been added.
     * Starts the server thread if it is not already running.
     */
    private void listenerCountIncreased() {
        synchronized (serverLock) {
            Thread thread = serverThread;
            if ((thread == null) || thread.isInterrupted() || !thread.isAlive()) {
                thread = new Thread(this, "OH-binding-" + GoveeBindingConstants.BINDING_ID);
                serverThread = thread;
                thread.start();
            }
        }
    }

    /**
     * Call this after one or more listeners have been removed.
     * Stops the server thread when listener count reaches zero.
     */
    private void listenerCountDecreased() {
        synchronized (serverLock) {
            if (thingHandlerListeners.isEmpty() && (discoveryListener == null)) {
                Thread thread = serverThread;
                DatagramSocket socket = serverSocket;
                if (thread != null) {
                    thread.interrupt(); // set interrupt flag before closing socket
                }
                if (socket != null) {
                    socket.close();
                }
                serverThread = null;
                serverSocket = null;
            }
        }
    }

    /**
     * Send discovery ping multicast on the given network interface and ipv4 address.
     */
    private void sendPing(NetworkInterface interFace, String ipv4Address) {
        try (DatagramChannel channel = (DatagramChannel) DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_TTL, 64)
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, interFace)
                .bind(new InetSocketAddress(ipv4Address, DISCOVERY_PORT)).configureBlocking(false)) {
            channel.send(ByteBuffer.wrap(DISCOVER_REQUEST.getBytes()), DISCOVERY_SOCKET_ADDRESS);
            logger.trace("Sent ping from {}:{} ({}) to {}:{} with content = {}", ipv4Address, DISCOVERY_PORT,
                    interFace.getDisplayName(), DISCOVERY_MULTICAST_ADDRESS, DISCOVERY_PORT, DISCOVER_REQUEST);
        } catch (IOException e) {
            logger.debug("Network error", e);
        }
    }
}
