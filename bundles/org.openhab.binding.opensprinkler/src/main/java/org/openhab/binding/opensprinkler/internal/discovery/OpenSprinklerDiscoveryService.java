/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerDiscoveryService} class allow manual discovery of
 * OpenSprinkler devices.
 *
 * @author Chris Graham - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.opensprinkler")
public class OpenSprinklerDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(OPENSPRINKLER_THING));

    private ExecutorService discoverySearchPool;

    public OpenSprinklerDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_DEFAULT_TIMEOUT_RATE, DISCOVERY_DEFAULT_AUTO_DISCOVER);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting discovery of OpenSprinkler devices.");

        try {
            List<String> ipList = getIpAddressScanList();

            discoverySearchPool = Executors.newFixedThreadPool(DISCOVERY_THREAD_POOL_SIZE);

            for (String ip : ipList) {
                discoverySearchPool.execute(new OpenSprinklerDiscoveryJob(this, ip));
            }

            discoverySearchPool.shutdown();
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
        ThingUID uid = new ThingUID(OPENSPRINKLER_THING, ip.replace('.', '_'));

        HashMap<String, Object> properties = new HashMap<>();

        properties.put("hostname", ip);
        properties.put("port", DEFAULT_API_PORT);
        properties.put("password", DEFAULT_ADMIN_PASSWORD);
        properties.put("refresh", DEFAULT_REFRESH_RATE);

        thingDiscovered(
                DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel("OpenSprinkler").build());
    }

    /**
     * Provide a string list of all the IP addresses associated with the network interfaces on
     * this machine.
     *
     * @return String list of IP addresses.
     * @throws UnknownHostException
     * @throws SocketException
     */
    private List<String> getIpAddressScanList() throws UnknownHostException, SocketException {
        List<String> results = new ArrayList<>();

        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);

        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            InetAddress ipAddress = address.getAddress();

            String cidrSubnet = ipAddress.getHostAddress() + "/" + DISCOVERY_SUBNET_MASK;

            /* Apache Subnet Utils only supports IP v4 for creating string list of IP's */
            if (ipAddress instanceof Inet4Address) {
                logger.debug("Found interface IPv4 address to scan: {}", cidrSubnet);

                SubnetUtils utils = new SubnetUtils(cidrSubnet);

                results.addAll(Arrays.asList(utils.getInfo().getAllAddresses()));
            } else if (ipAddress instanceof Inet6Address) {
                logger.debug("Found interface IPv6 address to scan: {}", cidrSubnet);
            } else {
                logger.debug("Found interface unknown IP type address to scan: {}", cidrSubnet);
            }
        }

        return results;
    }
}
