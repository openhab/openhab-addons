/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feican.internal;

import static org.openhab.binding.feican.FeicanBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Feican Bulbs. When sending a discovery UDP broadcast command on port 5000 to a Feican bulb. The
 * bulp will respond with its mac address send via UDP broadcast over port 6000.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true)
public class FeicanDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;
    private static final int RECEIVE_JOB_TIMEOUT = 20000;
    private static final int UDP_PACKET_TIMEOUT = RECEIVE_JOB_TIMEOUT - 50;
    private static final String FEICAN_NAME_PREFIX = "FC_";

    private final Logger logger = LoggerFactory.getLogger(FeicanDiscoveryService.class);

    ///// Network
    private final byte[] buffer = new byte[32];
    @Nullable
    private DatagramSocket discoverSocket;

    public FeicanDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Feican devices.");
        discoverThings();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Feican devices.");
        closeDiscoverSocket();
        super.stopScan();
    }

    /**
     * Performs the actual discovery of Feican devices (things).
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
                if (discoverSocket == null) {
                    break;
                } else {
                    discoverSocket.receive(receivePacket);
                }
                logger.debug("Feican device discovery returned package with length {}", receivePacket.getLength());
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
     * Opens a {@link DatagramSocket} and sends a packet for discovery of Feican devices.
     *
     * @throws SocketException
     * @throws IOException
     */
    private void startDiscoverSocket() throws SocketException, IOException {
        discoverSocket = new DatagramSocket(new InetSocketAddress(Connection.FEICAN_RECEIVE_PORT));
        discoverSocket.setBroadcast(true);
        discoverSocket.setSoTimeout(UDP_PACKET_TIMEOUT);
        final InetAddress broadcast = InetAddress.getByName("255.255.255.255");
        final DatagramPacket discoverPacket = new DatagramPacket(Commands.discover(), Commands.discover().length,
                broadcast, Connection.FEICAN_SEND_PORT);
        discoverSocket.send(discoverPacket);
        if (logger.isTraceEnabled()) {
            logger.trace("Discovery package sent: {}", new String(discoverPacket.getData(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    private void closeDiscoverSocket() {
        if (discoverSocket != null) {
            discoverSocket.close();
            discoverSocket = null;
        }
    }

    /**
     * Register a device (thing) with the discovered properties.
     *
     * @param packet containing data of detected device
     */
    private void thingDiscovered(DatagramPacket packet) {
        final String ipAddress = packet.getAddress().getHostAddress();
        if (packet.getData().length < 12) {
            logger.debug(
                    "Feican device was detected, but the retrieved data is incomplete: '{}'. Device not registered",
                    new String(packet.getData(), 0, packet.getLength() - 1, StandardCharsets.UTF_8));
        } else {
            String thingName = createThingName(packet.getData());
            ThingUID thingUID = new ThingUID(THING_TYPE_BULB, thingName.toLowerCase());
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(thingName)
                    .withProperties(collectProperties(ipAddress, stringToMac(packet.getData(), packet.getLength())))
                    .build());
        }
    }

    /**
     * Creates a name for the Feican device. The name is derived from the mac address (last 4 bytes) and prefixed with
     * FC_. This matches the wifi host it starts when not yet configured.
     *
     * @param byteMac mac address in bytes
     * @return the name for the device
     */
    private String createThingName(final byte[] byteMac) {
        return FEICAN_NAME_PREFIX + new String(byteMac, 8, 4, StandardCharsets.UTF_8);
    }

    /**
     * Converts a byte representation of a mac address to a real mac address.
     *
     * @param stringMac byte representation of a mac address
     * @return real mac address
     */
    private String stringToMac(byte[] data, int length) {
        return new String(data, 0, length - 1, StandardCharsets.UTF_8).replaceAll("(..)(?!$)", "$1:");
    }

    /**
     * Collects properties into a map.
     *
     * @param ipAddress IP address of the thing
     * @param mac mac address of the thing
     * @return map with properties
     */
    private Map<String, Object> collectProperties(String ipAddress, String mac) {
        final Map<String, Object> properties = new TreeMap<>();
        properties.put(CONFIG_IP, ipAddress);
        properties.put(PROPERTY_MAC, mac);
        return properties;
    }
}
