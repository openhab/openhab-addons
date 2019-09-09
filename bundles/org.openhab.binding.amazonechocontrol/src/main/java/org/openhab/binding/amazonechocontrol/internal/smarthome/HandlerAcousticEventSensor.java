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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_GLASS_BREAK_DETECTION_STATE;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_SMOKE_ALARM_DETECTION_STATE;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.ITEM_TYPE_CONTACT;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerAcousticEventSensor} is responsible for the Alexa.PowerControllerInterface
 *
 * @author Lukas Knoeller, Michael Geramb
 */
public class HandlerAcousticEventSensor extends HandlerBase {
    // Interface
    public static final String INTERFACE = "Alexa.AcousticEventSensor";
    // Channel definitions

    final static ChannelInfo glassBreakDetectionState = new ChannelInfo("glassBreakDetectionState" /* propertyName */ ,
            "glassBreakDetectionState" /* ChannelId */, CHANNEL_TYPE_GLASS_BREAK_DETECTION_STATE /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    final static ChannelInfo smokeAlarmDetectionState = new ChannelInfo("smokeAlarmDetectionState" /* propertyName */ ,
            "smokeAlarmDetectionState" /* ChannelId */, CHANNEL_TYPE_SMOKE_ALARM_DETECTION_STATE /* Channel Type */ ,
            ITEM_TYPE_CONTACT /* Item Type */);

    private ChannelInfo[] getAlarmChannels() {
        return new ChannelInfo[] { glassBreakDetectionState, smokeAlarmDetectionState };
    }

    @Override
    protected String[] GetSupportedInterface() {
        return new String[] { INTERFACE };
    }

    @Override
    protected @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property) {
        for (ChannelInfo channelInfo : getAlarmChannels()) {
            if (channelInfo.propertyName.equals(property)) {
                return new ChannelInfo[] { channelInfo };
            }
        }
        return null;
    }

    @Override
    protected void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Boolean glassBreakDetectionStateValue = null;
        Boolean smokeAlarmDetectionStateValue = null;
        for (JsonObject state : stateList) {
            if (glassBreakDetectionState.propertyName.equals(state.get("name").getAsString())) {
                if (glassBreakDetectionStateValue == null) {
                    glassBreakDetectionStateValue = !"NOT_DETECTED"
                            .equals(state.get("value").getAsJsonObject().get("value").getAsString());
                }
            } else if (smokeAlarmDetectionState.propertyName.equals(state.get("name").getAsString())) {
                if (smokeAlarmDetectionStateValue == null) {
                    smokeAlarmDetectionStateValue = !"NOT_DETECTED"
                            .equals(state.get("value").getAsJsonObject().get("value").getAsString());
                }
            }
        }
        updateState(glassBreakDetectionState.channelId, glassBreakDetectionStateValue == null ? UnDefType.UNDEF
                : (glassBreakDetectionStateValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
        updateState(smokeAlarmDetectionState.channelId, smokeAlarmDetectionStateValue == null ? UnDefType.UNDEF
                : (smokeAlarmDetectionStateValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    @Override
    protected boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException {
        return false;
    }

    @Override
    public @Nullable StateDescription findStateDescription(String channelUID, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        return null;
    }
}
