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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateTransformation;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * An {@link AbstractComponent}s derived class consists of one or multiple channels.
 * Each component channel consists of the determined channel type, channel type UID and the
 * channel description itself as well as the the channels state.
 *
 * After the discovery process has completed and the tree of components and component channels
 * have been built up, the channel types are registered to a custom channel type provider
 * before adding the channel descriptions to the Thing themselves.
 * <br>
 * <br>
 * An object of this class creates the required {@link ChannelType} and {@link ChannelTypeUID} as well
 * as keeps the {@link ChannelState} and {@link Channel} in one place.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentChannel {
    private static final String JINJA = "JINJA";

    private final ChannelUID channelUID;
    private final ChannelState channelState;
    private final Channel channel;
    private final ChannelType type;
    private final ChannelTypeUID channelTypeUID;
    private final ChannelStateUpdateListener channelStateUpdateListener;

    private ComponentChannel(ChannelUID channelUID, ChannelState channelState, Channel channel, ChannelType type,
            ChannelTypeUID channelTypeUID, ChannelStateUpdateListener channelStateUpdateListener) {
        super();
        this.channelUID = channelUID;
        this.channelState = channelState;
        this.channel = channel;
        this.type = type;
        this.channelTypeUID = channelTypeUID;
        this.channelStateUpdateListener = channelStateUpdateListener;
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
        // Make sure we set the callback again which might have been nulled during a stop
        channelState.setChannelStateUpdateListener(this.channelStateUpdateListener);

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
        private final AbstractComponent<?> component;
        private final String channelID;
        private final Value valueState;
        private final String label;
        private final ChannelStateUpdateListener channelStateUpdateListener;

        private @Nullable String stateTopic;
        private @Nullable String commandTopic;
        private boolean retain;
        private boolean trigger;
        private boolean isAdvanced;
        private @Nullable Integer qos;
        private @Nullable Predicate<Command> commandFilter;

        private @Nullable String templateIn;
        private @Nullable String templateOut;

        private String format = "%s";

        public Builder(AbstractComponent<?> component, String channelID, Value valueState, String label,
                ChannelStateUpdateListener channelStateUpdateListener) {
            this.component = component;
            this.channelID = channelID;
            this.valueState = valueState;
            this.label = label;
            this.isAdvanced = false;
            this.channelStateUpdateListener = channelStateUpdateListener;
        }

        public Builder stateTopic(@Nullable String stateTopic) {
            this.stateTopic = stateTopic;
            return this;
        }

        public Builder stateTopic(@Nullable String stateTopic, @Nullable String... templates) {
            this.stateTopic = stateTopic;
            if (stateTopic != null && !stateTopic.isBlank()) {
                for (String template : templates) {
                    if (template != null && !template.isBlank()) {
                        this.templateIn = template;
                        break;
                    }
                }
            }
            return this;
        }

        /**
         * @deprecated use commandTopic(String, boolean, int)
         * @param commandTopic topic
         * @param retain retain
         * @return this
         */
        @Deprecated
        public Builder commandTopic(@Nullable String commandTopic, boolean retain) {
            this.commandTopic = commandTopic;
            this.retain = retain;
            return this;
        }

        public Builder commandTopic(@Nullable String commandTopic, boolean retain, int qos) {
            return commandTopic(commandTopic, retain, qos, null);
        }

        public Builder commandTopic(@Nullable String commandTopic, boolean retain, int qos, @Nullable String template) {
            this.commandTopic = commandTopic;
            this.retain = retain;
            this.qos = qos;
            if (commandTopic != null && !commandTopic.isBlank()) {
                this.templateOut = template;
            }
            return this;
        }

        public Builder trigger(boolean trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder isAdvanced(boolean advanced) {
            this.isAdvanced = advanced;
            return this;
        }

        public Builder commandFilter(@Nullable Predicate<Command> commandFilter) {
            this.commandFilter = commandFilter;
            return this;
        }

        public Builder withFormat(String format) {
            this.format = format;
            return this;
        }

        public ComponentChannel build() {
            return build(true);
        }

        public ComponentChannel build(boolean addToComponent) {
            ChannelUID channelUID;
            ChannelState channelState;
            Channel channel;
            ChannelType type;
            ChannelTypeUID channelTypeUID;

            channelUID = new ChannelUID(component.getGroupUID(), channelID);
            channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID,
                    channelUID.getGroupId() + "_" + channelID);
            channelState = new HomeAssistantChannelState(
                    ChannelConfigBuilder.create().withRetain(retain).withQos(qos).withStateTopic(stateTopic)
                            .withCommandTopic(commandTopic).makeTrigger(trigger).withFormatter(format).build(),
                    channelUID, valueState, channelStateUpdateListener, commandFilter);

            if (this.trigger) {
                type = ChannelTypeBuilder.trigger(channelTypeUID, label)
                        .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL))
                        .isAdvanced(isAdvanced).build();
            } else {
                StateDescriptionFragment description = valueState.createStateDescription(commandTopic == null).build();
                type = ChannelTypeBuilder.state(channelTypeUID, label, channelState.getItemType())
                        .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HA_CHANNEL))
                        .withStateDescriptionFragment(description).isAdvanced(isAdvanced).build();
            }

            Configuration configuration = new Configuration();
            configuration.put("config", component.getChannelConfigurationJson());
            component.getHaID().toConfig(configuration);

            channel = ChannelBuilder.create(channelUID, channelState.getItemType()).withType(channelTypeUID)
                    .withKind(type.getKind()).withLabel(label).withConfiguration(configuration).build();

            ComponentChannel result = new ComponentChannel(channelUID, channelState, channel, type, channelTypeUID,
                    channelStateUpdateListener);

            TransformationServiceProvider transformationProvider = component.getTransformationServiceProvider();

            final String templateIn = this.templateIn;
            if (templateIn != null && transformationProvider != null) {
                channelState
                        .addTransformation(new ChannelStateTransformation(JINJA, templateIn, transformationProvider));
            }
            final String templateOut = this.templateOut;
            if (templateOut != null && transformationProvider != null) {
                channelState.addTransformationOut(
                        new ChannelStateTransformation(JINJA, templateOut, transformationProvider));
            }
            if (addToComponent) {
                component.getChannelMap().put(channelID, result);
            }
            return result;
        }
    }
}
