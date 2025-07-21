/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
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
 * The {@link CommunicationManager} component implements a sender to send commands to Govee devices,
 * and implements a thread that handles the notifications from all devices. It applies the status
 * information to the right Thing. It supports both discovery and status commands and notifications
 * concurrently.
 *
 * @author Stefan Höhn - Initial contribution
 * @author Danny Baumann - Thread-Safe design refactoring
 * @author Andrew Fiddian-Green - New threading model using java.nio channel
 */
@NonNullByDefault
@Component(service = CommunicationManager.class)
public class CommunicationManager {
    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
    private final Gson gson = new Gson();

    // list of Thing handler listeners that will receive state notifications
    private final Map<String, GoveeHandler> thingHandlerListeners = new ConcurrentHashMap<>();

    private @Nullable GoveeDiscoveryListener discoveryListener;
    private @Nullable Thread serverThread;
    private boolean serverStopFlag = false;

    private final Object paramsLock = new Object();
    private final Object serverLock = new Object();
    private final Object senderLock = new Object();

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
        serverStart();
    }

    @Deactivate
    public void deactivate() {
        thingHandlerListeners.clear();
        discoveryListener = null;
        serverStop();
    }

    /**
     * Thing handlers register themselves to receive state updates when they are initialized.
     */
    public void registerHandler(GoveeHandler handler) {
        thingHandlerListeners.put(ipAddressFrom(handler.getHostname()), handler);
    }

    /**
     * Thing handlers unregister themselves when they are destroyed.
     */
    public void unregisterHandler(GoveeHandler handler) {
        thingHandlerListeners.remove(ipAddressFrom(handler.getHostname()));
    }

    /**
     * Send a unicast command request to the device.
     */
    public void sendRequest(GoveeHandler handler, GenericGoveeRequest request) throws IOException {
        serverStart();
        synchronized (senderLock) {
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
    }

    /**
     * Send discovery multicast pings on any ipv4 address bound to any network interface in the given
     * list and then sleep for sufficient time until responses may have been received.
     */
    public void runDiscoveryForInterfaces(List<NetworkInterface> interfaces, GoveeDiscoveryListener listener) {
        serverStart();
        try {
            discoveryListener = listener;
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
        }
    }

    /**
     * This method gets executed on the server thread. It uses a {@link DatagramChannel} to listen on port
     * 4002 and it processes any notifications received. The task runs continuously in a loop until the
     * thread is externally interrupted.
     *
     * <li>In case of status notifications it forwards the message to the Thing handler listener.</li>
     * <li>In case of discovery notifications it forwards the message to the discovery listener.</li>
     * <li>If there is neither a Thing handler listener, nor a discovery listener, it logs an error.</li>
     */
    private void serverThreadTask() {
        synchronized (serverLock) {
            try {
                logger.trace("Server thread started.");
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                while (!serverStopFlag) {
                    try (DatagramChannel channel = DatagramChannel.open()
                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                            .bind(new InetSocketAddress(RESPONSE_PORT))) {
                        while (!serverStopFlag) {
                            String sourceIp = "";
                            try {
                                SocketAddress socketAddress = channel.receive(buffer.clear());
                                if ((socketAddress instanceof InetSocketAddress inetSocketAddress)
                                        && (inetSocketAddress.getAddress() instanceof InetAddress inetAddress)) {
                                    sourceIp = inetAddress.getHostAddress();
                                } else {
                                    logger.debug("Receive() - bad socketAddress={}", socketAddress);
                                    return;
                                }
                            } catch (ClosedByInterruptException e) {
                                // thrown if 'Thread.interrupt()' is called during 'channel.receive()'
                                logger.debug("Receive ClosedByInterruptException, isInterrupted={}, serverStopFlag={}",
                                        Thread.currentThread().isInterrupted(), serverStopFlag);
                                Thread.interrupted(); // clear 'interrupted' flag
                                break;
                            } catch (IOException e) {
                                logger.debug("Receive unexpected exception={}", e.getMessage());
                                break;
                            }

                            String message = new String(buffer.array(), 0, buffer.position());
                            logger.trace("Receive from sourceIp={}, message={}", sourceIp, message);

                            GoveeHandler handler = thingHandlerListeners.get(sourceIp);
                            boolean devStatus = message.contains("devStatus");
                            if (handler != null && devStatus) {
                                logger.debug("Notifying status of thing={} on sourcecIp={}",
                                        handler.getThing().getUID(), sourceIp);
                                handler.handleIncomingStatus(message);
                                continue;
                            }

                            GoveeDiscoveryListener discoveryListener = this.discoveryListener;
                            if (!devStatus && discoveryListener != null) {
                                try {
                                    DiscoveryResponse response = gson.fromJson(message, DiscoveryResponse.class);
                                    if (response != null) {
                                        logger.debug("Notifying discovery of device on sourceIp={}", sourceIp);
                                        discoveryListener.onDiscoveryResponse(response);
                                    }
                                } catch (JsonParseException e) {
                                    logger.debug("Discovery notification parse exception={}", e.getMessage());
                                }
                                continue;
                            }

                            logger.warn(
                                    "Unhandled message with sourceIp={}, devStatus={}, handler={}, discoveryListener={}",
                                    sourceIp, devStatus, handler, discoveryListener);
                        } // end of inner while loop
                    } catch (IOException e) {
                        logger.debug("Datagram channel create exception={}", e.getMessage());
                    }
                } // end of outer while loop
            } finally {
                serverThread = null;
                serverStopFlag = false;
                logger.trace("Server thread terminated.");
            }
        }
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
     * Starts the server thread if it is not already running.
     */
    private void serverStart() {
        synchronized (paramsLock) {
            Thread serverthread = serverThread;
            if (serverthread == null) {
                serverthread = new Thread(this::serverThreadTask, "OH-binding-" + GoveeBindingConstants.BINDING_ID);
                serverThread = serverthread;
                serverStopFlag = false;
                serverthread.start();
            }
        }
    }

    /**
     * Stops the server thread.
     */
    private void serverStop() {
        synchronized (paramsLock) {
            serverStopFlag = true;
            Thread serverthread = serverThread;
            if (serverthread != null) {
                serverthread.interrupt();
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
