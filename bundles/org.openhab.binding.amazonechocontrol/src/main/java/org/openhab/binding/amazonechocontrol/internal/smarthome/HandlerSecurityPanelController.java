/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_CONTACT;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

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
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerSecurityPanelController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerSecurityPanelController extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.SecurityPanelController";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_ARM_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "armState");

    private static final ChannelTypeUID CHANNEL_TYPE_BURGLARY_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "burglaryAlarm");

    private static final ChannelTypeUID CHANNEL_TYPE_CARBON_MONOXIDE_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "carbonMonoxideAlarm");

    private static final ChannelTypeUID CHANNEL_TYPE_FIRE_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "fireAlarm");

    private static final ChannelTypeUID CHANNEL_TYPE_WATER_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "waterAlarm");

    // Channel definitions
    private static final ChannelInfo ARM_STATE = new ChannelInfo("armState" /* propertyName */ ,
            "armState" /* ChannelId */, CHANNEL_TYPE_ARM_STATE /* Channel Type */ , ITEM_TYPE_STRING /* Item Type */);

    private static final ChannelInfo BURGLARY_ALARM = new ChannelInfo("burglaryAlarm" /* propertyName */ ,
            "burglaryAlarm" /* ChannelId */, CHANNEL_TYPE_BURGLARY_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    private static final ChannelInfo CARBON_MONOXIDE_ALARM = new ChannelInfo("carbonMonoxideAlarm" /* propertyName */ ,
            "carbonMonoxideAlarm" /* ChannelId */, CHANNEL_TYPE_CARBON_MONOXIDE_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    private static final ChannelInfo FIRE_ALARM = new ChannelInfo("fireAlarm" /* propertyName */ ,
            "fireAlarm" /* ChannelId */, CHANNEL_TYPE_FIRE_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    private static final ChannelInfo WATER_ALARM = new ChannelInfo("waterAlarm" /* propertyName */ ,
            "waterAlarm" /* ChannelId */, CHANNEL_TYPE_WATER_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    public HandlerSecurityPanelController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    private ChannelInfo[] getAlarmChannels() {
        return new ChannelInfo[] { BURGLARY_ALARM, CARBON_MONOXIDE_ALARM, FIRE_ALARM, WATER_ALARM };
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        if (ARM_STATE.propertyName.equals(property)) {
            return new ChannelInfo[] { ARM_STATE };
        }
        for (ChannelInfo channelInfo : getAlarmChannels()) {
            if (channelInfo.propertyName.equals(property)) {
                return new ChannelInfo[] { channelInfo };
            }
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        String armStateValue = null;
        Boolean burglaryAlarmValue = null;
        Boolean carbonMonoxideAlarmValue = null;
        Boolean fireAlarmValue = null;
        Boolean waterAlarmValue = null;
        for (JsonObject state : stateList) {
            if (ARM_STATE.propertyName.equals(state.get("name").getAsString())) {
                if (armStateValue == null) {
                    armStateValue = state.get("value").getAsString();
                }
            } else if (BURGLARY_ALARM.propertyName.equals(state.get("name").getAsString())) {
                if (burglaryAlarmValue == null) {
                    burglaryAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (CARBON_MONOXIDE_ALARM.propertyName.equals(state.get("name").getAsString())) {
                if (carbonMonoxideAlarmValue == null) {
                    carbonMonoxideAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (FIRE_ALARM.propertyName.equals(state.get("name").getAsString())) {
                if (fireAlarmValue == null) {
                    fireAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (WATER_ALARM.propertyName.equals(state.get("name").getAsString())) {
                if (waterAlarmValue == null) {
                    waterAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            }
        }
        updateState(ARM_STATE.channelId, armStateValue == null ? UnDefType.UNDEF : new StringType(armStateValue));
        updateState(BURGLARY_ALARM.channelId, burglaryAlarmValue == null ? UnDefType.UNDEF
                : (burglaryAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(CARBON_MONOXIDE_ALARM.channelId, carbonMonoxideAlarmValue == null ? UnDefType.UNDEF
                : (carbonMonoxideAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(FIRE_ALARM.channelId, fireAlarmValue == null ? UnDefType.UNDEF
                : (fireAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(WATER_ALARM.channelId, waterAlarmValue == null ? UnDefType.UNDEF
                : (waterAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(ARM_STATE.channelId)) {
            if (containsCapabilityProperty(capabilities, ARM_STATE.propertyName)) {
                if (command instanceof StringType) {
                    String armStateValue = command.toFullString();
                    if (!armStateValue.isEmpty()) {
                        connection.smartHomeCommand(entityId, "controlSecurityPanel", ARM_STATE.propertyName,
                                armStateValue);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelUID, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
