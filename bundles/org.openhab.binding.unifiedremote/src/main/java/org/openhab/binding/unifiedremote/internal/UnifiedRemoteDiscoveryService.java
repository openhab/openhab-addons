/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.unifiedremote.internal;

import static org.openhab.binding.unifiedremote.internal.UnifiedRemoteBindingConstants.*;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UnifiedRemoteDiscoveryService} discover Unified Remote Server Instances in the network.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.unifiedremote")
public class UnifiedRemoteDiscoveryService extends AbstractDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedRemoteDiscoveryService.class);
    static final int TIMEOUT_MS = 20000;
    private static final long DISCOVERY_RESULT_TTL = TimeUnit.MINUTES.toSeconds(5);

    public UnifiedRemoteDiscoveryService() {
        super(UnifiedRemoteBindingConstants.SUPPORTED_THING_TYPES, TIMEOUT_MS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning");
        Thread.currentThread().setName("Unified Remote Discovery thread");
        UnifiedRemoteUdpDiscovery client = new UnifiedRemoteUdpDiscovery();
        client.call(serverInfo -> addNewServer(serverInfo));
    }

    private void addNewServer(UnifiedRemoteUdpDiscovery.ServerInfo serverInfo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_HOSTNAME, serverInfo.host);
        properties.put(PARAMETER_TCP_PORT, serverInfo.tcpPort);
        properties.put(PARAMETER_UDP_PORT, serverInfo.udpPort);
        properties.put(PARAMETER_MAC, serverInfo.macAddress);
        properties.put(PARAMETER_NAME, serverInfo.name);
        thingDiscovered(DiscoveryResultBuilder.create(createThingUID(serverInfo.macAddress))
                .withTTL(DISCOVERY_RESULT_TTL).withProperties(properties).withLabel(serverInfo.name).build());
    }

    private ThingUID createThingUID(String mac) {
        return new ThingUID(THING_TYPE_UNIFIED_SERVER, mac);
    }

    public class UnifiedRemoteUdpDiscovery {
        /**
         * Port used for broadcast and listening.
         */
        public static final int DISCOVERY_PORT = 9511;
        /**
         * String the client sends, to disambiguate packets on this port.
         */
        public static final String DISCOVERY_REQUEST = "6N T|-Ar-A6N T|-Ar-A6N T|-Ar-A";
        /**
         * String the client sends, to disambiguate packets on this port.
         */
        public static final String DISCOVERY_RESPONSE_PREFIX = ")-b@ h): :)i)-b@ h): :)i)-b@ h): :)";
        /**
         * String used to replace non printable characters on service response
         */
        public static final String NON_PRINTABLE_CHARTS_REPLACEMENT = ": :";

        private static final int MAX_PACKET_SIZE = 2048;
        /**
         * maximum time to wait for a reply, in milliseconds.
         */
        private static final int TIMEOUT = 3000; // milliseonds

        public class ServerInfo {
            String name;
            int tcpPort;
            int udpPort;
            String host;
            String macAddress;
            String publicIp;

            ServerInfo(String host, int tcpPort, int udpPort, String name, String macAddress, String publicIp) {
                this.name = name;
                this.tcpPort = tcpPort;
                this.udpPort = udpPort;
                this.host = host;
                this.macAddress = macAddress;
                this.publicIp = publicIp;
            }
        }

        /**
         * Create a UDP socket on the service discovery broadcast port.
         *
         * @return open DatagramSocket if successful
         * @throws RuntimeException if cannot create the socket
         */
        public DatagramSocket createSocket() throws SocketException {
            DatagramSocket socket;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT);
            return socket;
        }

        private ServerInfo tryParseServerDiscovery(DatagramPacket receivePacket) {
            String host = receivePacket.getAddress().getHostAddress();
            try {
                String reply = new String(receivePacket.getData())
                        .replaceAll("[\\p{C}]", NON_PRINTABLE_CHARTS_REPLACEMENT)
                        .replaceAll("[^\\x00-\\x7F]", NON_PRINTABLE_CHARTS_REPLACEMENT);
                if (!reply.startsWith(DISCOVERY_RESPONSE_PREFIX))
                    return null;
                String[] parts = Arrays
                        .stream(reply.replace(DISCOVERY_RESPONSE_PREFIX, "").split(NON_PRINTABLE_CHARTS_REPLACEMENT))
                        .filter((String e) -> e.length() != 0).toArray(String[]::new);
                String name = parts[0];
                int tcpPort = Integer.parseInt(parts[1]);
                int udpPort = Integer.parseInt(parts[3]);
                String macAddress = parts[2];
                String publicIp = parts[4];
                return new ServerInfo(host, tcpPort, udpPort, name, macAddress, publicIp);
            } catch (Exception ex) {
                logger.error("Exception parsing server discovery response from {}: {}", host, ex.getMessage());
                return null;
            }
        }

        /**
         * Send broadcast packets with service request string until a response
         * is received. Return the response as String (even though it should
         * contain an internet address).
         *
         * @return String received from server. Should be server IP address.
         *         Returns empty string if failed to get valid reply.
         */
        public void call(Consumer<ServerInfo> listener) {
            byte[] receiveBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            DatagramSocket socket = null;
            try {
                socket = createSocket();
            } catch (SocketException e) {
                logger.error("Error creating socket: {}", e.getMessage());
                return;
            }
            byte[] packetData = DISCOVERY_REQUEST.getBytes();
            InetAddress broadcastAddress = null;
            try {
                broadcastAddress = InetAddress.getByName("255.255.255.255");
            } catch (UnknownHostException e) {
                /* This should never happen! */ }
            int servicePort = DISCOVERY_PORT;
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, broadcastAddress, servicePort);
            try {
                socket.send(packet);
                logger.debug("Sent packet to {}:{}", broadcastAddress.getHostAddress(), servicePort);
                for (int i = 0; i < 20; i++) {
                    socket.receive(receivePacket);
                    String host = receivePacket.getAddress().getHostAddress();
                    logger.debug("Received reply from {}", host);
                    ServerInfo serverInfo = tryParseServerDiscovery(receivePacket);
                    if (serverInfo != null)
                        listener.accept(serverInfo);
                }
            } catch (SocketTimeoutException ste) {
                logger.debug("SocketTimeoutException during socket operation: {}", ste.getMessage());
            } catch (IOException ioe) {
                logger.error("IOException during socket operation: {}", ioe.getMessage());
            }
            // should close the socket before returning
            if (socket != null)
                socket.close();
        }
    }
}
