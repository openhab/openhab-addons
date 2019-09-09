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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.*;
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
 * The {@link HandlerBrightnessController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerBrightnessController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.BrightnessController";
    // Channel definitions
    final static ChannelInfo brightness = new ChannelInfo("brightness" /* propertyName */ ,
            "brightness" /* ChannelId */, CHANNEL_TYPE_BRIGHTNESS /* Channel Type */ ,
            ITEM_TYPE_DIMMER /* Item Type */);

    Integer lastBrightness;

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (brightness.propertyName.equals(property)) {
            return new ChannelInfo[] { brightness };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer brightnessValue = null;
        for (JsonObject state : stateList) {
            if (brightness.propertyName.equals(state.get("name").getAsString())) {
                int value = state.get("value").getAsInt();
                // For groups take the maximum
                if (brightnessValue == null) {
                    brightnessValue = value;
                } else if (value > brightnessValue) {
                    brightnessValue = value;
                }
            }
        }
        if (brightnessValue != null) {
            lastBrightness = brightnessValue;
        }
        updateState(brightness.channelId, brightnessValue == null ? UnDefType.UNDEF : new PercentType(brightnessValue));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(brightness.channelId)) {
            if (ContainsCapabilityProperty(capabilties, brightness.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    if (lastBrightness != null) {
                        int newValue = lastBrightness++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", brightness.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    if (lastBrightness != null) {
                        int newValue = lastBrightness--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", brightness.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastBrightness = 0;
                    connection.smartHomeCommand(entityId, "setBrightness", brightness.propertyName, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastBrightness = 100;
                    connection.smartHomeCommand(entityId, "setBrightness", brightness.propertyName, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastBrightness = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setBrightness", brightness.propertyName,
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
