/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.discovery;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.russound.internal.net.SocketChannelSession;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.WaitingSessionListener;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.system.RioSystemConfig;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * This implementation of {@link DiscoveryService} will scan the network for any Russound RIO system devices. The scan
 * will occur against all network interfaces.
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.russound")
public class RioSystemDiscovery extends AbstractDiscoveryService {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(RioSystemDiscovery.class);

    /** The timeout to connect (in milliseconds) */
    private static final int CONN_TIMEOUT_IN_MS = 100;

    /** The {@link ExecutorService} to use for scanning - will be null if not scanning */
    private ExecutorService executorService = null;

    /** The number of network interfaces being scanned */
    private int nbrNetworkInterfacesScanning = 0;

    /**
     * Creates the system discovery service looking for {@link RioConstants#BRIDGE_TYPE_RIO}. The scan will take at most
     * 120 seconds (depending on how many network interfaces there are)
     */
    public RioSystemDiscovery() {
        super(ImmutableSet.of(RioConstants.BRIDGE_TYPE_RIO), 120);
    }

    /**
     * Starts the scan. For each network interface (that is up and not a loopback), all addresses will be iterated
     * and checked for something open on port 9621. If that port is open, a russound controller "type" command will be
     * issued. If the response is a correct pattern, we assume it's a rio system device and will emit a
     * {{@link #thingDiscovered(DiscoveryResult)}
     */
    @Override
    protected void startScan() {
        final List<NetworkInterface> interfaces;
        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e1) {
            logger.debug("Exception getting network interfaces: {}", e1.getMessage(), e1);
            return;
        }

        nbrNetworkInterfacesScanning = interfaces.size();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

        for (final NetworkInterface networkInterface : interfaces) {
            try {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
            } catch (SocketException e) {
                continue;
            }

            for (Iterator<InterfaceAddress> it = networkInterface.getInterfaceAddresses().iterator(); it.hasNext();) {
                final InterfaceAddress interfaceAddress = it.next();

                // don't bother with ipv6 addresses (russound doesn't support)
                if (interfaceAddress.getAddress() instanceof Inet6Address) {
                    continue;
                }

                final String subnetRange = interfaceAddress.getAddress().getHostAddress() + "/"
                        + interfaceAddress.getNetworkPrefixLength();

                logger.debug("Scanning subnet: {}", subnetRange);
                final SubnetUtils utils = new SubnetUtils(subnetRange);

                final String[] addresses = utils.getInfo().getAllAddresses();

                for (final String address : addresses) {
                    executorService.execute(() -> {
                        scanAddress(address);
                    });
                }
            }
        }

        // Finishes the scan and cleans up
        stopScan();
    }

    /**
     * Stops the scan by terminating the {@link #executorService} and shutting it down
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        try {
            executorService.awaitTermination(CONN_TIMEOUT_IN_MS * nbrNetworkInterfacesScanning, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // shutting down - doesn't matter
        }
        executorService.shutdown();
        executorService = null;

    }

    /**
     * Helper method to scan a specific address. Will open up port 9621 on the address and if opened, query for any
     * controller type (all 6 controllers are tested). If a valid type is found, a discovery result will be created.
     *
     * @param ipAddress a possibly null, possibly empty ip address (null/empty addresses will be ignored)
     */
    private void scanAddress(String ipAddress) {
        if (StringUtils.isEmpty(ipAddress)) {
            return;
        }

        final SocketSession session = new SocketChannelSession(ipAddress, RioConstants.RIO_PORT);
        try {
            final WaitingSessionListener listener = new WaitingSessionListener();
            session.addListener(listener);
            session.connect(CONN_TIMEOUT_IN_MS);
            logger.debug("Connected to port {}:{} - testing to see if RIO", ipAddress, RioConstants.RIO_PORT);

            // can't check for system properties because DMS responds to those -
            // need to check if any controllers are defined
            for (int c = 1; c < 7; c++) {
                session.sendCommand("GET C[" + c + "].type");
                final String resp = listener.getResponse();
                if (resp == null) {
                    continue;
                }
                if (!resp.startsWith("S C[" + c + "].type=\"")) {
                    continue;
                }
                final String type = resp.substring(13, resp.length() - 1);
                if (!StringUtils.isBlank(type)) {
                    logger.debug("Found a RIO type #{}", type);
                    addResult(ipAddress, type);
                    break;
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Connection was interrupted to port {}:{}", ipAddress, RioConstants.RIO_PORT);
        } catch (IOException e) {
            logger.trace("Connection couldn't be established to port {}:{}", ipAddress, RioConstants.RIO_PORT);
        } finally {
            try {
                session.disconnect();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /**
     * Helper method to add our ip address and system type as a discovery result.
     *
     * @param ipAddress a non-null, non-empty ip address
     * @param type a non-null, non-empty model type
     * @throws IllegalArgumentException if ipaddress or type is null or empty
     */
    private void addResult(String ipAddress, String type) {
        if (StringUtils.isEmpty(ipAddress)) {
            throw new IllegalArgumentException("ipAddress cannot be null or empty");
        }
        if (StringUtils.isEmpty(type)) {
            throw new IllegalArgumentException("type cannot be null or empty");
        }

        final Map<String, Object> properties = new HashMap<>(3);
        properties.put(RioSystemConfig.IP_ADDRESS, ipAddress);
        properties.put(RioSystemConfig.PING, 30);
        properties.put(RioSystemConfig.RETRY_POLLING, 10);
        properties.put(RioSystemConfig.SCAN_DEVICE, true);

        final String id = ipAddress.replace(".", "");
        final ThingUID uid = new ThingUID(RioConstants.BRIDGE_TYPE_RIO, id);
        if (uid != null) {
            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Russound " + type).build();
            thingDiscovered(result);
        }

    }
}
