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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link HandlerLockController} is responsible for the Alexa.LockController interface
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HandlerLockController extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.LockController";

    private static final ChannelInfo LOCK_STATE = new ChannelInfo("lockState", "lockState",
            Constants.CHANNEL_TYPE_LOCK_STATE);

    public HandlerLockController(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (LOCK_STATE.propertyName.equals(property)) {
            return Set.of(LOCK_STATE);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        Boolean lockStateValue = null;
        for (JsonObject state : stateList) {
            if (LOCK_STATE.propertyName.equals(state.get("name").getAsString())) {
                String value = state.get("value").getAsString();
                lockStateValue = "LOCKED".equals(value);

            }
        }
        smartHomeDeviceHandler.updateState(LOCK_STATE.channelId,
                lockStateValue == null ? UnDefType.UNDEF : OnOffType.from(lockStateValue));
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        if (channelId.equals(LOCK_STATE.channelId)) {
            if (containsCapabilityProperty(capabilities, LOCK_STATE.propertyName)) {
                if (command.equals(OnOffType.ON)) {
                    connection.smartHomeCommand(entityId, "lockAction", Map.of("targetLockState.value", "LOCKED"));
                    return true;
                } else if (command.equals(OnOffType.OFF)) {
                    connection.smartHomeCommand(entityId, "lockAction", Map.of("targetLockState.value", "UNLOCKED"));
                    return true;
                }
            }
        }
        return false;
    }
}
