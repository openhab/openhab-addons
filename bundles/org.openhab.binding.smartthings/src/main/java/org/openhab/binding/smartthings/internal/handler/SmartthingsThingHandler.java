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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.converter.SmartthingsConverter;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private int timeout;
    private SmartthingsHandlerFactory smartthingsHandlerFactory;
    private Map<ChannelUID, SmartthingsConverter> converters = new HashMap<>();

    private final String smartthingsConverterName = "smartthings-converter";

    public SmartthingsThingHandler(Thing thing, SmartthingsHandlerFactory smartthingsHandlerFactory) {
        super(thing);
        this.smartthingsHandlerFactory = smartthingsHandlerFactory;
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

        SmartthingsBridgeHandler smartthingsBridgeHandler = (SmartthingsBridgeHandler) bridge.getHandler();
        if (smartthingsBridgeHandler != null
                && smartthingsBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            String thingTypeId = thing.getThingTypeUID().getId();
            String smartthingsType = getSmartthingsAttributeFromChannel(channelUID);

            SmartthingsConverter converter = converters.get(channelUID);

            String path;
            String jsonMsg;
            if (command instanceof RefreshType) {
                path = "/state";
                // Go to ST hub and ask for current state
                jsonMsg = String.format(
                        "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\", \"openHabStartTime\": %d}",
                        thingTypeId, smartthingsName, smartthingsType, System.currentTimeMillis());
            } else {
                // Send update to ST hub
                path = "/update";
                jsonMsg = converter.convertToSmartthings(channelUID, command);

                // The smartthings hub won't (can't) return a response to this call. But, it will send a separate
                // message back to the SmartthingBridgeHandler.receivedPushMessage handler
            }

            try {
                smartthingsHandlerFactory.sendDeviceCommand(path, timeout, jsonMsg);
                // Smartthings will not return a response to this message but will send it's response message
                // which will get picked up by the SmartthingBridgeHandler.receivedPushMessage handler
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Attempt to send command to the Smartthings hub for {} failed with exception: {}",
                        smartthingsName, e.getMessage());
            }
        }
    }

    /**
     * Get the Smartthings capability reference "attribute" from the channel properties.
     * In OpenHAB each channel id corresponds to the Smartthings attribute. In the ChannelUID the
     * channel id is the last segment
     *
     * @param channelUID
     * @return channel id
     */
    private String getSmartthingsAttributeFromChannel(ChannelUID channelUID) {
        return channelUID.getId();
    }

    /**
     * State messages sent from the hub arrive here, are processed and the openHab state is updated.
     *
     * @param stateData
     */
    public void handleStateMessage(SmartthingsStateData stateData) {
        // First locate the channel
        Channel matchingChannel = null;
        for (Channel ch : thing.getChannels()) {
            if (ch.getUID().getAsString().endsWith(stateData.capabilityAttribute)) {
                matchingChannel = ch;
                break;
            }
        }
        if (matchingChannel == null) {
            return;
        }

        SmartthingsConverter converter = converters.get(matchingChannel.getUID());

        // If value from Smartthings is null then stop here
        State state;
        if (stateData.value != null) {
            state = converter.convertToOpenHab(matchingChannel.getAcceptedItemType(), stateData);
        } else {
            state = UnDefType.NULL;
        }

        updateState(matchingChannel.getUID(), state);
        logger.trace("Smartthings updated State for channel: {} to {}", matchingChannel.getUID().getAsString(),
                state.toString());
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(SmartthingsThingConfig.class);
        if (!validateConfig(config)) {
            return;
        }
        smartthingsName = config.smartthingsName;
        timeout = config.smartthingsTimeout;

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

        updateStatus(ThingStatus.ONLINE);
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
            Constructor<?> constr = Class.forName(converterClassName.toString()).getDeclaredConstructor(Thing.class);
            constr.setAccessible(true);
            return (SmartthingsConverter) constr.newInstance(thing);
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
        String name = config.smartthingsName;
        if (name.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings device name is missing");
            return false;
        }

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
