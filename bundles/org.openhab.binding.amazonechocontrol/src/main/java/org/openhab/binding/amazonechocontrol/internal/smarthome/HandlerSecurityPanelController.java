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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_CONTACT;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_STRING;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
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
 * The {@link HandlerSecurityPanelController} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
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
    final static ChannelInfo armState = new ChannelInfo("armState" /* propertyName */ , "armState" /* ChannelId */,
            CHANNEL_TYPE_ARM_STATE /* Channel Type */ , ITEM_TYPE_STRING /* Item Type */);

    final static ChannelInfo burglaryAlarm = new ChannelInfo("burglaryAlarm" /* propertyName */ ,
            "burglaryAlarm" /* ChannelId */, CHANNEL_TYPE_BURGLARY_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    final static ChannelInfo carbonMonoxideAlarm = new ChannelInfo("carbonMonoxideAlarm" /* propertyName */ ,
            "carbonMonoxideAlarm" /* ChannelId */, CHANNEL_TYPE_CARBON_MONOXIDE_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    final static ChannelInfo fireAlarm = new ChannelInfo("fireAlarm" /* propertyName */ , "fireAlarm" /* ChannelId */,
            CHANNEL_TYPE_FIRE_ALARM /* Channel Type */ , ITEM_TYPE_CONTACT /* Item Type */);

    final static ChannelInfo waterAlarm = new ChannelInfo("waterAlarm" /* propertyName */ ,
            "waterAlarm" /* ChannelId */, CHANNEL_TYPE_WATER_ALARM /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    private ChannelInfo[] getAlarmChannels() {
        return new ChannelInfo[] { burglaryAlarm, carbonMonoxideAlarm, fireAlarm, waterAlarm };
    }

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        if (armState.propertyName.equals(property)) {
            return new ChannelInfo[] { armState };
        }
        for (ChannelInfo channelInfo : getAlarmChannels()) {
            if (channelInfo.propertyName.equals(property)) {
                return new ChannelInfo[] { channelInfo };
            }
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        String armStateValue = null;
        Boolean burglaryAlarmValue = null;
        Boolean carbonMonoxideAlarmValue = null;
        Boolean fireAlarmValue = null;
        Boolean waterAlarmValue = null;
        for (JsonObject state : stateList) {
            if (armState.propertyName.equals(state.get("name").getAsString())) {
                if (armStateValue == null) {
                    armStateValue = state.get("value").getAsString();
                }
            } else if (burglaryAlarm.propertyName.equals(state.get("name").getAsString())) {
                if (burglaryAlarmValue == null) {
                    burglaryAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (carbonMonoxideAlarm.propertyName.equals(state.get("name").getAsString())) {
                if (carbonMonoxideAlarmValue == null) {
                    carbonMonoxideAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (fireAlarm.propertyName.equals(state.get("name").getAsString())) {
                if (fireAlarmValue == null) {
                    fireAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            } else if (waterAlarm.propertyName.equals(state.get("name").getAsString())) {
                if (waterAlarmValue == null) {
                    waterAlarmValue = "ALARM".equals(state.get("value").getAsString());
                }
            }
        }
        updateState(armState.channelId, armStateValue == null ? UnDefType.UNDEF : new StringType(armStateValue));
        updateState(burglaryAlarm.channelId, burglaryAlarmValue == null ? UnDefType.UNDEF
                : (burglaryAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(carbonMonoxideAlarm.channelId, carbonMonoxideAlarmValue == null ? UnDefType.UNDEF
                : (carbonMonoxideAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(fireAlarm.channelId, fireAlarmValue == null ? UnDefType.UNDEF
                : (fireAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(waterAlarm.channelId, waterAlarmValue == null ? UnDefType.UNDEF
                : (waterAlarmValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        if (channelId.equals(armState.channelId)) {

            if (ContainsCapabilityProperty(capabilties, armState.propertyName)) {
                if (command instanceof StringType) {
                    String armStateValue = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(armStateValue)) {
                        connection.smartHomeCommand(entityId, "controlSecurityPanel", armState.propertyName,
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
