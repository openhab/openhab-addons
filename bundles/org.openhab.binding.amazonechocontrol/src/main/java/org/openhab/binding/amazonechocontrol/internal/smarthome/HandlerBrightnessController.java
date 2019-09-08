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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_POWER_STATE;
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
    static final String ALEXA_PROPERTY = "brightness";
    static final String CHANNEL_UID = "brightness";
    static final ChannelTypeUID CHANNEL_TYPE = CHANNEL_TYPE_POWER_STATE;
    static final String ITEM_TYPE = ITEM_TYPE_DIMMER;

    Integer lastBrightness;

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (ALEXA_PROPERTY.contentEquals(property)) {
            return new ChannelInfo[] { new ChannelInfo(ALEXA_PROPERTY, CHANNEL_UID, CHANNEL_TYPE, ITEM_TYPE) };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList) {
        Integer brightness = null;
        for (JsonObject state : stateList) {
            if (ALEXA_PROPERTY.equals(state.get("name").getAsString())) {
                int value = state.get("value").getAsInt();
                // For groups take the maximum
                if (brightness == null) {
                    brightness = value;
                } else if (value > brightness) {
                    brightness = value;
                }
            }
        }
        if (brightness != null) {
            lastBrightness = brightness;
        }
        updateState(CHANNEL_UID, brightness == null ? UnDefType.UNDEF : new PercentType(brightness));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(CHANNEL_UID)) {
            if (ContainsCapabilityProperty(capabilties, ALEXA_PROPERTY)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    if (lastBrightness != null) {
                        int newValue = lastBrightness++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", ALEXA_PROPERTY, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    if (lastBrightness != null) {
                        int newValue = lastBrightness--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", ALEXA_PROPERTY, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastBrightness = 0;
                    connection.smartHomeCommand(entityId, "setBrightness", ALEXA_PROPERTY, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastBrightness = 100;
                    connection.smartHomeCommand(entityId, "setBrightness", ALEXA_PROPERTY, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastBrightness = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setBrightness", ALEXA_PROPERTY,
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
