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
package org.openhab.binding.epsonprojector.internal.discovery;

import static org.openhab.binding.epsonprojector.internal.EpsonProjectorBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MulticastListener} class is responsible for listening for the Epson projector device announcement
 * beacons on the multicast address, and then extracting the data fields out of the received datagram.
 *
 * @author Mark Hilbush - Initial contribution
 * @author Michael Lobstein - Adapted for the Epson Projector binding
 */
@NonNullByDefault
public class MulticastListener {
    private final Logger logger = LoggerFactory.getLogger(MulticastListener.class);

    private MulticastSocket socket;
    private InetSocketAddress inetSocketAddress;

    // Epson projector devices announce themselves on the AMX DDD multicast port
    private static final String AMX_MULTICAST_GROUP = "239.255.250.250";
    private static final int AMX_MULTICAST_PORT = 9131;

    // How long to wait in milliseconds for a discovery beacon
    public static final int DEFAULT_SOCKET_TIMEOUT_SEC = 3000;

    /*
     * Constructor joins the multicast group, throws IOException on failure.
     */
    public MulticastListener(String ipv4Address) throws IOException, SocketException {
        InetAddress ifAddress = InetAddress.getByName(ipv4Address);
        NetworkInterface networkInterface = getMulticastInterface(ipv4Address);
        logger.debug("Discovery job using address {} on network interface {}", ifAddress.getHostAddress(),
                networkInterface.getName());
        socket = new MulticastSocket(AMX_MULTICAST_PORT);
        socket.setNetworkInterface(networkInterface);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_SEC);
        inetSocketAddress = new InetSocketAddress(InetAddress.getByName(AMX_MULTICAST_GROUP), AMX_MULTICAST_PORT);
        socket.joinGroup(inetSocketAddress, null);
        logger.debug("Multicast listener joined multicast group {}:{}", AMX_MULTICAST_GROUP, AMX_MULTICAST_PORT);
    }

    public void shutdown() {
        logger.debug("Multicast listener closing down multicast socket");
        try {
            socket.leaveGroup(inetSocketAddress, null);
            socket.close();
        } catch (IOException e) {
            logger.debug("Exception shutting down multicast socket: {}", e.getMessage());
        }
    }

    /*
     * Wait on the multicast socket for an announcement beacon. Return null on socket timeout or error.
     * Otherwise, parse the beacon for information about the device and return the device properties.
     */
    public @Nullable Map<String, Object> waitForBeacon() throws IOException {
        byte[] bytes = new byte[600];
        boolean beaconFound;

        // Wait for a device to announce itself
        logger.trace("Multicast listener waiting for datagram on multicast port");
        DatagramPacket msgPacket = new DatagramPacket(bytes, bytes.length);
        try {
            socket.receive(msgPacket);
            beaconFound = true;
            logger.trace("Multicast listener got datagram of length {} from multicast port: {}", msgPacket.getLength(),
                    msgPacket.toString());
        } catch (SocketTimeoutException e) {
            beaconFound = false;
        }

        if (beaconFound) {
            // Return the device properties from the announcement beacon
            return parseAnnouncementBeacon(msgPacket);
        }

        return null;
    }

    /*
     * Parse the announcement beacon into the elements needed to create the thing.
     *
     * Example Epson beacon:
     * AMXB<-UUID=000048746B33><-SDKClass=VideoProjector><-GUID=EPSON_EMP001><-Revision=1.0.0>
     */
    private @Nullable Map<String, Object> parseAnnouncementBeacon(DatagramPacket packet) {
        String beacon = (new String(packet.getData(), StandardCharsets.UTF_8)).trim();
        logger.trace("Multicast listener parsing announcement packet: {}", beacon);

        if (beacon.toUpperCase(Locale.ENGLISH).contains("EPSON") && beacon.contains("VideoProjector")) {
            String[] parameterList = beacon.replace(">", "").split("<-");

            for (String parameter : parameterList) {
                String[] keyValue = parameter.split("=");

                if (keyValue.length == 2 && keyValue[0].contains("UUID") && !keyValue[1].isEmpty()) {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(Thing.PROPERTY_MAC_ADDRESS, keyValue[1]);
                    properties.put(THING_PROPERTY_HOST, packet.getAddress().getHostAddress());
                    properties.put(THING_PROPERTY_PORT, DEFAULT_PORT);
                    return properties;
                }
            }
            logger.debug("Multicast listener doesn't know how to parse beacon: {}", beacon);
        }
        return null;
    }

    private NetworkInterface getMulticastInterface(String interfaceIpAddress) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        NetworkInterface networkInterface;
        while (networkInterfaces.hasMoreElements()) {
            networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found interface address {} -> {}", interfaceAddress.toString(),
                            interfaceAddress.getAddress().toString());
                }
                if (interfaceAddress.getAddress().toString().endsWith("/" + interfaceIpAddress)) {
                    return networkInterface;
                }
            }
        }
        throw new SocketException("Unable to get network interface for " + interfaceIpAddress);
    }
}
