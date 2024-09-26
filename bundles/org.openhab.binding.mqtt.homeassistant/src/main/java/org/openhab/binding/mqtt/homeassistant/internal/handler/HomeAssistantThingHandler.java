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
package org.openhab.binding.mqtt.homeassistant.internal.handler;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
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
import org.openhab.binding.mqtt.homeassistant.internal.component.Update;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hubspot.jinjava.Jinjava;

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
    private static final Comparator<AbstractComponent<?>> COMPONENT_COMPARATOR = Comparator
            .comparing((AbstractComponent<?> component) -> component.hasGroup())
            .thenComparing(AbstractComponent::getName);
    private static final URI UPDATABLE_CONFIG_DESCRIPTION_URI = URI.create("thing-type:mqtt:homeassistant-updatable");

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);

    protected final MqttChannelTypeProvider channelTypeProvider;
    protected final MqttChannelStateDescriptionProvider stateDescriptionProvider;
    protected final ChannelTypeRegistry channelTypeRegistry;
    protected final Jinjava jinjava;
    public final int attributeReceiveTimeout;
    protected final DelayedBatchProcessing<AbstractComponent<?>> delayedProcessing;
    protected final DiscoverComponents discoverComponents;

    private final Gson gson;
    protected final Map<@Nullable String, AbstractComponent<?>> haComponents = new HashMap<>();

    protected HandlerConfiguration config = new HandlerConfiguration();
    private Set<HaID> discoveryHomeAssistantIDs = new HashSet<>();

    private boolean started;
    private boolean newStyleChannels;
    private @Nullable Update updateComponent;

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
            MqttChannelStateDescriptionProvider stateDescriptionProvider, ChannelTypeRegistry channelTypeRegistry,
            Jinjava jinjava, int subscribeTimeout, int attributeReceiveTimeout) {
        super(thing, subscribeTimeout);
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.jinjava = jinjava;
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        this.delayedProcessing = new DelayedBatchProcessing<>(attributeReceiveTimeout, this, scheduler);

        newStyleChannels = "true".equals(thing.getProperties().get("newStyleChannels"));

        this.discoverComponents = new DiscoverComponents(thing.getUID(), scheduler, this, this, gson, jinjava,
                newStyleChannels);
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

        ThingTypeUID typeID = getThing().getThingTypeUID();
        for (Channel channel : thing.getChannels()) {
            final String groupID = channel.getUID().getGroupId();
            // Already restored component?
            @Nullable
            AbstractComponent<?> component = haComponents.get(groupID);
            if (component != null) {
                continue;
            }
            HaID haID = HaID.fromConfig(config.basetopic, channel.getConfiguration());

            if (!config.topics.contains(haID.getTopic())) {
                // don't add a component for this channel that isn't configured on the thing
                // anymore
                // It will disappear from the thing when the thing type is updated below
                continue;
            }

            discoveryHomeAssistantIDs.add(haID);
            ThingUID thingUID = channel.getUID().getThingUID();
            String channelConfigurationJSON = (String) channel.getConfiguration().get("config");
            if (channelConfigurationJSON == null) {
                logger.warn("Provided channel does not have a 'config' configuration key!");
            } else {
                try {
                    component = ComponentFactory.createComponent(thingUID, haID, channelConfigurationJSON, this, this,
                            scheduler, gson, jinjava, newStyleChannels);
                    if (typeID.equals(MqttBindingConstants.HOMEASSISTANT_MQTT_THING)) {
                        typeID = calculateThingTypeUID(component);
                    }

                    haComponents.put(component.getGroupId(), component);
                } catch (ConfigurationException e) {
                    logger.error("Cannot restore component {}: {}", thing, e.getMessage());
                }
            }
        }
        if (updateThingType(typeID)) {
            super.initialize();
        }
    }

    @Override
    public void dispose() {
        removeStateDescriptions();
        // super.dispose() calls stop()
        super.dispose();
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
        String componentId;
        if (channelUID.isInGroup()) {
            componentId = channelUID.getGroupId();
        } else {
            componentId = channelUID.getId();
        }
        AbstractComponent<?> component;
        synchronized (haComponents) { // sync whenever discoverComponents is started
            component = haComponents.get(componentId);
        }
        if (component == null) {
            component = haComponents.get("");
            if (component == null) {
                return null;
            }
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
            ThingTypeUID typeID = getThing().getThingTypeUID();
            for (AbstractComponent<?> discovered : discoveredComponentsList) {
                if (typeID.equals(MqttBindingConstants.HOMEASSISTANT_MQTT_THING)) {
                    typeID = calculateThingTypeUID(discovered);
                }
                String id = discovered.getGroupId();
                AbstractComponent<?> known = haComponents.get(id);
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

                // Add component to the component map
                haComponents.put(id, discovered);
                // Start component / Subscribe to channel topics
                discovered.start(connection, scheduler, 0).exceptionally(e -> {
                    logger.warn("Failed to start component {}", discovered.getHaID(), e);
                    return null;
                });

                if (discovered instanceof Update) {
                    updateComponent = (Update) discovered;
                    updateComponent.setReleaseStateUpdateListener(this::releaseStateUpdated);
                }
            }
            updateThingType(typeID);
        }
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen) {
        if (availabilityTopicsSeen.orElse(messageReceived)) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        if (configurationParameters.containsKey("doUpdate")) {
            configurationParameters = new HashMap<>(configurationParameters);
            Object value = configurationParameters.remove("doUpdate");
            if (value instanceof Boolean doUpdate && doUpdate) {
                Update updateComponent = this.updateComponent;
                if (updateComponent == null) {
                    logger.warn(
                            "Received update command for Home Assistant device {}, but it does not have an update component.",
                            getThing().getUID());
                } else {
                    updateComponent.doUpdate();
                }
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    private boolean updateThingType(ThingTypeUID typeID) {
        // if this is a dynamic type, then we update the type
        if (!MqttBindingConstants.HOMEASSISTANT_MQTT_THING.equals(typeID)) {
            var thingTypeBuilder = channelTypeProvider.derive(typeID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING);

            if (getThing().getThingTypeUID().equals(MqttBindingConstants.HOMEASSISTANT_MQTT_THING)) {
                logger.debug("Migrating Home Assistant thing {} from generic type to dynamic type {}",
                        getThing().getUID(), typeID);

                // just create an empty thing type for now; channel configurations won't follow over
                // to the re-created Thing, so we need to re-discover them all anyway
                channelTypeProvider.putThingType(thingTypeBuilder.build());
                changeThingType(typeID, getConfig());
                return false;
            }

            synchronized (haComponents) { // sync whenever discoverComponents is started
                var sortedComponents = haComponents.values().stream().sorted(COMPONENT_COMPARATOR).toList();

                var channelGroupTypes = sortedComponents.stream().map(c -> c.getChannelGroupType(typeID.getId()))
                        .filter(Objects::nonNull).map(Objects::requireNonNull).toList();
                channelTypeProvider.updateChannelGroupTypesForPrefix(typeID.getId(), channelGroupTypes);

                var groupDefs = sortedComponents.stream().map(c -> c.getGroupDefinition(typeID.getId()))
                        .filter(Objects::nonNull).map(Objects::requireNonNull).toList();
                var channelDefs = sortedComponents.stream().map(AbstractComponent::getChannelDefinitions)
                        .flatMap(List::stream).toList();
                thingTypeBuilder.withChannelDefinitions(channelDefs).withChannelGroupDefinitions(groupDefs);
                Update updateComponent = this.updateComponent;
                if (updateComponent != null && updateComponent.isUpdatable()) {
                    thingTypeBuilder.withConfigDescriptionURI(UPDATABLE_CONFIG_DESCRIPTION_URI);
                }

                channelTypeProvider.putThingType(thingTypeBuilder.build());

                removeStateDescriptions();
                sortedComponents.stream().forEach(c -> c.addStateDescriptions(stateDescriptionProvider));

                ThingBuilder thingBuilder = editThing().withChannels();

                sortedComponents.stream().map(AbstractComponent::getChannels).flatMap(List::stream)
                        .forEach(c -> thingBuilder.withChannel(c));

                updateThing(thingBuilder.build());
            }
        }
        return true;
    }

    private ThingTypeUID calculateThingTypeUID(AbstractComponent component) {
        return new ThingTypeUID(MqttBindingConstants.BINDING_ID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING.getId()
                + "_" + component.getChannelConfiguration().getThingId(component.getHaID().objectID));
    }

    @Override
    public void handleRemoval() {
        synchronized (haComponents) {
            channelTypeProvider.removeThingType(thing.getThingTypeUID());
            channelTypeProvider.removeChannelGroupTypesForPrefix(thing.getThingTypeUID().getId());
            removeStateDescriptions();
        }
        super.handleRemoval();
    }

    private void removeStateDescriptions() {
        thing.getChannels().stream().forEach(c -> stateDescriptionProvider.remove(c.getUID()));
    }

    private void releaseStateUpdated(Update.ReleaseState state) {
        Map<String, String> properties = editProperties();
        properties = state.appendToProperties(properties);
        updateProperties(properties);
    }
}
