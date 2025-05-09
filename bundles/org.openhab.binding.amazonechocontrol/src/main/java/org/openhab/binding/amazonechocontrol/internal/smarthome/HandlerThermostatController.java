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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerThermostatController} is responsible for the Alexa.ThermostatController interface
 *
 * @author Sven Killig - Initial contribution
 */
@NonNullByDefault
public class HandlerThermostatController extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.ThermostatController";

    private static final ChannelInfo TARGET_SETPOINT = new ChannelInfo("targetSetpoint" /* propertyNameReceive */,
            "targetTemperature" /* propertyNameSend */, "targetSetpoint", CHANNEL_TYPE_TARGETSETPOINT);
    private static final ChannelInfo LOWER_SETPOINT = new ChannelInfo("lowerSetpoint",
            "lowerSetTemperature" /* propertyNameSend */, "lowerSetpoint", CHANNEL_TYPE_LOWERSETPOINT);
    private static final ChannelInfo UPPER_SETPOINT = new ChannelInfo("upperSetpoint",
            "upperSetTemperature" /* propertyNameSend */, "upperSetpoint", CHANNEL_TYPE_UPPERSETPOINT);
    private static final ChannelInfo MODE = new ChannelInfo("thermostatMode", "thermostatMode", "thermostatMode",
            CHANNEL_TYPE_THERMOSTATMODE);

    private static final Set<ChannelInfo> ALL_CHANNELS = Set.of(TARGET_SETPOINT, LOWER_SETPOINT, UPPER_SETPOINT, MODE);

    private final Map<String, Type> setpointCache = new HashMap<>();

    public HandlerThermostatController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        return ALL_CHANNELS.stream().filter(c -> c.propertyName.equals(property)).collect(Collectors.toSet());
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        ALL_CHANNELS.forEach(channel -> {
            State newState = null;
            for (JsonObject state : stateList) {
                if (channel.propertyName.equals(state.get("name").getAsString())) {
                    if ("thermostatMode".equals(channel.propertyName)) {
                        newState = new StringType(state.get("value").getAsString());
                    } else {
                        JsonObject value = state.get("value").getAsJsonObject();
                        // For groups take the first
                        if (newState == null) {
                            float temperature = value.get("value").getAsFloat();
                            String scale = value.get("scale").getAsString().toUpperCase();
                            if ("CELSIUS".equals(scale)) {
                                newState = new QuantityType<>(temperature, SIUnits.CELSIUS);
                            } else {
                                newState = new QuantityType<>(temperature, ImperialUnits.FAHRENHEIT);
                            }
                        }
                        setpointCache.put(channel.propertyNameSend, newState);
                    }
                }
            }
            smartHomeDeviceHandler.updateState(channel.channelId,
                    Objects.requireNonNullElse(newState, UnDefType.UNDEF));
        });
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        ChannelInfo channelInfo = ALL_CHANNELS.stream().filter(c -> c.channelId.equals(channelId)).findFirst()
                .orElse(null);
        if (channelInfo != null) {
            if (containsCapabilityProperty(capabilities, channelInfo.propertyName)) {
                if (command instanceof QuantityType) {
                    Map<String, Object> values = new HashMap<>();
                    if ("lowerSetTemperature".equals(channelInfo.propertyNameSend)) {
                        values.put("lowerSetTemperature", command);
                        values.put("upperSetTemperature", setpointCache.getOrDefault("upperSetTemperature", command));
                    } else if ("upperSetTemperature".equals(channelInfo.propertyNameSend)) {
                        values.put("upperSetTemperature", command);
                        values.put("lowerSetTemperature", setpointCache.getOrDefault("lowerSetTemperature", command));
                    } else {
                        values.put("targetTemperature", command);
                    }
                    connection.smartHomeCommand(entityId, "setTargetTemperature", values);
                    return true;
                }
                if (command instanceof StringType) {
                    connection.smartHomeCommand(entityId, "setThermostatMode",
                            Map.of(channelInfo.propertyNameSend, command));
                }
            }
        }

        return false;
    }
}
