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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerContactSensor} is responsible for the Alexa.ContactSensor interface
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HandlerContactSensor extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.ContactSensor";

    private static final ChannelInfo CONTACT_DETECTED_STATE = new ChannelInfo("detectionState", "detectionState",
            Constants.CHANNEL_TYPE_CONTACT_STATUS);

    public HandlerContactSensor(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (CONTACT_DETECTED_STATE.propertyName.equals(property)) {
            return Set.of(CONTACT_DETECTED_STATE);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        OpenClosedType contactClosed = null;
        for (JsonObject state : stateList) {
            if (CONTACT_DETECTED_STATE.propertyName.equals(state.get("name").getAsString())) {
                String value = state.get("value").getAsString();
                // For groups take true if all true
                switch (value) {
                    case "NOT_DETECTED":
                        contactClosed = OpenClosedType.CLOSED;
                        break;
                    case "DETECTED":
                        contactClosed = OpenClosedType.OPEN;
                        break;
                }
            }
        }
        smartHomeDeviceHandler.updateState(CONTACT_DETECTED_STATE.channelId,
                contactClosed == null ? UnDefType.UNDEF : contactClosed);
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        return false;
    }
}
