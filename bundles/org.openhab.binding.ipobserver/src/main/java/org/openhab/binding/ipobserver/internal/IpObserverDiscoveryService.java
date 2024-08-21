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
package org.openhab.binding.ipobserver.internal;

import static org.openhab.binding.ipobserver.internal.IpObserverBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpObserverDiscoveryService} is responsible for finding ipObserver devices.
 *
 * @author Matthew Skinner - Initial contribution.
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.ipobserver")
@NonNullByDefault
public class IpObserverDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_WEATHER_STATION);
    private ExecutorService discoverySearchPool = Executors.newFixedThreadPool(DISCOVERY_THREAD_POOL_SIZE);
    private HttpClient httpClient;

    @Activate
    public IpObserverDiscoveryService(@Reference HttpClientFactory httpClientFactory) {
        super(SUPPORTED_THING_TYPES_UIDS, 240);
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    public void submitDiscoveryResults(String ip) {
        ThingUID thingUID = new ThingUID(THING_WEATHER_STATION, ip.replace('.', '_'));
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("address", ip);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel("Weather Station")
                .withRepresentationProperty("address").build());
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
                    String host = currentIP.getHostAddress();
                    logger.debug("Unknown device was found at: {}", host);
                    discoverySearchPool.execute(new IpObserverDiscoveryJob(this, host));
                }
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void startScan() {
        discoverySearchPool = Executors.newFixedThreadPool(DISCOVERY_THREAD_POOL_SIZE);
        try {
            ipAddressScan();
        } catch (Exception exp) {
            logger.debug("IpObserver discovery service encountered an error while scanning for devices: {}",
                    exp.getMessage());
        }
    }

    @Override
    protected void stopScan() {
        discoverySearchPool.shutdown();
        super.stopScan();
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
