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
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerThermostatController} is responsible for the Alexa.ThermostatControllerInterface
 *
 * @author Sven Killig - Initial contribution
 */
@NonNullByDefault
public class HandlerThermostatController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.ThermostatController";
    // Channel definitions
    private static final ChannelInfo TARGET_SETPOINT = new ChannelInfo("targetSetpoint" /* propertyName */ ,
            "targetSetpoint" /* ChannelId */, CHANNEL_TYPE_TARGETSETPOINT /* Channel Type */ ,
            ITEM_TYPE_NUMBER_TEMPERATURE /* Item Type */);

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
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        QuantityType<Temperature> temperatureValue = null;
        for (JsonObject state : stateList) {
            if (TARGET_SETPOINT.propertyName.equals(state.get("name").getAsString())) {
                JsonObject value = state.get("value").getAsJsonObject();
                // For groups take the first
                if (temperatureValue == null) {
                    float temperature = value.get("value").getAsFloat();
                    String scale = value.get("scale").getAsString().toUpperCase();
                    if ("CELSIUS".equals(scale)) {
                        temperatureValue = new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
                    } else {
                        temperatureValue = new QuantityType<Temperature>(temperature, ImperialUnits.FAHRENHEIT);
                    }
                }
            }
        }
        updateState(TARGET_SETPOINT.channelId, temperatureValue == null ? UnDefType.UNDEF : temperatureValue);
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
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelId, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
