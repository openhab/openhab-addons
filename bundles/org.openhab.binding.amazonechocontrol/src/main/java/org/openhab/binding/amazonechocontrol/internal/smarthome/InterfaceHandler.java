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
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescription;

import com.google.gson.JsonObject;

/**
 * The {@link InterfaceHandler} is an interface for Alexa interface handlers
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface InterfaceHandler {
    Collection<ChannelInfo> initialize(List<JsonSmartHomeCapability> capabilities);

    List<String> getSupportedInterface();

    boolean hasChannel(String channelId);

    void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result);

    boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException;

    @Nullable
    List<CommandOption> getCommandDescription(Channel channel);

    @Nullable
    StateDescription getStateDescription(Channel channel);

    class UpdateChannelResult {
        public boolean needSingleUpdate;
    }
}
