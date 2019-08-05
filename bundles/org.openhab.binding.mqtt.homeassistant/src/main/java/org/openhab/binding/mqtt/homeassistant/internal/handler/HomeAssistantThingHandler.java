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
package org.openhab.binding.mqtt.homeassistant.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.tools.DelayedBatchProcessing;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractComponent;
import org.openhab.binding.mqtt.homeassistant.internal.CChannel;
import org.openhab.binding.mqtt.homeassistant.internal.CFactory;
import org.openhab.binding.mqtt.homeassistant.internal.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
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
 * A Component Instance equals an ESH Channel Group and the Component parts equal ESH Channels.<br>
 * <br>
 *
 * If a Components configuration changes, the known ChannelGroupType and ChannelTypes are replaced with the new ones.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantThingHandler extends AbstractMQTTThingHandler
        implements ComponentDiscovered, Consumer<List<AbstractComponent<?>>> {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);

    protected final MqttChannelTypeProvider channelTypeProvider;
    public final int attributeReceiveTimeout;
    protected final DelayedBatchProcessing<AbstractComponent<?>> delayedProcessing;
    protected final DiscoverComponents discoverComponents;

    private final Gson gson;
    protected final Map<String, AbstractComponent<?>> haComponents = new HashMap<>();

    protected HandlerConfiguration config = new HandlerConfiguration();
    private HaID discoveryHomeAssistantID = new HaID();

    protected final TransformationServiceProvider transformationServiceProvider;

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
        this.discoverComponents = new DiscoverComponents(thing.getUID(), scheduler, this, gson,
                this.transformationServiceProvider);
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void initialize() {
        config = getConfigAs(HandlerConfiguration.class);
        if (StringUtils.isEmpty(config.objectid)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID unknown");
            return;
        }
        discoveryHomeAssistantID = HaID.fromConfig(config);

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
                continue;
            }

            HaID haID = HaID.fromConfig(config.basetopic, channel.getConfiguration());
            ThingUID thingUID = channel.getUID().getThingUID();
            String channelConfigurationJSON = (String) channel.getConfiguration().get("config");
            if (channelConfigurationJSON == null) {
                logger.warn("Provided channel does not have a 'config' configuration key!");
            } else {
                component = CFactory.createComponent(thingUID, haID, channelConfigurationJSON, this, gson,
                        transformationServiceProvider);
            }

            if (component != null) {
                haComponents.put(component.uid().getId(), component);
                component.addChannelTypes(channelTypeProvider);
            } else {
                logger.warn("Could not restore component {}", thing);
            }
        }

        super.initialize();
    }

    @Override
    public void dispose() {
        discoverComponents.stopDiscovery();
        delayedProcessing.join();

        haComponents.values().forEach(c -> c.removeChannelTypes(channelTypeProvider));

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

        return future
                .thenCompose(b -> discoverComponents.startDiscovery(connection, 0, discoveryHomeAssistantID, this));
    }

    @Override
    protected void stop() {
        discoverComponents.stopDiscovery();
        delayedProcessing.join();
        // haComponents does not need to be synchronised -> the discovery thread is disabled
        haComponents.values().stream().map(e -> e.stop());
    }

    @SuppressWarnings({ "null", "unused" })
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
        CChannel componentChannel = component.channel(channelUID.getIdWithoutGroup());
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
    @SuppressWarnings("null")
    @Override
    public void accept(List<AbstractComponent<?>> discoveredComponentsList) {
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        List<Channel> channels = new ArrayList<>();
        synchronized (haComponents) { // sync whenever discoverComponents is started
            for (AbstractComponent<?> discovered : discoveredComponentsList) {
                AbstractComponent<?> known = haComponents.get(discovered.uid().getId());
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

                // Add channel and group types to the types registry
                channelTypeProvider.setChannelGroupType(discovered.groupTypeUID(), discovered.type());
                discovered.addChannelTypes(channelTypeProvider);
                // Add component to the component map
                haComponents.put(discovered.uid().getId(), discovered);
                // Start component / Subscribe to channel topics
                discovered.start(connection, scheduler, 0).exceptionally(e -> {
                    logger.warn("Failed to start component {}", discovered.uid(), e);
                    return null;
                });
            }

            // Add channels to Thing
            for (AbstractComponent<?> e : haComponents.values()) {
                for (CChannel entry : e.channelTypes().values()) {
                    channels.add(entry.getChannel());
                }
            }
        }

        updateThing(editThing().withChannels(channels).build());
        updateStatus(ThingStatus.ONLINE);
    }
}
