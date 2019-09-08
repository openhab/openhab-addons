package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.Properties;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.Property;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

import com.google.gson.JsonObject;

public abstract class HandlerBase {

    @Nullable
    SmartHomeDeviceHandler smartHomeDeviceHandler;
    Map<String, ChannelInfo> channels = new HashMap<>();

    protected abstract @Nullable ChannelInfo[] FindChannelInfos(SmartHomeCapability capability, String property);

    protected abstract void updateChannels(String interfaceName, List<JsonObject> stateList);

    protected abstract boolean handleCommand(Connection connection, SmartHomeDevice shd, String entityId,
            SmartHomeCapability[] capabilties, String channelId, Command command) throws IOException;

    public abstract @Nullable StateDescription findStateDescription(String channelId,
            StateDescription originalStateDescription, @Nullable Locale locale);

    public boolean hasChannel(String channelId) {
        return channels.containsKey(channelId);
    }

    protected abstract String[] GetSupportedInterface();

    SmartHomeDeviceHandler getSmartHomeDeviceHandler() throws IllegalStateException {
        SmartHomeDeviceHandler smartHomeDeviceHandler = this.smartHomeDeviceHandler;
        if (smartHomeDeviceHandler == null) {
            throw new IllegalStateException("Handler not intialized");
        }
        return smartHomeDeviceHandler;
    }

    public Collection<ChannelInfo> intialize(SmartHomeDeviceHandler smartHomeDeviceHandler,
            List<SmartHomeCapability> capabilties) {
        this.smartHomeDeviceHandler = smartHomeDeviceHandler;
        Map<String, ChannelInfo> channels = new HashMap<>();
        for (SmartHomeCapability capability : capabilties) {
            Properties properties = capability.properties;
            if (properties != null) {
                @Nullable
                Property @Nullable [] supported = properties.supported;
                if (supported != null) {
                    for (@Nullable
                    Property property : supported) {
                        if (property != null) {
                            String name = property.name;
                            if (name != null) {
                                ChannelInfo[] channelInfos = FindChannelInfos(capability, name);
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
            }
        }
        this.channels = channels;
        return channels.values();
    }

    protected boolean ContainsCapabilityProperty(SmartHomeCapability[] capabilties, String propertyName) {
        for (SmartHomeCapability capability : capabilties) {
            Properties properties = capability.properties;
            if (properties != null) {
                @Nullable
                Property @Nullable [] supportedProperties = properties.supported;
                if (supportedProperties != null) {
                    for (Property property : supportedProperties) {
                        if (property != null) {
                            if (StringUtils.equals(propertyName, property.name)) {
                                return true;
                            }
                        }
                    }
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
}
