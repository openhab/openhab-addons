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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelGroupUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.internal.generic.MqttTypeProvider;

/**
 * A HomeAssistant component is comparable to an ESH channel group.
 * It has a name and consists of multiple channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface HomeAssistentGroup {

    public ChannelGroupDefinition getGroupDefinition();

    /**
     * The unique id of this component within the ESH framework.
     */
    public ChannelGroupUID uid();

    /**
     * Each HomeAssistant component corresponds to an ESH Channel Group Type.
     */
    public ChannelGroupTypeUID groupTypeUID();

    /**
     * Return the channel group type.
     */
    public ChannelGroupType type();

    /**
     * Each component consists of multiple ESH Channels.
     */
    public Map<String, CChannel> channelTypes();

    /**
     * Add all channel types to the channel type provider.
     *
     * @param channelTypeProvider The channel type provider
     */
    public void addChannelTypes(MqttTypeProvider channelTypeProvider);

    /**
     * Removes all channels from the channel type provider.
     * Call this if the corresponding Thing handler gets disposed.
     *
     * @param channelTypeProvider The channel type provider
     */
    public void removeChannelTypes(MqttTypeProvider channelTypeProvider);

    /**
     * Unsubscribe from all state channels of the component.
     *
     * @return A future that completes as soon as all subscriptions removals have been performed. Completes
     *         exceptionally on errors.
     */
    public CompletableFuture<@Nullable Void> stop();

    /**
     * Subscribes to all state channels of the component and adds all channels to the provided channel type provider.
     *
     * @param connection The connection
     * @param channelStateUpdateListener A listener
     * @return A future that completes as soon as all subscriptions have been performed. Completes exceptionally on
     *         errors.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout);

    /**
     * Return a components channel. A HomeAssistant MQTT component consists of multiple functions
     * and those are mapped to one or more ESH channels. The channel IDs are constants within the
     * derived Component, like the {@link ComponentSwitch#switchChannelID}.
     *
     * @param channelID The channel ID
     * @return A components channel
     */
    public @Nullable CChannel channel(String channelID);

    /**
     * @return Returns the configuration hash value for easy comparison.
     */
    public int getConfigHash();
}
