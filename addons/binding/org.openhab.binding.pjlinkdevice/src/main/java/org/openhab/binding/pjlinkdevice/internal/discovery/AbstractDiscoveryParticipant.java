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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.pjlinkdevice.internal.PJLinkDeviceBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * @author Nils Schnabel - Initial contribution
 */
public abstract class AbstractDiscoveryParticipant extends AbstractDiscoveryService {
    protected final Logger logger = LoggerFactory.getLogger(DiscoveryParticipantClass1.class);
    private Integer scannedIPcount;
    private ExecutorService executorService;

    public AbstractDiscoveryParticipant(Set<@NonNull ThingTypeUID> supportedThingTypes, int timeout,
            boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
    }

    @Override
    protected void startScan() {
        logger.trace("PJLinkProjectorDiscoveryParticipant startScan");
        Set<InetAddress> addressesToScan = generateAddressesToScan();
        scannedIPcount = 0;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for (InetAddress ip : addressesToScan) {
            executorService.execute(() -> {
                Thread.currentThread().setName("Discovery thread " + ip);
                checkAddress(ip, PJLinkDeviceBindingConstants.DEFAULT_PORT,
                        PJLinkDeviceBindingConstants.DEFAULT_SCAN_TIMEOUT);

                synchronized (scannedIPcount) {
                    scannedIPcount += 1;
                    logger.info("Scanned {} of {} IPs", scannedIPcount, addressesToScan.size());
                    if (scannedIPcount == addressesToScan.size()) {
                        logger.info("Scan of {} IPs successful", scannedIPcount);
                        stopScan();
                    }
                }
            });
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        try {
            executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        executorService.shutdown();
        executorService = null;
    }

    public static ThingUID createServiceUID(String ip, int tcpPort) {
        // uid must not contains dots
        return new ThingUID(PJLinkDeviceBindingConstants.THING_TYPE_PJLINK,
                ip.replace('.', '_') + "_" + String.valueOf(tcpPort));
    }

    protected abstract void checkAddress(InetAddress ip, int tcpPort, int timeout);

    private Set<InetAddress> generateAddressesToScan() {
        try {
            Set<InetAddress> addressesToScan = new HashSet<InetAddress>();
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
            logger.warn("Could not enumerate network interfaces", e);
        }
        return ImmutableSet.of();
    }

    protected abstract void collectAddressesToScan(Set<InetAddress> addressesToScan, InterfaceAddress i);

}
