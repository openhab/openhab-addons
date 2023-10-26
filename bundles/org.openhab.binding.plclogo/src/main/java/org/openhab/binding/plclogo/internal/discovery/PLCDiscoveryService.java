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
package org.openhab.binding.plclogo.internal.discovery;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.THING_TYPE_DEVICE;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.model.script.actions.Ping;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PLCDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 * Based on network binding discovery service.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class)
public class PLCDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PLCDiscoveryService.class);
    private static final Set<ThingTypeUID> THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE);

    private static final String LOGO_HOST = "address";
    private static final int LOGO_PORT = 102;

    private static final int CONNECTION_TIMEOUT = 500;
    private static final int DISCOVERY_TIMEOUT = 30;

    private class Runner implements Runnable {
        private final ReentrantLock lock = new ReentrantLock();
        private String host;

        public Runner(final String address) {
            this.host = address;
        }

        @Override
        public void run() {
            try {
                if (Ping.checkVitality(host, LOGO_PORT, CONNECTION_TIMEOUT)) {
                    logger.debug("LOGO! device found at: {}.", host);

                    ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, host.replace('.', '_'));
                    DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                    builder.withProperty(LOGO_HOST, host);
                    builder.withLabel(host);

                    lock.lock();
                    try {
                        thingDiscovered(builder.build());
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException exception) {
                logger.debug("LOGO! device not found at: {}.", host);
            }
        }
    }

    /**
     * Constructor.
     */
    public PLCDiscoveryService() {
        super(THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
    }

    @Override
    protected void startScan() {
        stopScan();

        logger.debug("Start scan for LOGO! bridge");

        Enumeration<NetworkInterface> devices = null;
        try {
            devices = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException exception) {
            logger.warn("LOGO! bridge discovering: {}.", exception.toString());
        }

        Set<String> addresses = new TreeSet<>();
        while ((devices != null) && devices.hasMoreElements()) {
            NetworkInterface device = devices.nextElement();
            try {
                if (!device.isUp() || device.isLoopback()) {
                    continue;
                }
            } catch (SocketException exception) {
                logger.warn("LOGO! bridge discovering: {}.", exception.toString());
            }
            for (InterfaceAddress iface : device.getInterfaceAddresses()) {
                InetAddress address = iface.getAddress();
                if (address instanceof Inet4Address) {
                    String prefix = String.valueOf(iface.getNetworkPrefixLength());
                    SubnetUtils utilities = new SubnetUtils(address.getHostAddress() + "/" + prefix);
                    addresses.addAll(Arrays.asList(utilities.getInfo().getAllAddresses()));
                }
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (String address : addresses) {
            try {
                executor.execute(new Runner(address));
            } catch (RejectedExecutionException exception) {
                logger.warn("LOGO! bridge discovering: {}.", exception.toString());
            }
        }

        try {
            executor.awaitTermination(CONNECTION_TIMEOUT * addresses.size(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            logger.warn("LOGO! bridge discovering: {}.", exception.toString());
        }
        executor.shutdown();

        stopScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop scan for LOGO! bridge");
        super.stopScan();
    }
}
