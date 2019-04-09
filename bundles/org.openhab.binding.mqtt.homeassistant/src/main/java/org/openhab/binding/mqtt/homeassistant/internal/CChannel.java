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

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelDefinitionBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateTransformation;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.CFactory.ComponentConfiguration;

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
    private static final String JINJA = "JINJA";

    private final ChannelUID channelUID;
    private final ChannelState channelState; // Channel state (value)
    private final Channel channel; // ESH Channel
    private final ChannelType type;
    private final ChannelTypeUID channelTypeUID;

    private CChannel(ChannelUID channelUID, ChannelState channelState, Channel channel, ChannelType type,
            ChannelTypeUID channelTypeUID) {
        super();
        this.channelUID = channelUID;
        this.channelState = channelState;
        this.channel = channel;
        this.type = type;
        this.channelTypeUID = channelTypeUID;
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelState getState() {
        return channelState;
    }

    public CompletableFuture<@Nullable Void> stop() {
        return channelState.stop();
    }

    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return channelState.start(connection, scheduler, timeout);
    }

    public void addChannelTypes(MqttChannelTypeProvider channelTypeProvider) {
        channelTypeProvider.setChannelType(channelTypeUID, type);
    }

    public void removeChannelTypes(MqttChannelTypeProvider channelTypeProvider) {
        channelTypeProvider.removeChannelType(channelTypeUID);
    }

    public ChannelDefinition type() {
        return new ChannelDefinitionBuilder(channelUID.getId(), channelTypeUID).build();
    }

    public void resetState() {
        channelState.getCache().resetState();
    }

    public static class Builder {
        private AbstractComponent<?> component;
        private ComponentConfiguration componentConfiguration;
        private String channelID;
        private Value valueState;
        private String label;
        private @Nullable String state_topic;
        private @Nullable String command_topic;
        private boolean retain;
        private String unit = "";
        private @Nullable ChannelStateUpdateListener channelStateUpdateListener;

        private @Nullable String templateIn;

        public Builder(AbstractComponent<?> component, ComponentConfiguration componentConfiguration, String channelID,
                Value valueState, String label) {
            this.component = component;
            this.componentConfiguration = componentConfiguration;
            this.channelID = channelID;
            this.valueState = valueState;
            this.label = label;
        }

        public Builder listener(@Nullable ChannelStateUpdateListener channelStateUpdateListener) {
            this.channelStateUpdateListener = channelStateUpdateListener;
            return this;
        }

        public Builder stateTopic(@Nullable String state_topic) {
            this.state_topic = state_topic;
            return this;
        }

        public Builder stateTopic(@Nullable String state_topic, @Nullable String... templates) {
            this.state_topic = state_topic;
            if (StringUtils.isNotBlank(state_topic)) {
                for (String template : templates) {
                    if (StringUtils.isNotBlank(template)) {
                        this.templateIn = template;
                        break;
                    }
                }
            }
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder commandTopic(@Nullable String command_topic, boolean retain) {
            this.command_topic = command_topic;
            this.retain = retain;
            return this;
        }

        public CChannel build() {
            return build(true);
        }

        public CChannel build(boolean addToComponent) {
            ChannelUID channelUID;
            ChannelState channelState; // Channel state (value)
            Channel channel; // ESH Channel
            ChannelType type;
            ChannelTypeUID channelTypeUID;

            channelUID = new ChannelUID(component.channelGroupUID, channelID);
            channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID,
                    channelUID.getGroupId() + "_" + channelID);
            channelState = new ChannelState(
                    ChannelConfigBuilder.create().withRetain(retain).withStateTopic(state_topic)
                            .withCommandTopic(command_topic).build(),
                    channelUID, valueState, channelStateUpdateListener);

            if (StringUtils.isBlank(state_topic)) {
                type = ChannelTypeBuilder.trigger(channelTypeUID, label)
                        .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL)).build();
            } else {
                type = ChannelTypeBuilder.state(channelTypeUID, label, channelState.getItemType())
                        .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL))
                        .withStateDescription(valueState.createStateDescription(unit, command_topic == null)).build();
            }

            Configuration configuration = new Configuration();
            configuration.put("config", component.channelConfigurationJson);
            component.haID.toConfig(configuration);

            channel = ChannelBuilder.create(channelUID, channelState.getItemType()).withType(channelTypeUID)
                    .withKind(type.getKind()).withLabel(label).withConfiguration(configuration).build();

            CChannel result = new CChannel(channelUID, channelState, channel, type, channelTypeUID);

            @Nullable
            TransformationServiceProvider transformationProvider = componentConfiguration
                    .getTransformationServiceProvider();

            final String templateIn = this.templateIn;
            if (templateIn != null && transformationProvider != null) {
                channelState
                        .addTransformation(new ChannelStateTransformation(JINJA, templateIn, transformationProvider));
            }
            if (addToComponent) {
                component.channels.put(channelID, result);
            }
            return result;
        }
    }
}
