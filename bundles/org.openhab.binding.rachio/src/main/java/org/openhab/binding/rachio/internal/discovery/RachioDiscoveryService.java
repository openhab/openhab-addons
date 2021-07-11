/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.discovery;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioDiscoveryService} discovers all devices/zones reported by the Rachio Cloud. This requires the api
 * key to get access to the cloud data.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_REFRESH_SEC = 900;

    private final Logger logger = LoggerFactory.getLogger(RachioDiscoveryService.class);

    @Nullable
    private Future<?> scanTask;

    @Nullable
    private ScheduledFuture<?> discoveryJob;

    @Nullable
    private RachioBridgeHandler cloudHandler;

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in thing config)
     */
    @Override
    @Activate
    public void activate() {
        super.activate(null);
    }

    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    public RachioDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, BINDING_DISCOVERY_TIMEOUT_SEC, true);
        String uids = SUPPORTED_THING_TYPES_UIDS.toString();
        logger.debug("Thing types: {} registered.", uids);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof RachioBridgeHandler) {
            this.cloudHandler = (RachioBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.cloudHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery for new Rachio controllers");

        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discover, 10, DISCOVERY_REFRESH_SEC,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected synchronized void startScan() {
        Future<?> scanTask = this.scanTask;
        if (scanTask == null || scanTask.isDone()) {
            logger.debug("Starting Rachio discovery scan");
            scanTask = scheduler.submit(this::discover);
        }
    }

    protected synchronized void discover() {
        try {
            RachioBridgeHandler handler = cloudHandler;
            if (handler == null) {
                logger.debug("RachioDiscovery: Rachio Cloud access not set!");
                return;
            }

            HashMap<String, RachioDevice> deviceList = null;
            ThingUID bridgeUID;
            deviceList = handler.getDevices();
            bridgeUID = handler.getThing().getUID();

            if (deviceList == null) {
                logger.debug("Discovery: Rachio Cloud access not initialized yet!");
                return;
            }
            logger.debug("Found {} devices.", deviceList.size());
            for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
                RachioDevice dev = de.getValue();
                logger.debug("Check Rachio device with ID '{}'", dev.id);

                // register thing if it not already exists
                ThingUID devThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
                dev.setUID(bridgeUID, devThingUID);
                logger.info(" Rachio device discovered: '{}' (id {}), S/N={}, MAC={}", dev.name, dev.id,
                        dev.serialNumber, dev.macAddress);
                logger.debug("  latitude={}, longitude={}", dev.latitude, dev.longitude);
                logger.info("   device status={}, paused/sleep={}, on={}", dev.status, dev.getSleepMode(),
                        dev.getEnabled());
                Map<String, Object> properties = new HashMap<>(dev.fillProperties());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(devThingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(dev.getThingName()).build();
                thingDiscovered(discoveryResult);

                HashMap<String, RachioZone> zoneList = dev.getZones();
                logger.debug("Found {} zones for this device.", zoneList.size());
                for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                    RachioZone zone = ze.getValue();
                    logger.debug("Checking zone with ID '{}'", zone.id);

                    // register thing if it not already exists
                    ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, zone.getThingID());
                    zone.setUID(devThingUID, zoneThingUID);
                    logger.info("Zone#{} '{}' (id={}) added, enabled={}", zone.zoneNumber, zone.name, zone.id,
                            zone.getEnabled());

                    if (zone.getEnabled() == OnOffType.ON) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Map<String, Object> zproperties = (Map) zone.fillProperties();
                        DiscoveryResult zoneDiscoveryResult = DiscoveryResultBuilder.create(zoneThingUID)
                                .withProperties(zproperties).withBridge(bridgeUID)
                                .withLabel(dev.name + "[" + zone.zoneNumber + "]: " + zone.name).build();
                        thingDiscovered(zoneDiscoveryResult);
                    } else {
                        logger.info("Zone#{} '{}' is disabled, skip thing creation", zone.name, zone.id);
                    }
                }
            }
            logger.info("{}  Rachio device initialized.", deviceList.size());

            stopScan();
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while discovering Rachio devices/zones: {}", e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }
}
