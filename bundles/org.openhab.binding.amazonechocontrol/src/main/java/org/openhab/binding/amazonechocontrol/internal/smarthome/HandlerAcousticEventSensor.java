/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerAcousticEventSensor} is responsible for the Alexa.AcousticEventSensor interface
 *
 * @author Lukas Knoeller - Initial contribution
 * @author Michael Geramb - Initial contribution
 * @author Jan N. Klug - refactoring and new channels
 */
@NonNullByDefault
public class HandlerAcousticEventSensor extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.AcousticEventSensor";

    private static final Map<String, ChannelInfo> PROPERTY_NAME_TO_CHANNEL_INFO = Map.ofEntries(
            Map.entry("glassBreakDetectionState",
                    new ChannelInfo("glassBreakDetectionState", "glassBreakDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Glass Break")),
            Map.entry("beepingApplianceDetectionState",
                    new ChannelInfo("beepingApplianceDetectionState", "beepingApplianceDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Beeping Appliance")),
            Map.entry("runningWaterDetectionState",
                    new ChannelInfo("runningWaterDetectionState", "runningWaterDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Running Water")),
            Map.entry("dogBarkDetectionState",
                    new ChannelInfo("dogBarkDetectionState", "dogBarkDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Dog Bark")),
            Map.entry("humanPresenceDetectionState",
                    new ChannelInfo("humanPresenceDetectionState", "humanPresenceDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Human Presence")),
            Map.entry("smokeSirenDetectionState",
                    new ChannelInfo("smokeSirenDetectionState", "smokeSirenDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Smoke Siren")),
            Map.entry("snoreDetectionState",
                    new ChannelInfo("snoreDetectionState", "snoreDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Snore")),
            Map.entry("waterSoundsDetectionState",
                    new ChannelInfo("waterSoundsDetectionState", "waterSoundsDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Water Sounds")),
            Map.entry("coughDetectionState",
                    new ChannelInfo("coughDetectionState", "coughDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Cough")),
            Map.entry("carbonMonoxideSirenDetectionState",
                    new ChannelInfo("carbonMonoxideSirenDetectionState", "carbonMonoxideSirenDetectionState",
                            Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Carbon Monoxide Siren")),
            Map.entry("babyCryDetectionState", new ChannelInfo("babyCryDetectionState", "babyCryDetectionState",
                    Constants.CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION, "Baby Cry")));

    public HandlerAcousticEventSensor(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (property == null) {
            return Set.of();
        }
        ChannelInfo channelInfo = PROPERTY_NAME_TO_CHANNEL_INFO.get(property);
        if (channelInfo != null) {
            return Set.of(channelInfo);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        for (JsonObject state : stateList) {
            String propertyName = state.get("name").getAsString();
            ChannelInfo channelInfo = PROPERTY_NAME_TO_CHANNEL_INFO.get(propertyName);
            if (channelInfo != null) {
                smartHomeDeviceHandler.updateState(channelInfo.channelId,
                        !"NOT_DETECTED".equals(state.get("value").getAsJsonObject().get("value").getAsString())
                                ? OpenClosedType.CLOSED
                                : OpenClosedType.OPEN);
            }
        }
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command) throws IOException {
        return false;
    }
}
