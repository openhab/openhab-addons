/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerPercentageController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerPercentageController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.PercentageController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_PERCENTAGE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "percentage");

    // Channel definitions
    private static final ChannelInfo PERCENTAGE = new ChannelInfo("percentage" /* propertyName */ ,
            "percentage" /* ChannelId */, CHANNEL_TYPE_PERCENTAGE /* Channel Type */ ,
            ITEM_TYPE_DIMMER /* Item Type */);

    private @Nullable Integer lastPercentage;

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (PERCENTAGE.propertyName.equals(property)) {
            return new ChannelInfo[] { PERCENTAGE };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer percentageValue = null;
        for (JsonObject state : stateList) {
            if (PERCENTAGE.propertyName.equals(state.get("name").getAsString())) {
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
        updateState(PERCENTAGE.channelId, percentageValue == null ? UnDefType.UNDEF : new PercentType(percentageValue));
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(PERCENTAGE.channelId)) {
            if (containsCapabilityProperty(capabilties, PERCENTAGE.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    Integer lastPercentage = this.lastPercentage;
                    if (lastPercentage != null) {
                        int newValue = lastPercentage++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        this.lastPercentage = newValue;
                        connection.smartHomeCommand(entityId, "setPercentage", PERCENTAGE.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    Integer lastPercentage = this.lastPercentage;
                    if (lastPercentage != null) {
                        int newValue = lastPercentage--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        this.lastPercentage = newValue;
                        connection.smartHomeCommand(entityId, "setPercentage", PERCENTAGE.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastPercentage = 0;
                    connection.smartHomeCommand(entityId, "setPercentage", PERCENTAGE.propertyName, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastPercentage = 100;
                    connection.smartHomeCommand(entityId, "setPercentage", PERCENTAGE.propertyName, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastPercentage = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setPercentage", PERCENTAGE.propertyName, lastPercentage);
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
