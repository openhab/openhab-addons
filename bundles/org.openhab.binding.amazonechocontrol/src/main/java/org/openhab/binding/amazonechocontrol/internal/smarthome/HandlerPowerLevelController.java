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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_DIMMER;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerPowerLevelController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerPowerLevelController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.PowerLevelController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_POWER_LEVEL = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "powerLevel");

    // Channel definitions
    final static ChannelInfo powerLevel = new ChannelInfo("powerLevel" /* propertyName */ ,
            "powerLevel" /* ChannelId */, CHANNEL_TYPE_POWER_LEVEL /* Channel Type */ ,
            ITEM_TYPE_DIMMER /* Item Type */);

    Integer lastPowerLevel;

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (powerLevel.propertyName.equals(property)) {
            return new ChannelInfo[] { powerLevel };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer powerLevelValue = null;
        for (JsonObject state : stateList) {
            if (powerLevel.propertyName.equals(state.get("name").getAsString())) {
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
        updateState(powerLevel.channelId, powerLevelValue == null ? UnDefType.UNDEF : new PercentType(powerLevelValue));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(powerLevel.channelId)) {
            if (ContainsCapabilityProperty(capabilties, powerLevel.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    if (lastPowerLevel != null) {
                        int newValue = lastPowerLevel++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        lastPowerLevel = newValue;
                        connection.smartHomeCommand(entityId, "setPowerLevel", powerLevel.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    if (lastPowerLevel != null) {
                        int newValue = lastPowerLevel--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        lastPowerLevel = newValue;
                        connection.smartHomeCommand(entityId, "setPowerLevel", powerLevel.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastPowerLevel = 0;
                    connection.smartHomeCommand(entityId, "setPowerLevel", powerLevel.propertyName, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastPowerLevel = 100;
                    connection.smartHomeCommand(entityId, "setPowerLevel", powerLevel.propertyName, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastPowerLevel = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setPowerLevel", powerLevel.propertyName,
                            ((PercentType) command).floatValue() / 100);
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
