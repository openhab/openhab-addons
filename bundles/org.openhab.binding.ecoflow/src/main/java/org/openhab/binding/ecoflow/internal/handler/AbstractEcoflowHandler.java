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
package org.openhab.binding.ecoflow.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecoflow.internal.api.EcoflowApi;
import org.openhab.binding.ecoflow.internal.api.EcoflowApiException;
import org.openhab.binding.ecoflow.internal.api.dto.response.DeviceListResponseEntry;
import org.openhab.binding.ecoflow.internal.config.EcoflowDeltaConfiguration;
import org.openhab.binding.ecoflow.internal.util.SchedulerTask;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
abstract class AbstractEcoflowHandler extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(AbstractEcoflowHandler.class);

    private final SchedulerTask initTask;
    private final SchedulerTask mqttMessageWatchdogTask;
    protected String serialNumber = "<unset>";
    private final Map<String, ChannelMapping> mappingsByListId = new HashMap<>();
    private final Map<String, Map<String, ChannelMapping>> mappingsByMqttId = new HashMap<>();
    private final Map<String, Instant> mqttMessageTimestampsByGroup = new HashMap<>();

    private static final int MQTT_MESSAGE_TIMEOUT_SECONDS = 5 * 60;

    protected AbstractEcoflowHandler(Thing thing, List<ChannelMapping> mappings) {
        super(thing);
        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        mqttMessageWatchdogTask = new SchedulerTask(scheduler, logger, "MQTT Message Watchdog",
                this::checkForStaleMqttMessages);

        for (ChannelMapping mapping : mappings) {
            mappingsByListId.put(mapping.groupKey + "." + mapping.valueKey, mapping);
            Map<String, ChannelMapping> mqttMappings = mappingsByMqttId.get(mapping.groupKey);
            if (mqttMappings == null) {
                mqttMappings = new HashMap<>();
                mappingsByMqttId.put(mapping.groupKey, mqttMappings);
            }
            mqttMappings.put(mapping.valueKey, mapping);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final EcoflowApiHandler apiHandler = getApiHandler();
        if (apiHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        if (command == RefreshType.REFRESH) {
            initTask.submit();
            return;
        }

        convertCommand(channelUID.getId(), command).ifPresentOrElse(json -> {
            try {
                logger.trace("{}: Send request {}", serialNumber, json);
                apiHandler.getApi().sendSetRequest(serialNumber, json);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (EcoflowApiException e) {
                logger.warn("{}: Could not send command {} for channel {}", serialNumber, command, channelUID, e);
            }
        }, () -> {
            logger.warn("{}: Channel {} got unhandled command {}", serialNumber, channelUID, command);
        });
    }

    @Override
    public void initialize() {
        serialNumber = getConfigAs(EcoflowDeltaConfiguration.class).serialNumber;
        if (serialNumber.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.config-error-no-serial");
        } else {
            logger.debug("{}: Initializing handler", serialNumber);
            initTask.setNamePrefix(serialNumber);
            mqttMessageWatchdogTask.setNamePrefix(serialNumber);
            updateStatus(ThingStatus.UNKNOWN);
            // Now wait for MQTT connection callback
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        initTask.cancel();
        mqttMessageWatchdogTask.cancel();
        mqttMessageTimestampsByGroup.clear();
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    private void initDevice() {
        final EcoflowApiHandler apiHandler = getApiHandler();
        if (apiHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        final EcoflowApi api = apiHandler.getApi();
        try {
            Optional<DeviceListResponseEntry> deviceStatusOpt = api.getDeviceList().stream()
                    .filter(d -> d.serialNumber.equals(serialNumber)).findFirst();
            if (!deviceStatusOpt.isPresent()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-device-not-present");
                return;
            }

            DeviceListResponseEntry deviceStatus = deviceStatusOpt.get();
            initializeChannelStates(api, deviceStatus.isOnline());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        } catch (EcoflowApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void handleMqttConnected() {
        initTask.submit();
        mqttMessageWatchdogTask.scheduleRecurring(2, TimeUnit.MINUTES, false);
    }

    public void handleQuotaMessage(JsonObject payload) {
        JsonObject params = payload.getAsJsonObject("params");
        if (params == null) {
            logger.warn("{}: No parameters in quota message payload: {}", serialNumber, payload);
            return;
        }

        Optional<String> groupKeyOpt = extractGroupKeyFromMqttMessage(payload);
        logger.trace("{}: Got MQTT message for quota: group {}, payload {}", serialNumber,
                groupKeyOpt.orElse("<unknown>"), payload);
        groupKeyOpt.flatMap(groupKey -> Optional.ofNullable(mappingsByMqttId.get(groupKey)))
                .ifPresent(mappings -> updateStatesFromJson(params, mappings));
        groupKeyOpt.ifPresent(groupKey -> {
            synchronized (mqttMessageTimestampsByGroup) {
                mqttMessageTimestampsByGroup.put(groupKey, Instant.now());
            }
        });
    }

    public void handleStatusMessage(JsonObject payload) {
        boolean online = payload.getAsJsonObject("params").get("status").getAsInt() != 0;
        @Nullable
        EcoflowApiHandler apiHandler = getApiHandler();
        if (apiHandler != null) {
            try {
                initializeChannelStates(apiHandler.getApi(), online);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (EcoflowApiException e) {
                logger.warn("{}: Could not update thing state after status message {}", serialNumber, payload, e);
            }
        }
    }

    private void initializeChannelStates(EcoflowApi api, boolean online)
            throws EcoflowApiException, InterruptedException {
        if (online) {
            JsonObject data = api.getDeviceData(serialNumber);
            logger.trace("{}: Update channel states from JSON data {}", serialNumber, data);

            updateStatesFromJson(data, mappingsByListId);
            updateStatus(ThingStatus.ONLINE);
        } else {
            for (Channel channel : getThing().getChannels()) {
                updateState(channel.getUID(), UnDefType.NULL);
            }
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void updateStatesFromJson(JsonObject json, Map<String, ChannelMapping> mappings) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            ChannelMapping mapping = mappings.get(entry.getKey());
            if (mapping != null && entry.getValue().isJsonPrimitive()) {
                State converted = mapping.converter.convertValue(entry.getValue().getAsJsonPrimitive());
                logger.trace("{}: Updating state of channel {} from JSON property {} with value {} -> {}", serialNumber,
                        mapping.channelId, entry.getKey(), entry.getValue(), converted);
                updateState(mapping.channelId, converted);
            }
        }
    }

    private void checkForStaleMqttMessages() {
        final Instant now = Instant.now();
        Set<String> staleGroups = new HashSet<>();
        synchronized (mqttMessageTimestampsByGroup) {
            if (mqttMessageTimestampsByGroup.isEmpty()) {
                staleGroups.add("<all>");
            } else {
                for (Map.Entry<String, Instant> entry : mqttMessageTimestampsByGroup.entrySet()) {
                    logger.trace("{}: MQTT message group {} last timestamp: {} ago", serialNumber, entry.getKey(),
                            Duration.between(entry.getValue(), now));
                    if (entry.getValue().plusSeconds(MQTT_MESSAGE_TIMEOUT_SECONDS).isBefore(now)) {
                        staleGroups.add(entry.getKey());
                    }
                }
            }
        }
        if (!staleGroups.isEmpty()) {
            logger.warn("{}: MQTT messages in group(s) {} appear stalled, updating subscription", serialNumber,
                    staleGroups);
            final EcoflowApiHandler apiHandler = getApiHandler();
            if (apiHandler != null) {
                mqttMessageWatchdogTask.cancel();
                synchronized (mqttMessageTimestampsByGroup) {
                    mqttMessageTimestampsByGroup.clear();
                }
                apiHandler.resubscribeToDevice(serialNumber);
            }
        }
    }

    private @Nullable EcoflowApiHandler getApiHandler() {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            return null;
        }
        if (bridge.getHandler() instanceof EcoflowApiHandler handler) {
            return handler;
        }
        throw new IllegalStateException("AbstractEcoflowHandler must be a child handler of EcoflowApiHandler");
    }

    protected static interface ValueConverter {
        State convertValue(JsonElement value);
    }

    protected static record ChannelMapping(String groupKey, String valueKey, String channelId,
            ValueConverter converter) {
        public ChannelMapping(String groupKey, String valueKey, String channelId, double factor, Unit<?> unit) {
            this(groupKey, valueKey, channelId, value -> new QuantityType<>(value.getAsDouble() * factor, unit));
        }
    }

    protected abstract Optional<JsonObject> convertCommand(String channelId, Command command);

    protected abstract Optional<String> extractGroupKeyFromMqttMessage(JsonObject payload);
}
