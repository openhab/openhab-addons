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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
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
 * The {@link HandlerColorController} is responsible for the Alexa.ColorTemperatureController
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerColorController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.ColorController";
    public static final String INTERFACE_COLOR_PROPERTIES = "Alexa.ColorPropertiesController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_COLOR_NAME = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorName");

    private static final ChannelTypeUID CHANNEL_TYPE_COLOR = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "color");

    // Channel and Properties
    private static final ChannelInfo COLOR = new ChannelInfo("color" /* propertyName */, "color" /* ChannelId */,
            CHANNEL_TYPE_COLOR /* Channel Type */, ITEM_TYPE_COLOR /* Item Type */);

    private static final ChannelInfo COLOR_PROPERTIES = new ChannelInfo("colorProperties" /* propertyName */,
            "colorName" /* ChannelId */, CHANNEL_TYPE_COLOR_NAME /* Channel Type */, ITEM_TYPE_STRING /* Item Type */);

    private @Nullable HSBType lastColor;
    private @Nullable String lastColorName;

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE, INTERFACE_COLOR_PROPERTIES };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (COLOR.propertyName.contentEquals(property)) {
            return new ChannelInfo[] { COLOR, COLOR_PROPERTIES };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        if (INTERFACE.equals(interfaceName)) {
            // WRITING TO THIS CHANNEL DOES CURRENTLY NOT WORK, BUT WE LEAVE THE CODE FOR FUTURE USE!
            HSBType colorValue = null;
            for (JsonObject state : stateList) {
                if (COLOR.propertyName.equals(state.get("name").getAsString())) {
                    JsonObject value = state.get("value").getAsJsonObject();
                    // For groups take the maximum
                    if (colorValue == null) {
                        colorValue = new HSBType(new DecimalType(value.get("hue").getAsInt()),
                                new PercentType(value.get("saturation").getAsInt() * 100),
                                new PercentType(value.get("brightness").getAsInt() * 100));
                    }
                }
            }
            if (colorValue != null) {
                if (!colorValue.equals(lastColor)) {
                    result.needSingleUpdate = true;
                    lastColor = colorValue;
                }
            }
            updateState(COLOR.channelId, colorValue == null ? UnDefType.UNDEF : colorValue);
        }
        if (INTERFACE_COLOR_PROPERTIES.equals(interfaceName)) {
            String colorNameValue = null;
            for (JsonObject state : stateList) {
                if (COLOR_PROPERTIES.propertyName.equals(state.get("name").getAsString())) {
                    if (colorNameValue == null) {
                        result.needSingleUpdate = false;
                        colorNameValue = state.get("value").getAsJsonObject().get("name").getAsString();
                    }
                }
            }
            if (lastColorName == null) {
                lastColorName = colorNameValue;
            } else if (colorNameValue == null && lastColorName != null) {
                colorNameValue = lastColorName;
            }
            updateState(COLOR_PROPERTIES.channelId,
                    lastColorName == null ? UnDefType.UNDEF : new StringType(lastColorName));
        }
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(COLOR.channelId)) {
            if (containsCapabilityProperty(capabilties, COLOR.propertyName)) {
                if (command instanceof HSBType) {
                    HSBType color = ((HSBType) command);
                    JsonObject colorObject = new JsonObject();
                    colorObject.addProperty("hue", color.getHue());
                    colorObject.addProperty("saturation", color.getSaturation().floatValue() / 100);
                    colorObject.addProperty("brightness", color.getBrightness().floatValue() / 100);
                    connection.smartHomeCommand(entityId, "setColor", "color", colorObject);
                }
            }
        }
        if (channelId.equals(COLOR_PROPERTIES.channelId)) {
            if (containsCapabilityProperty(capabilties, COLOR.propertyName)) {
                if (command instanceof StringType) {
                    String colorName = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(colorName)) {
                        lastColorName = colorName;
                        connection.smartHomeCommand(entityId, "setColor", "colorName", colorName);
                        return true;
                    }
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
