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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelGroupUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.CFactory.ComponentConfiguration;

/**
 * A HomeAssistant component is comparable to an ESH channel group.
 * It has a name and consists of multiple channels.
 *
 * @author David Graeff - Initial contribution
 * @param <C> Config class derived from {@link BaseChannelConfiguration}
 */
@NonNullByDefault
public abstract class AbstractComponent<C extends BaseChannelConfiguration> {
    // Component location fields
    private final ComponentConfiguration componentConfiguration;
    protected final ChannelGroupTypeUID channelGroupTypeUID;
    protected final ChannelGroupUID channelGroupUID;
    protected final HaID haID;

    // Channels and configuration
    protected final Map<String, CChannel> channels = new TreeMap<>();
    // The hash code ({@link String#hashCode()}) of the configuration string
    // Used to determine if a component has changed.
    protected final int configHash;
    protected final String channelConfigurationJson;
    protected final C channelConfiguration;

    /**
     * Provide a thingUID and HomeAssistant topic ID to determine the ESH channel group UID and type.
     *
     * @param thing A ThingUID
     * @param haID A HomeAssistant topic ID
     * @param configJson The configuration string
     * @param gson A Gson instance
     */
    public AbstractComponent(CFactory.ComponentConfiguration componentConfiguration, Class<C> clazz) {
        this.componentConfiguration = componentConfiguration;

        this.channelConfigurationJson = componentConfiguration.getConfigJSON();
        this.channelConfiguration = componentConfiguration.getConfig(clazz);
        this.configHash = channelConfigurationJson.hashCode();

        this.haID = componentConfiguration.getHaID();

        String groupId = channelConfiguration.unique_id;
        if (groupId == null || StringUtils.isBlank(groupId)) {
            groupId = this.haID.getFallbackGroupId();
        }

        this.channelGroupTypeUID = new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID, groupId);
        this.channelGroupUID = new ChannelGroupUID(componentConfiguration.getThingUID(), groupId);
    }

    protected CChannel.Builder buildChannel(String channelID, Value valueState, String label) {
        return new CChannel.Builder(this, componentConfiguration, channelID, valueState, label);
    }

    /**
     * Subscribes to all state channels of the component and adds all channels to the provided channel type provider.
     *
     * @param connection The connection
     * @param channelStateUpdateListener A listener
     * @return A future that completes as soon as all subscriptions have been performed. Completes exceptionally on
     *         errors.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return channels.values().stream().map(v -> v.start(connection, scheduler, timeout))
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Unsubscribe from all state channels of the component.
     *
     * @return A future that completes as soon as all subscriptions removals have been performed. Completes
     *         exceptionally on errors.
     */
    public CompletableFuture<@Nullable Void> stop() {
        return channels.values().stream().map(v -> v.stop()).reduce(CompletableFuture.completedFuture(null),
                (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Add all channel types to the channel type provider.
     *
     * @param channelTypeProvider The channel type provider
     */
    public void addChannelTypes(MqttChannelTypeProvider channelTypeProvider) {
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
    }

    /**
     * Each HomeAssistant component corresponds to an ESH Channel Group Type.
     */
    public ChannelGroupTypeUID groupTypeUID() {
        return channelGroupTypeUID;
    }

    /**
     * The unique id of this component within the ESH framework.
     */
    public ChannelGroupUID uid() {
        return channelGroupUID;
    }

    /**
     * Component (Channel Group) name.
     */
    public String name() {
        return channelConfiguration.name;
    }

    /**
     * Each component consists of multiple ESH Channels.
     */
    public Map<String, CChannel> channelTypes() {
        return channels;
    }

    /**
     * Return a components channel. A HomeAssistant MQTT component consists of multiple functions
     * and those are mapped to one or more ESH channels. The channel IDs are constants within the
     * derived Component, like the {@link ComponentSwitch#switchChannelID}.
     *
     * @param channelID The channel ID
     * @return A components channel
     */
    public @Nullable CChannel channel(String channelID) {
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
    public ChannelGroupType type() {
        final List<ChannelDefinition> channelDefinitions = channels.values().stream().map(c -> c.type())
                .collect(Collectors.toList());
        return ChannelGroupTypeBuilder.instance(channelGroupTypeUID, name()).withChannelDefinitions(channelDefinitions)
                .build();
    }

    /**
     * Resets all channel states to state UNDEF. Call this method after the connection
     * to the MQTT broker got lost.
     */
    public void resetState() {
        channels.values().forEach(c -> c.resetState());
    }

}
