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
package org.openhab.binding.opensprinkler.internal.discovery;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerDiscoveryService} class allow manual discovery of
 * OpenSprinkler devices.
 *
 * @author Chris Graham - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.opensprinkler")
@NonNullByDefault
public class OpenSprinklerDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(OPENSPRINKLER_HTTP_BRIDGE));
    private ExecutorService discoverySearchPool = scheduler;
    private OpenSprinklerApiFactory apiFactory;

    @Activate
    public OpenSprinklerDiscoveryService(@Reference OpenSprinklerApiFactory apiFactory) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_DEFAULT_TIMEOUT_RATE, DISCOVERY_DEFAULT_AUTO_DISCOVER);
        this.apiFactory = apiFactory;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    OpenSprinklerApiFactory getApiFactory() {
        return this.apiFactory;
    }

    @Override
    protected void startScan() {
        discoverySearchPool = Executors.newFixedThreadPool(DISCOVERY_THREAD_POOL_SIZE);
        try {
            ipAddressScan();
        } catch (Exception exp) {
            logger.debug("OpenSprinkler discovery service encountered an error while scanning for devices: {}",
                    exp.getMessage());
        }
        logger.debug("Completed discovery of OpenSprinkler devices.");
    }

    /**
     * Create a new Thing with an IP address given. Uses default port and password.
     *
     * @param ip IP address of the OpenSprinkler device as a string.
     */
    public void submitDiscoveryResults(String ip) {
        ThingUID bridgeUID = new ThingUID(OPENSPRINKLER_HTTP_BRIDGE, ip.replace('.', '_'));
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hostname", ip);
        properties.put("port", 80);
        properties.put("password", DEFAULT_ADMIN_PASSWORD);
        properties.put("refresh", 60);
        thingDiscovered(DiscoveryResultBuilder.create(bridgeUID).withProperties(properties)
                .withLabel("OpenSprinkler HTTP Bridge").withRepresentationProperty("hostname").build());
        // Now create the Device thing
        properties.clear();
        properties.put("hostname", ip);
        ThingUID uid = new ThingUID(OPENSPRINKLER_DEVICE, bridgeUID, ip.replace('.', '_'));
        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withRepresentationProperty("hostname").withLabel("OpenSprinkler Device").build());
    }

    private void scanSingleSubnet(InterfaceAddress hostAddress) {
        byte[] broadcastAddress = hostAddress.getBroadcast().getAddress();
        // Create subnet mask from length
        int shft = 0xffffffff << (32 - hostAddress.getNetworkPrefixLength());
        byte oct1 = (byte) (((byte) ((shft & 0xff000000) >> 24)) & 0xff);
        byte oct2 = (byte) (((byte) ((shft & 0x00ff0000) >> 16)) & 0xff);
        byte oct3 = (byte) (((byte) ((shft & 0x0000ff00) >> 8)) & 0xff);
        byte oct4 = (byte) (((byte) (shft & 0x000000ff)) & 0xff);
        byte[] subnetMask = new byte[] { oct1, oct2, oct3, oct4 };
        // calc first IP to start scanning from on this subnet
        byte[] startAddress = new byte[4];
        startAddress[0] = (byte) (broadcastAddress[0] & subnetMask[0]);
        startAddress[1] = (byte) (broadcastAddress[1] & subnetMask[1]);
        startAddress[2] = (byte) (broadcastAddress[2] & subnetMask[2]);
        startAddress[3] = (byte) (broadcastAddress[3] & subnetMask[3]);
        // Loop from start of subnet to the broadcast address.
        for (int i = ByteBuffer.wrap(startAddress).getInt(); i < ByteBuffer.wrap(broadcastAddress).getInt(); i++) {
            try {
                InetAddress currentIP = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(i).array());
                // Try to reach each IP with a timeout of 500ms which is enough for local network
                if (currentIP.isReachable(500)) {
                    String host = currentIP.getHostAddress().toString();
                    logger.debug("Unknown device was found at: {}", host);
                    discoverySearchPool.execute(new OpenSprinklerDiscoveryJob(this, host));
                }
            } catch (IOException e) {
            }
        }
    }

    private void ipAddressScan() {
        try {
            for (Enumeration<NetworkInterface> enumNetworks = NetworkInterface.getNetworkInterfaces(); enumNetworks
                    .hasMoreElements();) {
                NetworkInterface networkInterface = enumNetworks.nextElement();
                List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress hostAddress : list) {
                    InetAddress inetAddress = hostAddress.getAddress();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        logger.debug("Scanning all IP address's that IP {}/{} is on", hostAddress.getAddress(),
                                hostAddress.getNetworkPrefixLength());
                        scanSingleSubnet(hostAddress);
                    }
                }
            }
        } catch (SocketException ex) {
        }
    }
}
