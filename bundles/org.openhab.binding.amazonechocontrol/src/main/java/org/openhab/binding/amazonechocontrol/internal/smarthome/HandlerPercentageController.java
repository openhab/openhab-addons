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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_PERCENTAGE;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_DIMMER;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerPercentageController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerPercentageController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.PercentageController";
    // Channel definitions
    final static ChannelInfo percentage = new ChannelInfo("percentage" /* propertyName */ ,
            "percentage" /* ChannelId */, CHANNEL_TYPE_PERCENTAGE /* Channel Type */ ,
            ITEM_TYPE_DIMMER /* Item Type */);

    Integer lastPercentage;

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (percentage.propertyName.equals(property)) {
            return new ChannelInfo[] { percentage };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer percentageValue = null;
        for (JsonObject state : stateList) {
            if (percentage.propertyName.equals(state.get("name").getAsString())) {
                int value = state.get("value").getAsInt();
                // For groups take the maximum
                if (percentageValue == null) {
                    percentageValue = value;
                } else if (value > percentageValue) {
                    percentageValue = value;
                }
            }
        }
        if (percentageValue != null) {
            lastPercentage = percentageValue;
        }
        updateState(percentage.channelId, percentageValue == null ? UnDefType.UNDEF : new PercentType(percentageValue));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(percentage.channelId)) {
            if (ContainsCapabilityProperty(capabilties, percentage.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    if (lastPercentage != null) {
                        int newValue = lastPercentage++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        lastPercentage = newValue;
                        connection.smartHomeCommand(entityId, "setPercentage", percentage.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    if (lastPercentage != null) {
                        int newValue = lastPercentage--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        lastPercentage = newValue;
                        connection.smartHomeCommand(entityId, "setPercentage", percentage.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastPercentage = 0;
                    connection.smartHomeCommand(entityId, "setPercentage", percentage.propertyName, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastPercentage = 100;
                    connection.smartHomeCommand(entityId, "setPercentage", percentage.propertyName, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastPercentage = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setPercentage", percentage.propertyName,
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
