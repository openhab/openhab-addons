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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.component.ComponentFactory.ComponentConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

import com.google.gson.Gson;

/**
 * A HomeAssistant component is comparable to a channel group.
 * It has a name and consists of multiple channels.
 *
 * @author David Graeff - Initial contribution
 * @param <C> Config class derived from {@link AbstractChannelConfiguration}
 */
@NonNullByDefault
public abstract class AbstractComponent<C extends AbstractChannelConfiguration> {
    private static final String JINJA_PREFIX = "JINJA:";

    // Component location fields
    private final ComponentConfiguration componentConfiguration;
    protected final ChannelGroupTypeUID channelGroupTypeUID;
    protected final ChannelGroupUID channelGroupUID;
    protected final HaID haID;

    // Channels and configuration
    protected final Map<String, ComponentChannel> channels = new TreeMap<>();
    // The hash code ({@link String#hashCode()}) of the configuration string
    // Used to determine if a component has changed.
    protected final int configHash;
    protected final String channelConfigurationJson;
    protected final C channelConfiguration;

    protected boolean configSeen;

    /**
     * Creates component based on generic configuration and component configuration type.
     *
     * @param componentConfiguration generic componentConfiguration with not parsed JSON config
     * @param clazz target configuration type
     */
    public AbstractComponent(ComponentFactory.ComponentConfiguration componentConfiguration, Class<C> clazz) {
        this.componentConfiguration = componentConfiguration;

        this.channelConfigurationJson = componentConfiguration.getConfigJSON();
        this.channelConfiguration = componentConfiguration.getConfig(clazz);
        this.configHash = channelConfigurationJson.hashCode();

        this.haID = componentConfiguration.getHaID();

        String groupId = this.haID.getGroupId(channelConfiguration.getUniqueId());

        this.channelGroupTypeUID = new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID, groupId);
        this.channelGroupUID = new ChannelGroupUID(componentConfiguration.getThingUID(), groupId);

        this.configSeen = false;

        String availabilityTopic = this.channelConfiguration.getAvailabilityTopic();
        if (availabilityTopic != null) {
            String availabilityTemplate = this.channelConfiguration.getAvailabilityTemplate();
            if (availabilityTemplate != null) {
                availabilityTemplate = JINJA_PREFIX + availabilityTemplate;
            }
            componentConfiguration.getTracker().addAvailabilityTopic(availabilityTopic,
                    this.channelConfiguration.getPayloadAvailable(), this.channelConfiguration.getPayloadNotAvailable(),
                    availabilityTemplate, componentConfiguration.getTransformationServiceProvider());
        }
    }

    protected ComponentChannel.Builder buildChannel(String channelID, Value valueState, String label,
            ChannelStateUpdateListener channelStateUpdateListener) {
        return new ComponentChannel.Builder(this, channelID, valueState, label, channelStateUpdateListener);
    }

    public void setConfigSeen() {
        this.configSeen = true;
    }

    /**
     * Subscribes to all state channels of the component and adds all channels to the provided channel type provider.
     *
     * @param connection connection to the MQTT broker
     * @param scheduler thing scheduler
     * @param timeout channel subscription timeout
     * @return A future that completes as soon as all subscriptions have been performed. Completes exceptionally on
     *         errors.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return channels.values().stream().map(cChannel -> cChannel.start(connection, scheduler, timeout))
                .collect(FutureCollector.allOf());
    }

    /**
     * Unsubscribes from all state channels of the component.
     *
     * @return A future that completes as soon as all subscriptions removals have been performed. Completes
     *         exceptionally on errors.
     */
    public CompletableFuture<@Nullable Void> stop() {
        return channels.values().stream().map(ComponentChannel::stop).collect(FutureCollector.allOf());
    }

    /**
     * Add all channel types to the channel type provider.
     *
     * @param channelTypeProvider The channel type provider
     */
    public void addChannelTypes(MqttChannelTypeProvider channelTypeProvider) {
        channelTypeProvider.setChannelGroupType(getGroupTypeUID(), getType());
        channels.values().forEach(v -> v.addChannelTypes(channelTypeProvider));
    }

    /**
     * Removes all channels from the channel type provider.
     * Call this if the corresponding Thing handler gets disposed.
     *
     * @param channelTypeProvider The channel type provider
     */
    public void removeChannelTypes(MqttChannelTypeProvider channelTypeProvider) {
        channels.values().forEach(v -> v.removeChannelTypes(channelTypeProvider));
        channelTypeProvider.removeChannelGroupType(getGroupTypeUID());
    }

    /**
     * Each HomeAssistant component corresponds to a Channel Group Type.
     */
    public ChannelGroupTypeUID getGroupTypeUID() {
        return channelGroupTypeUID;
    }

    /**
     * The unique id of this component.
     */
    public ChannelGroupUID getGroupUID() {
        return channelGroupUID;
    }

    /**
     * Component (Channel Group) name.
     */
    public String getName() {
        return channelConfiguration.getName();
    }

    /**
     * Each component consists of multiple Channels.
     */
    public Map<String, ComponentChannel> getChannelMap() {
        return channels;
    }

    /**
     * Return a components channel. A HomeAssistant MQTT component consists of multiple functions
     * and those are mapped to one or more channels. The channel IDs are constants within the
     * derived Component, like the {@link Switch#SWITCH_CHANNEL_ID}.
     *
     * @param channelID The channel ID
     * @return A components channel
     */
    public @Nullable ComponentChannel getChannel(String channelID) {
        return channels.get(channelID);
    }

    /**
     * @return Returns the configuration hash value for easy comparison.
     */
    public int getConfigHash() {
        return configHash;
    }

    /**
     * Return the channel group type.
     */
    public ChannelGroupType getType() {
        final List<ChannelDefinition> channelDefinitions = channels.values().stream().map(ComponentChannel::type)
                .collect(Collectors.toList());
        return ChannelGroupTypeBuilder.instance(channelGroupTypeUID, getName())
                .withChannelDefinitions(channelDefinitions).build();
    }

    /**
     * Resets all channel states to state UNDEF. Call this method after the connection
     * to the MQTT broker got lost.
     */
    public void resetState() {
        channels.values().forEach(ComponentChannel::resetState);
    }

    /**
     * Return the channel group definition for this component.
     */
    public ChannelGroupDefinition getGroupDefinition() {
        return new ChannelGroupDefinition(channelGroupUID.getId(), getGroupTypeUID(), getName(), null);
    }

    public HaID getHaID() {
        return haID;
    }

    public String getChannelConfigurationJson() {
        return channelConfigurationJson;
    }

    @Nullable
    public TransformationServiceProvider getTransformationServiceProvider() {
        return componentConfiguration.getTransformationServiceProvider();
    }

    public boolean isEnabledByDefault() {
        return channelConfiguration.isEnabledByDefault();
    }

    public Gson getGson() {
        return componentConfiguration.getGson();
    }
}
