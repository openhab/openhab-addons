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
package org.openhab.binding.rachio.internal.discovery;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
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

    private @Nullable Future<?> scanTask;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @Nullable RachioBridgeHandler cloudHandler;

    @Override
    @Activate
    public void activate() {
        super.activate(null);
    }

    @Override
    @Deactivate
    public void deactivate() {
        cancelDiscoveryJobs();
        RachioBridgeHandler handler = cloudHandler;
        if (handler != null) {
            handler.unregisterDiscoveryService(this);
        }
        super.deactivate();
    }

    public RachioDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, BINDING_DISCOVERY_TIMEOUT_SEC, true);
        String uids = SUPPORTED_THING_TYPES_UIDS.toString();
        logger.debug("Thing types: {} registered.", uids);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        RachioBridgeHandler currentHandler = cloudHandler;
        if (currentHandler != null) {
            currentHandler.unregisterDiscoveryService(this);
        }

        if (handler instanceof RachioBridgeHandler) {
            RachioBridgeHandler rachioHandler = (RachioBridgeHandler) handler;
            this.cloudHandler = rachioHandler;
            rachioHandler.registerDiscoveryService(this);
        } else {
            this.cloudHandler = null;
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
            this.discoveryJob = discoveryJob;
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        cancelDiscoveryJobs();
        super.stopBackgroundDiscovery();
    }

    @Override
    protected synchronized void startScan() {
        Future<?> scanTask = this.scanTask;
        if (scanTask == null || scanTask.isDone()) {
            logger.debug("Starting Rachio discovery scan");
            scanTask = scheduler.submit((Runnable) this::discover);
            this.scanTask = scanTask;
        }
    }

    protected synchronized void discover() {
        discover("scan");
    }

    public synchronized void discoverFromCurrentCloudState(String reason) {
        Future<?> scanTask = this.scanTask;
        if (scanTask == null || scanTask.isDone()) {
            logger.debug("Starting automatic Rachio discovery from current cloud state ({})", reason);
            scanTask = scheduler.submit((Runnable) () -> discover("automatic " + reason));
            this.scanTask = scanTask;
        } else {
            logger.debug(
                    "Automatic Rachio discovery from current cloud state ({}) skipped; discovery is already running",
                    reason);
        }
    }

    private synchronized void discover(String source) {
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
            DiscoveryCounts counts = new DiscoveryCounts();
            logger.debug("RachioDiscovery: {} discovered {} irrigation controller device(s).", source,
                    deviceList.size());
            for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
                RachioDevice dev = de.getValue();
                logger.debug("Check Rachio device with ID '{}'", dev.id);

                // register thing if it not already exists
                ThingUID devThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
                dev.setUID(bridgeUID, devThingUID);
                logger.debug(" Rachio device discovered: '{}' (id {}), S/N={}, MAC={}", dev.name, dev.id,
                        dev.serialNumber, dev.macAddress);
                logger.debug("   device status={}, sleepMode={}, on={}", dev.status, dev.getSleepMode(),
                        dev.getEnabled());
                Map<String, Object> properties = new HashMap<>(dev.fillProperties());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(devThingUID).withProperties(properties)
                        .withRepresentationProperty(PROPERTY_DEV_ID).withBridge(bridgeUID).withLabel(dev.getThingName())
                        .build();
                thingDiscovered(discoveryResult);
                counts.controllers++;

                HashMap<String, RachioZone> zoneList = dev.getZones();
                logger.debug("Found {} zones for this device.", zoneList.size());
                for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                    RachioZone zone = ze.getValue();
                    logger.debug("Checking zone with ID '{}'", zone.id);

                    // register thing if it not already exists
                    ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, zone.getThingID());
                    zone.setUID(devThingUID, zoneThingUID);
                    logger.debug("Zone#{} '{}' (id={}) added, enabled={}", zone.zoneNumber, zone.name, zone.id,
                            zone.getEnabled());

                    if (zone.getEnabled() == OnOffType.ON) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Map<String, Object> zproperties = (Map) zone.fillProperties();
                        DiscoveryResult zoneDiscoveryResult = DiscoveryResultBuilder.create(zoneThingUID)
                                .withProperties(zproperties).withRepresentationProperty(PROPERTY_ZONE_ID)
                                .withBridge(bridgeUID).withLabel(dev.name + "[" + zone.zoneNumber + "]: " + zone.name)
                                .build();
                        thingDiscovered(zoneDiscoveryResult);
                        counts.zones++;
                    } else {
                        logger.debug("Zone#{} '{}' is disabled, skip thing creation", zone.zoneNumber, zone.name);
                    }
                }
                counts.schedules += discoverScheduleRules(bridgeUID, dev);
                counts.flexSchedules += discoverFlexScheduleRules(bridgeUID, dev);
            }
            logger.debug("{} Rachio device initialized.", deviceList.size());

            counts.add(discoverSmartHoseTimers(handler, bridgeUID));

            logger.debug(
                    "RachioDiscovery: {} discovery emitted controllers={}, zones={}, schedules={}, flexSchedules={}, baseStations={}, valves={}, valvePrograms={}",
                    source, counts.controllers, counts.zones, counts.schedules, counts.flexSchedules,
                    counts.baseStations, counts.valves, counts.valvePrograms);

            stopScan();
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while discovering Rachio devices/zones: {}", e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    private synchronized void cancelDiscoveryJobs() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }

        Future<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private int discoverScheduleRules(ThingUID bridgeUID, RachioDevice dev) {
        int count = 0;
        for (RachioCloudScheduleRule scheduleRule : dev.scheduleRules) {
            DiscoveryResult discoveryResult = buildScheduleDiscoveryResult(bridgeUID, dev, scheduleRule);
            if (discoveryResult != null) {
                thingDiscovered(discoveryResult);
                count++;
            }
        }
        return count;
    }

    private int discoverFlexScheduleRules(ThingUID bridgeUID, RachioDevice dev) {
        int count = 0;
        for (RachioCloudScheduleRule scheduleRule : dev.flexScheduleRules) {
            DiscoveryResult discoveryResult = buildFlexScheduleDiscoveryResult(bridgeUID, dev, scheduleRule);
            if (discoveryResult != null) {
                logger.debug(
                        "RachioDiscovery: Flex schedule discovery result emitted: thingUid={}, bridgeUid={}, flexScheduleRuleId={}",
                        discoveryResult.getThingUID(), bridgeUID, scheduleRule.id);
                thingDiscovered(discoveryResult);
                count++;
            }
        }
        return count;
    }

    static @Nullable DiscoveryResult buildScheduleDiscoveryResult(ThingUID bridgeUID, RachioDevice dev,
            RachioCloudScheduleRule scheduleRule) {
        if (scheduleRule.id.isBlank()) {
            return null;
        }
        ThingUID scheduleThingUID = new ThingUID(THING_TYPE_SCHEDULE, bridgeUID, scheduleRule.id);
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_SCHEDULE_RULE_ID, scheduleRule.id);
        properties.put(PROPERTY_DEV_ID, dev.id);
        properties.put(PROPERTY_NAME, scheduleRule.name);
        properties.put("type", scheduleRule.type);
        return DiscoveryResultBuilder.create(scheduleThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_SCHEDULE_RULE_ID).withBridge(bridgeUID)
                .withLabel(dev.name + ": " + scheduleRule.name).build();
    }

    static @Nullable DiscoveryResult buildFlexScheduleDiscoveryResult(ThingUID bridgeUID, RachioDevice dev,
            RachioCloudScheduleRule scheduleRule) {
        if (scheduleRule.id.isBlank()) {
            return null;
        }
        ThingUID scheduleThingUID = new ThingUID(THING_TYPE_FLEX_SCHEDULE, bridgeUID, scheduleRule.id);
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_FLEX_SCHEDULE_RULE_ID, scheduleRule.id);
        properties.put(PROPERTY_DEV_ID, dev.id);
        properties.put(PROPERTY_NAME, scheduleRule.name);
        properties.put("type", scheduleRule.type);
        return DiscoveryResultBuilder.create(scheduleThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_FLEX_SCHEDULE_RULE_ID).withBridge(bridgeUID)
                .withLabel(dev.name + ": " + scheduleRule.name).build();
    }

    private DiscoveryCounts discoverSmartHoseTimers(RachioBridgeHandler handler, ThingUID bridgeUID) {
        DiscoveryCounts counts = new DiscoveryCounts();
        try {
            for (RachioBaseStation baseStation : handler.listBaseStations()) {
                DiscoveryResult baseStationResult = buildBaseStationDiscoveryResult(bridgeUID, baseStation);
                if (baseStationResult != null) {
                    thingDiscovered(baseStationResult);
                    counts.baseStations++;
                }

                if (baseStation.id.isBlank()) {
                    continue;
                }
                Set<String> discoveredProgramIds = new HashSet<>();
                try {
                    for (RachioValveProgram program : handler.listValveProgramsForBaseStation(baseStation.id)) {
                        DiscoveryResult programResult = buildValveProgramDiscoveryResult(bridgeUID, baseStation,
                                program);
                        if (programResult != null) {
                            thingDiscovered(programResult);
                            discoveredProgramIds.add(program.id);
                            counts.valvePrograms++;
                        }
                    }
                } catch (RachioApiException e) {
                    logger.debug("Smart Hose Timer Program discovery skipped for base station '{}': {}", baseStation.id,
                            e.getMessage());
                }
                for (RachioValve valve : handler.listValves(baseStation.id)) {
                    DiscoveryResult valveResult = buildValveDiscoveryResult(bridgeUID, baseStation, valve);
                    if (valveResult != null) {
                        thingDiscovered(valveResult);
                        counts.valves++;
                    }
                    if (valve.id.isBlank()) {
                        continue;
                    }
                    try {
                        for (RachioValveProgram program : handler.listValveProgramsForValve(valve.id)) {
                            if (discoveredProgramIds.contains(program.id)) {
                                continue;
                            }
                            DiscoveryResult programResult = buildValveProgramDiscoveryResult(bridgeUID, baseStation,
                                    program);
                            if (programResult != null) {
                                thingDiscovered(programResult);
                                discoveredProgramIds.add(program.id);
                                counts.valvePrograms++;
                            }
                        }
                    } catch (RachioApiException e) {
                        logger.debug("Smart Hose Timer Program discovery skipped for valve '{}': {}", valve.id,
                                e.getMessage());
                    }
                }
            }
        } catch (RachioApiException e) {
            logger.debug("Smart Hose Timer discovery skipped: {}", e.getMessage());
        }
        return counts;
    }

    static @Nullable DiscoveryResult buildBaseStationDiscoveryResult(ThingUID bridgeUID,
            RachioBaseStation baseStation) {
        if (baseStation.id.isBlank()) {
            return null;
        }
        ThingUID baseStationThingUID = new ThingUID(THING_TYPE_BASE_STATION, bridgeUID, baseStation.getThingID());
        Map<String, Object> properties = new HashMap<>(baseStation.fillProperties());
        return DiscoveryResultBuilder.create(baseStationThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_BASE_STATION_ID).withBridge(bridgeUID)
                .withLabel(baseStation.getThingName()).build();
    }

    static @Nullable DiscoveryResult buildValveDiscoveryResult(ThingUID bridgeUID, RachioBaseStation baseStation,
            RachioValve valve) {
        if (valve.id.isBlank()) {
            return null;
        }
        ThingUID valveThingUID = new ThingUID(THING_TYPE_VALVE, bridgeUID, valve.getThingID());
        Map<String, Object> properties = new HashMap<>(valve.fillProperties());
        if (valve.baseStationId.isBlank() && !baseStation.id.isBlank()) {
            properties.put(PROPERTY_BASE_STATION_ID, baseStation.id);
        }
        return DiscoveryResultBuilder.create(valveThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_VALVE_ID).withBridge(bridgeUID)
                .withLabel(baseStation.getThingName() + ": " + valve.getThingName()).build();
    }

    static @Nullable DiscoveryResult buildValveProgramDiscoveryResult(ThingUID bridgeUID, RachioBaseStation baseStation,
            RachioValveProgram program) {
        if (program.id.isBlank()) {
            return null;
        }
        ThingUID programThingUID = new ThingUID(THING_TYPE_VALVE_PROGRAM, bridgeUID, program.getThingID());
        Map<String, Object> properties = new HashMap<>(program.fillProperties());
        if (program.getBaseStationId().isBlank() && !baseStation.id.isBlank()) {
            properties.put(PROPERTY_BASE_STATION_ID, baseStation.id);
        }
        return DiscoveryResultBuilder.create(programThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_VALVE_PROGRAM_ID).withBridge(bridgeUID)
                .withLabel(baseStation.getThingName() + ": " + program.getThingName()).build();
    }

    private static class DiscoveryCounts {
        private int controllers;
        private int zones;
        private int schedules;
        private int flexSchedules;
        private int baseStations;
        private int valves;
        private int valvePrograms;

        private void add(DiscoveryCounts other) {
            controllers += other.controllers;
            zones += other.zones;
            schedules += other.schedules;
            flexSchedules += other.flexSchedules;
            baseStations += other.baseStations;
            valves += other.valves;
            valvePrograms += other.valvePrograms;
        }
    }
}
