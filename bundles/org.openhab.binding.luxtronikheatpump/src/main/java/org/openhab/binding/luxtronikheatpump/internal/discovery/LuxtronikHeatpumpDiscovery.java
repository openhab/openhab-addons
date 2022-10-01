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
package org.openhab.binding.luxtronikheatpump.internal.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxtronikheatpump.internal.ChannelUpdaterJob;
import org.openhab.binding.luxtronikheatpump.internal.HeatpumpConnector;
import org.openhab.binding.luxtronikheatpump.internal.LuxtronikHeatpumpBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for Luxtronik heat pumps.
 * As the heat pump seems undiscoverable using mdns or upnp we currently iterate over all
 * IPs and send a socket request on port 8888 / 8889 and detect new heat pumps based on the results.
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        LuxtronikHeatpumpDiscovery.class }, configurationPid = "discovery.luxtronik")
public class LuxtronikHeatpumpDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LuxtronikHeatpumpDiscovery.class);

    /**
     * HTTP read timeout (in milliseconds) - allows us to shutdown the listening every TIMEOUT
     */
    private static final int TIMEOUT_MS = 500;

    /**
     * Timeout in seconds of the complete scan
     */
    private static final int FULL_SCAN_TIMEOUT_SECONDS = 30;

    /**
     * Total number of concurrent threads during scanning.
     */
    private static final int SCAN_THREADS = 10;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    private int octet;
    private int ipMask;
    private int addressCount;
    private @Nullable CidrAddress baseIp;

    /**
     * The {@link ExecutorService} to run the listening threads on.
     */
    private @Nullable ExecutorService executorService;

    /**
     * Constructs the discovery class using the thing IDs that we can discover.
     */
    public LuxtronikHeatpumpDiscovery() {
        super(LuxtronikHeatpumpBindingConstants.SUPPORTED_THING_TYPES_UIDS, FULL_SCAN_TIMEOUT_SECONDS, false);
    }

    private void setupBaseIp(CidrAddress adr) {
        byte[] octets = adr.getAddress().getAddress();
        addressCount = (1 << (32 - adr.getPrefix())) - 2;
        ipMask = 0xFFFFFFFF << (32 - adr.getPrefix());
        octets[0] &= ipMask >> 24;
        octets[1] &= ipMask >> 16;
        octets[2] &= ipMask >> 8;
        octets[3] &= ipMask;
        try {
            InetAddress iAdr = InetAddress.getByAddress(octets);
            baseIp = new CidrAddress(iAdr, (short) adr.getPrefix());
        } catch (UnknownHostException e) {
            logger.debug("Could not build net ip address.", e);
        }
        octet = 0;
    }

    private synchronized String getNextIPAddress(CidrAddress adr) {
        octet++;
        octet &= ~ipMask;
        byte[] octets = adr.getAddress().getAddress();
        octets[2] += (octet >> 8);
        octets[3] += octet;
        String address = "";
        try {
            InetAddress iAdr = null;
            iAdr = InetAddress.getByAddress(octets);
            address = iAdr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.debug("Could not find next ip address.", e);
        }
        return address;
    }

    /**
     * {@inheritDoc}
     *
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Request this hosts first IPV4 address.</li>
     * <li>Send a socket request on port 8888 / 8889 to all IPs on the subnet.</li>
     * <li>The response is then investigated to see if is an answer from a heat pump</li>
     * </ul>
     * The process will continue until all addresses are checked, timeout or {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        if (executorService != null) {
            stopScan();
        }

        CidrAddress localAdr = getLocalIP4Address();
        if (localAdr == null) {
            stopScan();
            return;
        }
        setupBaseIp(localAdr);
        CidrAddress baseAdr = baseIp;
        scanning = true;
        ExecutorService localExecutorService = Executors.newFixedThreadPool(SCAN_THREADS);
        executorService = localExecutorService;
        for (int i = 0; i < addressCount; i++) {

            localExecutorService.execute(() -> {
                if (scanning && baseAdr != null) {
                    String ipAdd = getNextIPAddress(baseAdr);

                    if (!discoverFromIp(ipAdd, 8889)) {
                        discoverFromIp(ipAdd, 8888);
                    }
                }
            });
        }
    }

    private boolean discoverFromIp(String ipAdd, int port) {
        HeatpumpConnector connection = new HeatpumpConnector(ipAdd, port);

        try {
            connection.read();
            Integer[] heatpumpValues = connection.getValues();
            Map<String, Object> properties = ChannelUpdaterJob.getProperties(heatpumpValues);
            properties.put("port", port);

            String type = properties.get("heatpumpType").toString();
            ThingTypeUID typeId = LuxtronikHeatpumpBindingConstants.THING_TYPE_HEATPUMP;
            ThingUID uid = new ThingUID(typeId, type);

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(type)
                    .build();
            thingDiscovered(result);

            return true;
        } catch (IOException e) {
            // no heatpump found on given ip / port
        }

        return false;
    }

    /**
     * Tries to find valid IP4 address.
     *
     * @return An IP4 address or null if none is found.
     */
    private @Nullable CidrAddress getLocalIP4Address() {
        List<CidrAddress> adrList = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> a.getAddress() instanceof Inet4Address).collect(Collectors.toList());

        for (CidrAddress adr : adrList) {
            // Don't return a "fake" DHCP lease.
            if (!adr.toString().startsWith("169.254.")) {
                return adr;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening threads to end naturally
     * within {@link #TIMEOUT_MS) * {@link #SCAN_THREADS} time then shutdown the {@link #executorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        ExecutorService localExecutorService = executorService;
        if (localExecutorService != null) {
            scanning = false;
            try {
                localExecutorService.awaitTermination(TIMEOUT_MS * SCAN_THREADS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.debug("Stop scan interrupted.", e);
            }
            localExecutorService.shutdown();
            executorService = null;
        }
    }
}
