/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerBrightnessController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerBrightnessController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.BrightnessController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_BRIGHTNESS = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "brightness");

    // Channel definitions
    private static final ChannelInfo BRIGHTNESS = new ChannelInfo("brightness" /* propertyName */ ,
            "brightness" /* ChannelId */, CHANNEL_TYPE_BRIGHTNESS /* Channel Type */ ,
            ITEM_TYPE_DIMMER /* Item Type */);

    private @Nullable Integer lastBrightness;

    public HandlerBrightnessController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (BRIGHTNESS.propertyName.equals(property)) {
            return new ChannelInfo[] { BRIGHTNESS };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Integer brightnessValue = null;
        for (JsonObject state : stateList) {
            if (BRIGHTNESS.propertyName.equals(state.get("name").getAsString())) {
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
        updateState(BRIGHTNESS.channelId, brightnessValue == null ? UnDefType.UNDEF : new PercentType(brightnessValue));
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(BRIGHTNESS.channelId)) {
            if (containsCapabilityProperty(capabilities, BRIGHTNESS.propertyName)) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    Integer lastBrightness = this.lastBrightness;
                    if (lastBrightness != null) {
                        int newValue = lastBrightness++;
                        if (newValue > 100) {
                            newValue = 100;
                        }
                        this.lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", BRIGHTNESS.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    Integer lastBrightness = this.lastBrightness;
                    if (lastBrightness != null) {
                        int newValue = lastBrightness--;
                        if (newValue < 0) {
                            newValue = 0;
                        }
                        this.lastBrightness = newValue;
                        connection.smartHomeCommand(entityId, "setBrightness", BRIGHTNESS.propertyName, newValue);
                        return true;
                    }
                } else if (command.equals(OnOffType.OFF)) {
                    lastBrightness = 0;
                    connection.smartHomeCommand(entityId, "setBrightness", BRIGHTNESS.propertyName, 0);
                    return true;
                } else if (command.equals(OnOffType.ON)) {
                    lastBrightness = 100;
                    connection.smartHomeCommand(entityId, "setBrightness", BRIGHTNESS.propertyName, 100);
                    return true;
                } else if (command instanceof PercentType) {
                    lastBrightness = ((PercentType) command).intValue();
                    connection.smartHomeCommand(entityId, "setBrightness", BRIGHTNESS.propertyName,
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
