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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_CONTACT;

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
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerAcousticEventSensor} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HandlerAcousticEventSensor extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.AcousticEventSensor";

    // Channel types
    private static final ChannelTypeUID CHANNEL_TYPE_GLASS_BREAK_DETECTION_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "glassBreakDetectionState");
    private static final ChannelTypeUID CHANNEL_TYPE_SMOKE_ALARM_DETECTION_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "smokeAlarmDetectionState");

    // Channel definitions
    private static final ChannelInfo GLASS_BREAK_DETECTION_STATE = new ChannelInfo(
            "glassBreakDetectionState" /* propertyName */ , "glassBreakDetectionState" /* ChannelId */,
            CHANNEL_TYPE_GLASS_BREAK_DETECTION_STATE /* Channel Type */ , ITEM_TYPE_CONTACT /* Item Type */);
    private static final ChannelInfo SMOKE_ALARM_DETECTION_STATE = new ChannelInfo(
            "smokeAlarmDetectionState" /* propertyName */ , "smokeAlarmDetectionState" /* ChannelId */,
            CHANNEL_TYPE_SMOKE_ALARM_DETECTION_STATE /* Channel Type */ , ITEM_TYPE_CONTACT /* Item Type */);

    public HandlerAcousticEventSensor(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler);
    }

    private ChannelInfo[] getAlarmChannels() {
        return new ChannelInfo[] { GLASS_BREAK_DETECTION_STATE, SMOKE_ALARM_DETECTION_STATE };
    }

    @Override
    public String[] getSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property) {
        for (ChannelInfo channelInfo : getAlarmChannels()) {
            if (channelInfo.propertyName.equals(property)) {
                return new ChannelInfo[] { channelInfo };
            }
        }
        return null;
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Boolean glassBreakDetectionStateValue = null;
        Boolean smokeAlarmDetectionStateValue = null;
        for (JsonObject state : stateList) {
            if (GLASS_BREAK_DETECTION_STATE.propertyName.equals(state.get("name").getAsString())) {
                if (glassBreakDetectionStateValue == null) {
                    glassBreakDetectionStateValue = !"NOT_DETECTED"
                            .equals(state.get("value").getAsJsonObject().get("value").getAsString());
                }
            } else if (SMOKE_ALARM_DETECTION_STATE.propertyName.equals(state.get("name").getAsString())) {
                if (smokeAlarmDetectionStateValue == null) {
                    smokeAlarmDetectionStateValue = !"NOT_DETECTED"
                            .equals(state.get("value").getAsJsonObject().get("value").getAsString());
                }
            }
        }
        updateState(GLASS_BREAK_DETECTION_STATE.channelId, glassBreakDetectionStateValue == null ? UnDefType.UNDEF
                : (glassBreakDetectionStateValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(SMOKE_ALARM_DETECTION_STATE.channelId, smokeAlarmDetectionStateValue == null ? UnDefType.UNDEF
                : (smokeAlarmDetectionStateValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    @Override
    public boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command) throws IOException {
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelUID, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
