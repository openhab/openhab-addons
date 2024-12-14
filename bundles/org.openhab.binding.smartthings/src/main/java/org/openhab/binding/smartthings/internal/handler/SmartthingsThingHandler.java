/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.converter.SmartthingsConverter;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatus;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatusCapabilities;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatusComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatusProperties;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsThingHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsThingHandler.class);

    private SmartthingsThingConfig config;
    private String smartthingsName;
    private Map<ChannelUID, SmartthingsConverter> converters = new HashMap<>();

    private final String smartthingsConverterName = "smartthings-converter";
    private @Nullable ScheduledFuture<?> pollingJob = null;

    public SmartthingsThingHandler(Thing thing) {
        super(thing);
        smartthingsName = ""; // Initialize here so it can be NonNull but it should always get a value in initialize()
        config = new SmartthingsThingConfig();
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

        SmartthingsCloudBridgeHandler cloudBridge = (SmartthingsCloudBridgeHandler) bridge.getHandler();

        if (cloudBridge != null && cloudBridge.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            SmartthingsConverter converter = converters.get(channelUID);

            String jsonMsg = "";
            if (command instanceof RefreshType) {
                refreshDevice();
            } else {
                // @todo : review this
                if (converter != null) {
                    jsonMsg = converter.convertToSmartthings(thing, channelUID, command);
                }
            }

            SmartthingsApi api = cloudBridge.getSmartthingsApi();
            Map<String, String> properties = this.getThing().getProperties();
            String deviceId = properties.get("deviceId");

            try {
                if (deviceId != null) {
                    api.sendCommand(deviceId, jsonMsg);
                }
            } catch (SmartthingsException ex) {
                logger.error("Unable to send command: {}", ex.getMessage());
            }

        }
    }

    public void refreshDevice(String componentId, String capa, String attr, Object value) {
        String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(attr), '-') + "-channel")
                .toLowerCase();

        ChannelUID chanUid = new ChannelUID(this.getThing().getUID(), "default", channelName);
        Channel chan = thing.getChannel(chanUid);

        if (converters.containsKey(chanUid)) {
            SmartthingsConverter converter = converters.get(chanUid);

            // converter.convertToOpenHab(channelName, value);

            if (chan != null) {
                if (attr.equals("switch")) {
                    if (("on".equals(value))) {
                        updateState(chanUid, OnOffType.ON);
                    } else {
                        updateState(chanUid, OnOffType.OFF);
                    }
                }
                if (attr.equals("level")) {
                    if (value instanceof String) {
                        int val = java.lang.Integer.parseInt(((String) value));
                        updateState(chanUid, new PercentType(val));
                    } else {
                        updateState(chanUid, new PercentType(((Double) value).intValue()));
                    }
                }
            }
        }
    }

    public void refreshDevice() {
        Bridge bridge = getBridge();

        SmartthingsCloudBridgeHandler cloudBridge = (SmartthingsCloudBridgeHandler) bridge.getHandler();
        SmartthingsApi api = cloudBridge.getSmartthingsApi();
        Map<String, String> properties = this.getThing().getProperties();

        String deviceId = properties.get("deviceId");

        if (deviceId != null) {
            try {
                SmartthingsStatus status = api.getStatus(deviceId);

                List<Channel> channels = thing.getChannels();

                if (status != null) {

                    if (status.components.containsKey("main")) {
                        SmartthingsStatusComponent component = status.components.get("main");

                        for (String capaKey : component.keySet()) {
                            SmartthingsStatusCapabilities capa = component.get(capaKey);

                            for (String propertyKey : capa.keySet()) {
                                SmartthingsStatusProperties props = capa.get(propertyKey);
                                Object value = props.value;
                                String timestamp = props.timestamp;

                                refreshDevice("main", "capa", propertyKey, value);
                            }

                        }
                    }
                }
            } catch (SmartthingsException ex) {
                logger.error("Unable to update device : {}", deviceId);
            }
        }
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(SmartthingsThingConfig.class);
        if (!validateConfig(config)) {
            return;
        }
        smartthingsName = config.smartthingsName;

        // Create converters for each channel
        for (Channel ch : thing.getChannels()) {
            @Nullable
            String converterName = ch.getProperties().get(smartthingsConverterName);
            // Will be null if no explicit converter was specified
            if (converterName == null || converterName.isEmpty()) {
                // A converter was Not specified so use the channel id
                converterName = ch.getUID().getId();
            }

            // Try to get the converter
            SmartthingsConverter cvtr = getConverter(converterName);
            if (cvtr == null) {
                // If there is no channel specific converter the get the "default" converter
                cvtr = getConverter("default");
            }

            if (cvtr != null) {
                // cvtr should never be null because there should always be a "default" converter
                converters.put(ch.getUID(), cvtr);
            }
        }

        refreshDevice();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 5, TimeUnit.SECONDS);

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

        // refreshDevice();
    }

    private @Nullable SmartthingsConverter getConverter(String converterName) {
        // Converter name will be a name such as "switch" which has to be converted into the full class name such as
        // org.openhab.binding.smartthings.internal.converter.SmartthingsSwitchConveter
        StringBuffer converterClassName = new StringBuffer(
                "org.openhab.binding.smartthings.internal.converter.Smartthings");
        converterClassName.append(Character.toUpperCase(converterName.charAt(0)));
        converterClassName.append(converterName.substring(1));
        converterClassName.append("Converter");
        try {
            Constructor<?> constr = Class.forName(converterClassName.toString())
                    .getDeclaredConstructor(SmartthingsTypeRegistry.class, Thing.class);
            constr.setAccessible(true);
            Bridge bridge = getBridge();
            SmartthingsCloudBridgeHandler cloudBridge = (SmartthingsCloudBridgeHandler) bridge.getHandler();
            SmartthingsTypeRegistry typeRegistry = cloudBridge.getSmartthingsTypeRegistry();
            return (SmartthingsConverter) constr.newInstance(typeRegistry, thing);
        } catch (ClassNotFoundException e) {
            // Most of the time there is no channel specific converter, the default converter is all that is needed.
            logger.trace("No Custom converter exists for {} ({})", converterName, converterClassName);
        } catch (NoSuchMethodException e) {
            logger.warn("NoSuchMethodException occurred for {} ({}) {}", converterName, converterClassName,
                    e.getMessage());
        } catch (InvocationTargetException e) {
            logger.warn("InvocationTargetException occurred for {} ({}) {}", converterName, converterClassName,
                    e.getMessage());
        } catch (IllegalAccessException e) {
            logger.warn("IllegalAccessException occurred for {} ({}) {}", converterName, converterClassName,
                    e.getMessage());
        } catch (InstantiationException e) {
            logger.warn("InstantiationException occurred for {} ({}) {}", converterName, converterClassName,
                    e.getMessage());
        }
        return null;
    }

    private boolean validateConfig(SmartthingsThingConfig config) {
        return true;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<>();

        // The name must be provided
        String stName = config.smartthingsName;
        if (stName.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SmartthingsBindingConstants.SMARTTHINGS_NAME)
                    .withMessageKeySuffix(SmartthingsThingConfigStatusMessage.SMARTTHINGS_NAME_MISSING)
                    .withArguments(SmartthingsBindingConstants.SMARTTHINGS_NAME).build());
        }

        return configStatusMessages;
    }

    public String getSmartthingsName() {
        return smartthingsName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("smartthingsName :").append(smartthingsName);
        sb.append(", thing UID: ").append(this.thing.getUID());
        sb.append(", thing label: ").append(this.thing.getLabel());
        return sb.toString();
    }
}
