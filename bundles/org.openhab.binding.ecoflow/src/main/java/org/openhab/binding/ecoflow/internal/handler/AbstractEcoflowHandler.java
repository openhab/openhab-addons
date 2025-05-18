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

package org.openhab.binding.ecoflow.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final String mqttParamObjectName;
    protected String serialNumber = "<unset>";
    private final Map<String, ChannelMapping> mappingsByListId = new HashMap<>();
    private final Map<String, Map<String, ChannelMapping>> mappingsByMqttId = new HashMap<>();

    protected AbstractEcoflowHandler(Thing thing, List<ChannelMapping> mappings, String mqttParamObjectName) {
        super(thing);
        this.mqttParamObjectName = mqttParamObjectName;
        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);

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
        final EcoflowApi api = getApiFromHandler();
        if (api == null) {
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
                api.sendSetRequest(serialNumber, json);
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
            updateStatus(ThingStatus.UNKNOWN);
            // Now wait for MQTT connection callback
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        initTask.cancel();
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    private void initDevice() {
        final EcoflowApi api = getApiFromHandler();
        if (api == null) {
            logger.trace("{}: No API, setting offline", serialNumber);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

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
    }

    public void handleQuotaMessage(JsonObject payload) {
        logger.trace("{}: Got MQTT message for quota: {}", serialNumber, payload);
        JsonObject params = payload.getAsJsonObject(mqttParamObjectName);
        extractGroupKeyFromMqttMessage(payload) //
                .flatMap(groupKey -> Optional.ofNullable(mappingsByMqttId.get(groupKey))) //
                .ifPresent(mappings -> updateStatesFromJson(params, mappings));
    }

    public void handleStatusMessage(JsonObject payload) {
        boolean online = payload.getAsJsonObject("params").get("status").getAsInt() != 0;
        @Nullable
        EcoflowApi api = getApiFromHandler();
        if (api != null) {
            try {
                initializeChannelStates(api, online);
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

    private @Nullable EcoflowApi getApiFromHandler() {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            return null;
        }
        if (bridge.getHandler() instanceof EcoflowApiHandler handler) {
            return handler.getApi();
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
