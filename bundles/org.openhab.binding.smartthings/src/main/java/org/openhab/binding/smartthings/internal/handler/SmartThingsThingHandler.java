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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsDeviceIdResolver;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverter;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatus;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusCapabilities;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusProperties;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistryImpl;
import org.openhab.binding.smartthings.internal.type.UidUtils;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsThingHandler extends BaseThingHandler {
    private static final String CAPABILITY_AIR_CONDITIONER_FAN_MODE = "airConditionerFanMode";
    private static final String CAPABILITY_AIR_CONDITIONER_MODE = "airConditionerMode";
    private static final String CAPABILITY_AIR_CONDITIONER_OPTIONAL_MODE = "custom.airConditionerOptionalMode";
    private static final String CAPABILITY_FRAME_AMBIENT = "samsungvd.ambient";
    private static final String CAPABILITY_OVEN_OPERATING_STATE = "samsungce.ovenOperatingState";
    private static final String CAPABILITY_THERMOSTAT_COOLING_SETPOINT = "thermostatCoolingSetpoint";
    private static final String CAPABILITY_TV_CHANNEL = "tvChannel";
    private static final String ATTRIBUTE_OPERATION_TIME = "operationTime";
    private static final String CHANNEL_AIR_CONDITIONER_MODE = "air-conditioner-mode";
    private static final String CHANNEL_AMBIENT = "ambient";
    private static final String CHANNEL_AMBIENT_MODE = "ambient-mode";
    private static final String CHANNEL_FAN_MODE = "fan-mode";
    private static final String CHANNEL_OPTIONAL_MODE = "ac-optional-mode";
    private static final String CHANNEL_SETPOINT = "cooling-setpoint";
    private static final String CHANNEL_TV_CHANNEL = "tv-channel";
    private static final String STATIC_CHANNEL_MODE = "mode";
    private static final String STATIC_CHANNEL_ART_MODE = "art-mode";
    private static final String STATIC_CHANNEL_SETPOINT = "setpoint";
    private static final String STATIC_CHANNEL_TV_CHANNEL = "channel";
    private static final int STATUS_REFRESH_COMMUNICATION_FAILURE_THRESHOLD = 3;
    private static final String OPTIONAL_MODE_OFF = "off";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsThingHandler.class);

    private String smartThingsName;

    private String deviceId;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private long lastRefresh = System.nanoTime();
    private @Nullable Object airConditionerFanModeValue;
    private boolean airConditionerOptionalModeActive;
    private int consecutiveStatusRefreshCommunicationFailures;

    public SmartThingsThingHandler(Thing thing) {
        super(thing);
        deviceId = "";
        smartThingsName = ""; // Initialize here so it can be NonNull but it should always get a value in initialize()
    }

    /**
     * Called when openHAB receives a command for this handler
     *
     * @param channelUID The channel the command was sent to
     * @param command The command sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();

        // Check if the bridge has not been initialized yet
        if (bridge == null) {
            logger.debug(
                    "The bridge has not been initialized yet. Can not process command for channel {} with command {}.",
                    channelUID.getAsString(), command.toFullString());
            return;
        }

        SmartThingsAccountHandler accountHander = (SmartThingsAccountHandler) bridge.getHandler();

        if (accountHander != null && accountHander.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            if (command instanceof RefreshType) {
                refreshDevice();
            } else {
                SmartThingsApi api = accountHander.getSmartThingsApi();
                if (api != null) {
                    handleCommand(api, channelUID, command);
                }
            }
        }
    }

    void handleCommand(SmartThingsApi api, ChannelUID channelUID, Command command) {
        SmartThingsConverter converter = getConverter(channelUID);
        if (converter == null) {
            logger.warn("No converter found for command {} on channel {}.", command.toFullString(), channelUID);
            return;
        }

        String jsonMsg;
        try {
            jsonMsg = converter.convertToSmartThings(thing, channelUID, command);
        } catch (SmartThingsException ex) {
            logger.warn("Unable to convert command {} for channel {}: {}", command.toFullString(), channelUID,
                    ex.getMessage());
            logger.debug("Unable to convert SmartThings command.", ex);
            return;
        }

        if (jsonMsg.isBlank()) {
            logger.debug("No SmartThings command was created for command {} on channel {}.", command.toFullString(),
                    channelUID);
            return;
        }

        try {
            api.sendCommand(deviceId, jsonMsg);
        } catch (SmartThingsException ex) {
            logger.warn("Unable to send command {} to SmartThings channel {}: {}", command.toFullString(), channelUID,
                    ex.getMessage());
            logger.debug("Unable to send SmartThings command payload: {}", jsonMsg, ex);
        }
    }

    public void refreshChannel(String deviceType, String componentId, String namespace, String capaKey, String attr,
            Object value) throws SmartThingsException {
        String channelName = SmartThingsTypeRegistryImpl.getChannelName(attr);
        String capabilityId = namespace.isBlank() ? capaKey : namespace + "." + capaKey;

        if (handleMergedAirConditionerFanMode(deviceType, componentId, capabilityId, channelName, value)) {
            return;
        }

        ChannelUID channelUID = findChannelUID(deviceType, componentId, capabilityId, channelName);

        if (channelUID == null) {
            logger.trace("Ignoring state update for undeclared channel: component: {}, capability: {}, attribute: {}",
                    componentId, capabilityId, attr);
            return;
        }

        logger.trace("refreshDevice called: channelName:{}", channelName);

        updateChannelState(channelUID, value);
    }

    private boolean handleMergedAirConditionerFanMode(String deviceType, String componentId, String capabilityId,
            String channelName, Object value) throws SmartThingsException {
        if (CAPABILITY_AIR_CONDITIONER_FAN_MODE.equals(capabilityId) && CHANNEL_FAN_MODE.equals(channelName)) {
            airConditionerFanModeValue = value;
            return airConditionerOptionalModeActive;
        }

        if (!CAPABILITY_AIR_CONDITIONER_OPTIONAL_MODE.equals(capabilityId)
                || !CHANNEL_OPTIONAL_MODE.equals(channelName)) {
            return false;
        }

        ChannelUID optionalModeChannel = findChannelUIDByChannelName(deviceType, componentId, capabilityId,
                channelName);
        if (optionalModeChannel != null) {
            return false;
        }

        ChannelUID fanModeChannel = findChannelUID(deviceType, componentId, CAPABILITY_AIR_CONDITIONER_FAN_MODE,
                CHANNEL_FAN_MODE);
        if (fanModeChannel == null) {
            return true;
        }

        if (OPTIONAL_MODE_OFF.equalsIgnoreCase(value.toString())) {
            airConditionerOptionalModeActive = false;
            Object fanModeValue = airConditionerFanModeValue;
            if (fanModeValue != null) {
                updateChannelState(fanModeChannel, fanModeValue);
            } else {
                updateState(fanModeChannel, UnDefType.UNDEF);
            }
            return true;
        }

        airConditionerOptionalModeActive = true;
        updateChannelState(fanModeChannel, value);
        return true;
    }

    private void updateChannelState(ChannelUID channelUID, Object value) throws SmartThingsException {
        SmartThingsConverter converter = getConverter(channelUID);
        if (converter != null) {
            State state = converter.convertToOpenHab(thing, channelUID, value);
            updateState(channelUID, state);
        }
    }

    private @Nullable SmartThingsConverter getConverter(ChannelUID channelUID) {
        Channel channel = thing.getChannel(channelUID);
        if (channel != null) {
            String converterKey = channel.getProperties().get(SmartThingsBindingConstants.CONVERTER);
            if (converterKey != null && !converterKey.isBlank()) {
                return SmartThingsConverterFactory.getConverter(converterKey);
            }
        }
        return SmartThingsConverterFactory.getConverter(channelUID.getIdWithoutGroup());
    }

    private @Nullable ChannelUID findChannelUID(String deviceType, String componentId, String capabilityId,
            String channelName) {
        ChannelUID channelUID = findChannelUIDByChannelName(deviceType, componentId, capabilityId, channelName);
        if (channelUID != null) {
            return channelUID;
        }

        String staticChannelName = getStaticChannelName(capabilityId, channelName);
        return channelName.equals(staticChannelName) ? null
                : findChannelUIDByChannelName(deviceType, componentId, capabilityId, staticChannelName);
    }

    private @Nullable ChannelUID findChannelUIDByChannelName(String deviceType, String componentId, String capabilityId,
            String channelName) {
        String dynamicGroupId = SmartThingsTypeRegistryImpl.getChannelGroupId(deviceType, componentId, capabilityId);
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), dynamicGroupId, channelName);
        if (thing.getChannel(channelUID) != null) {
            return channelUID;
        }

        channelUID = new ChannelUID(this.getThing().getUID(), UidUtils.sanitizeId(componentId), channelName);
        if (thing.getChannel(channelUID) != null) {
            return channelUID;
        }

        return findChannelUIDByProperties(componentId, capabilityId, channelName);
    }

    private String getStaticChannelName(String capabilityId, String channelName) {
        if (CAPABILITY_AIR_CONDITIONER_MODE.equals(capabilityId) && CHANNEL_AIR_CONDITIONER_MODE.equals(channelName)) {
            return STATIC_CHANNEL_MODE;
        }
        if (CAPABILITY_THERMOSTAT_COOLING_SETPOINT.equals(capabilityId) && CHANNEL_SETPOINT.equals(channelName)) {
            return STATIC_CHANNEL_SETPOINT;
        }
        if (CAPABILITY_TV_CHANNEL.equals(capabilityId) && CHANNEL_TV_CHANNEL.equals(channelName)) {
            return STATIC_CHANNEL_TV_CHANNEL;
        }
        if (CAPABILITY_FRAME_AMBIENT.equals(capabilityId)
                && (CHANNEL_AMBIENT.equals(channelName) || CHANNEL_AMBIENT_MODE.equals(channelName))) {
            return STATIC_CHANNEL_ART_MODE;
        }
        return channelName;
    }

    private @Nullable ChannelUID findChannelUIDByProperties(String componentId, String capabilityId,
            String channelName) {
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (!channelName.equals(channelUID.getIdWithoutGroup())) {
                continue;
            }

            Map<String, String> properties = channel.getProperties();
            if (componentId.equals(properties.get(SmartThingsBindingConstants.COMPONENT))
                    && capabilityId.equals(properties.get(SmartThingsBindingConstants.CAPABILITY))) {
                return channelUID;
            }
        }
        return null;
    }

    public void refreshDevice(String deviceType, String componentId, String capa, String attr, Object value) {
        try {
            logger.trace("refreshDevice called: deviceType:{} componentId: {} capa: {} attr :{} value: {}", deviceType,
                    componentId, capa, attr, value);
            String namespace = "";
            String capaKey = capa;
            if (capa.contains(".")) {
                String[] idComponents = capa.split("\\.");
                namespace = idComponents[0];
                capaKey = idComponents[1];
            }

            if (attr.indexOf("Range") >= 0) {
                return;
            }

            if (value instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object subValue = entry.getValue();
                    if (key != null && subValue != null) {
                        String subKey = key.toString();
                        refreshChannel(deviceType, componentId, namespace, capaKey, subKey, subValue);
                    }
                }
            } else {
                refreshChannel(deviceType, componentId, namespace, capaKey, attr, value);
            }

            handleSuccessfulStateRefresh();
        } catch (Exception ex) {
            logger.warn("Unable to refresh SmartThings state for {}: {}", this.getThing().getUID(), ex.getMessage());
            logger.debug("Unable to refresh SmartThings state.", ex);
        }
    }

    public void sendUpdateState(ChannelUID channelUid, State state) {
        updateState(channelUid, state);
    }

    public void refreshDevice() {
        logger.trace("Refresh device called");
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return;
        }
        SmartThingsApi api = accountHandler.getSmartThingsApi();
        if (api == null) {
            return;
        }

        logger.trace("Refresh device for deviceId: {}", deviceId);

        try {
            SmartThingsStatus status = api.getStatus(deviceId);

            logger.trace("Refresh device for deviceId: status : {}", status);

            if (status != null) {
                for (String componentKey : status.components.keySet()) {
                    SmartThingsStatusComponent component = status.components.get(componentKey);

                    if (component != null) {
                        for (String capaKey : component.keySet()) {
                            SmartThingsStatusCapabilities capa = component.get(capaKey);

                            if (capa == null) {
                                continue;
                            }

                            if (SmartThingsPropMappings.isProperties(capaKey)) {
                                refreshDeviceProps(capa, componentKey, capaKey);
                            } else {
                                refreshDeviceFromCapa(capa, componentKey, capaKey);
                            }
                        }
                    }
                }
            }

            handleSuccessfulStateRefresh();
        } catch (SmartThingsException ex) {
            if (ex.isCommunicationError()) {
                handleStatusRefreshCommunicationFailure(ex);
            } else {
                logger.warn("Unable to update SmartThings device {}: {}", deviceId, ex.getMessage());
                logger.debug("Unable to update SmartThings device.", ex);
            }
        }
    }

    private void handleSuccessfulStateRefresh() {
        consecutiveStatusRefreshCommunicationFailures = 0;
        if (ThingStatusDetail.COMMUNICATION_ERROR.equals(thing.getStatusInfo().getStatusDetail()) && isBridgeOnline()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        }
    }

    private boolean isBridgeOnline() {
        Bridge bridge = getBridge();
        return bridge != null && ThingStatus.ONLINE.equals(bridge.getStatus());
    }

    private void handleStatusRefreshCommunicationFailure(SmartThingsException ex) {
        consecutiveStatusRefreshCommunicationFailures++;
        if (consecutiveStatusRefreshCommunicationFailures >= STATUS_REFRESH_COMMUNICATION_FAILURE_THRESHOLD) {
            logger.warn("Communication error while updating SmartThings device {} after {} consecutive attempts: {}",
                    deviceId, consecutiveStatusRefreshCommunicationFailures, ex.getMessage());
            logger.debug("Unable to update SmartThings device.", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        } else {
            logger.debug("Transient communication error while updating SmartThings device {} ({} of {}): {}", deviceId,
                    consecutiveStatusRefreshCommunicationFailures, STATUS_REFRESH_COMMUNICATION_FAILURE_THRESHOLD,
                    ex.getMessage());
            logger.trace("Transient SmartThings status refresh communication error.", ex);
        }
    }

    public void refreshDeviceFromCapa(SmartThingsStatusCapabilities capa, String componentKey, String capaKey) {
        for (String propertyKey : capa.keySet()) {
            SmartThingsStatusProperties props = capa.get(propertyKey);

            if (props != null) {
                Object value = getStatusPropertyValue(capaKey, propertyKey, props);

                if (value != null) {
                    logger.trace("Refresh device for deviceId: value : {}", value);

                    if (SmartThingsTypeRegistryImpl.shouldIgnoreCapaKey(capaKey)) {
                        continue;
                    }

                    refreshDevice(getSmartThingsDeviceType(), componentKey, capaKey, propertyKey, value);
                }
            }
        }
    }

    private @Nullable Object getStatusPropertyValue(String capaKey, String propertyKey,
            SmartThingsStatusProperties props) {
        String timestamp = props.timestamp;
        if (CAPABILITY_OVEN_OPERATING_STATE.equals(capaKey) && ATTRIBUTE_OPERATION_TIME.equals(propertyKey)
                && timestamp != null && !timestamp.isBlank()) {
            return timestamp;
        }
        return props.value;
    }

    public void refreshDeviceProps(SmartThingsStatusCapabilities capa, String componentKey, String capaKey) {
        Map<String, String> propMaps = new HashMap<>(editProperties());
        propMaps.remove(SmartThingsBindingConstants.DEVICE_ID);
        for (String propertyKey : capa.keySet()) {
            SmartThingsStatusProperties props = capa.get(propertyKey);

            if (props == null) {
                continue;
            }
            Object propsValue = props.value;

            if (propsValue == null) {
                continue;
            }
            logger.trace("refreshDeviceProps for deviceId: value : {}", propsValue);

            String propertyName = SmartThingsPropMappings.getPropertyName(propertyKey);
            if (propertyName == null) {
                continue;
            }
            addProps(propMaps, propertyName, propsValue.toString());
        }
        updateProperties(propMaps);
    }

    private void addProps(Map<String, String> props, String key, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        props.put(key, value);
    }

    @Override
    public void initialize() {
        // Create converters for each channel

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return;
        }

        removeDeviceIdProperty();

        deviceId = resolveDeviceId();
        if (deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID is not configured");
            return;
        }

        refreshDevice();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 1, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    String resolveDeviceId() {
        return SmartThingsDeviceIdResolver.getDeviceId(thing);
    }

    public String getSmartThingsDeviceType() {
        String deviceType = thing.getProperties().get(SmartThingsBindingConstants.DEVICE_TYPE);
        return deviceType != null && !deviceType.isBlank() ? deviceType : thing.getThingTypeUID().getId();
    }

    void removeDeviceIdProperty() {
        Map<String, String> properties = new HashMap<>(editProperties());
        if (properties.remove(SmartThingsBindingConstants.DEVICE_ID) != null) {
            updateProperties(properties);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> lcPollingJob = pollingJob;
        if (lcPollingJob != null) {
            lcPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void pollingCode() {
        Bridge lcBridge = getBridge();

        if (lcBridge == null) {
            return;
        }

        if (lcBridge.getStatus() == ThingStatus.OFFLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
        }

        if (lcBridge.getStatus() != ThingStatus.ONLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                logger.debug("Bridge is not ready, don't enter polling for now!");
                return;
            }
        }

        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) lcBridge.getHandler();
        if (accountHandler != null) {
            int pollingTime = accountHandler.getPollingTime();
            if (pollingTime != -1) {
                long now = System.nanoTime();
                if (now - lastRefresh > TimeUnit.SECONDS.toNanos(pollingTime)) {
                    refreshDevice();
                    lastRefresh = now;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("smartthingsName :").append(smartThingsName);
        sb.append(", thing UID: ").append(thing.getUID());
        sb.append(", thing label: ").append(thing.getLabel());
        return sb.toString();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SmartThingsActions.class);
    }

    public @Nullable SmartThingsApi getApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        SmartThingsAccountHandler accountHandler = (SmartThingsAccountHandler) bridge.getHandler();
        if (accountHandler == null) {
            return null;
        }
        SmartThingsApi api = accountHandler.getSmartThingsApi();
        return api;
    }
}
