/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    // Epson-specific properties defined in this binding
    private String uid = "";
    private String ipAddress = "";

    // Epson projector devices announce themselves on a multicast port
    private static final String EPSON_MULTICAST_GROUP = "239.255.250.250";
    private static final int EPSON_MULTICAST_PORT = 9131;

    // How long to wait in milliseconds for a discovery beacon
    public static final int DEFAULT_SOCKET_TIMEOUT_SEC = 3000;

    /*
     * Constructor joins the multicast group, throws IOException on failure.
     */
    public MulticastListener(String ipv4Address) throws IOException, SocketException {
        InetAddress ifAddress = InetAddress.getByName(ipv4Address);
        logger.debug("Discovery job using address {} on network interface {}", ifAddress.getHostAddress(),
                NetworkInterface.getByInetAddress(ifAddress).getName());
        socket = new MulticastSocket(EPSON_MULTICAST_PORT);
        socket.setInterface(ifAddress);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_SEC);
        InetAddress mcastAddress = InetAddress.getByName(EPSON_MULTICAST_GROUP);
        socket.joinGroup(mcastAddress);
        logger.debug("Multicast listener joined multicast group {}:{}", EPSON_MULTICAST_GROUP, EPSON_MULTICAST_PORT);
    }

    public void shutdown() {
        logger.debug("Multicast listener closing down multicast socket");
        socket.close();
    }

    /*
     * Wait on the multicast socket for an announcement beacon. Return false on socket timeout or error.
     * Otherwise, parse the beacon for information about the device.
     */
    public boolean waitForBeacon() throws IOException {
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
            // Get the device properties from the announcement beacon
            parseAnnouncementBeacon(msgPacket);
        }

        return beaconFound;
    }

    /*
     * Parse the announcement beacon into the elements needed to create the thing.
     *
     * Example Epson beacon:
     * AMXB<-UUID=000048746B33><-SDKClass=VideoProjector><-GUID=EPSON_EMP001><-Revision=1.0.0>
     */
    private void parseAnnouncementBeacon(DatagramPacket packet) {
        String beacon = (new String(packet.getData(), StandardCharsets.UTF_8)).trim();

        logger.trace("Multicast listener parsing announcement packet: {}", beacon);

        clearProperties();

        if (beacon.toUpperCase().contains("EPSON") && beacon.toUpperCase().contains("VIDEOPROJECTOR")) {
            ipAddress = packet.getAddress().getHostAddress();
            parseEpsonAnnouncementBeacon(beacon);
        } else {
            logger.debug("Multicast listener doesn't know how to parse beacon: {}", beacon);
        }
    }

    private void parseEpsonAnnouncementBeacon(String beacon) {
        String[] parameterList = beacon.split("<-");

        for (String parameter : parameterList) {
            String[] keyValue = parameter.split("=");

            if (keyValue.length != 2) {
                continue;
            }

            if (keyValue[0].contains("UUID")) {
                uid = keyValue[1].substring(0, keyValue[1].length() - 1);
            }
        }
    }

    private void clearProperties() {
        uid = "";
        ipAddress = "";
    }

    public String getUID() {
        return uid;
    }

    public String getIPAddress() {
        return ipAddress;
    }
}
