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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsThingHandler.class);

    private String smartthingsName;

    private @Nullable ScheduledFuture<?> pollingJob = null;

    public SmartThingsThingHandler(Thing thing) {
        super(thing);
        smartthingsName = ""; // Initialize here so it can be NonNull but it should always get a value in initialize()
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

        SmartThingsCloudBridgeHandler cloudBridge = (SmartThingsCloudBridgeHandler) bridge.getHandler();

        if (cloudBridge != null && cloudBridge.getThing().getStatus().equals(ThingStatus.ONLINE)) {
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

                    SmartThingsApi api = cloudBridge.getSmartThingsApi();
                    Map<String, String> properties = this.getThing().getProperties();
                    String deviceId = properties.get("deviceId");
                    /*
                     * if (channelUID.getId().equals("oven_main#data")) {
                     * jsonMsg = "";
                     * jsonMsg += "{";
                     * jsonMsg += "   \"commands\":";
                     * jsonMsg += "     [";
                     * jsonMsg += "        {";
                     * jsonMsg += "          \"component\":\"main\",";
                     * jsonMsg += "          \"capability`\":\"refresh\",";
                     * jsonMsg += "          \"command\":\"refresh \"";
                     * jsonMsg += "        }";
                     * jsonMsg += "     ]";
                     * jsonMsg += "}";
                     * }
                     */
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

            String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(attr), '-'))
                    .toLowerCase(Locale.ROOT);

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

        SmartThingsCloudBridgeHandler cloudBridge = (SmartThingsCloudBridgeHandler) bridge.getHandler();
        if (cloudBridge == null) {
            return;
        }
        SmartThingsApi api = cloudBridge.getSmartThingsApi();
        Map<String, String> properties = getThing().getProperties();

        String deviceId = properties.get("deviceId");

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

                                if (capa != null) {
                                    for (String propertyKey : capa.keySet()) {
                                        SmartThingsStatusProperties props = capa.get(propertyKey);

                                        if (props != null) {
                                            Object value = props.value;

                                            if (value != null) {
                                                logger.trace("refrehDevice for deviceId: value : {}", value);

                                                refreshDevice(thing.getThingTypeUID().getId(), componentKey, capaKey,
                                                        propertyKey, value);
                                            }
                                        }
                                    }
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

    @Override
    public void initialize() {
        // Create converters for each channel

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        SmartThingsCloudBridgeHandler cloudBridge = (SmartThingsCloudBridgeHandler) bridge.getHandler();
        if (cloudBridge == null) {
            return;
        }

        SmartThingsTypeRegistry typeRegistry = cloudBridge.getSmartThingsTypeRegistry();

        SmartThingsConverterFactory.registerConverters(typeRegistry);
        SmartThingsStateHandlerFactory.registerStateHandler();

        // testCommand();
        refreshDevice();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 5, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    public void testCommand() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        SmartThingsCloudBridgeHandler cloudBridge = (SmartThingsCloudBridgeHandler) bridge.getHandler();
        if (cloudBridge == null) {
            return;
        }
        SmartThingsApi api = cloudBridge.getSmartThingsApi();
        String deviceId = "702C1F72-C35A-0000-0000-000000000000";

        String jsonMsg = "";
        jsonMsg += "{";
        jsonMsg += "   \"commands\":";
        jsonMsg += "     [";
        jsonMsg += "        {";
        jsonMsg += "          \"component\":\"main\",";
        jsonMsg += "          \"capability\":\"ovenOperatingState\",";
        jsonMsg += "          \"command\":\"start\",";
        jsonMsg += "          \"arguments\":[\"Baker\", 300 , 210 ]";
        jsonMsg += "        }";
        jsonMsg += "     ]";
        jsonMsg += "}";

        try {
            api.sendCommand(deviceId, jsonMsg);
        } catch (SmartThingsException ex) {
            logger.error("exception: ", ex);
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

        // refreshDevice();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("smartthingsName :").append(smartthingsName);
        sb.append(", thing UID: ").append(thing.getUID());
        sb.append(", thing label: ").append(thing.getLabel());
        return sb.toString();
    }
}
