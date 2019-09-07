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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_COLOR_NAME;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerColorController} is responsible for the Alexa.ColorTemperatureController
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerColorController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.ColorController";
    public static final String INTERFACE_COLOR_PROPERTIES = "Alexa.ColorPropertiesController";
    // Channel and Properties
    static final String ALEXA_PROPERTY = "color";
    static final String CHANNEL_UID = "colorName";
    static final ChannelTypeUID CHANNEL_TYPE = CHANNEL_TYPE_COLOR_NAME;
    static final String ITEM_TYPE = ITEM_TYPE_STRING;

    static final String COLOR_ALEXA_PROPERTY = "color";
    static final String COLOR_CHANNEL_UID = "color";
    static final ChannelTypeUID COLOR_CHANNEL_TYPE = CHANNEL_TYPE_COLOR;
    static final String COLOR_ITEM_TYPE_COLOR = ITEM_TYPE_COLOR;
    // List of all actions
    static final String ACTION = "setColor";
    static final String ALEXA_PROPERTY_ACTION = "colorName";

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE, INTERFACE_COLOR_PROPERTIES };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (ALEXA_PROPERTY.contentEquals(property)) {
            return new ChannelInfo[] {
                    new ChannelInfo(COLOR_ALEXA_PROPERTY, COLOR_CHANNEL_UID, COLOR_CHANNEL_TYPE, COLOR_ITEM_TYPE_COLOR),
                    new ChannelInfo(ALEXA_PROPERTY, CHANNEL_UID, CHANNEL_TYPE, ITEM_TYPE) };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList) {
        if (INTERFACE.equals(interfaceName)) {
            HSBType color = null;
            for (JsonObject state : stateList) {
                if (COLOR_ALEXA_PROPERTY.equals(state.get("name").getAsString())) {

                    JsonObject value = state.get("value").getAsJsonObject();
                    // For groups take the maximum
                    if (color == null) {
                        color = new HSBType(new DecimalType(value.get("hue").getAsInt()),
                                new PercentType(value.get("saturation").getAsInt() * 100),
                                new PercentType(value.get("brightness").getAsInt() * 100));
                    }
                }
            }
            updateState(CHANNEL_UID, color == null ? UnDefType.UNDEF : color);
        }
        if (INTERFACE_COLOR_PROPERTIES.equals(interfaceName)) {
            String colorName = null;
            for (JsonObject state : stateList) {
                if (ALEXA_PROPERTY.equals(state.get("name").getAsString())) {
                    if (colorName == null) {
                        colorName = state.get("value").getAsJsonObject().get("name").getAsString();
                    }
                }
            }
            updateState(CHANNEL_UID, colorName == null ? UnDefType.UNDEF : new StringType(colorName));
        }
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(CHANNEL_UID)) {

            if (ContainsCapabilityProperty(capabilties, ALEXA_PROPERTY)) {
                if (command instanceof StringType) {
                    String colorTemperatureName = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(colorTemperatureName)) {

                        connection.smartHomeCommand(entityId, ACTION, ALEXA_PROPERTY_ACTION, colorTemperatureName);
                        return true;
                    }
                }
            }
        }
        if (channelId.equals(COLOR_CHANNEL_UID)) {

            if (ContainsCapabilityProperty(capabilties, ALEXA_PROPERTY)) {
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
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(Connection connection, String channelId,
            StateDescription originalStateDescription, @Nullable Locale locale) {
        return null;
    }
}
