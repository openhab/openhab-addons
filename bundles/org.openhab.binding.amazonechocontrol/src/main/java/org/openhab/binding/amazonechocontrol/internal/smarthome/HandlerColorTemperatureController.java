/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_NUMBER;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerColorTemperatureController} is responsible for the Alexa.ColorTemperatureController
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerColorTemperatureController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.ColorTemperatureController";
    public static final String INTERFACE_COLOR_PROPERTIES = "Alexa.ColorPropertiesController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_COLOR_TEMPERATURE_NAME = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorTemperatureName");

    private static final ChannelTypeUID CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorTemperatureInKelvin");

    // Channel and Properties
    private static final ChannelInfo COLOR_TEMPERATURE_IN_KELVIN = new ChannelInfo(
            "colorTemperatureInKelvin" /* propertyName */ , "colorTemperatureInKelvin" /* ChannelId */,
            CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN /* Channel Type */ , ITEM_TYPE_NUMBER /* Item Type */);

    private static final ChannelInfo COLOR_TEMPERATURE_NAME = new ChannelInfo("colorProperties" /* propertyName */ ,
            "colorTemperatureName" /* ChannelId */, CHANNEL_TYPE_COLOR_TEMPERATURE_NAME /* Channel Type */ ,
            ITEM_TYPE_STRING /* Item Type */);

    private @Nullable Integer lastColorTemperature;
    private @Nullable String lastColorName;

    public HandlerColorTemperatureController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE, INTERFACE_COLOR_PROPERTIES };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (COLOR_TEMPERATURE_IN_KELVIN.propertyName.contentEquals(property)) {
            return new ChannelInfo[] { COLOR_TEMPERATURE_IN_KELVIN, COLOR_TEMPERATURE_NAME };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        if (INTERFACE.equals(interfaceName)) {
            Integer colorTemperatureInKelvinValue = null;
            for (JsonObject state : stateList) {
                if (COLOR_TEMPERATURE_IN_KELVIN.propertyName.equals(state.get("name").getAsString())) {
                    int value = state.get("value").getAsInt();
                    // For groups take the maximum
                    if (colorTemperatureInKelvinValue == null) {
                        colorTemperatureInKelvinValue = value;
                    }
                }
            }
            if (colorTemperatureInKelvinValue != null && !colorTemperatureInKelvinValue.equals(lastColorTemperature)) {
                lastColorTemperature = colorTemperatureInKelvinValue;
                result.needSingleUpdate = true;
            }
            updateState(COLOR_TEMPERATURE_IN_KELVIN.channelId, colorTemperatureInKelvinValue == null ? UnDefType.UNDEF
                    : new DecimalType(colorTemperatureInKelvinValue));
        }
        if (INTERFACE_COLOR_PROPERTIES.equals(interfaceName)) {
            String colorTemperatureNameValue = null;
            for (JsonObject state : stateList) {
                if (COLOR_TEMPERATURE_NAME.propertyName.equals(state.get("name").getAsString())) {
                    if (colorTemperatureNameValue == null) {
                        result.needSingleUpdate = false;
                        colorTemperatureNameValue = state.get("value").getAsJsonObject().get("name").getAsString();
                    }
                }
            }
            if (lastColorName == null) {
                lastColorName = colorTemperatureNameValue;
            } else if (colorTemperatureNameValue == null && lastColorName != null) {
                colorTemperatureNameValue = lastColorName;
            }
            updateState(COLOR_TEMPERATURE_NAME.channelId,
                    colorTemperatureNameValue == null ? UnDefType.UNDEF : new StringType(colorTemperatureNameValue));
        }
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(COLOR_TEMPERATURE_IN_KELVIN.channelId)) {
            // WRITING TO THIS CHANNEL DOES CURRENTLY NOT WORK, BUT WE LEAVE THE CODE FOR FUTURE USE!
            if (containsCapabilityProperty(capabilities, COLOR_TEMPERATURE_IN_KELVIN.propertyName)) {
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
        if (channelId.equals(COLOR_TEMPERATURE_NAME.channelId)) {
            if (containsCapabilityProperty(capabilities, COLOR_TEMPERATURE_IN_KELVIN.propertyName)) {
                if (command instanceof StringType) {
                    String colorTemperatureName = command.toFullString();
                    if (!colorTemperatureName.isEmpty()) {
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
