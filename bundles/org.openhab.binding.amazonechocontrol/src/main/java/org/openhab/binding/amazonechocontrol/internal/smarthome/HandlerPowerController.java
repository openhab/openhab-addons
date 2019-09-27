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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_SWITCH;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * The {@link HandlerPowerController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerPowerController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.PowerController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_POWER_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "powerState");

    // Channel definitions
    final static ChannelInfo powerState = new ChannelInfo("powerState" /* propertyName */ ,
            "powerState" /* ChannelId */, CHANNEL_TYPE_POWER_STATE /* Channel Type */ ,
            ITEM_TYPE_SWITCH /* Item Type */);

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (powerState.propertyName.equals(property)) {
            return new ChannelInfo[] { powerState };
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Boolean powerStateValue = null;
        for (JsonObject state : stateList) {
            if (powerState.propertyName.equals(state.get("name").getAsString())) {
                String value = state.get("value").getAsString();
                // For groups take true if all true
                if ("ON".equals(value)) {
                    powerStateValue = true;
                } else if (powerStateValue == null) {
                    powerStateValue = false;
                }

            }
        }
        updateState(powerState.channelId,
                powerStateValue == null ? UnDefType.UNDEF : (powerStateValue ? OnOffType.ON : OnOffType.OFF));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(powerState.channelId)) {

            if (ContainsCapabilityProperty(capabilties, powerState.propertyName)) {
                if (command.equals(OnOffType.ON)) {
                    connection.smartHomeCommand(entityId, "turnOn");
                    return true;
                } else if (command.equals(OnOffType.OFF)) {
                    connection.smartHomeCommand(entityId, "turnOff");
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
