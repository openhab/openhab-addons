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
package org.openhab.binding.dreame.internal.handler;

import static org.openhab.binding.dreame.internal.DreameBindingConstants.*;
import static org.openhab.binding.dreame.internal.status.DreameStatusMapper.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameMqttClient;
import org.openhab.binding.dreame.internal.config.DreameMowerConfiguration;
import org.openhab.binding.dreame.internal.model.DreameAction;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowerHeartbeat;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;
import org.openhab.binding.dreame.internal.model.DreameMowerTask;
import org.openhab.binding.dreame.internal.model.DreameMowerTaskStatus;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameMapPngRenderer;
import org.openhab.binding.dreame.internal.util.DreameMapSvgRenderer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Represents one Dreame robotic mower.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameMowerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DreameMowerHandler.class);

    private static final List<DreameProperty> POLLED_PROPERTIES = List.of(DreameProperty.STATE, DreameProperty.ERROR,
            DreameProperty.BATTERY_LEVEL, DreameProperty.CHARGING_STATUS, DreameProperty.STATUS, DreameProperty.DND);
    private static final Duration STATISTICS_REFRESH_INTERVAL = Duration.ofMinutes(15);
    private static final Duration MAP_REFRESH_INTERVAL = Duration.ofMinutes(15);
    private static final Duration PNG_REFRESH_INTERVAL = Duration.ofSeconds(10);
    private static final long STATISTICS_EVENT_DELAY_SECONDS = 2;
    private static final long STATISTICS_DOCKED_DELAY_SECONDS = 5;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> statisticsRefreshJob;
    private @Nullable DreameMqttClient mqttClient;
    private @Nullable DreameMqttConfiguration mqttConfiguration;
    private final AtomicLong updateSequence = new AtomicLong();
    private final AtomicBoolean pollInProgress = new AtomicBoolean();
    private final AtomicBoolean pollPending = new AtomicBoolean();
    private final Map<DreameProperty, Long> mqttPropertyRevisions = new ConcurrentHashMap<>();
    private DreameMowerConfiguration config = new DreameMowerConfiguration();
    private volatile boolean active;
    private volatile @Nullable DreameMapData mapData;
    private volatile @Nullable DreameMowerPose mowerPose;
    private Instant nextStatisticsRefresh = Instant.EPOCH;
    private Instant nextMapRefresh = Instant.EPOCH;
    private Instant nextPngRefresh = Instant.EPOCH;
    private final Gson gson = new Gson();

    public DreameMowerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        active = true;
        config = getConfigAs(DreameMowerConfiguration.class);
        if (config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID must be configured");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The mower must be attached to a Dreame account bridge");
            return;
        }
        bridgeStatusChanged(bridge.getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::poll);
            return;
        }
        DreameAccountHandler account = accountHandler();
        DreameDevice device = device(account);
        if (account == null || device == null) {
            return;
        }
        scheduler.execute(() -> executeCommand(account, device, channelUID, command));
    }

    private void executeCommand(DreameAccountHandler account, DreameDevice device, ChannelUID channelUID,
            Command command) {
        try {
            logger.debug("Executing {} command {} for device {}", channelUID.getId(), command,
                    DreameDiagnostics.maskIdentifier(device.id()));
            if (CHANNEL_COMMAND.equals(channelUID.getId()) && command instanceof StringType stringCommand) {
                DreameAction action = DreameAction.valueOf(stringCommand.toString().toUpperCase(Locale.ROOT));
                account.getApiClient().callAction(device, action);
            } else if (CHANNEL_ZONE_MOWING.equals(channelUID.getId()) && command instanceof StringType stringCommand) {
                List<Integer> zoneIds = parseZoneIds(stringCommand.toString());
                validateZoneIds(zoneIds);
                account.getApiClient().startZoneMowing(device, zoneIds);
            } else if (CHANNEL_DND.equals(channelUID.getId()) && command instanceof OnOffType onOff) {
                account.getApiClient().setProperty(device, DreameProperty.DND, onOff == OnOffType.ON);
            } else if (CHANNEL_CUTTING_HEIGHT.equals(channelUID.getId()) && command instanceof DecimalType decimal) {
                if (!supportsElectronicCuttingHeight(device.model())) {
                    throw new IllegalArgumentException(
                            "Electronic cutting-height control is not supported by " + device.model());
                }
                setCuttingHeight(account, device, decimal.toBigDecimal());
            }
            poll();
        } catch (DreameCloudException | IllegalArgumentException e) {
            logger.debug("Cloud command failed for device {}: {}", DreameDiagnostics.maskIdentifier(device.id()),
                    e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    static List<Integer> parseZoneIds(String value) {
        LinkedHashSet<Integer> result = new LinkedHashSet<>();
        for (String token : value.split(",")) {
            int zoneId = Integer.parseInt(token.trim());
            if (zoneId < 1) {
                throw new IllegalArgumentException("Zone IDs must be positive integers");
            }
            result.add(zoneId);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("At least one zone ID is required");
        }
        return List.copyOf(result);
    }

    private void validateZoneIds(List<Integer> zoneIds) {
        DreameMapData currentMapData = mapData;
        if (currentMapData == null) {
            throw new IllegalArgumentException("Map data is not available yet");
        }
        java.util.Set<Integer> available = new java.util.HashSet<>();
        currentMapData.zones().forEach(zone -> available.add(zone.id()));
        List<Integer> invalid = zoneIds.stream().filter(zoneId -> !available.contains(zoneId)).toList();
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Unknown zone IDs: " + invalid);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        stopPolling();
        stopMqtt();
        if (active && bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, config.refreshInterval, TimeUnit.SECONDS);
        } else if (active) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void poll() {
        if (!active) {
            return;
        }
        if (!pollInProgress.compareAndSet(false, true)) {
            pollPending.set(true);
            return;
        }
        try {
            doPoll();
        } finally {
            pollInProgress.set(false);
            if (active && pollPending.getAndSet(false)) {
                scheduler.execute(this::poll);
            }
        }
    }

    private void doPoll() {
        DreameAccountHandler account = accountHandler();
        DreameDevice device = device(account);
        if (account == null || device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device ID was not found in the Dreamehome account");
            return;
        }
        try {
            long pollRevision = updateSequence.get();
            DreameStatus status = account.getApiClient().getProperties(device, POLLED_PROPERTIES);
            logger.trace("Received properties {} for device {} ({})", status.properties(),
                    DreameDiagnostics.maskIdentifier(device.id()), device.model());
            updateStatusChannels(status, true, pollRevision);
            refreshStatistics(account, device);
            refreshMapData(account, device);
            updateState(CHANNEL_FIRMWARE, new StringType(device.version()));
            updateStatus(ThingStatus.ONLINE);
            ensureMqtt(account, device);
        } catch (DreameCloudException e) {
            logger.debug("Cloud polling failed for device {}: {}", DreameDiagnostics.maskIdentifier(device.id()),
                    e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshMapData(DreameAccountHandler account, DreameDevice device) {
        Instant now = Instant.now();
        if (now.isBefore(nextMapRefresh)) {
            return;
        }
        nextMapRefresh = now.plus(MAP_REFRESH_INTERVAL);
        try {
            DreameMapData mapData = account.getApiClient().getMapData(device);
            this.mapData = mapData;
            logger.debug("Received {} maps and {} mowing zones for device {}", mapData.maps().size(),
                    mapData.zones().size(), DreameDiagnostics.maskIdentifier(device.id()));
            updateState(CHANNEL_CURRENT_MAP_ID,
                    mapData.currentMapId() > 0 ? new DecimalType(mapData.currentMapId()) : UnDefType.UNDEF);
            updateState(CHANNEL_MAPS, new StringType(gson.toJson(mapData.maps())));
            updateState(CHANNEL_ZONES, new StringType(gson.toJson(mapData.zones())));
            mapData.maps().stream().filter(map -> map.id() == mapData.currentMapId()).findFirst()
                    .ifPresent(map -> refreshCuttingHeight(account, device, map.index()));
            nextPngRefresh = Instant.EPOCH;
            updateMapImage();
        } catch (DreameCloudException e) {
            logger.debug("Cloud map query failed for device {}: {}", DreameDiagnostics.maskIdentifier(device.id()),
                    e.getMessage());
        }
    }

    private void refreshCuttingHeight(DreameAccountHandler account, DreameDevice device, int mapIndex) {
        if (!supportsElectronicCuttingHeight(device.model())) {
            logger.debug("Electronic cutting-height control is not supported by mower model {}", device.model());
            updateState(CHANNEL_CUTTING_HEIGHT, UnDefType.UNDEF);
            return;
        }
        try {
            BigDecimal cuttingHeight = account.getApiClient().getCuttingHeight(device, mapIndex);
            logger.debug("Received cutting height {} cm for map index {} of device {}", cuttingHeight, mapIndex,
                    DreameDiagnostics.maskIdentifier(device.id()));
            updateState(CHANNEL_CUTTING_HEIGHT, new DecimalType(cuttingHeight));
        } catch (DreameCloudException e) {
            logger.debug("Cloud cutting-height query failed for device {}: {}",
                    DreameDiagnostics.maskIdentifier(device.id()), e.getMessage());
            updateState(CHANNEL_CUTTING_HEIGHT, UnDefType.UNDEF);
        }
    }

    static boolean supportsElectronicCuttingHeight(String model) {
        return !"mova.mower.g2405c".equals(model);
    }

    private void setCuttingHeight(DreameAccountHandler account, DreameDevice device, BigDecimal height)
            throws DreameCloudException {
        DreameMapData currentMapData = mapData;
        if (currentMapData == null) {
            throw new DreameCloudException("Map data is not available yet");
        }
        int mapIndex = currentMapData.maps().stream().filter(map -> map.id() == currentMapData.currentMapId())
                .findFirst().orElseThrow(() -> new DreameCloudException("Active map was not found")).index();
        account.getApiClient().setCuttingHeight(device, mapIndex, height);
        refreshCuttingHeight(account, device, mapIndex);
    }

    private void refreshStatistics(DreameAccountHandler account, DreameDevice device) {
        Instant now = Instant.now();
        if (now.isBefore(nextStatisticsRefresh)) {
            return;
        }
        nextStatisticsRefresh = now.plus(STATISTICS_REFRESH_INTERVAL);
        try {
            DreameMowingStatistics statistics = account.getApiClient().getMowingStatistics(device);
            logger.trace("Received {} historical mowing sessions for device {}", statistics.sessions(),
                    DreameDiagnostics.maskIdentifier(device.id()));
            updateState(CHANNEL_MOWING_SESSIONS, new DecimalType(statistics.sessions()));
            updateState(CHANNEL_TOTAL_MOWING_TIME, new QuantityType<>(statistics.totalMinutes(), Units.MINUTE));
            updateState(CHANNEL_TOTAL_MOWED_AREA, new QuantityType<>(statistics.totalArea(), SIUnits.SQUARE_METRE));
        } catch (DreameCloudException e) {
            logger.debug("Cloud statistics query failed for device {}: {}",
                    DreameDiagnostics.maskIdentifier(device.id()), e.getMessage());
        }
    }

    private void refreshStatisticsAfterMission(DreameAccountHandler account, DreameDevice device) {
        if (!active) {
            return;
        }
        nextStatisticsRefresh = Instant.EPOCH;
        refreshStatistics(account, device);
    }

    private void scheduleStatisticsRefresh(DreameAccountHandler account, DreameDevice device, long delaySeconds) {
        ScheduledFuture<?> job = statisticsRefreshJob;
        if (job != null) {
            job.cancel(false);
        }
        statisticsRefreshJob = scheduler.schedule(() -> {
            statisticsRefreshJob = null;
            refreshStatisticsAfterMission(account, device);
        }, delaySeconds, TimeUnit.SECONDS);
    }

    private void ensureMqtt(DreameAccountHandler account, DreameDevice device) {
        try {
            DreameMqttConfiguration nextConfiguration = account.getApiClient().mqttConfiguration(device);
            DreameMqttConfiguration currentConfiguration = mqttConfiguration;
            DreameMqttClient currentClient = mqttClient;
            if (currentConfiguration != null && currentConfiguration.sameConnection(nextConfiguration)
                    && currentClient != null && currentClient.isConnected()) {
                return;
            }
            stopMqtt();
            DreameMqttClient newClient = new DreameMqttClient(nextConfiguration, POLLED_PROPERTIES, status -> {
                if (active) {
                    long revision = updateSequence.incrementAndGet();
                    status.properties().forEach(property -> mqttPropertyRevisions.put(property, revision));
                    scheduler.execute(() -> {
                        if (active) {
                            updateChannelsFromMqtt(status);
                            if (status.mapChanged()) {
                                logger.debug("Refreshing map data after MQTT change notification for device {}",
                                        DreameDiagnostics.maskIdentifier(device.id()));
                                nextMapRefresh = Instant.EPOCH;
                                refreshMapData(account, device);
                            }
                            if (status.missionCompleted()) {
                                scheduleStatisticsRefresh(account, device, STATISTICS_EVENT_DELAY_SECONDS);
                            } else if (status.contains(DreameProperty.STATE)
                                    && isDockedState(status.integer(DreameProperty.STATE, 0))) {
                                logger.debug("Scheduling mowing statistics refresh after dock arrival for device {}",
                                        DreameDiagnostics.maskIdentifier(device.id()));
                                scheduleStatisticsRefresh(account, device, STATISTICS_DOCKED_DELAY_SECONDS);
                            }
                        }
                    });
                }
            });
            mqttConfiguration = nextConfiguration;
            mqttClient = newClient;
            newClient.connect();
        } catch (DreameCloudException | MqttException e) {
            stopMqtt();
            logger.debug("Dreame MQTT connection failed for device {}: {}",
                    DreameDiagnostics.maskIdentifier(device.id()), e.getMessage(), e);
        }
    }

    private void updateChannelsFromMqtt(DreameStatus status) {
        logger.trace("Received MQTT properties {} position={} task={} taskActive={} missionCompleted={} mapChanged={}",
                status.properties(), status.mowerPose() != null, status.mowerTaskStatus() != null,
                status.mowerTaskActive(), status.missionCompleted(), status.mapChanged());
        updateStatusChannels(status, false, null);
        DreameMowerPose pose = status.mowerPose();
        if (pose != null) {
            mowerPose = pose;
            updateState(CHANNEL_POSITION_X, new DecimalType(pose.x()));
            updateState(CHANNEL_POSITION_Y, new DecimalType(pose.y()));
            updateState(CHANNEL_HEADING, new DecimalType(BigDecimal.valueOf(pose.heading())));
            updateMapImage();
        }
        DreameMowerTask task = status.mowerTask();
        if (task != null) {
            updateState(CHANNEL_CURRENT_ZONE, new StringType(Integer.toString(task.regionId())));
            updateState(CHANNEL_MOWING_PROGRESS, new QuantityType<>(task.progress(), Units.PERCENT));
            updateState(CHANNEL_PLANNED_MOWING_AREA, new QuantityType<>(task.plannedArea(), SIUnits.SQUARE_METRE));
            updateState(CHANNEL_CURRENT_MOWED_AREA, new QuantityType<>(task.mowedArea(), SIUnits.SQUARE_METRE));
        }
        DreameMowerTaskStatus taskStatus = status.mowerTaskStatus();
        if (taskStatus != null) {
            updateState(CHANNEL_TASK_EXECUTABLE, OnOffType.from(taskStatus.executable()));
            updateState(CHANNEL_TASK_OPERATION, new DecimalType(taskStatus.operation()));
            updateState(CHANNEL_TASK_STATE, new StringType(taskOperationName(taskStatus.operation())));
            Long taskTime = taskStatus.time();
            updateState(CHANNEL_TASK_TIME, taskTime == null ? UnDefType.UNDEF : new DecimalType(taskTime));
            if (!taskStatus.regionIds().isEmpty()) {
                updateState(CHANNEL_CURRENT_ZONE, new StringType(taskStatus.regionIds().stream().map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","))));
            }
        }
        Boolean taskActive = status.mowerTaskActive();
        if (taskActive != null) {
            updateState(CHANNEL_TASK_ACTIVE, OnOffType.from(taskActive));
        }
        DreameMowerHeartbeat heartbeat = status.mowerHeartbeat();
        if (heartbeat != null) {
            updateState(CHANNEL_DOCKING_STATE, new StringType(dockingName(heartbeat.dockingState())));
            updateState(CHANNEL_LOCATION_STATE, new DecimalType(heartbeat.locationState()));
            updateRssi(CHANNEL_WIFI_RSSI, heartbeat.wifiRssi());
            updateRssi(CHANNEL_BLE_RSSI, heartbeat.bleRssi());
            updateRssi(CHANNEL_LTE_RSSI, heartbeat.lteRssi());
        }
    }

    private void updateMapImage() {
        DreameMapData currentMapData = mapData;
        if (currentMapData == null) {
            return;
        }
        DreameDevice device = device(accountHandler());
        boolean rotateClockwise = device != null && device.model().startsWith("mova.mower.");
        String svg = DreameMapSvgRenderer.render(currentMapData.geometries(), currentMapData.currentMapId(), mowerPose,
                rotateClockwise);
        updateState(CHANNEL_MAP_SVG,
                svg == null ? UnDefType.UNDEF : new RawType(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml"));
        Instant now = Instant.now();
        if (isLinked(CHANNEL_MAP_PNG) && !now.isBefore(nextPngRefresh)) {
            nextPngRefresh = now.plus(PNG_REFRESH_INTERVAL);
            byte[] png = DreameMapPngRenderer.render(currentMapData.geometries(), currentMapData.currentMapId(),
                    mowerPose, rotateClockwise);
            updateState(CHANNEL_MAP_PNG, png == null ? UnDefType.UNDEF : new RawType(png, "image/png"));
        }
    }

    private void updateStatusChannels(DreameStatus status, boolean clearMissing, @Nullable Long pollRevision) {
        if (status.contains(DreameProperty.STATE) && canApply(DreameProperty.STATE, pollRevision)) {
            Boolean taskActive = taskActiveFromState(status.integer(DreameProperty.STATE, 0));
            if (taskActive != null) {
                updateState(CHANNEL_TASK_ACTIVE, OnOffType.from(taskActive));
            }
        }
        updateIntegerState(status, DreameProperty.STATE, CHANNEL_STATE, value -> new StringType(stateName(value)),
                clearMissing, pollRevision);
        updateIntegerState(status, DreameProperty.BATTERY_LEVEL, CHANNEL_BATTERY_LEVEL, DecimalType::new, clearMissing,
                pollRevision);
        updateIntegerState(status, DreameProperty.CHARGING_STATUS, CHANNEL_CHARGING_STATUS,
                value -> new StringType(chargingName(value)), clearMissing, pollRevision);
        updateIntegerState(status, DreameProperty.ERROR, CHANNEL_ERROR_CODE,
                value -> new StringType(Integer.toString(value)), clearMissing, pollRevision);
        if (canApply(DreameProperty.DND, pollRevision)) {
            if (status.contains(DreameProperty.DND)) {
                updateState(CHANNEL_DND, OnOffType.from(status.bool(DreameProperty.DND, false)));
            } else if (clearMissing) {
                updateState(CHANNEL_DND, UnDefType.UNDEF);
            }
        }
        updateIntegerState(status, DreameProperty.MOWING_SESSIONS, CHANNEL_MOWING_SESSIONS, DecimalType::new, false,
                pollRevision);
        updateIntegerState(status, DreameProperty.TOTAL_MOWING_TIME, CHANNEL_TOTAL_MOWING_TIME,
                value -> new QuantityType<>(value, Units.MINUTE), false, pollRevision);
        updateIntegerState(status, DreameProperty.TOTAL_MOWED_AREA, CHANNEL_TOTAL_MOWED_AREA,
                value -> new QuantityType<>(value, SIUnits.SQUARE_METRE), false, pollRevision);
    }

    private void updateIntegerState(DreameStatus status, DreameProperty property, String channel,
            IntegerStateConverter converter, boolean clearMissing, @Nullable Long pollRevision) {
        if (!canApply(property, pollRevision)) {
            logger.trace("Ignoring stale REST value for {} because a newer MQTT update was received", property);
            return;
        }
        if (status.contains(property)) {
            updateState(channel, converter.convert(status.integer(property, 0)));
        } else if (clearMissing) {
            updateState(channel, UnDefType.UNDEF);
        }
    }

    private boolean canApply(DreameProperty property, @Nullable Long pollRevision) {
        return pollRevision == null || isPollPropertyCurrent(pollRevision, mqttPropertyRevisions.get(property));
    }

    static boolean isPollPropertyCurrent(long pollRevision, @Nullable Long mqttRevision) {
        return mqttRevision == null || mqttRevision <= pollRevision;
    }

    @FunctionalInterface
    private interface IntegerStateConverter {
        State convert(int value);
    }

    private @Nullable DreameAccountHandler accountHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof DreameAccountHandler account ? account : null;
    }

    private @Nullable DreameDevice device(@Nullable DreameAccountHandler account) {
        return account == null ? null : account.getDevice(config.deviceId);
    }

    private void updateRssi(String channel, int rssi) {
        updateState(channel, isRssiAvailable(rssi) ? new DecimalType(rssi) : UnDefType.UNDEF);
    }

    private void stopPolling() {
        ScheduledFuture<?> job = pollingJob;
        pollingJob = null;
        if (job != null) {
            job.cancel(true);
        }
    }

    private void stopStatisticsRefresh() {
        ScheduledFuture<?> job = statisticsRefreshJob;
        statisticsRefreshJob = null;
        if (job != null) {
            job.cancel(true);
        }
    }

    private void stopMqtt() {
        DreameMqttClient client = mqttClient;
        mqttClient = null;
        mqttConfiguration = null;
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void dispose() {
        active = false;
        pollPending.set(false);
        mqttPropertyRevisions.clear();
        mapData = null;
        mowerPose = null;
        stopPolling();
        stopStatisticsRefresh();
        stopMqtt();
        super.dispose();
    }
}
