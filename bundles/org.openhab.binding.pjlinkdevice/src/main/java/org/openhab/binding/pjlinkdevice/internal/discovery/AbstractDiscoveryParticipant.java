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
package org.openhab.binding.pjlinkdevice.internal.discovery;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pjlinkdevice.internal.PJLinkDeviceBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery of PJLink devices. Checks IP addresses in parallel processing.
 * 
 * Generating IP addresses and checking them is done by the subclasses implementing
 * {@link AbstractDiscoveryParticipant#generateAddressesToScan} and {@link AbstractDiscoveryParticipant#checkAddress}
 * 
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDiscoveryParticipant extends AbstractDiscoveryService {
    protected final Logger logger = LoggerFactory.getLogger(AbstractDiscoveryParticipant.class);
    private Integer scannedIPcount = 0;
    private @Nullable ExecutorService executorService = null;

    public AbstractDiscoveryParticipant(Set<ThingTypeUID> supportedThingTypes, int timeout,
            boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
    }

    protected ExecutorService getExecutorService() {
        ExecutorService executorService = this.executorService;
        if (executorService == null) {
            this.executorService = executorService = Executors
                    .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        return executorService;
    }

    @Override
    protected void startScan() {
        logger.trace("PJLinkProjectorDiscoveryParticipant startScan");
        Set<InetAddress> addressesToScan = generateAddressesToScan();
        scannedIPcount = 0;
        for (InetAddress ip : addressesToScan) {
            getExecutorService().execute(() -> {
                Thread.currentThread().setName("Discovery thread " + ip);
                checkAddress(ip, PJLinkDeviceBindingConstants.DEFAULT_PORT,
                        PJLinkDeviceBindingConstants.DEFAULT_SCAN_TIMEOUT_SECONDS);

                synchronized (scannedIPcount) {
                    scannedIPcount += 1;
                    logger.debug("Scanned {} of {} IPs", scannedIPcount, addressesToScan.size());
                    if (scannedIPcount == addressesToScan.size()) {
                        logger.debug("Scan of {} IPs successful", scannedIPcount);
                        stopScan();
                    }
                }
            });
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        ExecutorService executorService = this.executorService;
        if (executorService == null) {
            return;
        }

        try {
            executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        executorService.shutdown();
    }

    public static ThingUID createServiceUID(String ip, int tcpPort) {
        // uid must not contains dots
        return new ThingUID(PJLinkDeviceBindingConstants.THING_TYPE_PJLINK,
                ip.replace('.', '_') + "_" + String.valueOf(tcpPort));
    }

    protected abstract void checkAddress(InetAddress ip, int tcpPort, int timeout);

    private Set<InetAddress> generateAddressesToScan() {
        try {
            Set<InetAddress> addressesToScan = new HashSet<>();
            ArrayList<NetworkInterface> interfaces = java.util.Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress i : networkInterface.getInterfaceAddresses()) {
                    collectAddressesToScan(addressesToScan, i);
                }
            }
            return addressesToScan;
        } catch (SocketException e) {
            logger.debug("Could not enumerate network interfaces", e);
        }
        return new HashSet<>();
    }

    protected abstract void collectAddressesToScan(Set<InetAddress> addressesToScan, InterfaceAddress i);
}
