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
package org.openhab.binding.mideaac.internal.discovery;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.dto.CloudProviderDTO;
import org.openhab.binding.mideaac.internal.handler.CommandBase;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaACDiscoveryService} service for Midea AC.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - OH naming conventions
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.mideaac")
public class MideaACDiscoveryService extends AbstractDiscoveryService {

    private static int discoveryTimeoutSeconds = 5;
    private final int receiveJobTimeout = 20000;
    private final int udpPacketTimeout = receiveJobTimeout - 50;
    private final String mideaacNamePrefix = "MideaAC";

    private final Logger logger = LoggerFactory.getLogger(MideaACDiscoveryService.class);

    ///// Network
    private byte[] buffer = new byte[512];
    @Nullable
    private DatagramSocket discoverSocket;

    @Nullable
    DiscoveryHandler discoveryHandler;

    private Security security;

    /**
     * Discovery Service
     */
    public MideaACDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, discoveryTimeoutSeconds, false);
        this.security = new Security(CloudProviderDTO.getCloudProvider(""));
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Midea AC devices.");
        discoverThings();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Midea AC devices.");
        closeDiscoverSocket();
        super.stopScan();
    }

    /**
     * Performs the actual discovery of Midea AC devices (things).
     */
    private void discoverThings() {
        try {
            final DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            // No need to call close first, because the caller of this method already has done it.
            startDiscoverSocket();
            // Runs until the socket call gets a time out and throws an exception. When a time out is triggered it means
            // no data was present and nothing new to discover.
            while (true) {
                // Set packet length in case a previous call reduced the size.
                receivePacket.setLength(buffer.length);
                DatagramSocket discoverSocket = this.discoverSocket;
                if (discoverSocket == null) {
                    break;
                } else {
                    discoverSocket.receive(receivePacket);
                }
                logger.debug("Midea AC device discovery returned package with length {}", receivePacket.getLength());
                if (receivePacket.getLength() > 0) {
                    thingDiscovered(receivePacket);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovering poller timeout...");
        } catch (IOException e) {
            logger.debug("Error during discovery: {}", e.getMessage());
        } finally {
            closeDiscoverSocket();
            removeOlderResults(getTimestampOfLastScan());
        }
    }

    /**
     * Performs the actual discovery of a specific Midea AC device (thing)
     * 
     * @param ipAddress IP Address
     * @param discoveryHandler Discovery Handler
     */
    public void discoverThing(String ipAddress, DiscoveryHandler discoveryHandler) {
        try {
            final DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            // No need to call close first, because the caller of this method already has done it.
            startDiscoverSocket(ipAddress, discoveryHandler);
            // Runs until the socket call gets a time out and throws an exception. When a time out is triggered it means
            // no data was present and nothing new to discover.
            while (true) {
                // Set packet length in case a previous call reduced the size.
                receivePacket.setLength(buffer.length);
                DatagramSocket discoverSocket = this.discoverSocket;
                if (discoverSocket == null) {
                    break;
                } else {
                    discoverSocket.receive(receivePacket);
                }
                logger.debug("Midea AC device discovery returned package with length {}", receivePacket.getLength());
                if (receivePacket.getLength() > 0) {
                    thingDiscovered(receivePacket);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovering poller timeout...");
        } catch (IOException e) {
            logger.debug("Error during discovery: {}", e.getMessage());
        } finally {
            closeDiscoverSocket();
        }
    }

    /**
     * Opens a {@link DatagramSocket} and sends a packet for discovery of Midea AC devices.
     *
     * @throws SocketException
     * @throws IOException
     */
    private void startDiscoverSocket() throws SocketException, IOException {
        startDiscoverSocket("255.255.255.255", null);
    }

    /**
     * Start the discovery Socket
     * 
     * @param ipAddress broadcast IP Address
     * @param discoveryHandler Discovery handler
     * @throws SocketException Socket Exception
     * @throws IOException IO Exception
     */
    public void startDiscoverSocket(String ipAddress, @Nullable DiscoveryHandler discoveryHandler)
            throws SocketException, IOException {
        logger.trace("Discovering: {}", ipAddress);
        this.discoveryHandler = discoveryHandler;
        discoverSocket = new DatagramSocket(new InetSocketAddress(Connection.MIDEAAC_RECEIVE_PORT));
        DatagramSocket discoverSocket = this.discoverSocket;
        if (discoverSocket != null) {
            discoverSocket.setBroadcast(true);
            discoverSocket.setSoTimeout(udpPacketTimeout);
            final InetAddress broadcast = InetAddress.getByName(ipAddress);
            {
                final DatagramPacket discoverPacket = new DatagramPacket(CommandBase.discover(),
                        CommandBase.discover().length, broadcast, Connection.MIDEAAC_SEND_PORT1);
                discoverSocket.send(discoverPacket);
                logger.trace("Broadcast discovery package sent to port: {}", Connection.MIDEAAC_SEND_PORT1);
            }
            {
                final DatagramPacket discoverPacket = new DatagramPacket(CommandBase.discover(),
                        CommandBase.discover().length, broadcast, Connection.MIDEAAC_SEND_PORT2);
                discoverSocket.send(discoverPacket);
                logger.trace("Broadcast discovery package sent to port: {}", Connection.MIDEAAC_SEND_PORT2);
            }
        }
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    private void closeDiscoverSocket() {
        DatagramSocket discoverSocket = this.discoverSocket;
        if (discoverSocket != null) {
            discoverSocket.close();
            this.discoverSocket = null;
        }
    }

    /**
     * Register a device (thing) with the discovered properties.
     *
     * @param packet containing data of detected device
     */
    private void thingDiscovered(DatagramPacket packet) {
        DiscoveryResult dr = discoveryPacketReceived(packet);
        if (dr != null) {
            DiscoveryHandler discoveryHandler = this.discoveryHandler;
            if (discoveryHandler != null) {
                discoveryHandler.discovered(dr);
            } else {
                thingDiscovered(dr);
            }
        }
    }

    /**
     * Parses the packet to extract the device properties
     * 
     * @param packet returned paket from device
     * @return extracted device properties
     */
    @Nullable
    public DiscoveryResult discoveryPacketReceived(DatagramPacket packet) {
        final String ipAddress = packet.getAddress().getHostAddress();
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

        logger.debug("Midea AC discover data ({}) from {}: '{}'", data.length, ipAddress, Utils.bytesToHex(data));

        if (data.length >= 104 && (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("5A5A")
                || Utils.bytesToHex(Arrays.copyOfRange(data, 8, 10)).equals("5A5A"))) {
            logger.trace("Device supported");
            String mSmartId, mSmartVersion = "", mSmartip = "", mSmartPort = "", mSmartSN = "", mSmartSSID = "",
                    mSmartType = "";
            if (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("5A5A")) {
                mSmartVersion = "2";
            }
            if (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("8370")) {
                mSmartVersion = "3";
            }
            if (Utils.bytesToHex(Arrays.copyOfRange(data, 8, 10)).equals("5A5A")) {
                data = Arrays.copyOfRange(data, 8, data.length - 16);
            }

            logger.trace("Version: {}", mSmartVersion);

            byte[] id = Arrays.copyOfRange(data, 20, 26);
            logger.trace("Id Bytes: {}", Utils.bytesToHex(id));

            byte[] idReverse = Utils.reverse(id);

            BigInteger bigId = new BigInteger(1, idReverse);
            mSmartId = bigId.toString(10);

            logger.debug("Id: '{}'", mSmartId);

            byte[] encryptData = Arrays.copyOfRange(data, 40, data.length - 16);
            logger.debug("Encrypt data: '{}'", Utils.bytesToHex(encryptData));

            byte[] reply = security.aesDecrypt(encryptData);
            logger.debug("Length: {}, Reply: '{}'", reply.length, Utils.bytesToHex(reply));

            mSmartip = Byte.toUnsignedInt(reply[3]) + "." + Byte.toUnsignedInt(reply[2]) + "."
                    + Byte.toUnsignedInt(reply[1]) + "." + Byte.toUnsignedInt(reply[0]);
            logger.debug("IP: '{}'", mSmartip);

            byte[] portIdBytes = Utils.reverse(Arrays.copyOfRange(reply, 4, 8));
            BigInteger portId = new BigInteger(1, portIdBytes);
            mSmartPort = portId.toString(10);
            logger.debug("Port: '{}'", mSmartPort);

            mSmartSN = new String(reply, 8, 40 - 8, StandardCharsets.UTF_8);
            logger.debug("SN: '{}'", mSmartSN);

            logger.trace("SSID length: '{}'", Byte.toUnsignedInt(reply[40]));

            mSmartSSID = new String(reply, 41, reply[40], StandardCharsets.UTF_8);
            logger.debug("SSID: '{}'", mSmartSSID);

            mSmartType = mSmartSSID.split("_")[1];
            logger.debug("Type: '{}'", mSmartType);

            String thingName = createThingName(packet.getAddress().getAddress(), mSmartId);
            ThingUID thingUID = new ThingUID(THING_TYPE_MIDEAAC, thingName.toLowerCase());

            return DiscoveryResultBuilder.create(thingUID).withLabel(thingName)
                    .withRepresentationProperty(CONFIG_IP_ADDRESS).withThingType(THING_TYPE_MIDEAAC)
                    .withProperties(collectProperties(ipAddress, mSmartVersion, mSmartId, mSmartPort, mSmartSN,
                            mSmartSSID, mSmartType))
                    .build();
        } else if (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 6)).equals("3C3F786D6C20")) {
            logger.debug("Midea AC v1 device was detected, supported, but not implemented yet.");
            return null;
        } else {
            logger.debug(
                    "Midea AC device was detected, but the retrieved data is incomplete or not supported. Device not registered");
            return null;
        }
    }

    /**
     * Creates a OH name for the Midea AC device.
     * 
     * @return the name for the device
     */
    private String createThingName(final byte[] byteIP, String id) {
        return mideaacNamePrefix + "-" + Byte.toUnsignedInt(byteIP[3]) + "-" + id;
    }

    /**
     * Collects properties into a map.
     *
     * @param ipAddress IP address of the thing
     * @param version Version 2 or 3
     * @param id ID of the device
     * @param port Port of the device
     * @param sn Serial number of the device
     * @param ssid Serial id converted with StandardCharsets.UTF_8
     * @param type Type of device (ac)
     * @return Map with properties
     */
    private Map<String, Object> collectProperties(String ipAddress, String version, String id, String port, String sn,
            String ssid, String type) {
        Map<String, Object> properties = new TreeMap<>();
        properties.put(CONFIG_IP_ADDRESS, ipAddress);
        properties.put(CONFIG_IP_PORT, port);
        properties.put(CONFIG_DEVICEID, id);
        properties.put(PROPERTY_VERSION, version);
        properties.put(PROPERTY_SN, sn);
        properties.put(PROPERTY_SSID, ssid);
        properties.put(PROPERTY_TYPE, type);

        return properties;
    }
}
