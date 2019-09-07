/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_COLOR_TEMPERATURE_NAME;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_NUMBER;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerColorTemperatureController} is responsible for the Alexa.ColorTemperatureController
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerColorTemperatureController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.ColorTemperatureController";
    public static final String INTERFACE_COLOR_PROPERTIES = "Alexa.ColorPropertiesController";
    // Channel and Properties
    static final String ALEXA_PROPERTY = "colorTemperatureInKelvin";
    static final String CHANNEL_UID = "colorTemperatureInKelvin";
    static final ChannelTypeUID CHANNEL_TYPE = CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN;
    static final String ITEM_TYPE = ITEM_TYPE_NUMBER;

    static final String ALEXA_PROPERTY_COLOR_NAME = "colorName";
    static final String CHANNEL_UID_COLOR_NAME = "colorName";
    static final ChannelTypeUID CHANNEL_TYPE_COLOR_NAME = CHANNEL_TYPE_COLOR_TEMPERATURE_NAME;
    static final String COLOR_ITEM_TYPE_COLOR = ITEM_TYPE_STRING;
    // List of all actions
    static final String ACTION = "setColorTemperature";
    static final String ALEXA_PROPERTY_ACTION = "colorTemperature";

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE, INTERFACE_COLOR_PROPERTIES };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (ALEXA_PROPERTY.contentEquals(property)) {
            return new ChannelInfo[] { new ChannelInfo(ALEXA_PROPERTY, CHANNEL_UID, CHANNEL_TYPE, ITEM_TYPE),
                    new ChannelInfo(ALEXA_PROPERTY_COLOR_NAME, CHANNEL_UID_COLOR_NAME, CHANNEL_TYPE_COLOR_NAME,
                            COLOR_ITEM_TYPE_COLOR) };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList) {
        if (INTERFACE.equals(interfaceName)) {
            Integer colorTemperature = null;
            for (JsonObject state : stateList) {
                if (ALEXA_PROPERTY.equals(state.get("name").getAsString())) {
                    int value = state.get("value").getAsInt();
                    // For groups take the maximum
                    if (colorTemperature == null) {
                        colorTemperature = value;
                    }
                }
            }
            updateState(CHANNEL_UID, colorTemperature == null ? UnDefType.UNDEF : new DecimalType(colorTemperature));
        }
        if (INTERFACE_COLOR_PROPERTIES.equals(interfaceName)) {
            String colorName = null;
            for (JsonObject state : stateList) {
                if (ALEXA_PROPERTY_COLOR_NAME.equals(state.get("name").getAsString())) {
                    if (colorName == null) {
                        colorName = state.get("value").getAsJsonObject().get("name").getAsString();
                    }
                }
            }
            updateState(CHANNEL_UID_COLOR_NAME, colorName == null ? UnDefType.UNDEF : new StringType(colorName));
        }
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(CHANNEL_UID)) {
            if (ContainsCapabilityProperty(capabilties, ALEXA_PROPERTY)) {
                if (command instanceof DecimalType) {
                    int intValue = ((DecimalType) command).intValue();
                    if (intValue < 1000) {
                        intValue = 1000;
                    }
                    if (intValue > 10000) {
                        intValue = 10000;
                    }
                    connection.smartHomeCommand(entityId, ACTION, ALEXA_PROPERTY_ACTION, intValue);
                    return true;
                }
            }
        }
        if (channelId.equals(CHANNEL_UID_COLOR_NAME)) {
            if (ContainsCapabilityProperty(capabilties, ALEXA_PROPERTY)) {
                if (command instanceof StringType) {
                    String colorTemperatureName = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(colorTemperatureName)) {
                        connection.smartHomeCommand(entityId, ACTION, ALEXA_PROPERTY_ACTION, colorTemperatureName);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelUID, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
