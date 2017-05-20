/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.discovery;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PLCDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PLCDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT = 30;
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DEVICE);

    private static final int CONNECTION_TIMEOUT = 500;
    private TreeSet<String> addresses = new TreeSet<String>();

    private ExecutorService executor = null;

    private class Runner implements Runnable {
        private final ReentrantLock lock = new ReentrantLock();
        private final String host;

        public Runner(final String address) {
            Objects.requireNonNull(address, "IP may not be null");
            this.host = address;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            // gets every ip which can be assigned on the given network
            InetAddress address = null;
            try {
                address = InetAddress.getByName(host);
                if (!address.isReachable(CONNECTION_TIMEOUT / 5)) {
                    address = null;
                }
            } catch (IOException exception) {
                logger.debug("LOGO! device not found at: {}.", host);
                address = null;
            }

            final InetSocketAddress endpoint = new InetSocketAddress(host, 102);
            if (!endpoint.isUnresolved() && (address != null)) {
                Socket socket = new Socket();
                try {
                    socket.connect(endpoint, CONNECTION_TIMEOUT);
                    logger.info("LOGO! device found at: {}.", host);

                    String hostname = address.getHostName();
                    if (!hostname.matches(UID.SEGMENT_PATTERN)) {
                        // Replace invalid char's, since UID has no method for this.
                        hostname = hostname.replaceAll("[^A-Za-z0-9_-]", "_");
                    }

                    final ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, hostname);
                    DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                    builder = builder.withProperty(LOGO_HOST, host);
                    builder = builder.withLabel(hostname);

                    lock.lock();
                    try {
                        thingDiscovered(builder.build());
                    } finally {
                        lock.unlock();
                    }
                } catch (IOException exception) {
                    logger.debug("LOGO! device not found at: {}.", host);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException exception) {
                        logger.error("LOGO! bridge discovering: {}.", exception.toString());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public PLCDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startScan() {
        if (executor != null) {
            stopScan();
        }

        try {
            Enumeration<NetworkInterface> devices = NetworkInterface.getNetworkInterfaces();
            while (devices.hasMoreElements()) {
                NetworkInterface device = devices.nextElement();
                if (device.isLoopback()) {
                    continue;
                }
                for (InterfaceAddress iface : device.getInterfaceAddresses()) {
                    InetAddress address = iface.getAddress();
                    if (address instanceof Inet4Address) {
                        final String prefix = String.valueOf(iface.getNetworkPrefixLength());
                        SubnetUtils utilities = new SubnetUtils(address.getHostAddress() + "/" + prefix);
                        addresses.addAll(Arrays.asList(utilities.getInfo().getAllAddresses()));
                    }
                }
            }
        } catch (SocketException exception) {
            addresses.clear();
            logger.error("LOGO! bridge discovering: {}.", exception.toString());
        }

        executor = Executors.newFixedThreadPool(10 * Runtime.getRuntime().availableProcessors());
        for (String address : addresses) {
            executor.execute(new Runner(address));
        }
        stopScan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop scan for LOGO! bridge");
        super.stopScan();

        if (executor != null) {
            try {
                executor.awaitTermination(CONNECTION_TIMEOUT * addresses.size(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                logger.error("LOGO! bridge discovering: {}.", exception.toString());
            }
            executor.shutdown();
            executor = null;
        }
        addresses.clear();
    }

}
