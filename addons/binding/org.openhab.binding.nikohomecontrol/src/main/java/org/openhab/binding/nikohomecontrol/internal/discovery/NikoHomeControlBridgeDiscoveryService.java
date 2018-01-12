/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlDiscover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeDiscoveryService} is used to discover a Niko Home Control IP-interface in the local
 * network.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlBridgeDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeDiscoveryService.class);

    private ScheduledFuture<?> nhcDiscoveryJob;

    private static final int TIMEOUT = 5;
    private static final int REFRESH_INTERVAL = 60;

    public NikoHomeControlBridgeDiscoveryService() {
        super(NikoHomeControlBindingConstants.BRIDGE_THING_TYPES_UIDS, TIMEOUT);
        logger.debug("Niko Home Control: bridge discovery service started");
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    private void discoverBridge() {
        try {
            NikoHomeControlDiscover nhcDiscover = new NikoHomeControlDiscover();
            addBridge(nhcDiscover.getAddr(), nhcDiscover.getNhcBridgeId());
        } catch (IOException e) {
            logger.debug("Niko Home Control: no bridge found.");
        }
    }

    private Runnable nhcDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            discoverBridge();
        }
    };

    private void addBridge(InetAddress addr, String bridgeId) {
        logger.debug("Niko Home Control: bridge found at {}", addr);

        String bridgeName = "Niko Home Control Bridge";
        ThingUID uid = new ThingUID(BINDING_ID, "bridge", bridgeId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(bridgeName)
                .withProperty(CONFIG_HOST_NAME, addr.getHostAddress()).build();
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
        logger.debug("Niko Home Control: Start background bridge discovery");
        if (nhcDiscoveryJob == null || nhcDiscoveryJob.isCancelled()) {
            nhcDiscoveryJob = scheduler.scheduleWithFixedDelay(nhcDiscoveryRunnable, 0, REFRESH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Niko Home Control: Stop bridge background discovery");
        if (nhcDiscoveryJob != null && !nhcDiscoveryJob.isCancelled()) {
            nhcDiscoveryJob.cancel(true);
            nhcDiscoveryJob = null;
        }
    }
}
