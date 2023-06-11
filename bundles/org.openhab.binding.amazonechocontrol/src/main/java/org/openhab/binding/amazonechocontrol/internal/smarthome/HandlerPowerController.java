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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_SWITCH;

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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerPowerController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerPowerController extends HandlerBase {
    private final Logger logger = LoggerFactory.getLogger(HandlerPowerController.class);

    // Interface
    public static final String INTERFACE = "Alexa.PowerController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_POWER_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "powerState");

    // Channel definitions
    private static final ChannelInfo POWER_STATE = new ChannelInfo("powerState" /* propertyName */ ,
            "powerState" /* ChannelId */, CHANNEL_TYPE_POWER_STATE /* Channel Type */ ,
            ITEM_TYPE_SWITCH /* Item Type */);

    public HandlerPowerController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (POWER_STATE.propertyName.equals(property)) {
            return new ChannelInfo[] { POWER_STATE };
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        logger.trace("{} received {}", this.smartHomeDeviceHandler.getId(), stateList);
        Boolean powerStateValue = null;
        for (JsonObject state : stateList) {
            if (POWER_STATE.propertyName.equals(state.get("name").getAsString())) {
                String value = state.get("value").getAsString();
                // For groups take true if all true
                powerStateValue = "ON".equals(value);
            }
        }
        logger.trace("{} final state {}", this.smartHomeDeviceHandler.getId(), powerStateValue);
        updateState(POWER_STATE.channelId, powerStateValue == null ? UnDefType.UNDEF : OnOffType.from(powerStateValue));
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(POWER_STATE.channelId)) {
            if (containsCapabilityProperty(capabilities, POWER_STATE.propertyName)) {
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
