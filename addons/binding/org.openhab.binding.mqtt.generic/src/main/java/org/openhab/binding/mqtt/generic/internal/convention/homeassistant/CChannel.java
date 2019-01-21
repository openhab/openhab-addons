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

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelState;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.values.Value;

/**
 * An {@link AbstractComponent}s derived class consists of one or multiple channels.
 * Each component channel consists of the determined ESH channel type, channel type UID and the
 * ESH channel description itself as well as the the channels state.
 *
 * After the discovery process has completed and the tree of components and component channels
 * have been built up, the channel types are registered to a custom channel type provider
 * before adding the channel descriptions to the ESH Thing themselves.
 * <br>
 * <br>
 * An object of this class creates the required {@link ChannelType} and {@link ChannelTypeUID} as well
 * as keeps the {@link ChannelState} and {@link Channel} in one place.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CChannel {
    public final ChannelUID channelUID;
    public final ChannelState channelState; // Channel state (value)
    public final Channel channel; // ESH Channel
    public final ChannelType type;
    public final ChannelTypeUID channelTypeUID;

    /**
     * Create a HomeAssistant Component Channel.
     *
     * @param component The parent component.
     * @param channelID The channel ID
     * @param valueState A value container that is used to construct a {@link ChannelState}.
     * @param state_topic The optional state topic.
     * @param command_topic The optional command topic. Either the command or the state topic have to be set!
     * @param label The label for this channel. Should be internationalized.
     * @param unit The unit for this channel. Can be empty.
     */
    public CChannel(AbstractComponent component, String channelID, Value valueState, @Nullable String state_topic,
            @Nullable String command_topic, String label, String unit,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        this.channelUID = new ChannelUID(component.channelGroupUID, channelID);
        channelTypeUID = component.haID.getChannelTypeID(channelID);
        channelState = new ChannelState(ChannelConfigBuilder.create().withRetain(true).withStateTopic(state_topic)
                .withCommandTopic(command_topic).build(), channelUID, valueState, channelStateUpdateListener);

        if (StringUtils.isBlank(state_topic)) {
            type = ChannelTypeBuilder.trigger(channelTypeUID, label)
                    .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL)).build();
        } else {
            type = ChannelTypeBuilder.state(channelTypeUID, label, channelState.getItemType())
                    .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL))
                    .withStateDescription(valueState.createStateDescription(unit, command_topic == null)).build();
        }

        Configuration configuration = new Configuration();
        configuration.put("config", component.configJson);
        channel = ChannelBuilder.create(channelUID, channelState.getItemType()).withType(channelTypeUID)
                .withKind(type.getKind()).withLabel(label).withConfiguration(configuration).build();
    }
}
