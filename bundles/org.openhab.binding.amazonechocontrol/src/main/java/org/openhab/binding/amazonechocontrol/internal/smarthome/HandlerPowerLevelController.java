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
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerPowerLevelController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerPowerLevelController extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.PowerLevelController";

    private static final ChannelInfo POWER_LEVEL = new ChannelInfo("powerLevel", "powerLevel",
            Constants.CHANNEL_TYPE_POWER_LEVEL);

    private @Nullable Integer lastPowerLevel;

    public HandlerPowerLevelController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (POWER_LEVEL.propertyName.equals(property)) {
            return Set.of(POWER_LEVEL);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer powerLevelValue = null;
        for (JsonObject state : stateList) {
            if (POWER_LEVEL.propertyName.equals(state.get("name").getAsString())) {
                int value = state.get("value").getAsInt();
                // For groups take the maximum
                if (powerLevelValue == null) {
                    powerLevelValue = value;
                } else if (value > powerLevelValue) {
                    powerLevelValue = value;
                }
            }
        }
        if (powerLevelValue != null) {
            lastPowerLevel = powerLevelValue;
        }
        smartHomeDeviceHandler.updateState(POWER_LEVEL.channelId,
                powerLevelValue == null ? UnDefType.UNDEF : new PercentType(powerLevelValue));
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(POWER_LEVEL.channelId)) {
            if (containsCapabilityProperty(capabilities, POWER_LEVEL.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    Integer lastPowerLevel = this.lastPowerLevel;
                    if (lastPowerLevel != null) {
                        int newValue = lastPowerLevel++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        this.lastPowerLevel = newValue;
                        connection.smartHomeCommand(entityId, "setPowerLevel",
                                Map.of(POWER_LEVEL.propertyName, newValue));
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    Integer lastPowerLevel = this.lastPowerLevel;
                    if (lastPowerLevel != null) {
                        int newValue = lastPowerLevel--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        this.lastPowerLevel = newValue;
                        connection.smartHomeCommand(entityId, "setPowerLevel",
                                Map.of(POWER_LEVEL.propertyName, newValue));
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastPowerLevel = 0;
                    connection.smartHomeCommand(entityId, "setPowerLevel", Map.of(POWER_LEVEL.propertyName, 0));
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastPowerLevel = 100;
                    connection.smartHomeCommand(entityId, "setPowerLevel", Map.of(POWER_LEVEL.propertyName, 100));
                    return true;
                } else if (command instanceof PercentType) {
                    lastPowerLevel = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setPowerLevel",
                            Map.of(POWER_LEVEL.propertyName, ((PercentType) command).floatValue() / 100));
                    return true;
                }
            }
        }
        return false;
    }
}
