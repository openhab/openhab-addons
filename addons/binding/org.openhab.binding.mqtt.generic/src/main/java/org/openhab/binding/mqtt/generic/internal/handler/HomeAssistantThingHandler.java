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
package org.openhab.binding.mqtt.generic.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.CChannel;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.CFactory;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.HaID;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.HandlerConfiguration;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.HomeAssistentGroup;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelState;
import org.openhab.binding.mqtt.generic.internal.generic.MqttTypeProvider;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.tools.DelayedBatchProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles HomeAssistant MQTT object things. Such an HA Object can have multiple HA Components with different instances
 * of those Components. This handler auto-discovers all available Components and Component Instances and
 * adds any new appearing components over time.<br>
 * <br>
 *
 * The specification does not cover the case of disappearing Components. This handler doesn't as well therefore.<br>
 * <br>
 *
 * A Component Instance equals an ESH Channel Group and the Component parts equal ESH Channels.<br>
 * <br>
 *
 * If a Components configuration changes, the known ChannelGroupType and ChannelTypes are replaced with the new ones.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantThingHandler extends AbstractMQTTThingHandler
        implements ComponentDiscovered, Consumer<List<HomeAssistentGroup>> {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);

    protected final MqttTypeProvider typeProvider;
    public final int attributeReceiveTimeout;
    protected final DelayedBatchProcessing<HomeAssistentGroup> delayedProcessing;

    private final Gson gson = new Gson();
    protected final Map<String, HomeAssistentGroup> haComponents = new HashMap<String, HomeAssistentGroup>();

    private Set<HaID> homeAssistantIDs = new HashSet<>();
    private Map<HaID, DiscoverComponents> discoverComponents = new HashMap<>();

    protected final TransformationServiceProvider transformationServiceProvider;

    /**
     * Create a new thing handler for HomeAssistant MQTT components.
     * A channel type provider and a topic value receive timeout must be provided.
     *
     * @param thing                   The thing of this handler
     * @param channelTypeProvider     A channel type provider
     * @param subscribeTimeout        Timeout for the entire tree parsing and subscription. In milliseconds.
     * @param attributeReceiveTimeout The timeout per attribute field subscription. In milliseconds.
     */
    public HomeAssistantThingHandler(Thing thing, MqttTypeProvider typeProvider,
            TransformationServiceProvider transformationServiceProvider, int subscribeTimeout,
            int attributeReceiveTimeout) {
        super(thing, subscribeTimeout);
        this.typeProvider = typeProvider;
        this.transformationServiceProvider = transformationServiceProvider;
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        this.delayedProcessing = new DelayedBatchProcessing<>(attributeReceiveTimeout, this, scheduler);
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void initialize() {
        HandlerConfiguration config = getConfigAs(HandlerConfiguration.class);
        if (config.objectid.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID unknown");
            return;
        }
        for (String objectid : config.objectid) {
            homeAssistantIDs.add(new HaID(config.basetopic, objectid, "", ""));
        }

        for (Channel channel : thing.getChannels()) {
            final String groupID = channel.getUID().getGroupId();
            if (groupID == null) {
                logger.warn("Channel {} has no groupd ID", channel.getLabel());
                continue;
            }
            // Already restored component?
            HomeAssistentGroup component = haComponents.get(groupID);
            if (component != null) {
                continue;
            }

            component = CFactory.createComponent(config.basetopic, channel, this, gson, transformationServiceProvider);

            if (component != null) {
                haComponents.put(component.uid().getId(), component);
                typeProvider.setChannelGroupType(component.groupTypeUID(), component.type());
                component.addChannelTypes(typeProvider);

                updateThingType();
            } else {
                logger.warn("Could not restore component {}", thing);
            }
        }

        super.initialize();
    }

    @Override
    public void dispose() {
        discoverComponents.values().forEach(dc -> dc.stopDiscovery());
        discoverComponents.clear();
        delayedProcessing.join();

        haComponents.values().forEach(c -> {
            c.removeChannelTypes(typeProvider);
            typeProvider.removeChannelGroupType(c.groupTypeUID());
        });

        // Unsubscribe from all components and component channel MQTT topics and more importantly
        // remove the reference to this handler.
        try {
            haComponents.values().stream().map(e -> e.stop())
                    .reduce(CompletableFuture.completedFuture(null), (a, v) -> a.thenCompose(b -> v))
                    .get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
            // Ignore any interrupts and timeouts on finish
        }
        super.dispose();
    }

    /**
     * Start a background discovery for the configured HA MQTT object-id.
     */
    @Override
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        connection.setRetain(true);
        connection.setQos(1);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "No response from the device yet");

        // Start all known components and channels within the components and put the Thing offline
        // if any subscribing failed ( == broker connection lost)
        CompletableFuture<@Nullable Void> future = haComponents.values().stream()
                .map(e -> e.start(connection, scheduler, attributeReceiveTimeout))
                .reduce(CompletableFuture.completedFuture(null), (a, v) -> a.thenCompose(b -> v)) // reduce to one
                .exceptionally(e -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    return null;
                });

        return future.thenCompose(b -> startDiscovery(connection));
    }

    private CompletableFuture<@Nullable Void> startDiscovery(MqttBrokerConnection connection) {
        return homeAssistantIDs.stream().map(id -> startDiscoveryForId(connection, id))
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    private CompletableFuture<@Nullable Void> startDiscoveryForId(MqttBrokerConnection connection, HaID id) {
        DiscoverComponents dc = discoverComponents.computeIfAbsent(id,
                i -> new DiscoverComponents(thing.getUID(), scheduler, this, gson, transformationServiceProvider));
        return dc.startDiscovery(connection, 0, id, this);
    }

    @Override
    protected void stop() {
        discoverComponents.values().forEach(dc -> dc.stopDiscovery());
        discoverComponents.clear();
        delayedProcessing.join();
        // haComponents does not need to be synchronized -> the discovery thread is disabled
        haComponents.values().stream().map(e -> e.stop());
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        String groupID = channelUID.getGroupId();
        if (groupID == null) {
            return null;
        }
        HomeAssistentGroup component;
        synchronized (haComponents) { // sync whenever discoverComponents is started
            component = haComponents.get(groupID);
        }
        if (component == null) {
            return null;
        }
        CChannel componentChannel = component.channel(channelUID.getIdWithoutGroup());
        if (componentChannel == null) {
            return null;
        }
        return componentChannel.channelState;
    }

    /**
     * Callback of {@link DiscoverComponents}. Add to a delayed batch processor.
     */
    @Override
    public void componentDiscovered(HaID homeAssistantTopicID, HomeAssistentGroup component) {
        delayedProcessing.accept(component);
    }

    /**
     * Callback of {@link DelayedBatchProcessing}.
     * Add all newly discovered components to the Thing and start the components.
     */
    @SuppressWarnings("null")
    @Override
    public void accept(List<HomeAssistentGroup> discoveredComponentsList) {
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        List<Channel> channels = new ArrayList<>();
        synchronized (haComponents) { // sync whenever discoverComponents is started
            for (HomeAssistentGroup discovered : discoveredComponentsList) {
                HomeAssistentGroup known = haComponents.get(discovered.uid().getId());
                // Is component already known?
                if (known != null) {
                    if (discovered.getConfigHash() != known.getConfigHash()) {
                        // Don't wait for the future to complete. We are also not interested in failures.
                        // The component will be replaced in a moment.
                        known.stop();
                    } else {
                        continue;
                    }
                }

                // Add thing, channel and group types to the types registry
                typeProvider.setChannelGroupType(discovered.groupTypeUID(), discovered.type());
                discovered.addChannelTypes(typeProvider);
                // Add component to the component map
                haComponents.put(discovered.uid().getId(), discovered);

                updateThingType();

                // Start component / Subscribe to channel topics
                discovered.start(connection, scheduler, 0).exceptionally(e -> {
                    logger.warn("Failed to start component {}", discovered.uid(), e);
                    return null;
                });
            }

            // Add channels to Thing
            for (HomeAssistentGroup e : haComponents.values()) {
                for (CChannel entry : e.channelTypes().values()) {
                    channels.add(entry.channel);
                }
            }
        }

        updateThing(editThing().withChannels(channels).build());
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateThingType() {
        // if this is a dynamic type, then we update the type
        ThingTypeUID typeID = thing.getThingTypeUID();
        if (!MqttBindingConstants.HOMEASSISTANT_MQTT_THING.equals(typeID)) {

            List<ChannelGroupDefinition> groupDefs = haComponents.values().stream().map(c -> c.getGroupDefinition())
                    .collect(Collectors.toList());
            List<ChannelDefinition> channelDefs = haComponents.values().stream().map(c -> c.type())
                    .map(t -> t.getChannelDefinitions()).flatMap(List::stream).collect(Collectors.toList());

            ThingType thingType = typeProvider.derive(typeID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING)
                    .withChannelDefinitions(channelDefs).withChannelGroupDefinitions(groupDefs).build();

            typeProvider.setThingType(typeID, thingType);
        }
    }
}
