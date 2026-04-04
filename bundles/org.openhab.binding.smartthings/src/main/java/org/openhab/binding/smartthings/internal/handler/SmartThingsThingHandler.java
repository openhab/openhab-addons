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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverter;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatus;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusCapabilities;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusProperties;
import org.openhab.binding.smartthings.internal.statehandler.SmartThingsStateHandler;
import org.openhab.binding.smartthings.internal.statehandler.SmartThingsStateHandlerFactory;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistryImpl;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsThingHandler.class);

    private String smartThingsName;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private long lastRefresh = System.nanoTime();

    public SmartThingsThingHandler(Thing thing) {
        super(thing);
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
            // channelUID
            SmartThingsConverter converter = SmartThingsConverterFactory.getConverter(channelUID.getIdWithoutGroup());

            String jsonMsg = "";
            if (command instanceof RefreshType) {
                refreshDevice();
            } else {
                try {
                    if (converter != null) {
                        jsonMsg = converter.convertToSmartThings(thing, channelUID, command);
                    }

                    SmartThingsApi api = accountHander.getSmartThingsApi();
                    if (api == null) {
                        return;
                    }
                    Map<String, String> properties = this.getThing().getProperties();
                    String deviceId = properties.get(SmartThingsBindingConstants.DEVICE_ID);

                    if (deviceId != null) {
                        api.sendCommand(deviceId, jsonMsg);
                    }
                } catch (SmartThingsException ex) {
                    logger.error("Unable to send command: {}", ex.toString());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                }
            }
        }
    }

    public void refreshChannel(String deviceType, String componentId, String namespace, String capaKey, String attr,
            Object value) throws SmartThingsException {
        String channelName = SmartThingsTypeRegistryImpl.getChannelName(attr);

        String groupId = deviceType + "_" + componentId + "_";

        if (!"".equals(namespace)) {
            groupId = groupId + namespace + "_";
        }
        groupId = groupId + capaKey;

        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), groupId, channelName);

        logger.trace("refreshDevice called: channelName:{}", channelName);

        // channelUID
        SmartThingsConverter converter = SmartThingsConverterFactory.getConverter(channelUID.getIdWithoutGroup());
        SmartThingsStateHandler stateHandler = SmartThingsStateHandlerFactory.getStateHandler(deviceType);

        if (converter != null) {
            State state = converter.convertToOpenHab(thing, channelUID, value);
            updateState(channelUID, state);

            if (stateHandler != null) {
                logger.trace("refreshDevice called: stateHandler:{}", stateHandler);
                stateHandler.handleStateChange(channelUID, deviceType, componentId, state, this);
            }
        }
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

        } catch (Exception ex) {
            logger.error("Unable to refresh device: {} {}", this.getThing().getUID(), ex.toString(), ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    public void sendUpdateState(ChannelUID channelUid, State state) {
        updateState(channelUid, state);
    }

    public void refreshDevice() {
        logger.trace("refreh Device called");
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

        Map<String, String> properties = getThing().getProperties();

        String deviceId = properties.get(SmartThingsBindingConstants.DEVICE_ID);

        logger.trace("refrehDevice for deviceId: {}", deviceId);

        if (deviceId != null) {
            try {
                SmartThingsStatus status = api.getStatus(deviceId);

                logger.trace("refrehDevice for deviceId: status : {}", status);

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
            } catch (SmartThingsException ex) {
                logger.error("Unable to update device : {}", deviceId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        }
    }

    public void refreshDeviceFromCapa(SmartThingsStatusCapabilities capa, String componentKey, String capaKey) {
        for (String propertyKey : capa.keySet()) {
            SmartThingsStatusProperties props = capa.get(propertyKey);

            if (props != null) {
                Object value = props.value;

                if (value != null) {
                    logger.trace("refrehDevice for deviceId: value : {}", value);

                    refreshDevice(thing.getThingTypeUID().getId(), componentKey, capaKey, propertyKey, value);
                }
            }
        }
    }

    public void refreshDeviceProps(SmartThingsStatusCapabilities capa, String componentKey, String capaKey) {
        Map<String, String> propMaps = this.editProperties();
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

        SmartThingsTypeRegistry typeRegistry = accountHandler.getSmartThingsTypeRegistry();

        SmartThingsConverterFactory.registerConverters(typeRegistry);
        SmartThingsStateHandlerFactory.registerStateHandler();

        refreshDevice();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 1, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
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
