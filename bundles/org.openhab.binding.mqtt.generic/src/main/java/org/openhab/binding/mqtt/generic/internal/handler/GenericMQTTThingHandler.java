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
package org.openhab.binding.mqtt.generic.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.generic.values.ValueFactory;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler manages manual created Things with manually added channels to link to MQTT topics.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class GenericMQTTThingHandler extends AbstractMQTTThingHandler implements ChannelStateUpdateListener {
    private final Logger logger = LoggerFactory.getLogger(GenericMQTTThingHandler.class);
    final Map<ChannelUID, ChannelState> channelStateByChannelUID = new HashMap<>();
    protected final MqttChannelStateDescriptionProvider stateDescProvider;
    protected final TransformationServiceProvider transformationServiceProvider;

    /**
     * Creates a new Thing handler for generic MQTT channels.
     *
     * @param thing The thing of this handler
     * @param stateDescProvider A channel state provider
     * @param transformationServiceProvider The transformation service provider
     * @param subscribeTimeout The subscribe timeout
     */
    public GenericMQTTThingHandler(Thing thing, MqttChannelStateDescriptionProvider stateDescProvider,
            TransformationServiceProvider transformationServiceProvider, int subscribeTimeout) {
        super(thing, subscribeTimeout);
        this.stateDescProvider = stateDescProvider;
        this.transformationServiceProvider = transformationServiceProvider;
    }

    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        return channelStateByChannelUID.get(channelUID);
    }

    /**
     * Subscribe on all channel static topics on all {@link ChannelState}s.
     * If subscribing on all channels worked, the thing is put ONLINE, else OFFLINE.
     *
     * @param connection A started broker connection
     */
    @Override
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        // availability topics are also started asynchronously, so no problem here
        clearAllAvailabilityTopics();
        initializeAvailabilityTopicsFromConfig();
        return channelStateByChannelUID.values().stream().map(c -> c.start(connection, scheduler, 0))
                .collect(FutureCollector.allOf()).thenRun(() -> calculateAndUpdateThingStatus(false));
    }

    @Override
    protected void stop() {
        channelStateByChannelUID.values().forEach(c -> c.getCache().resetState());
        super.stop();
    }

    @Override
    public void dispose() {
        // Remove all state descriptions of this handler
        channelStateByChannelUID.forEach((uid, state) -> stateDescProvider.remove(uid));
        super.dispose();
        // there is a design flaw, we can't clean up our stuff because it is needed by the super-class on disposal for
        // unsubscribing
        channelStateByChannelUID.clear();
    }

    @Override
    public CompletableFuture<Void> unsubscribeAll() {
        return CompletableFuture.allOf(
                channelStateByChannelUID.values().stream().map(ChannelState::stop).toArray(CompletableFuture[]::new));
    }

    /**
     * For every Thing channel there exists a corresponding {@link ChannelState}. It consists of the MQTT state
     * and MQTT command topic, the ChannelUID and a value state.
     *
     * @param channelConfig The channel configuration that contains MQTT state and command topic and multiple other
     *            configurations.
     * @param channelUID The channel UID
     * @param valueState The channel value state
     * @return
     */
    protected ChannelState createChannelState(ChannelConfig channelConfig, ChannelUID channelUID, Value valueState) {
        ChannelState state = new ChannelState(channelConfig, channelUID, valueState, this);

        // Incoming value transformations
        state.addTransformation(channelConfig.transformationPattern, transformationServiceProvider);
        // Outgoing value transformations
        state.addTransformationOut(channelConfig.transformationPatternOut, transformationServiceProvider);

        return state;
    }

    @Override
    public void initialize() {
        initializeAvailabilityTopicsFromConfig();

        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Framework failure: callback must not be null");
            return;
        }

        ThingBuilder thingBuilder = editThing();
        boolean modified = false;

        List<ChannelUID> configErrors = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                logger.warn("Channel {} has no type", channel.getLabel());
                continue;
            }
            final ChannelConfig channelConfig = channel.getConfiguration().as(ChannelConfig.class);

            if (channelTypeUID
                    .equals(new ChannelTypeUID(MqttBindingConstants.BINDING_ID, MqttBindingConstants.NUMBER))) {
                Unit<?> unit = UnitUtils.parseUnit(channelConfig.unit);
                String dimension = unit == null ? null : UnitUtils.getDimensionName(unit);
                String expectedItemType = dimension == null ? "Number" : "Number:" + dimension; // unknown dimension ->
                // Number
                String actualItemType = channel.getAcceptedItemType();
                if (!expectedItemType.equals(actualItemType)) {
                    ChannelBuilder channelBuilder = callback.createChannelBuilder(channel.getUID(), channelTypeUID)
                            .withAcceptedItemType(expectedItemType).withConfiguration(channel.getConfiguration());
                    String label = channel.getLabel();
                    if (label != null) {
                        channelBuilder.withLabel(label);
                    }
                    String description = channel.getDescription();
                    if (description != null) {
                        channelBuilder.withDescription(description);
                    }
                    thingBuilder.withoutChannel(channel.getUID());
                    thingBuilder.withChannel(channelBuilder.build());
                    modified = true;
                }
            }

            try {
                Value value = ValueFactory.createValueState(channelConfig, channelTypeUID.getId());
                ChannelState channelState = createChannelState(channelConfig, channel.getUID(), value);
                channelStateByChannelUID.put(channel.getUID(), channelState);
                StateDescription description = value.createStateDescription(channelConfig.commandTopic.isBlank())
                        .build().toStateDescription();
                if (description != null) {
                    stateDescProvider.setDescription(channel.getUID(), description);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Configuration error for channel '{}'", channel.getUID(), e);
                configErrors.add(channel.getUID());
            }
        }

        if (modified) {
            updateThing(thingBuilder.build());
        }

        // If some channels could not start up, put the entire thing offline and display the channels
        // in question to the user.
        if (!configErrors.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Remove and recreate: "
                    + configErrors.stream().map(ChannelUID::getAsString).collect(Collectors.joining(",")));
            return;
        }
        super.initialize();
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availibilityTopicsSeen) {
        if (availibilityTopicsSeen.orElse(true)) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        }
    }

    private void initializeAvailabilityTopicsFromConfig() {
        GenericThingConfiguration config = getConfigAs(GenericThingConfiguration.class);

        String availabilityTopic = config.availabilityTopic;

        if (availabilityTopic != null) {
            addAvailabilityTopic(availabilityTopic, config.payloadAvailable, config.payloadNotAvailable,
                    config.transformationPattern, transformationServiceProvider);
        } else {
            clearAllAvailabilityTopics();
        }
    }
}
