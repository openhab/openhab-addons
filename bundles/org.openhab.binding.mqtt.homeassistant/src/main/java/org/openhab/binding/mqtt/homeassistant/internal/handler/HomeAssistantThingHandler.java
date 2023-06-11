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
package org.openhab.binding.mqtt.homeassistant.internal.handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.tools.DelayedBatchProcessing;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.binding.mqtt.homeassistant.internal.component.ComponentFactory;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.util.ThingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handles HomeAssistant MQTT object things. Such an HA Object can have multiple HA Components with different instances
 * of those Components. This handler auto-discovers all available Components and Component Instances and
 * adds any new appearing components over time.<br>
 * <br>
 *
 * The specification does not cover the case of disappearing Components. This handler doesn't as well therefore.<br>
 * <br>
 *
 * A Component Instance equals a Channel Group and the Component parts equal Channels.<br>
 * <br>
 *
 * If a Components configuration changes, the known ChannelGroupType and ChannelTypes are replaced with the new ones.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantThingHandler extends AbstractMQTTThingHandler
        implements ComponentDiscovered, Consumer<List<AbstractComponent<?>>> {
    public static final String AVAILABILITY_CHANNEL = "availability";
    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_UID = Comparator
            .comparing(channel -> channel.getUID().toString());;

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);

    protected final MqttChannelTypeProvider channelTypeProvider;
    public final int attributeReceiveTimeout;
    protected final DelayedBatchProcessing<AbstractComponent<?>> delayedProcessing;
    protected final DiscoverComponents discoverComponents;

    private final Gson gson;
    protected final Map<String, AbstractComponent<?>> haComponents = new HashMap<>();

    protected HandlerConfiguration config = new HandlerConfiguration();
    private Set<HaID> discoveryHomeAssistantIDs = new HashSet<>();

    protected final TransformationServiceProvider transformationServiceProvider;

    private boolean started;

    /**
     * Create a new thing handler for HomeAssistant MQTT components.
     * A channel type provider and a topic value receive timeout must be provided.
     *
     * @param thing The thing of this handler
     * @param channelTypeProvider A channel type provider
     * @param subscribeTimeout Timeout for the entire tree parsing and subscription. In milliseconds.
     * @param attributeReceiveTimeout The timeout per attribute field subscription. In milliseconds.
     */
    public HomeAssistantThingHandler(Thing thing, MqttChannelTypeProvider channelTypeProvider,
            TransformationServiceProvider transformationServiceProvider, int subscribeTimeout,
            int attributeReceiveTimeout) {
        super(thing, subscribeTimeout);
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();
        this.channelTypeProvider = channelTypeProvider;
        this.transformationServiceProvider = transformationServiceProvider;
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        this.delayedProcessing = new DelayedBatchProcessing<>(attributeReceiveTimeout, this, scheduler);
        this.discoverComponents = new DiscoverComponents(thing.getUID(), scheduler, this, this, gson,
                this.transformationServiceProvider);
    }

    @Override
    public void initialize() {
        started = false;

        config = getConfigAs(HandlerConfiguration.class);
        if (config.topics.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device topics unknown");
            return;
        }
        discoveryHomeAssistantIDs.addAll(HaID.fromConfig(config));

        for (Channel channel : thing.getChannels()) {
            final String groupID = channel.getUID().getGroupId();
            if (groupID == null) {
                logger.warn("Channel {} has no groupd ID", channel.getLabel());
                continue;
            }
            // Already restored component?
            @Nullable
            AbstractComponent<?> component = haComponents.get(groupID);
            if (component != null) {
                // the types may have been removed in dispose() so we need to add them again
                component.addChannelTypes(channelTypeProvider);
                continue;
            }

            HaID haID = HaID.fromConfig(config.basetopic, channel.getConfiguration());
            discoveryHomeAssistantIDs.add(haID);
            ThingUID thingUID = channel.getUID().getThingUID();
            String channelConfigurationJSON = (String) channel.getConfiguration().get("config");
            if (channelConfigurationJSON == null) {
                logger.warn("Provided channel does not have a 'config' configuration key!");
            } else {
                try {
                    component = ComponentFactory.createComponent(thingUID, haID, channelConfigurationJSON, this, this,
                            scheduler, gson, transformationServiceProvider);
                    haComponents.put(component.getGroupUID().getId(), component);
                    component.addChannelTypes(channelTypeProvider);
                } catch (ConfigurationException e) {
                    logger.error("Cannot not restore component {}: {}", thing, e.getMessage());
                }
            }
        }
        updateThingType();

        super.initialize();
    }

    @Override
    public void dispose() {
        // super.dispose() calls stop()
        super.dispose();
        haComponents.values().forEach(c -> c.removeChannelTypes(channelTypeProvider));
    }

    @Override
    public CompletableFuture<Void> unsubscribeAll() {
        // already unsubscribed everything by calling stop()
        return CompletableFuture.allOf();
    }

    /**
     * Start a background discovery for the configured HA MQTT object-id.
     */
    @Override
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        started = true;

        connection.setQos(1);
        updateStatus(ThingStatus.UNKNOWN);

        // Start all known components and channels within the components and put the Thing offline
        // if any subscribing failed ( == broker connection lost)
        CompletableFuture<@Nullable Void> future = CompletableFuture.allOf(super.start(connection),
                haComponents.values().stream().map(e -> e.start(connection, scheduler, attributeReceiveTimeout))
                        .reduce(CompletableFuture.completedFuture(null), (a, v) -> a.thenCompose(b -> v)) // reduce to
                                                                                                          // one
                        .exceptionally(e -> {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                            return null;
                        }));

        return future
                .thenCompose(b -> discoverComponents.startDiscovery(connection, 0, discoveryHomeAssistantIDs, this));
    }

    @Override
    protected void stop() {
        if (started) {
            discoverComponents.stopDiscovery();
            delayedProcessing.join();
            // haComponents does not need to be synchronised -> the discovery thread is disabled
            haComponents.values().stream().map(AbstractComponent::stop) //
                    // we need to join all the stops, otherwise they might not be done when start is called
                    .collect(FutureCollector.allOf()).join();

            started = false;
        }
        super.stop();
    }

    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        String groupID = channelUID.getGroupId();
        if (groupID == null) {
            return null;
        }
        AbstractComponent<?> component;
        synchronized (haComponents) { // sync whenever discoverComponents is started
            component = haComponents.get(groupID);
        }
        if (component == null) {
            return null;
        }
        ComponentChannel componentChannel = component.getChannel(channelUID.getIdWithoutGroup());
        if (componentChannel == null) {
            return null;
        }
        return componentChannel.getState();
    }

    /**
     * Callback of {@link DiscoverComponents}. Add to a delayed batch processor.
     */
    @Override
    public void componentDiscovered(HaID homeAssistantTopicID, AbstractComponent<?> component) {
        delayedProcessing.accept(component);
    }

    /**
     * Callback of {@link DelayedBatchProcessing}.
     * Add all newly discovered components to the Thing and start the components.
     */
    @Override
    public void accept(List<AbstractComponent<?>> discoveredComponentsList) {
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        synchronized (haComponents) { // sync whenever discoverComponents is started
            for (AbstractComponent<?> discovered : discoveredComponentsList) {
                AbstractComponent<?> known = haComponents.get(discovered.getGroupUID().getId());
                // Is component already known?
                if (known != null) {
                    if (discovered.getConfigHash() != known.getConfigHash()) {
                        // Don't wait for the future to complete. We are also not interested in failures.
                        // The component will be replaced in a moment.
                        known.stop();
                    } else {
                        known.setConfigSeen();
                        continue;
                    }
                }

                // Add channel and group types to the types registry
                discovered.addChannelTypes(channelTypeProvider);
                // Add component to the component map
                haComponents.put(discovered.getGroupUID().getId(), discovered);
                // Start component / Subscribe to channel topics
                discovered.start(connection, scheduler, 0).exceptionally(e -> {
                    logger.warn("Failed to start component {}", discovered.getGroupUID(), e);
                    return null;
                });

                List<Channel> discoveredChannels = discovered.getChannelMap().values().stream()
                        .map(ComponentChannel::getChannel).collect(Collectors.toList());
                if (known != null) {
                    // We had previously known component with different config hash
                    // We remove all conflicting old channels, they will be re-added below based on the new discovery
                    logger.debug(
                            "Received component {} with slightly different config. Making sure we re-create conflicting channels...",
                            discovered.getGroupUID());
                    removeJustRediscoveredChannels(discoveredChannels);
                }

                // Add newly discovered channels. We sort the channels
                // for (mostly) consistent jsondb serialization
                discoveredChannels.sort(CHANNEL_COMPARATOR_BY_UID);
                ThingHelper.addChannelsToThing(thing, discoveredChannels);
            }
            updateThingType();
        }
    }

    private void removeJustRediscoveredChannels(List<Channel> discoveredChannels) {
        ArrayList<Channel> mutableChannels = new ArrayList<>(getThing().getChannels());
        Set<ChannelUID> newChannelUIDs = discoveredChannels.stream().map(Channel::getUID).collect(Collectors.toSet());
        // Take current channels but remove those channels that were just re-discovered
        List<Channel> existingChannelsWithNewlyDiscoveredChannelsRemoved = mutableChannels.stream()
                .filter(existingChannel -> !newChannelUIDs.contains(existingChannel.getUID()))
                .collect(Collectors.toList());
        if (existingChannelsWithNewlyDiscoveredChannelsRemoved.size() < mutableChannels.size()) {
            // We sort the channels for (mostly) consistent jsondb serialization
            existingChannelsWithNewlyDiscoveredChannelsRemoved.sort(CHANNEL_COMPARATOR_BY_UID);
            updateThingChannels(existingChannelsWithNewlyDiscoveredChannelsRemoved);
        }
    }

    private void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen) {
        if (availabilityTopicsSeen.orElse(messageReceived)) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        }
    }

    private void updateThingType() {
        // if this is a dynamic type, then we update the type
        ThingTypeUID typeID = thing.getThingTypeUID();
        if (!MqttBindingConstants.HOMEASSISTANT_MQTT_THING.equals(typeID)) {
            List<ChannelGroupDefinition> groupDefs;
            List<ChannelDefinition> channelDefs;
            synchronized (haComponents) { // sync whenever discoverComponents is started
                groupDefs = haComponents.values().stream().map(AbstractComponent::getGroupDefinition)
                        .collect(Collectors.toList());
                channelDefs = haComponents.values().stream().map(AbstractComponent::getType)
                        .map(ChannelGroupType::getChannelDefinitions).flatMap(List::stream)
                        .collect(Collectors.toList());
            }
            ThingType thingType = channelTypeProvider.derive(typeID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING)
                    .withChannelDefinitions(channelDefs).withChannelGroupDefinitions(groupDefs).build();

            channelTypeProvider.setThingType(typeID, thingType);
        }
    }
}
