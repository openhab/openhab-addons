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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDiscoverySnapshot;
import org.openhab.binding.rachio.internal.api.RachioDiscoverySnapshot.DeviceSnapshot;
import org.openhab.binding.rachio.internal.api.RachioDiscoverySnapshot.ScheduleSnapshot;
import org.openhab.binding.rachio.internal.api.RachioDiscoverySnapshot.ZoneSnapshot;
import org.openhab.binding.rachio.internal.api.RachioSmartHoseSnapshot;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
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
    private final Object jobLock = new Object();
    private final AtomicBoolean discoveryRunning = new AtomicBoolean();

    private @Nullable Future<?> scanTask;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private volatile @Nullable RachioBridgeHandler cloudHandler;

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

        synchronized (jobLock) {
            ScheduledFuture<?> discoveryJob = this.discoveryJob;
            if (discoveryJob == null || discoveryJob.isCancelled()) {
                this.discoveryJob = scheduler.scheduleWithFixedDelay(() -> discover("background", false), 10,
                        DISCOVERY_REFRESH_SEC, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        cancelDiscoveryJobs();
        super.stopBackgroundDiscovery();
    }

    @Override
    protected void startScan() {
        boolean scanScheduled = false;
        synchronized (jobLock) {
            Future<?> scanTask = this.scanTask;
            if (scanTask == null || scanTask.isDone()) {
                logger.debug("Starting Rachio discovery scan");
                this.scanTask = scheduler.submit((Runnable) () -> discover("scan", true));
                scanScheduled = true;
            }
        }
        if (!scanScheduled) {
            logger.debug("Rachio discovery scan skipped; another requested discovery task is active");
            stopScan();
        }
    }

    protected void discover() {
        discover("background", false);
    }

    public void discoverFromCurrentCloudState(String reason) {
        synchronized (jobLock) {
            Future<?> scanTask = this.scanTask;
            if (scanTask == null || scanTask.isDone()) {
                logger.debug("Starting automatic Rachio discovery from current cloud state ({})", reason);
                this.scanTask = scheduler.submit((Runnable) () -> discover("automatic " + reason, false));
            } else {
                logger.debug(
                        "Automatic Rachio discovery from current cloud state ({}) skipped; discovery is already running",
                        reason);
            }
        }
    }

    private void discover(String source, boolean completeScan) {
        if (!discoveryRunning.compareAndSet(false, true)) {
            logger.debug("RachioDiscovery: {} discovery skipped; another discovery run is active", source);
            if (completeScan) {
                stopScan();
            }
            return;
        }
        try {
            RachioBridgeHandler handler = cloudHandler;
            if (handler == null) {
                logger.debug("RachioDiscovery: Rachio Cloud access not set!");
                return;
            }

            RachioDiscoverySnapshot snapshot = handler.getDiscoverySnapshot();
            List<DeviceSnapshot> deviceList = snapshot.devices();
            ThingUID bridgeUID = handler.getThing().getUID();
            DiscoveryCounts counts = new DiscoveryCounts();
            logger.debug("RachioDiscovery: {} discovered {} irrigation controller device(s).", source,
                    deviceList.size());
            for (DeviceSnapshot dev : deviceList) {
                logger.debug("Check Rachio device with ID '{}'", dev.id());

                // register thing if it not already exists
                ThingUID devThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, dev.thingId());
                logger.debug(" Rachio device discovered: '{}' (id {})", dev.name(), dev.id());
                logger.debug("   device status={}, sleepMode={}, on={}", dev.status(), dev.sleepMode(), dev.enabled());
                Map<String, Object> properties = new HashMap<>(dev.properties());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(devThingUID).withProperties(properties)
                        .withRepresentationProperty(PROPERTY_DEV_ID).withBridge(bridgeUID).withLabel(dev.name())
                        .build();
                thingDiscovered(discoveryResult);
                counts.controllers++;

                List<ZoneSnapshot> zoneList = dev.zones();
                logger.debug("Found {} zones for this device.", zoneList.size());
                for (ZoneSnapshot zone : zoneList) {
                    logger.debug("Checking zone with ID '{}'", zone.id());

                    // register thing if it not already exists
                    ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, zone.thingId());
                    logger.debug("Zone#{} '{}' (id={}) added, enabled={}", zone.zoneNumber(), zone.name(), zone.id(),
                            zone.enabled());

                    if (zone.enabled()) {
                        Map<String, Object> zproperties = new HashMap<>(zone.properties());
                        DiscoveryResult zoneDiscoveryResult = DiscoveryResultBuilder.create(zoneThingUID)
                                .withProperties(zproperties).withRepresentationProperty(PROPERTY_ZONE_ID)
                                .withBridge(bridgeUID)
                                .withLabel(dev.name() + "[" + zone.zoneNumber() + "]: " + zone.name()).build();
                        thingDiscovered(zoneDiscoveryResult);
                        counts.zones++;
                    } else {
                        logger.debug("Zone#{} '{}' is disabled, skip thing creation", zone.zoneNumber(), zone.name());
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

        } catch (RuntimeException e) {
            logger.warn("Unexpected error while discovering Rachio devices/zones", e);
        } finally {
            discoveryRunning.set(false);
            if (completeScan) {
                stopScan();
            }
        }
    }

    @Override
    protected void stopScan() {
        super.stopScan();
    }

    private void cancelDiscoveryJobs() {
        synchronized (jobLock) {
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
    }

    private int discoverScheduleRules(ThingUID bridgeUID, DeviceSnapshot dev) {
        int count = 0;
        for (ScheduleSnapshot scheduleRule : dev.schedules()) {
            DiscoveryResult discoveryResult = buildScheduleDiscoveryResult(bridgeUID, dev, scheduleRule);
            if (discoveryResult != null) {
                thingDiscovered(discoveryResult);
                count++;
            }
        }
        return count;
    }

    private int discoverFlexScheduleRules(ThingUID bridgeUID, DeviceSnapshot dev) {
        int count = 0;
        for (ScheduleSnapshot scheduleRule : dev.flexSchedules()) {
            DiscoveryResult discoveryResult = buildFlexScheduleDiscoveryResult(bridgeUID, dev, scheduleRule);
            if (discoveryResult != null) {
                logger.debug(
                        "RachioDiscovery: Flex schedule discovery result emitted: thingUid={}, bridgeUid={}, flexScheduleRuleId={}",
                        discoveryResult.getThingUID(), bridgeUID, scheduleRule.id());
                thingDiscovered(discoveryResult);
                count++;
            }
        }
        return count;
    }

    static @Nullable DiscoveryResult buildScheduleDiscoveryResult(ThingUID bridgeUID, DeviceSnapshot dev,
            ScheduleSnapshot scheduleRule) {
        if (scheduleRule.id().isBlank()) {
            return null;
        }
        ThingUID scheduleThingUID = new ThingUID(THING_TYPE_SCHEDULE, bridgeUID, scheduleRule.id());
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_SCHEDULE_RULE_ID, scheduleRule.id());
        properties.put(PROPERTY_DEV_ID, dev.id());
        properties.put(PROPERTY_NAME, scheduleRule.name());
        properties.put("type", scheduleRule.type());
        return DiscoveryResultBuilder.create(scheduleThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_SCHEDULE_RULE_ID).withBridge(bridgeUID)
                .withLabel(dev.name() + ": " + scheduleRule.name()).build();
    }

    static @Nullable DiscoveryResult buildFlexScheduleDiscoveryResult(ThingUID bridgeUID, DeviceSnapshot dev,
            ScheduleSnapshot scheduleRule) {
        if (scheduleRule.id().isBlank()) {
            return null;
        }
        ThingUID scheduleThingUID = new ThingUID(THING_TYPE_FLEX_SCHEDULE, bridgeUID, scheduleRule.id());
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_FLEX_SCHEDULE_RULE_ID, scheduleRule.id());
        properties.put(PROPERTY_DEV_ID, dev.id());
        properties.put(PROPERTY_NAME, scheduleRule.name());
        properties.put("type", scheduleRule.type());
        return DiscoveryResultBuilder.create(scheduleThingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_FLEX_SCHEDULE_RULE_ID).withBridge(bridgeUID)
                .withLabel(dev.name() + ": " + scheduleRule.name()).build();
    }

    private DiscoveryCounts discoverSmartHoseTimers(RachioBridgeHandler handler, ThingUID bridgeUID) {
        DiscoveryCounts counts = new DiscoveryCounts();
        try {
            RachioSmartHoseSnapshot snapshot = handler.getSmartHoseSnapshot();
            for (RachioBaseStation baseStation : snapshot.baseStations().values()) {
                DiscoveryResult baseStationResult = buildBaseStationDiscoveryResult(bridgeUID, baseStation);
                if (baseStationResult != null) {
                    thingDiscovered(baseStationResult);
                    counts.baseStations++;
                }

                for (RachioValve valve : snapshot.valves().values()) {
                    if (!baseStation.id.equals(valve.baseStationId)) {
                        continue;
                    }
                    DiscoveryResult valveResult = buildValveDiscoveryResult(bridgeUID, baseStation, valve);
                    if (valveResult != null) {
                        thingDiscovered(valveResult);
                        counts.valves++;
                    }
                }
                for (RachioValveProgram program : snapshot.programs().values()) {
                    if (!baseStation.id.equals(program.getBaseStationId())) {
                        continue;
                    }
                    DiscoveryResult programResult = buildValveProgramDiscoveryResult(bridgeUID, baseStation, program);
                    if (programResult != null) {
                        thingDiscovered(programResult);
                        counts.valvePrograms++;
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
