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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerColorTemperatureController} is responsible for the Alexa.ColorTemperatureController interface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerColorTemperatureController extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.ColorTemperatureController";
    public static final String INTERFACE_COLOR_PROPERTIES = "Alexa.ColorPropertiesController";

    private static final ChannelInfo COLOR_TEMPERATURE_IN_KELVIN = new ChannelInfo("colorTemperatureInKelvin",
            "colorTemperatureInKelvin", DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR_TEMPERATURE_ABS);
    private static final ChannelInfo COLOR_TEMPERATURE_NAME = new ChannelInfo("colorProperties", "colorTemperatureName",
            Constants.CHANNEL_TYPE_COLOR_TEMPERATURE_NAME);

    private @Nullable Integer lastColorTemperature;
    private @Nullable String lastColorName;

    public HandlerColorTemperatureController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE, INTERFACE_COLOR_PROPERTIES));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (COLOR_TEMPERATURE_IN_KELVIN.propertyName.equals(property)) {
            return Set.of(COLOR_TEMPERATURE_IN_KELVIN, COLOR_TEMPERATURE_NAME);
        }
        return Set.of();
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
            smartHomeDeviceHandler.updateState(COLOR_TEMPERATURE_IN_KELVIN.channelId,
                    colorTemperatureInKelvinValue == null ? UnDefType.UNDEF
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
            smartHomeDeviceHandler.updateState(COLOR_TEMPERATURE_NAME.channelId,
                    colorTemperatureNameValue == null ? UnDefType.UNDEF : new StringType(colorTemperatureNameValue));
        }
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(COLOR_TEMPERATURE_IN_KELVIN.channelId)) {
            // WRITING TO THIS CHANNEL DOES CURRENTLY NOT WORK, BUT WE LEAVE THE CODE FOR FUTURE USE!
            if (containsCapabilityProperty(capabilities, COLOR_TEMPERATURE_IN_KELVIN.propertyName)) {
                QuantityType<?> kelvinQuantity = null;
                if (command instanceof QuantityType<?> genericQuantity) {
                    kelvinQuantity = genericQuantity.toInvertibleUnit(Units.KELVIN);
                } else if (command instanceof DecimalType decimal) {
                    kelvinQuantity = QuantityType.valueOf(decimal.intValue(), Units.KELVIN);
                }
                if (kelvinQuantity != null) {
                    int kelvin = kelvinQuantity.intValue();
                    if (kelvin < 1000) {
                        kelvin = 1000;
                    }
                    if (kelvin > 10000) {
                        kelvin = 10000;
                    }
                    connection.smartHomeCommand(entityId, "setColorTemperature",
                            Map.of("colorTemperatureInKelvin", kelvin));
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
                        connection.smartHomeCommand(entityId, "setColorTemperature",
                                Map.of("colorTemperatureName", colorTemperatureName));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
