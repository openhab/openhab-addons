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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameVacuumApi;
import org.openhab.binding.dreame.internal.api.DreameVacuumMqttClient;
import org.openhab.binding.dreame.internal.config.DreameVacuumConfiguration;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumCapabilities;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;
import org.openhab.binding.dreame.internal.model.DreameVacuumStatus;
import org.openhab.binding.dreame.internal.util.DreameVacuumDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapState;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
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

/**
 * Exposes vacuum status and controls with periodic connectivity checks.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameVacuumHandler extends BaseThingHandler {
    private static final int STATUS_FAILURES_BEFORE_OFFLINE = 2;
    private final Logger logger = LoggerFactory.getLogger(DreameVacuumHandler.class);
    private boolean active;
    private final Executor commandExecutor;
    private final Deque<PendingCommand> commands = new ArrayDeque<>();
    private boolean commandWorkerRunning;

    private record PendingCommand(int generation, DreameVacuumAction action) {
    }

    private int connectionGeneration;
    private final Map<String, State> states = new HashMap<>();
    private final Set<String> availableProperties = new HashSet<>();
    private final DreameVacuumMapState mapState = new DreameVacuumMapState();
    private long statusRevision;
    private int consecutiveStatusFailures;
    private long mapRevision;
    private final Map<String, Long> channelRevisions = new HashMap<>();
    private final AtomicInteger generation = new AtomicInteger();
    private @Nullable ScheduledFuture<?> mqttJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable DreameVacuumMqttClient mqttClient;
    private @Nullable DreameMqttConfiguration mqttConfiguration;
    private @Nullable DreameVacuumApi mapApi;
    private @Nullable DreameDevice mapDevice;
    private boolean mapQueryRunning;
    private boolean mapListQueryRunning;
    private @Nullable String mapListObjectName;
    private boolean immediateBaseRequestTriggered;

    public DreameVacuumHandler(Thing thing) {
        super(thing);
        commandExecutor = scheduler;
    }

    DreameVacuumHandler(Thing thing, Executor commandExecutor) {
        super(thing);
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void initialize() {
        synchronized (this) {
            active = true;
            inspectDevice();
        }
    }

    @Override
    public synchronized void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (active) {
            inspectDevice();
        }
    }

    private void inspectDevice() {
        stopMqtt();
        DreameVacuumConfiguration config = getConfigAs(DreameVacuumConfiguration.class);
        if (config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.device-id");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE
                || !(bridge.getHandler() instanceof DreameAccountHandler account)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        for (DreameDevice device : account.getVacuumDevices()) {
            if (config.deviceId.equals(device.id())) {
                logger.trace("Vacuum discovery diagnostics: {}", DreameVacuumDiagnostics.describe(device));
                if (!supportsMqttDiagnostics(device)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.configuration-error.vacuum-model");
                    return;
                }
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/unknown.awaiting-device-status");
                int currentGeneration = generation.get();
                mapApi = account.getVacuumApi();
                mapDevice = device;
                mqttJob = scheduler.scheduleWithFixedDelay(() -> maintainMqtt(account, device, currentGeneration), 0,
                        60, TimeUnit.SECONDS);
                pollingJob = scheduler.scheduleWithFixedDelay(() -> refreshDevice(account, device, currentGeneration),
                        0, config.refreshInterval, TimeUnit.SECONDS);
                return;
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.configuration-error.vacuum-not-found");
    }

    static boolean supportsMqttDiagnostics(DreameDevice device) {
        return DreameVacuumCapabilities.isSupported(device);
    }

    private void refreshDevice(DreameAccountHandler account, DreameDevice device, int currentGeneration) {
        DreameVacuumApi api = account.getVacuumApi();
        if (api != null) {
            refreshProperties(api, device, currentGeneration);
            refreshMap(api, device, currentGeneration);
        }
    }

    void refreshMap(DreameVacuumApi api, DreameDevice device, int currentGeneration) {
        int currentConnection;
        long revision;
        synchronized (this) {
            if (!active || generation.get() != currentGeneration || !mapState.needsBase() || mapQueryRunning) {
                return;
            }
            mapQueryRunning = true;
            currentConnection = connectionGeneration;
            revision = mapRevision;
        }
        try {
            String encoded = api.getVacuumMap(device, () -> generation.get() == currentGeneration);
            if (encoded == null) {
                return;
            }
            synchronized (this) {
                if (active && generation.get() == currentGeneration && connectionGeneration == currentConnection
                        && mapRevision == revision) {
                    receiveMapData(currentGeneration, currentConnection, encoded);
                }
            }
        } catch (DreameCloudException | IllegalArgumentException e) {
            logger.debug("Vacuum map query failed; retrying on the next refresh");
        } finally {
            synchronized (this) {
                if (generation.get() == currentGeneration && connectionGeneration == currentConnection) {
                    mapQueryRunning = false;
                }
            }
        }
    }

    void refreshProperties(DreameVacuumApi api, DreameDevice device, int currentGeneration) {
        int currentConnection;
        long revision;
        synchronized (this) {
            if (!active || generation.get() != currentGeneration) {
                return;
            }
            currentConnection = connectionGeneration;
            revision = statusRevision;
        }
        try {
            DreameVacuumProperties properties = api.getVacuumProperties(device,
                    () -> generation.get() == currentGeneration);
            Map<String, State> updates = DreameVacuumStatus.channelUpdates(properties,
                    DreameVacuumCapabilities.usesNewStateSchema(device));
            if (updates.isEmpty()) {
                throw new DreameCloudException("Vacuum returned no valid status properties");
            }
            synchronized (this) {
                if (!active || generation.get() != currentGeneration || connectionGeneration != currentConnection) {
                    return;
                }
                availableProperties.addAll(properties.numeric().keySet());
                availableProperties.addAll(properties.text().keySet());
                // A push received during the request is newer than its snapshot for that channel.
                updates.forEach((channel, state) -> {
                    if (channelRevisions.getOrDefault(channel, 0L) <= revision) {
                        publishState(channel, state);
                    }
                });
                consecutiveStatusFailures = 0;
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (DreameCloudException | IllegalArgumentException e) {
            synchronized (this) {
                if (active && generation.get() == currentGeneration && connectionGeneration == currentConnection
                        && statusRevision == revision) {
                    consecutiveStatusFailures++;
                    if (consecutiveStatusFailures >= STATUS_FAILURES_BEFORE_OFFLINE) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.communication-error.device-status");
                    } else {
                        logger.debug("Device status query failed once; retrying on the next refresh");
                    }
                }
            }
        }
        try {
            String objectName = api.getVacuumMapListObjectName(device, () -> generation.get() == currentGeneration);
            if (objectName != null) {
                receiveMapList(currentGeneration, currentConnection, objectName);
            }
        } catch (DreameCloudException | IllegalArgumentException e) {
            logger.trace("Vacuum saved-map reference query failed; retrying on the next refresh");
        }
    }

    private void maintainMqtt(DreameAccountHandler account, DreameDevice device, int currentGeneration) {
        if (generation.get() != currentGeneration) {
            return;
        }
        try {
            DreameMqttConfiguration configuration = account.getApiClient().mqttConfiguration(device);
            DreameVacuumMqttClient previous;
            int currentConnection;
            synchronized (this) {
                if (!active || generation.get() != currentGeneration) {
                    return;
                }
                DreameVacuumMqttClient current = mqttClient;
                DreameMqttConfiguration previousConfiguration = mqttConfiguration;
                if (current != null && current.isSubscribed() && previousConfiguration != null
                        && configuration.sameConnection(previousConfiguration)) {
                    return;
                }
                currentConnection = ++connectionGeneration;
                clearStates();
                previous = current;
                mqttClient = null;
                mqttConfiguration = null;
            }
            if (previous != null) {
                previous.close();
            }
            DreameVacuumMqttClient replacement = new DreameVacuumMqttClient(configuration, device.model(),
                    diagnostic -> {
                        if (generation.get() == currentGeneration) {
                            logger.trace("Vacuum MQTT diagnostics: {}", diagnostic);
                        }
                    }, properties -> receiveProperties(currentGeneration, currentConnection, properties),
                    map -> receiveMapData(currentGeneration, currentConnection, map),
                    objectName -> receiveMapList(currentGeneration, currentConnection, objectName));
            boolean accepted = false;
            try {
                synchronized (this) {
                    if (active && generation.get() == currentGeneration) {
                        mqttClient = replacement;
                        mqttConfiguration = configuration;
                        replacement.connect();
                        accepted = true;
                    }
                }
            } finally {
                if (!accepted) {
                    replacement.close();
                }
            }
        } catch (DreameCloudException | MqttException | IllegalArgumentException e) {
            if (generation.get() == currentGeneration) {
                logger.debug("Vacuum MQTT setup failed; retrying in 60 seconds");
            }
        }
    }

    private void stopMqtt() {
        generation.incrementAndGet();
        commands.clear();
        connectionGeneration++;
        clearStates();
        ScheduledFuture<?> job = mqttJob;
        mqttJob = null;
        if (job != null) {
            job.cancel(false);
        }
        ScheduledFuture<?> poll = pollingJob;
        pollingJob = null;
        if (poll != null) {
            poll.cancel(false);
        }
        DreameVacuumMqttClient client = mqttClient;
        mqttClient = null;
        mqttConfiguration = null;
        mapApi = null;
        mapDevice = null;
        mapQueryRunning = false;
        mapListQueryRunning = false;
        mapListObjectName = null;
        immediateBaseRequestTriggered = false;
        if (client != null) {
            scheduler.execute(client::close);
        }
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (active && command == RefreshType.REFRESH
                && (DreameVacuumStatus.CHANNELS.contains(channelUID.getId()) || "map-png".equals(channelUID.getId())
                        || "map-svg".equals(channelUID.getId()) || "rooms".equals(channelUID.getId()))) {
            updateState(channelUID, states.getOrDefault(channelUID.getId(), UnDefType.UNDEF));
            return;
        }
        if (!active || !(command instanceof StringType text)) {
            return;
        }
        if ("room-cleaning".equals(channelUID.getId())) {
            List<Integer> roomIds = parseRoomIds(text.toString());
            if (roomIds.isEmpty()) {
                logger.debug("Ignoring invalid room-cleaning command");
                return;
            }
            int suction = settingValue("suction-level", DreameVacuumSetting.SUCTION_LEVEL, 1);
            int water = settingValue("water-volume", DreameVacuumSetting.WATER_VOLUME, 2);
            int currentGeneration = generation.get();
            commandExecutor.execute(() -> executeRoomCleaning(currentGeneration, roomIds, suction, water));
            return;
        }
        DreameVacuumSetting setting = switch (channelUID.getId()) {
            case "suction-level" -> DreameVacuumSetting.SUCTION_LEVEL;
            case "water-volume" -> DreameVacuumSetting.WATER_VOLUME;
            case "cleaning-mode" -> DreameVacuumSetting.CLEANING_MODE;
            case "drying-time" -> DreameVacuumSetting.DRYING_TIME;
            case "clean-genius" -> DreameVacuumSetting.CLEAN_GENIUS;
            case "cleaning-route" -> DreameVacuumSetting.CLEANING_ROUTE;
            default -> null;
        };
        if (setting != null) {
            Integer value = setting.value(text.toString());
            if (value == null) {
                logger.debug("Ignoring unsupported {} command", channelUID.getId());
                return;
            }
            int currentGeneration = generation.get();
            commandExecutor.execute(() -> executeSetting(currentGeneration, setting, value));
            return;
        }
        if (!"command".equals(channelUID.getId())) {
            return;
        }
        DreameVacuumAction action;
        try {
            action = DreameVacuumAction.valueOf(text.toString().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logger.debug("Ignoring unsupported vacuum command");
            return;
        }
        if (commands.size() >= 8) {
            logger.debug("Vacuum command queue is full; command ignored");
            return;
        }
        commands.addLast(new PendingCommand(generation.get(), action));
        if (!commandWorkerRunning) {
            commandWorkerRunning = true;
            commandExecutor.execute(this::drainCommands);
        }
    }

    private void drainCommands() {
        while (true) {
            PendingCommand pending;
            synchronized (this) {
                if (commands.isEmpty()) {
                    commandWorkerRunning = false;
                    return;
                }
                pending = java.util.Objects.requireNonNull(commands.removeFirst());
            }
            try {
                executeCommand(pending);
            } catch (RuntimeException e) {
                // Keep the queue usable without exposing exception text that could contain cloud data.
                logger.warn("Unexpected vacuum command processing failure ({})", e.getClass().getSimpleName());
            }
        }
    }

    private void executeCommand(PendingCommand pending) {
        if (generation.get() != pending.generation()) {
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE
                || !(bridge.getHandler() instanceof DreameAccountHandler account)) {
            return;
        }
        DreameVacuumApi api = account.getVacuumApi();
        if (api == null) {
            return;
        }
        Object configuredId = getConfig().get("deviceId");
        for (DreameDevice device : account.getVacuumDevices()) {
            if (device.id().equals(configuredId) && supportsMqttDiagnostics(device)) {
                if (!supportsAction(pending.action())) {
                    logger.debug("Vacuum command {} is unavailable for model {}", pending.action(), device.model());
                    return;
                }
                try {
                    api.callVacuumAction(device, pending.action(),
                            () -> generation.get() == pending.generation() && bridge.getStatus() == ThingStatus.ONLINE);
                    if (generation.get() == pending.generation()) {
                        publishCommandState("command", pending.action().name(), pending.generation());
                        logger.debug("Vacuum command {} acknowledged; waiting for device status", pending.action());
                    }
                } catch (DreameCloudException | IllegalArgumentException e) {
                    if (generation.get() == pending.generation()) {
                        logger.debug("Vacuum command {} failed; no automatic retry", pending.action());
                    }
                }
                return;
            }
        }
    }

    private void executeSetting(int currentGeneration, DreameVacuumSetting setting, int value) {
        if (generation.get() != currentGeneration) {
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE
                || !(bridge.getHandler() instanceof DreameAccountHandler account)) {
            return;
        }
        DreameVacuumApi api = account.getVacuumApi();
        Object configuredId = getConfig().get("deviceId");
        if (api == null) {
            return;
        }
        for (DreameDevice device : account.getVacuumDevices()) {
            if (device.id().equals(configuredId) && supportsMqttDiagnostics(device)) {
                synchronized (this) {
                    if (!availableProperties.contains(setting.address())) {
                        logger.debug("Vacuum setting {} is unavailable for model {}", setting, device.model());
                        return;
                    }
                }
                try {
                    api.setVacuumSetting(device, setting, value,
                            () -> generation.get() == currentGeneration && bridge.getStatus() == ThingStatus.ONLINE);
                    String state = setting.state(value);
                    if (state != null) {
                        publishCommandState(setting.channelId(), state, currentGeneration);
                    }
                    logger.debug("Vacuum setting {} acknowledged; waiting for device status", setting);
                } catch (DreameCloudException | IllegalArgumentException e) {
                    logger.debug("Vacuum setting {} failed; no automatic retry", setting);
                }
                return;
            }
        }
    }

    private List<Integer> parseRoomIds(String command) {
        try {
            List<Integer> ids = java.util.Arrays.stream(command.split(",")).map(String::trim)
                    .filter(value -> !value.isEmpty()).map(Integer::valueOf).distinct().toList();
            return ids.size() <= 32 && ids.stream().allMatch(id -> id >= 1 && id <= 63) ? ids : List.of();
        } catch (NumberFormatException e) {
            return List.of();
        }
    }

    private int settingValue(String channel, DreameVacuumSetting setting, int fallback) {
        State state = states.get(channel);
        Integer value = state instanceof StringType string ? setting.value(string.toString()) : null;
        return value != null ? value : fallback;
    }

    private void executeRoomCleaning(int currentGeneration, List<Integer> roomIds, int suction, int water) {
        if (generation.get() != currentGeneration) {
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE
                || !(bridge.getHandler() instanceof DreameAccountHandler account)) {
            return;
        }
        DreameVacuumApi api = account.getVacuumApi();
        Object configuredId = getConfig().get("deviceId");
        if (api == null) {
            return;
        }
        for (DreameDevice device : account.getVacuumDevices()) {
            if (device.id().equals(configuredId) && supportsMqttDiagnostics(device)) {
                try {
                    api.cleanVacuumRooms(device, roomIds, suction, water,
                            () -> generation.get() == currentGeneration && bridge.getStatus() == ThingStatus.ONLINE);
                    if (generation.get() == currentGeneration) {
                        String roomCommand = roomIds.stream().map(String::valueOf)
                                .collect(java.util.stream.Collectors.joining(","));
                        publishCommandState("room-cleaning", roomCommand, currentGeneration);
                        logger.debug("Vacuum room cleaning acknowledged; waiting for device status");
                    }
                } catch (DreameCloudException | IllegalArgumentException e) {
                    logger.debug("Vacuum room cleaning failed; no automatic retry");
                }
                return;
            }
        }
    }

    synchronized void receiveProperties(int currentGeneration, int currentConnection, Map<String, Integer> properties) {
        if (!active || generation.get() != currentGeneration || connectionGeneration != currentConnection) {
            return;
        }
        DreameDevice device = mapDevice;
        Map<String, State> updates = DreameVacuumStatus.channelUpdates(properties,
                device == null || DreameVacuumCapabilities.usesNewStateSchema(device));
        availableProperties.addAll(properties.keySet());
        if (!updates.isEmpty()) {
            updates.forEach(this::publishState);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private synchronized boolean supportsAction(DreameVacuumAction action) {
        return switch (action) {
            case AUTO_EMPTY -> availableProperties.contains("15/3");
            case WASH_MOPS, PAUSE_WASHING -> availableProperties.contains("4/25");
            case START_DRYING, STOP_DRYING ->
                availableProperties.contains("4/25") || availableProperties.contains("4/40");
            default -> true;
        };
    }

    synchronized void receiveMapData(int currentGeneration, int currentConnection, String encoded) {
        if (!active || generation.get() != currentGeneration || connectionGeneration != currentConnection) {
            return;
        }
        DreameVacuumMapState.Images images = mapState.accept(encoded);
        if (images != null) {
            mapRevision++;
            immediateBaseRequestTriggered = false;
            publishMap(images);
            logger.debug("Vacuum map frame published: {}", mapState.diagnostic());
        } else {
            logger.debug("Vacuum map frame not published: {}", mapState.diagnostic());
            requestMissingBase(currentGeneration);
        }
    }

    synchronized void receiveMapList(int currentGeneration, int currentConnection, String objectName) {
        if (!active || generation.get() != currentGeneration || connectionGeneration != currentConnection
                || objectName.equals(mapListObjectName) || mapListQueryRunning) {
            return;
        }
        DreameVacuumApi api = mapApi;
        DreameDevice device = mapDevice;
        if (api == null || device == null) {
            return;
        }
        mapListQueryRunning = true;
        commandExecutor.execute(() -> refreshMapList(api, device, currentGeneration, currentConnection, objectName));
    }

    private void refreshMapList(DreameVacuumApi api, DreameDevice device, int currentGeneration, int currentConnection,
            String objectName) {
        try {
            String raw = api.getVacuumMapList(device, objectName, () -> generation.get() == currentGeneration);
            synchronized (this) {
                if (active && generation.get() == currentGeneration && connectionGeneration == currentConnection) {
                    DreameVacuumMapState.Images images = mapState.acceptMapList(raw);
                    mapListObjectName = objectName;
                    if (images != null) {
                        publishMap(images);
                        logger.debug("Vacuum saved-map room metadata published: {}", mapState.diagnostic());
                    } else {
                        logger.debug("Vacuum saved-map list did not update the current map: {}", mapState.diagnostic());
                    }
                }
            }
        } catch (DreameCloudException | IllegalArgumentException e) {
            logger.debug("Vacuum saved-map list query failed; waiting for the next device update");
        } finally {
            synchronized (this) {
                if (generation.get() == currentGeneration && connectionGeneration == currentConnection) {
                    mapListQueryRunning = false;
                }
            }
        }
    }

    private void publishMap(DreameVacuumMapState.Images images) {
        RawType png = new RawType(images.png(), "image/png");
        publishState("map-png", png);
        publishState("map-svg", new RawType(images.svg(), "image/svg+xml"));
        publishState("rooms", new StringType(images.rooms()));
    }

    private void requestMissingBase(int currentGeneration) {
        DreameVacuumApi api = mapApi;
        DreameDevice device = mapDevice;
        if (!mapState.needsBase() || immediateBaseRequestTriggered || api == null || device == null) {
            return;
        }
        immediateBaseRequestTriggered = true;
        commandExecutor.execute(() -> refreshMap(api, device, currentGeneration));
    }

    private void publishState(String channel, State state) {
        channelRevisions.put(channel, ++statusRevision);
        states.put(channel, state);
        updateState(channel, state);
    }

    private synchronized void publishCommandState(String channel, String value, int commandGeneration) {
        if (active && generation.get() == commandGeneration) {
            publishState(channel, new StringType(value));
        }
    }

    private void clearStates() {
        mapState.clear();
        mapListQueryRunning = false;
        mapListObjectName = null;
        immediateBaseRequestTriggered = false;
        mapRevision++;
        updateState("map-png", UnDefType.UNDEF);
        updateState("map-svg", UnDefType.UNDEF);
        updateState("rooms", UnDefType.UNDEF);
        states.clear();
        availableProperties.clear();
        channelRevisions.clear();
        DreameVacuumStatus.CHANNELS.forEach(channel -> updateState(channel, UnDefType.UNDEF));
    }

    @Override
    public synchronized void dispose() {
        active = false;
        stopMqtt();
        super.dispose();
    }
}
