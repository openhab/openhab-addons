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
package org.openhab.binding.unifiedremote.internal;

import static org.openhab.binding.unifiedremote.internal.UnifiedRemoteBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UnifiedRemoteDiscoveryService} discover Unified Remote Server Instances in the network.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.unifiedremote")
@NonNullByDefault
public class UnifiedRemoteDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(UnifiedRemoteDiscoveryService.class);
    static final int TIMEOUT_MS = 20000;
    private static final long DISCOVERY_RESULT_TTL_SEC = TimeUnit.MINUTES.toSeconds(5);

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
    private static final int SOCKET_TIMEOUT_MS = 3000;

    public UnifiedRemoteDiscoveryService() {
        super(SUPPORTED_THING_TYPES, TIMEOUT_MS, false);
    }

    @Override
    protected void startScan() {
        sendBroadcast(this::addNewServer);
    }

    private void addNewServer(ServerInfo serverInfo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_MAC_ADDRESS, serverInfo.macAddress);
        properties.put(PARAMETER_HOSTNAME, serverInfo.host);
        properties.put(PARAMETER_TCP_PORT, serverInfo.tcpPort);
        properties.put(PARAMETER_UDP_PORT, serverInfo.udpPort);
        thingDiscovered(
                DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_UNIFIED_REMOTE_SERVER, serverInfo.macAddress))
                        .withTTL(DISCOVERY_RESULT_TTL_SEC).withRepresentationProperty(PARAMETER_MAC_ADDRESS)
                        .withProperties(properties).withLabel(serverInfo.name).build());
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
        socket.setSoTimeout(TIMEOUT_MS);
        return socket;
    }

    private ServerInfo tryParseServerDiscovery(DatagramPacket receivePacket) throws ParseException {
        String host = receivePacket.getAddress().getHostAddress();
        String reply = new String(receivePacket.getData()).replaceAll("[\\p{C}]", NON_PRINTABLE_CHARTS_REPLACEMENT)
                .replaceAll("[^\\x00-\\x7F]", NON_PRINTABLE_CHARTS_REPLACEMENT);
        if (!reply.startsWith(DISCOVERY_RESPONSE_PREFIX)) {
            throw new ParseException("Bad discovery response prefix", 0);
        }
        String[] parts = Arrays
                .stream(reply.replace(DISCOVERY_RESPONSE_PREFIX, "").split(NON_PRINTABLE_CHARTS_REPLACEMENT))
                .filter((String e) -> e.length() != 0).toArray(String[]::new);
        String name = parts[0];
        int tcpPort = Integer.parseInt(parts[1]);
        int udpPort = Integer.parseInt(parts[3]);
        String macAddress = parts[2];
        return new ServerInfo(host, tcpPort, udpPort, name, macAddress);
    }

    /**
     * Send broadcast packets with service request string until a response
     * is received.
     *
     * @param listener Listener to process the String received from server. Should be server IP address.
     * 
     */
    public void sendBroadcast(Consumer<ServerInfo> listener) {
        byte[] receiveBuffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        DatagramSocket socket = null;
        try {
            socket = createSocket();
        } catch (SocketException e) {
            logger.debug("Error creating discovery socket: {}", e.getMessage());
            return;
        }
        byte[] packetData = DISCOVERY_REQUEST.getBytes();
        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            int servicePort = DISCOVERY_PORT;
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, broadcastAddress, servicePort);
            socket.send(packet);
            logger.debug("Sent packet to {}:{}", broadcastAddress.getHostAddress(), servicePort);
            for (int i = 0; i < 20; i++) {
                socket.receive(receivePacket);
                String host = receivePacket.getAddress().getHostAddress();
                logger.debug("Received reply from {}", host);
                try {
                    ServerInfo serverInfo = tryParseServerDiscovery(receivePacket);
                    listener.accept(serverInfo);
                } catch (ParseException ex) {
                    logger.debug("Unable to parse server discovery response from {}: {}", host, ex.getMessage());
                }
            }
        } catch (SocketTimeoutException ste) {
            logger.debug("SocketTimeoutException during socket operation: {}", ste.getMessage());
        } catch (IOException ioe) {
            logger.debug("IOException during socket operation: {}", ioe.getMessage());
        } finally {
            socket.close();
        }
    }

    public class ServerInfo {
        String name;
        int tcpPort;
        int udpPort;
        String host;
        String macAddress;

        ServerInfo(String host, int tcpPort, int udpPort, String name, String macAddress) {
            this.name = name;
            this.tcpPort = tcpPort;
            this.udpPort = udpPort;
            this.host = host;
            this.macAddress = macAddress;
        }
    }
}
