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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.govee.internal.model.DiscoveryResponse;
import org.openhab.binding.govee.internal.model.GenericGoveeRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
 */
@NonNullByDefault
@Component(service = CommunicationManager.class)
public class CommunicationManager {
    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
    private final Gson gson = new Gson();
    // Holds a list of all thing handlers to send them thing updates via the receiver-Thread
    private final Map<String, GoveeHandler> thingHandlers = new HashMap<>();
    @Nullable
    private StatusReceiver receiverThread;

    private static final String DISCOVERY_MULTICAST_ADDRESS = "239.255.255.250";
    private static final int DISCOVERY_PORT = 4001;
    private static final int RESPONSE_PORT = 4002;
    private static final int REQUEST_PORT = 4003;

    private static final int INTERFACE_TIMEOUT_SEC = 5;

    private static final String DISCOVER_REQUEST = "{\"msg\": {\"cmd\": \"scan\", \"data\": {\"account_topic\": \"reserve\"}}}";

    public interface DiscoveryResultReceiver {
        void onResultReceived(DiscoveryResponse result);
    }

    @Activate
    public CommunicationManager() {
    }

    public void registerHandler(GoveeHandler handler) {
        synchronized (thingHandlers) {
            thingHandlers.put(handler.getHostname(), handler);
            if (receiverThread == null) {
                receiverThread = new StatusReceiver();
                receiverThread.start();
            }
        }
    }

    public void unregisterHandler(GoveeHandler handler) {
        synchronized (thingHandlers) {
            thingHandlers.remove(handler.getHostname());
            if (thingHandlers.isEmpty()) {
                StatusReceiver receiver = receiverThread;
                if (receiver != null) {
                    receiver.stopReceiving();
                }
                receiverThread = null;
            }
        }
    }

    public void sendRequest(GoveeHandler handler, GenericGoveeRequest request) throws IOException {
        final String hostname = handler.getHostname();
        final DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        final String message = gson.toJson(request);
        final byte[] data = message.getBytes();
        final InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, REQUEST_PORT);
        logger.trace("Sending {} to {}", message, hostname);
        socket.send(packet);
        socket.close();
    }

    public void runDiscoveryForInterface(NetworkInterface intf, DiscoveryResultReceiver receiver) throws IOException {
        synchronized (receiver) {
            StatusReceiver localReceiver = null;
            StatusReceiver activeReceiver = null;

            try {
                if (receiverThread == null) {
                    localReceiver = new StatusReceiver();
                    localReceiver.start();
                    activeReceiver = localReceiver;
                } else {
                    activeReceiver = receiverThread;
                }

                if (activeReceiver != null) {
                    activeReceiver.setDiscoveryResultsReceiver(receiver);
                }

                final InetAddress broadcastAddress = InetAddress.getByName(DISCOVERY_MULTICAST_ADDRESS);
                final InetSocketAddress socketAddress = new InetSocketAddress(broadcastAddress, RESPONSE_PORT);
                final Instant discoveryStartTime = Instant.now();
                final Instant discoveryEndTime = discoveryStartTime.plusSeconds(INTERFACE_TIMEOUT_SEC);

                try (MulticastSocket sendSocket = new MulticastSocket(socketAddress)) {
                    sendSocket.setSoTimeout(INTERFACE_TIMEOUT_SEC * 1000);
                    sendSocket.setReuseAddress(true);
                    sendSocket.setBroadcast(true);
                    sendSocket.setTimeToLive(2);
                    sendSocket.joinGroup(new InetSocketAddress(broadcastAddress, RESPONSE_PORT), intf);

                    byte[] requestData = DISCOVER_REQUEST.getBytes();

                    DatagramPacket request = new DatagramPacket(requestData, requestData.length, broadcastAddress,
                            DISCOVERY_PORT);
                    sendSocket.send(request);
                }

                do {
                    try {
                        receiver.wait(INTERFACE_TIMEOUT_SEC * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } while (Instant.now().isBefore(discoveryEndTime));
            } finally {
                if (activeReceiver != null) {
                    activeReceiver.setDiscoveryResultsReceiver(null);
                }
                if (localReceiver != null) {
                    localReceiver.stopReceiving();
                }
            }
        }
    }

    private class StatusReceiver extends Thread {
        private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
        private boolean stopped = false;
        private @Nullable DiscoveryResultReceiver discoveryResultReceiver;

        private @Nullable MulticastSocket socket;

        StatusReceiver() {
            super("GoveeStatusReceiver");
        }

        synchronized void setDiscoveryResultsReceiver(@Nullable DiscoveryResultReceiver receiver) {
            discoveryResultReceiver = receiver;
        }

        void stopReceiving() {
            stopped = true;
            interrupt();
            if (socket != null) {
                socket.close();
            }

            try {
                join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    socket = new MulticastSocket(RESPONSE_PORT);
                    byte[] buffer = new byte[10240];
                    socket.setReuseAddress(true);
                    while (!stopped) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        if (!socket.isClosed()) {
                            socket.receive(packet);
                        } else {
                            logger.warn("Socket was unexpectedly closed");
                            break;
                        }
                        if (stopped) {
                            break;
                        }

                        String response = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        String deviceIPAddress = packet.getAddress().toString().replace("/", "");
                        logger.trace("Response from {} = {}", deviceIPAddress, response);

                        final DiscoveryResultReceiver discoveryReceiver;
                        synchronized (this) {
                            discoveryReceiver = discoveryResultReceiver;
                        }
                        if (discoveryReceiver != null) {
                            // We're in discovery mode: try to parse result as discovery message and signal the receiver
                            // if parsing was successful
                            try {
                                DiscoveryResponse result = gson.fromJson(response, DiscoveryResponse.class);
                                if (result != null) {
                                    synchronized (discoveryReceiver) {
                                        discoveryReceiver.onResultReceived(result);
                                        discoveryReceiver.notifyAll();
                                    }
                                }
                            } catch (JsonParseException e) {
                                logger.debug(
                                        "JsonParseException when trying to parse the response, probably a status message",
                                        e);
                            }
                        } else {
                            final @Nullable GoveeHandler handler;
                            synchronized (thingHandlers) {
                                handler = thingHandlers.get(deviceIPAddress);
                            }
                            if (handler == null) {
                                logger.warn("thing Handler for {} couldn't be found.", deviceIPAddress);
                            } else {
                                logger.debug("processing status updates for thing {} ", handler.getThing().getLabel());
                                handler.handleIncomingStatus(response);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.warn("exception when receiving status packet", e);
                    // as we haven't received a packet we also don't know where it should have come from
                    // hence, we don't know which thing put offline.
                    // a way to monitor this would be to keep track in a list, which device answers we expect
                    // and supervise an expected answer within a given time but that will make the whole
                    // mechanism much more complicated and may be added in the future
                } finally {
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                }
            }
        }
    }
}
