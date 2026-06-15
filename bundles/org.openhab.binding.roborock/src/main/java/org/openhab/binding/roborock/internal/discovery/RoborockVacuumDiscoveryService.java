/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

    protected @Nullable HomeData getHomeData() {
        return thingHandler.getHomeData();
    }

    private void findDevice(@Nullable String name, @Nullable String id, @Nullable Devices[] devices) {
        if (id == null || name == null || devices == null) {
            logger.debug("Skipping device discovery: product id, name, or devices array is null.");
            return;
        }

        ThingUID bridgeUID = thingHandler.getThing().getUID();

        for (Devices device : devices) {
            if (device == null || device.productId == null || device.duid == null) {
                continue;
            }

            if (id.equals(device.productId)) {
                if ("1.0".equals(device.pv) || "B01".equals(device.pv)) {
                    Configuration configuration = new Configuration();
                    configuration.put(THING_CONFIG_DUID, device.duid);
                    configuration.put(THING_PROPERTY_PROTOCOL, device.pv);
                    configuration.put(THING_PROPERTY_DEVICE_NAME, name);
                    configuration.put(THING_PROPERTY_SN, (device.sn != null) ? device.sn : "N/A");

                    String label = (device.name != null) ? device.name : "Roborock Vacuum";

                    DiscoveryResult result = DiscoveryResultBuilder
                            .create(new ThingUID(ROBOROCK_VACUUM, bridgeUID, device.duid))
                            .withProperties(configuration.getProperties()).withLabel(label)
                            .withRepresentationProperty(THING_CONFIG_DUID).withBridge(bridgeUID).build();

                    thingDiscovered(result);
                } else {
                    logger.info("Vacuum with duid {}, not added as protocol {} is not (yet) supported.", device.duid,
                            device.pv);
                }
            }
        }
    }

    private void discover() {
        HomeData homeData = getHomeData();

        if (homeData == null || homeData.result == null || homeData.result.products == null) {
            return;
        }

        Devices[] devices = homeData.result.devices != null ? homeData.result.devices : new Devices[0];
        Devices[] receivedDevices = homeData.result.receivedDevices != null ? homeData.result.receivedDevices
                : new Devices[0];

        for (HomeData.Products product : homeData.result.products) {
            if (product == null) {
                continue;
            }
            findDevice(product.name, product.id, devices);
            findDevice(product.name, product.id, receivedDevices);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
