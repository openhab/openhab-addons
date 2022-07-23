/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerThermostatController} is responsible for the Alexa.ThermostatControllerInterface
 *
 * @author Sven Killig - Initial contribution
 */
@NonNullByDefault
public class HandlerThermostatController extends HandlerBase {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(HandlerThermostatController.class);
    // Interface
    public static final String INTERFACE = "Alexa.ThermostatController";
    // Channel definitions
    private static final ChannelInfo TARGET_SETPOINT = new ChannelInfo("targetSetpoint" /* propertyName */ ,
            "targetSetpoint" /* ChannelId */, CHANNEL_TYPE_TARGETSETPOINT /* Channel Type */ ,
            ITEM_TYPE_NUMBER_TEMPERATURE /* Item Type */);
    private static final ChannelInfo LOWER_SETPOINT = new ChannelInfo("lowerSetpoint" /* propertyName */ ,
            "lowerSetpoint" /* ChannelId */, CHANNEL_TYPE_LOWERSETPOINT /* Channel Type */ ,
            ITEM_TYPE_NUMBER_TEMPERATURE /* Item Type */);
    private static final ChannelInfo UPPER_SETPOINT = new ChannelInfo("upperSetpoint" /* propertyName */ ,
            "upperSetpoint" /* ChannelId */, CHANNEL_TYPE_UPPERSETPOINT /* Channel Type */ ,
            ITEM_TYPE_NUMBER_TEMPERATURE /* Item Type */);
    private static final ChannelInfo THERMOSTAT_MODE = new ChannelInfo("thermostatMode" /* propertyName */ ,
            "thermostatMode" /* ChannelId */, CHANNEL_TYPE_THERMOSTATMODE /* Channel Type */ ,
            ITEM_TYPE_STRING /* Item Type */);

    public HandlerThermostatController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (TARGET_SETPOINT.propertyName.equals(property)) {
            return new ChannelInfo[] { TARGET_SETPOINT };
        }
        if (LOWER_SETPOINT.propertyName.equals(property)) {
            return new ChannelInfo[] { LOWER_SETPOINT };
        }
        if (UPPER_SETPOINT.propertyName.equals(property)) {
            return new ChannelInfo[] { UPPER_SETPOINT };
        }
        if (THERMOSTAT_MODE.propertyName.equals(property)) {
            return new ChannelInfo[] { THERMOSTAT_MODE };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        for (JsonObject state : stateList) {
            QuantityType<Temperature> temperatureValue = null;
            logger.debug("Updating {} with state: {}", interfaceName, state.toString());
            if (TARGET_SETPOINT.propertyName.equals(state.get("name").getAsString())) {
                // For groups take the first
                if (temperatureValue == null) {
                    JsonObject value = state.get("value").getAsJsonObject();
                    float temperature = value.get("value").getAsFloat();
                    String scale = value.get("scale").getAsString().toUpperCase();
                    if ("CELSIUS".equals(scale)) {
                        temperatureValue = new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
                    } else {
                        temperatureValue = new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
                    }
                }
                updateState(TARGET_SETPOINT.channelId, temperatureValue == null ? UnDefType.UNDEF : temperatureValue);
            }
            if (THERMOSTAT_MODE.propertyName.equals(state.get("name").getAsString())) {
                // For groups take the first
                String operation = state.get("value").getAsString().toUpperCase();
                StringType operationValue = new StringType(operation);
                updateState(THERMOSTAT_MODE.channelId, operationValue);
            }
            if (UPPER_SETPOINT.propertyName.equals(state.get("name").getAsString())) {
                // For groups take the first
                if (temperatureValue == null) {
                    JsonObject value = state.get("value").getAsJsonObject();
                    float temperature = value.get("value").getAsFloat();
                    String scale = value.get("scale").getAsString().toUpperCase();
                    if ("CELSIUS".equals(scale)) {
                        temperatureValue = new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
                    } else {
                        temperatureValue = new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
                    }
                }
                updateState(UPPER_SETPOINT.channelId, temperatureValue == null ? UnDefType.UNDEF : temperatureValue);
            }
            if (LOWER_SETPOINT.propertyName.equals(state.get("name").getAsString())) {
                // For groups take the first
                if (temperatureValue == null) {
                    JsonObject value = state.get("value").getAsJsonObject();
                    float temperature = value.get("value").getAsFloat();
                    String scale = value.get("scale").getAsString().toUpperCase();
                    if ("CELSIUS".equals(scale)) {
                        temperatureValue = new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
                    } else {
                        temperatureValue = new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
                    }
                }
                updateState(LOWER_SETPOINT.channelId, temperatureValue == null ? UnDefType.UNDEF : temperatureValue);
            }
        }
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(TARGET_SETPOINT.channelId)) {
            if (containsCapabilityProperty(capabilities, TARGET_SETPOINT.propertyName)) {
                if (command instanceof QuantityType) {
                    connection.smartHomeCommand(entityId, "setTargetTemperature", "targetTemperature", command);
                    return true;
                }
            }
        }
        if (channelId.equals(LOWER_SETPOINT.channelId)) {
            if (containsCapabilityProperty(capabilities, LOWER_SETPOINT.propertyName)) {
                if (command instanceof QuantityType) {
                    connection.smartHomeCommand(entityId, "setTargetTemperature", "lowerSetTemperature", command);
                    return true;
                }
            }
        }
        if (channelId.equals(UPPER_SETPOINT.channelId)) {
            if (containsCapabilityProperty(capabilities, UPPER_SETPOINT.propertyName)) {
                if (command instanceof QuantityType) {
                    connection.smartHomeCommand(entityId, "setTargetTemperature", "upperSetTemperature", command);
                    return true;
                }
            }
        }
        if (channelId.equals(THERMOSTAT_MODE.channelId)) {
            if (containsCapabilityProperty(capabilities, THERMOSTAT_MODE.propertyName)) {
                if (command instanceof StringType) {
                    connection.smartHomeCommand(entityId, "setThermostatMode", "thermostatMode", command);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelId, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
