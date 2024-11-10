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
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlDiscover;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeDiscoveryService} is used to discover a Niko Home Control IP-interface in the local
 * network.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.nikohomecontrol")
public class NikoHomeControlBridgeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeDiscoveryService.class);

    private volatile @Nullable ScheduledFuture<?> nhcDiscoveryJob;

    private @NonNullByDefault({}) NetworkAddressService networkAddressService;

    private static final int TIMOUT_S = 5;
    private static final int REFRESH_INTERVAL_S = 60;

    public NikoHomeControlBridgeDiscoveryService() {
        super(NikoHomeControlBindingConstants.BRIDGE_THING_TYPES_UIDS, TIMOUT_S);
        logger.debug("bridge discovery service started");
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    private void discoverBridge() {
        try {
            String broadcastAddr = networkAddressService.getConfiguredBroadcastAddress();
            if (broadcastAddr == null) {
                logger.warn("discovery not possible, no broadcast address found");
                return;
            }
            logger.debug("discovery broadcast on {}", broadcastAddr);
            NikoHomeControlDiscover nhcDiscover = new NikoHomeControlDiscover(broadcastAddr);
            for (String nhcController : nhcDiscover.getNhcBridgeIds()) {
                InetAddress addr = nhcDiscover.getAddr(nhcController);
                if (addr != null) {
                    if (nhcDiscover.isNhcII(nhcController)) {
                        addNhcIIBridge(addr, nhcController);
                    } else {
                        addNhcIBridge(addr, nhcController);
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("bridge discovery IO exception");
        }
    }

    private void addNhcIBridge(InetAddress addr, String bridgeId) {
        logger.debug("NHC I bridge found at {}", addr);

        String bridgeName = "Niko Home Control Bridge";
        ThingUID uid = new ThingUID(BINDING_ID, "bridge", bridgeId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(bridgeName)
                .withProperty(CONFIG_HOST_NAME, addr.getHostAddress()).withProperty(CONFIG_CONTROLLER_ID, bridgeId)
                .withRepresentationProperty(CONFIG_CONTROLLER_ID).build();
        thingDiscovered(discoveryResult);
    }

    private void addNhcIIBridge(InetAddress addr, String bridgeId) {
        logger.debug("NHC II bridge found at {}", addr);

        String bridgeName = "Niko Home Control II Bridge";
        ThingUID uid = new ThingUID(BINDING_ID, "bridge2", bridgeId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(bridgeName)
                .withProperty(CONFIG_HOST_NAME, addr.getHostAddress()).withProperty(CONFIG_CONTROLLER_ID, bridgeId)
                .withRepresentationProperty(CONFIG_CONTROLLER_ID).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        discoverBridge();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start bridge background discovery");
        ScheduledFuture<?> job = nhcDiscoveryJob;
        if (job == null || job.isCancelled()) {
            nhcDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discoverBridge, 0, REFRESH_INTERVAL_S,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop bridge background discovery");
        ScheduledFuture<?> job = nhcDiscoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            nhcDiscoveryJob = null;
        }
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
