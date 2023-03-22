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
package org.openhab.binding.globalcache.internal.discovery;

import static org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MulticastListener} class is responsible for listening for the GlobalCache device announcement
 * beacons on the multicast address, and then extracting the data fields out of the received datagram.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MulticastListener {
    private final Logger logger = LoggerFactory.getLogger(MulticastListener.class);

    private MulticastSocket socket;

    private String serialNumber = "";
    private String vendor = "";
    private String model = "";
    private String softwareRevision = "";
    private String hardwareRevision = "";

    // GlobalCache-specific properties defined in this binding
    private String uid = "";
    private String ipAddress = "";
    private String macAddress = "";

    private Date lastUpdate;

    // GC devices announce themselves on a multicast port
    private static final String GC_MULTICAST_GROUP = "239.255.250.250";
    private static final int GC_MULTICAST_PORT = 9131;

    // How long to wait in milliseconds for a discovery beacon
    public static final int DEFAULT_SOCKET_TIMEOUT = 3000;

    /*
     * Constructor joins the multicast group, throws IOException on failure.
     */
    public MulticastListener(String ipv4Address) throws IOException, SocketException {
        InetAddress ifAddress = InetAddress.getByName(ipv4Address);
        NetworkInterface netIF = NetworkInterface.getByInetAddress(ifAddress);
        logger.debug("Discovery job using address {} on network interface {}", ifAddress.getHostAddress(),
                netIF != null ? netIF.getName() : "UNKNOWN");
        socket = new MulticastSocket(GC_MULTICAST_PORT);
        socket.setInterface(ifAddress);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
        InetAddress mcastAddress = InetAddress.getByName(GC_MULTICAST_GROUP);
        socket.joinGroup(mcastAddress);
        logger.debug("Multicast listener joined multicast group {}:{}", GC_MULTICAST_GROUP, GC_MULTICAST_PORT);
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
     * Example iTach beacon:
     *
     * AMXB<-UUID=GlobalCache_000C1E021777><-SDKClass=Utility><-Make=GlobalCache><-Model=iTachWF2IR>
     * <-Revision=710-1001-05><-Pkg_Level=GCPK002><-Config-URL=http://192.168.1.90><-PCB_PN=025-0026-06>
     * <-Status=Ready>CR
     *
     * Example GC-100 beacon:
     *
     * AMXB<-UUID=GC100_000C1E00F0E9_GlobalCache><-SDKClass=Utility><-Make=GlobalCache><-Model=GC-100-06>
     * <-Revision=1.0.0><Config-Name=GC-100><Config-URL=http://192.168.1.70>CR
     *
     */
    private void parseAnnouncementBeacon(DatagramPacket packet) {
        String beacon = (new String(packet.getData())).trim();

        logger.trace("Multicast listener parsing announcement packet: {}", beacon);

        clearProperties();

        if (beacon.contains(GC_MODEL_ITACH)) {
            parseItachAnnouncementBeacon(beacon);
        } else if (beacon.contains(GC_MODEL_GC_100)) {
            parseGC100AnnouncementBeacon(beacon);
        } else if (beacon.contains(GC_MODEL_ZMOTE)) {
            parseZmoteAnnouncementBeacon(beacon);
        } else {
            logger.debug("Multicast listener doesn't know how to parse beacon: {}", beacon);
        }
    }

    private void parseItachAnnouncementBeacon(String beacon) {
        String[] parameterList = beacon.split("<-");

        for (String parameter : parameterList) {
            String[] keyValue = parameter.split("=");

            if (keyValue.length != 2) {
                continue;
            }

            if (keyValue[0].contains("UUID")) {
                uid = keyValue[1].substring(0, keyValue[1].length() - 1);
                macAddress = uid.substring(uid.indexOf("_") + 1);
                serialNumber = macAddress;
            } else if (keyValue[0].contains("Make")) {
                vendor = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Model")) {
                model = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Revision")) {
                softwareRevision = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Config-URL")) {
                ipAddress = keyValue[1].substring(keyValue[1].indexOf("://") + 3, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("PCB_PN")) {
                hardwareRevision = keyValue[1].substring(0, keyValue[1].length() - 1);
            }
        }
        lastUpdate = new Date();
    }

    /*
     * AMXB<-UUID=GC100_000C1E00F0E9_GlobalCache><-SDKClass=Utility><-Make=GlobalCache><-Model=GC-100-06>
     * <-Revision=1.0.0><Config-Name=GC-100><Config-URL=http://192.168.1.70>CR
     */
    private void parseGC100AnnouncementBeacon(String beacon) {
        String[] parameterList = beacon.split("<");

        for (String parameter : parameterList) {
            String[] keyValue = parameter.split("=");

            if (keyValue.length != 2) {
                continue;
            }

            if (keyValue[0].contains("UUID")) {
                uid = keyValue[1].substring(0, keyValue[1].length() - 1);
                macAddress = uid.subSequence(6, 18).toString();
                serialNumber = macAddress;
            } else if (keyValue[0].contains("Make")) {
                vendor = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Model")) {
                model = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Revision")) {
                softwareRevision = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Config-URL")) {
                ipAddress = keyValue[1].substring(keyValue[1].indexOf("://") + 3, keyValue[1].length() - 1);
            }
        }
        hardwareRevision = "N/A";
        lastUpdate = new Date(System.currentTimeMillis());
    }

    /*
     * AMXB<-UUID=CI00a1b2c3><-Type=ZMT2><-Make=zmote.io><-Model=ZV-2><-Revision=2.1.4><-Config-URL=http://192.168.1.12>
     */
    private void parseZmoteAnnouncementBeacon(String beacon) {
        String[] parameterList = beacon.split("<-");

        for (String parameter : parameterList) {
            String[] keyValue = parameter.split("=");
            if (keyValue.length != 2) {
                continue;
            }

            if (keyValue[0].contains("UUID")) {
                uid = keyValue[1].substring(0, keyValue[1].length() - 1);
                serialNumber = uid;
            } else if (keyValue[0].contains("Make")) {
                vendor = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Model")) {
                model = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Revision")) {
                softwareRevision = keyValue[1].substring(0, keyValue[1].length() - 1);
            } else if (keyValue[0].contains("Config-URL")) {
                ipAddress = keyValue[1].substring(keyValue[1].indexOf("://") + 3, keyValue[1].length() - 1);
            }
        }

        hardwareRevision = "N/A";
        lastUpdate = new Date(System.currentTimeMillis());
    }

    private void clearProperties() {
        serialNumber = "";
        vendor = "";
        model = "";
        softwareRevision = "";
        hardwareRevision = "";
        uid = "";
        ipAddress = "";
        macAddress = "";
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public String getUID() {
        return uid;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public String getMACAddress() {
        return macAddress;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public boolean isITach() {
        return model.contains(GC_MODEL_ITACH);
    }

    public boolean isGC100() {
        return model.contains(GC_MODEL_GC_100);
    }

    public ThingTypeUID getThingTypeUID() {
        logger.trace("Multicast listener looking up thing type for model {} at IP {}", model, ipAddress);

        switch (model) {
            case GC_MODEL_ITACHIP2IR:
            case GC_MODEL_ITACHWF2IR:
                return THING_TYPE_ITACH_IR;

            case GC_MODEL_ITACHIP2CC:
            case GC_MODEL_ITACHWF2CC:
                return THING_TYPE_ITACH_CC;

            case GC_MODEL_ITACHIP2SL:
            case GC_MODEL_ITACHWF2SL:
                return THING_TYPE_ITACH_SL;

            case GC_MODEL_ITACHFLEXETH:
            case GC_MODEL_ITACHFLEXETHPOE:
            case GC_MODEL_ITACHFLEXWIFI:
                return THING_TYPE_ITACH_FLEX;

            case GC_MODEL_GC_100_06:
                return THING_TYPE_GC_100_06;

            case GC_MODEL_GC_100_12:
                return THING_TYPE_GC_100_12;

            case GC_MODEL_ZMOTE:
                return THING_TYPE_ZMOTE;

            default:
                return null;
        }
    }
}
