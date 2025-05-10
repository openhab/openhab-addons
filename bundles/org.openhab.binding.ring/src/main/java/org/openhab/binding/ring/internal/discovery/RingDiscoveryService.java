/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.discovery;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.RingDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RingDiscoveryService is responsible for auto detecting a Ring
 * device in the local network.
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - Stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ring")
public class RingDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(RingDiscoveryService.class);
    private ScheduledFuture<?> discoveryJob;

    public RingDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 5, true);
    }

    public void activate() {
        logger.debug("Starting Ring discovery...");
        startScan();
        startBackgroundDiscovery();
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping Ring discovery...");
        stopBackgroundDiscovery();
        stopScan();
    }

    private void discover() {
        RingDeviceRegistry registry = RingDeviceRegistry.getInstance();
        for (RingDevice device : registry.getRingDevices(RingDeviceRegistry.Status.ADDED)) {
            thingDiscovered(device.getDiscoveryResult());
            registry.setStatus(device.getId(), RingDeviceRegistry.Status.DISCOVERED);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    discover();
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };
        discoveryJob = scheduler.scheduleWithFixedDelay(runnable, 0, 120, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.info("Stop Ring background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device search...");
        try {
            discover();
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }
}
