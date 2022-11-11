/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.Properties;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.Property;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public abstract class HandlerBase {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(HandlerBase.class);

    protected SmartHomeDeviceHandler smartHomeDeviceHandler;
    protected Map<String, ChannelInfo> channels = new HashMap<>();

    public HandlerBase(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        this.smartHomeDeviceHandler = smartHomeDeviceHandler;
    }

    protected abstract ChannelInfo @Nullable [] findChannelInfos(SmartHomeCapability capability, String property);

    public abstract void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result);

    public abstract boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            List<SmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException;

    public abstract @Nullable StateDescription findStateDescription(String channelId,
            StateDescription originalStateDescription, @Nullable Locale locale);

    public boolean hasChannel(String channelId) {
        return channels.containsKey(channelId);
    }

    public abstract String[] getSupportedInterface();

    SmartHomeDeviceHandler getSmartHomeDeviceHandler() {
        return smartHomeDeviceHandler;
    }

    public Collection<ChannelInfo> initialize(List<SmartHomeCapability> capabilities) {
        Map<String, ChannelInfo> channels = new HashMap<>();
        for (SmartHomeCapability capability : capabilities) {
            Properties properties = capability.properties;
            if (properties != null) {
                List<JsonSmartHomeCapabilities.Property> supported = Objects.requireNonNullElse(properties.supported,
                        List.of());
                logger.trace("{} | {}", capability.toString(), supported.toString());
                for (Property property : supported) {
                    String name = property.name;
                    if (name != null) {
                        ChannelInfo[] channelInfos = findChannelInfos(capability, name);
                        if (channelInfos != null) {
                            for (ChannelInfo channelInfo : channelInfos) {
                                if (channelInfo != null) {
                                    channels.put(channelInfo.channelId, channelInfo);
                                }
                            }
                        }
                    }

                }
            }
        }
        this.channels = channels;
        return channels.values();
    }

    protected boolean containsCapabilityProperty(List<SmartHomeCapability> capabilities, String propertyName) {
        for (SmartHomeCapability capability : capabilities) {
            Properties properties = capability.properties;
            if (properties != null) {
                List<JsonSmartHomeCapabilities.Property> supported = Objects.requireNonNullElse(properties.supported,
                        List.of());
                if (supported.stream().anyMatch(p -> propertyName.equals(p.name))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateState(String channelId, State state) {
        getSmartHomeDeviceHandler().updateState(channelId, state);
    }

    public static class ChannelInfo {
        public final String propertyName;
        public final String channelId;
        public final String itemType;
        public ChannelTypeUID channelTypeUID;

        public ChannelInfo(String propertyName, String channelId, ChannelTypeUID channelTypeUID, String itemType) {
            this.propertyName = propertyName;
            this.channelId = channelId;
            this.itemType = itemType;
            this.channelTypeUID = channelTypeUID;
        }
    }

    public static class UpdateChannelResult {
        public boolean needSingleUpdate;
    }
}
