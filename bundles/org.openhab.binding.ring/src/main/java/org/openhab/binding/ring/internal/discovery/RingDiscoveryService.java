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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.RingDevice;
import org.openhab.binding.ring.internal.data.RingDeviceTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The RingDiscoveryService is responsible for auto detecting a Ring
 * device in the local network.
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - Stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ring")
@NonNullByDefault
public class RingDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(RingDiscoveryService.class);
    private @Nullable ScheduledFuture<?> discoveryJob;

    private final Gson gson = new Gson();

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
            RingDeviceTO deviceTO = gson.fromJson(device.getJsonObject(), RingDeviceTO.class);
            if (deviceTO != null) {
                thingDiscovered(device.getDiscoveryResult(deviceTO));
                registry.setStatus(deviceTO.id, RingDeviceRegistry.Status.DISCOVERED);
            }
        }
    }

    private void refresh() {
        discover();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, 120, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.info("Stop Ring background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job != null) {
            job.cancel(true);
        }
        discoveryJob = null;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device search...");
        discover();
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }
}
