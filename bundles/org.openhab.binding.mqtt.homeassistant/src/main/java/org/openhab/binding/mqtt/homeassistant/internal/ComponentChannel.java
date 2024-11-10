/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.StateDescription;

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
    private final ChannelState channelState;
    private Channel channel;
    private final @Nullable StateDescription stateDescription;
    private final @Nullable CommandDescription commandDescription;
    private final ChannelStateUpdateListener channelStateUpdateListener;

    private ComponentChannel(ChannelState channelState, Channel channel, @Nullable StateDescription stateDescription,
            @Nullable CommandDescription commandDescription, ChannelStateUpdateListener channelStateUpdateListener) {
        super();
        this.channelState = channelState;
        this.channel = channel;
        this.stateDescription = stateDescription;
        this.commandDescription = commandDescription;
        this.channelStateUpdateListener = channelStateUpdateListener;
    }

    public Channel getChannel() {
        return channel;
    }

    public void resetUID(ChannelUID channelUID) {
        channel = ChannelBuilder.create(channelUID, channel.getAcceptedItemType()).withType(channel.getChannelTypeUID())
                .withKind(channel.getKind()).withLabel(Objects.requireNonNull(channel.getLabel()))
                .withConfiguration(channel.getConfiguration()).withAutoUpdatePolicy(channel.getAutoUpdatePolicy())
                .build();
        channelState.setChannelUID(channelUID);
    }

    public void resetConfiguration(Configuration configuration) {
        channel = ChannelBuilder.create(channel).withConfiguration(configuration).build();
    }

    public void clearConfiguration() {
        resetConfiguration(new Configuration());
    }

    public ChannelState getState() {
        return channelState;
    }

    public @Nullable StateDescription getStateDescription() {
        return stateDescription;
    }

    public @Nullable CommandDescription getCommandDescription() {
        return commandDescription;
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

    public ChannelDefinition channelDefinition() {
        return new ChannelDefinitionBuilder(channel.getUID().getId(),
                Objects.requireNonNull(channel.getChannelTypeUID())).withLabel(channel.getLabel()).build();
    }

    public void resetState() {
        channelState.getCache().resetState();
    }

    public static class Builder {
        private final AbstractComponent<?> component;
        private final String channelID;
        private ChannelTypeUID channelTypeUID;
        private final Value valueState;
        private final String label;
        private final ChannelStateUpdateListener channelStateUpdateListener;

        private @Nullable String stateTopic;
        private @Nullable String commandTopic;
        private boolean retain;
        private boolean trigger;
        private boolean isAdvanced;
        private @Nullable AutoUpdatePolicy autoUpdatePolicy;
        private @Nullable Integer qos;
        private @Nullable Predicate<Command> commandFilter;

        private @Nullable String templateIn;
        private @Nullable String templateOut;

        private @Nullable Configuration configuration;

        private String format = "%s";

        public Builder(AbstractComponent<?> component, String channelID, ChannelTypeUID channelTypeUID,
                Value valueState, String label, ChannelStateUpdateListener channelStateUpdateListener) {
            this.component = component;
            this.channelID = channelID;
            this.channelTypeUID = channelTypeUID;
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

        public Builder withAutoUpdatePolicy(@Nullable AutoUpdatePolicy autoUpdatePolicy) {
            this.autoUpdatePolicy = autoUpdatePolicy;
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

        public Builder withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        // If the component explicitly specifies optimistic, or it's missing a state topic
        // put it in optimistic mode (which, in openHAB parlance, means to auto-update the
        // item).
        public Builder inferOptimistic(@Nullable Boolean optimistic) {
            String localStateTopic = stateTopic;
            if (optimistic == null && (localStateTopic == null || localStateTopic.isBlank())
                    || optimistic != null && optimistic == true) {
                this.autoUpdatePolicy = AutoUpdatePolicy.RECOMMEND;
            }
            return this;
        }

        public ComponentChannel build() {
            return build(true);
        }

        public ComponentChannel build(boolean addToComponent) {
            ChannelUID channelUID;
            ChannelState channelState;
            Channel channel;
            ChannelTransformation incomingTransformation = null, outgoingTransformation = null;

            channelUID = component.buildChannelUID(channelID);
            ChannelConfigBuilder channelConfigBuilder = ChannelConfigBuilder.create().withRetain(retain).withQos(qos)
                    .withStateTopic(stateTopic).withCommandTopic(commandTopic).makeTrigger(trigger)
                    .withFormatter(format);

            String localTemplateIn = templateIn;
            if (localTemplateIn != null) {
                incomingTransformation = new HomeAssistantChannelTransformation(component.getJinjava(), component,
                        localTemplateIn);
            }
            String localTemplateOut = templateOut;
            if (localTemplateOut != null) {
                outgoingTransformation = new HomeAssistantChannelTransformation(component.getJinjava(), component,
                        localTemplateOut);
            }

            channelState = new HomeAssistantChannelState(channelConfigBuilder.build(), channelUID, valueState,
                    channelStateUpdateListener, commandFilter, incomingTransformation, outgoingTransformation);

            // disabled by default components should always show up as advanced
            if (!component.isEnabledByDefault()) {
                isAdvanced = true;
            }
            if (isAdvanced) {
                channelTypeUID = new ChannelTypeUID(channelTypeUID.getBindingId(),
                        channelTypeUID.getId() + "-advanced");
            }

            ChannelKind kind;
            StateDescription stateDescription = null;
            CommandDescription commandDescription = null;
            if (this.trigger) {
                kind = ChannelKind.TRIGGER;
            } else {
                kind = ChannelKind.STATE;
                stateDescription = valueState.createStateDescription(commandTopic == null).build().toStateDescription();
                commandDescription = valueState.createCommandDescription().build();
            }

            Configuration configuration = this.configuration;
            if (configuration == null) {
                configuration = new Configuration();
                configuration.put("config", component.getChannelConfigurationJson());
                component.getHaID().toConfig(configuration);
            }

            channel = ChannelBuilder.create(channelUID, channelState.getItemType()).withType(channelTypeUID)
                    .withKind(kind).withLabel(label).withConfiguration(configuration)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();

            ComponentChannel result = new ComponentChannel(channelState, channel, stateDescription, commandDescription,
                    channelStateUpdateListener);

            if (addToComponent) {
                component.getChannelMap().put(channelID, result);
            }
            return result;
        }
    }
}
