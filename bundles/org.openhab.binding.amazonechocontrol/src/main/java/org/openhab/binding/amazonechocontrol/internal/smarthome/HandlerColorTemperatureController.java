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
    final static ChannelInfo colorTemperatureInKelvin = new ChannelInfo("colorTemperatureInKelvin" /* propertyName */ ,
            "colorTemperatureInKelvin" /* ChannelId */, CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN /* Channel Type */ ,
            ITEM_TYPE_NUMBER /* Item Type */);

    final static ChannelInfo colorTemperatureName = new ChannelInfo("colorProperties" /* propertyName */ ,
            "colorTemperatureName" /* ChannelId */, CHANNEL_TYPE_COLOR_TEMPERATURE_NAME /* Channel Type */ ,
            ITEM_TYPE_STRING /* Item Type */);

    @Nullable
    Integer lastColorTemperature;
    @Nullable
    String lastColorName;

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE, INTERFACE_COLOR_PROPERTIES };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (colorTemperatureInKelvin.propertyName.contentEquals(property)) {
            return new ChannelInfo[] { colorTemperatureInKelvin, colorTemperatureName };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        if (INTERFACE.equals(interfaceName)) {
            Integer colorTemperatureInKelvinValue = null;
            for (JsonObject state : stateList) {
                if (colorTemperatureInKelvin.propertyName.equals(state.get("name").getAsString())) {
                    int value = state.get("value").getAsInt();
                    // For groups take the maximum
                    if (colorTemperatureInKelvinValue == null) {
                        colorTemperatureInKelvinValue = value;
                    }
                }
            }
            if (colorTemperatureInKelvinValue != null && !colorTemperatureInKelvinValue.equals(lastColorTemperature)) {
                lastColorTemperature = colorTemperatureInKelvinValue;
                result.NeedSingleUpdate = true;
            }
            updateState(colorTemperatureInKelvin.channelId, colorTemperatureInKelvinValue == null ? UnDefType.UNDEF
                    : new DecimalType(colorTemperatureInKelvinValue));
        }
        if (INTERFACE_COLOR_PROPERTIES.equals(interfaceName)) {
            String colorTemperatureNameValue = null;
            for (JsonObject state : stateList) {
                if (colorTemperatureName.propertyName.equals(state.get("name").getAsString())) {
                    if (colorTemperatureNameValue == null) {
                        result.NeedSingleUpdate = false;
                        colorTemperatureNameValue = state.get("value").getAsJsonObject().get("name").getAsString();
                    }
                }
            }
            if (lastColorName == null) {
                lastColorName = colorTemperatureNameValue;
            } else if (colorTemperatureNameValue == null && lastColorName != null) {
                colorTemperatureNameValue = lastColorName;
            }
            updateState(colorTemperatureName.channelId,
                    colorTemperatureNameValue == null ? UnDefType.UNDEF : new StringType(colorTemperatureNameValue));
        }
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(colorTemperatureInKelvin.channelId)) {
            // WRITING TO THIS CHANNEL DOES CURRENTLY NOT WORK, BUT WE LEAVE THE CODE FOR FUTURE USE!
            if (ContainsCapabilityProperty(capabilties, colorTemperatureInKelvin.propertyName)) {
                if (command instanceof DecimalType) {
                    int intValue = ((DecimalType) command).intValue();
                    if (intValue < 1000) {
                        intValue = 1000;
                    }
                    if (intValue > 10000) {
                        intValue = 10000;
                    }
                    connection.smartHomeCommand(entityId, "setColorTemperature", "colorTemperatureInKelvin", intValue);
                    return true;
                }
            }
        }
        if (channelId.equals(colorTemperatureName.channelId)) {
            if (ContainsCapabilityProperty(capabilties, colorTemperatureInKelvin.propertyName)) {
                if (command instanceof StringType) {
                    String colorTemperatureName = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(colorTemperatureName)) {
                        lastColorName = colorTemperatureName;
                        connection.smartHomeCommand(entityId, "setColorTemperature", "colorTemperatureName",
                                colorTemperatureName);
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
