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
package org.openhab.binding.roborock.internal.discovery;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockAccountHandler;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.HomeData.Devices;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RoborockVacuumDiscoveryService is responsible for auto detecting a vacuum
 * cleaner in the Roborock ecosystem.
 *
 * @author Paul Smedley - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = RoborockVacuumDiscoveryService.class)
@NonNullByDefault
public class RoborockVacuumDiscoveryService extends AbstractThingHandlerDiscoveryService<RoborockAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumDiscoveryService.class);

    private @Nullable ScheduledFuture<?> discoveryJob;

    public RoborockVacuumDiscoveryService() {
        super(RoborockAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5);
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Roborock background discovery.");
        ScheduledFuture<?> job = this.discoveryJob;
        if (job == null || job.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 1, 30, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Roborock background discovery");
        ScheduledFuture<?> job = this.discoveryJob;
        if (job != null) {
            job.cancel(true);
            discoveryJob = null;
        }
    }

    @Nullable
    protected HomeData getHomeData() {
        return thingHandler.getHomeData();
    }

    private void findDevice(Devices devices[]) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (int i = 0; i < devices.length; i++) {
            if ("1.0".equals(devices[i].pv)) {
                Configuration configuration = new Configuration();
                configuration.put(THING_CONFIG_DUID, devices[i].duid);
                configuration.put(THING_PROPERTY_SN, (devices[i].sn != null) ? devices[i].sn : "N/A");

                DiscoveryResult result = DiscoveryResultBuilder
                        .create(new ThingUID(ROBOROCK_VACUUM, bridgeUID, devices[i].duid))
                        .withProperties(configuration.getProperties()).withLabel(devices[i].name)
                        .withRepresentationProperty(THING_CONFIG_DUID).withBridge(bridgeUID).build();

                thingDiscovered(result);
            } else {
                logger.info("Vacuum with duid {}, not added as protocol {} is not (yet) supported.", devices[i].duid,
                        devices[i].pv);
            }
        }
    }

    private void discover() {
        HomeData homeData;
        homeData = getHomeData();

        if (homeData != null) {
            Devices devices[] = homeData.result.devices;
            findDevice(devices);
            // also check for shared devices
            Devices receivedDevices[] = homeData.result.receivedDevices;
            findDevice(receivedDevices);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
