/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeassistant.internal.handler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.homeassistant.internal.DiscoverComponents.ComponentIdentity;
import org.openhab.binding.homeassistant.internal.HaID;
import org.openhab.binding.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.homeassistant.internal.HomeAssistantBindingConstants;
import org.openhab.binding.homeassistant.internal.HomeAssistantChannelLinkageChecker;
import org.openhab.binding.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.homeassistant.internal.actions.HomeAssistantUpdateThingActions;
import org.openhab.binding.homeassistant.internal.component.AbstractComponent;
import org.openhab.binding.homeassistant.internal.component.ComponentFactory;
import org.openhab.binding.homeassistant.internal.component.Update;
import org.openhab.binding.homeassistant.internal.exception.ConfigurationException;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.tools.DelayedBatchProcessing;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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
 * @author Cody Cutrer - Resolve normalized component ID collisions
 * @author Cody Cutrer - Preserve discovery identities across lifecycle changes
 */
@NonNullByDefault
public class HomeAssistantThingHandler extends AbstractMQTTThingHandler
        implements ComponentDiscovered, Consumer<List<Object>>, HomeAssistantChannelLinkageChecker {
    private static final String COMPONENT_IDS_PROPERTY = "componentIds";
    private final Map<String, String> restoredComponentIds = new HashMap<>();
    public static final String AVAILABILITY_CHANNEL = "availability";
    private static final Comparator<AbstractComponent<?>> COMPONENT_COMPARATOR = Comparator
            .comparing((AbstractComponent<?> component) -> component.hasGroup())
            .thenComparing(AbstractComponent::getName);

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);

    protected final BaseThingHandlerFactory thingHandlerFactory;
    protected final MqttChannelTypeProvider channelTypeProvider;
    protected final MqttChannelStateDescriptionProvider stateDescriptionProvider;
    protected final ChannelTypeRegistry channelTypeRegistry;
    protected final HomeAssistantPythonBridge python;
    protected final UnitProvider unitProvider;
    public final int attributeReceiveTimeout;
    protected final DelayedBatchProcessing<Object> delayedProcessing;
    protected final DiscoverComponents discoverComponents;

    private final Gson gson;
    protected final Map<@Nullable String, AbstractComponent<?>> haComponents = new HashMap<>();
    protected final Map<@Nullable String, AbstractComponent<?>> haComponentsByUniqueId = new HashMap<>();
    protected final Map<ComponentIdentity, AbstractComponent<?>> haComponentsByDiscoveryId = new HashMap<>();
    protected final Map<ChannelUID, ChannelState> channelStates = new HashMap<>();

    protected HandlerConfiguration config = new HandlerConfiguration();
    private Set<HaID> discoveryHomeAssistantIDs = new HashSet<>();

    private boolean started;
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
    public HomeAssistantThingHandler(Thing thing, BaseThingHandlerFactory thingHandlerFactory,
            MqttChannelTypeProvider channelTypeProvider, MqttChannelStateDescriptionProvider stateDescriptionProvider,
            ChannelTypeRegistry channelTypeRegistry, Gson gson, HomeAssistantPythonBridge python,
            UnitProvider unitProvider, int subscribeTimeout, int attributeReceiveTimeout) {
        super(thing, subscribeTimeout);
        this.gson = gson;
        this.thingHandlerFactory = thingHandlerFactory;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.python = python;
        this.unitProvider = unitProvider;
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        this.delayedProcessing = new DelayedBatchProcessing<>(attributeReceiveTimeout, this, scheduler);
        this.discoverComponents = new DiscoverComponents(thing.getUID(), scheduler, this, this, this, gson, python,
                unitProvider);
    }

    @Override
    public void initialize() {
        started = false;
        restoredComponentIds.clear();
        restoredComponentIds.putAll(restoreComponentIds());

        config = getConfigAs(HandlerConfiguration.class);
        if (config.topics.isEmpty()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-component-topics");
            return;
        }
        if (!validateTopics()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.only-one-device");
            return;
        }
        discoveryHomeAssistantIDs.addAll(HaID.fromConfig(config));

        ThingTypeUID typeID = getThing().getThingTypeUID();
        restorePersistedDeviceConfigFromThingConfig();

        for (Channel channel : thing.getChannels()) {
            final String groupID = channel.getUID().getGroupId();
            if (groupID != null) {
                // Already restored component via another channel in the component?
                AbstractComponent<?> component = haComponents.get(groupID);
                if (component != null) {
                    continue;
                }
            }
            Configuration multiComponentChannelConfig = channel.getConfiguration();
            if (!multiComponentChannelConfig.containsKey("component")
                    || !multiComponentChannelConfig.containsKey("objectid")
                    || !multiComponentChannelConfig.containsKey("config")) {
                // Must be a secondary channel
                continue;
            }

            List<Configuration> flattenedConfig = flattenChannelConfiguration(multiComponentChannelConfig,
                    channel.getUID());
            for (Configuration channelConfig : flattenedConfig) {
                HaID haID = HaID.fromConfig(config.basetopic, channelConfig);

                if (!config.topics.contains(haID.getTopic())) {
                    // don't add a component for this channel that isn't configured on the thing
                    // anymore. It will disappear from the thing when the thing type is updated below
                    continue;
                }

                discoveryHomeAssistantIDs.add(haID);
                ThingUID thingUID = channel.getUID().getThingUID();
                String channelConfigurationJSON = (String) channelConfig.get("config");
                try {
                    List<AbstractComponent<?>> components = ComponentFactory.createComponent(thingUID, haID,
                            channelConfigurationJSON, this, this, this, scheduler, gson, python, unitProvider);
                    if (!components.isEmpty() && typeID.equals(HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING)
                            || typeID.getBindingId().equals(HomeAssistantBindingConstants.LEGACY_BINDING_ID)) {
                        typeID = calculateThingTypeUID(components.getFirst());
                    }
                    components.forEach(component -> {
                        addComponent(component);
                    });
                } catch (ConfigurationException e) {
                    logger.warn("Cannot restore component {}: {}", thing, e.getMessage());
                }
            }
        }
        if (updateThingType(typeID)) {
            super.initialize();
        }
    }

    @Override
    public void dispose() {
        discoveryHomeAssistantIDs.clear();
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
        logger.debug("Starting MQTT connection for {}", getThing().getUID());
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
                            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
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

            haComponents.clear();
            haComponentsByUniqueId.clear();
            haComponentsByDiscoveryId.clear();
            channelStates.clear();
            updateComponent = null;
            started = false;
        }
        super.stop();
    }

    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        synchronized (haComponents) { // sync whenever discoverComponents is started
            return channelStates.get(channelUID);
        }
    }

    /**
     * Callback of {@link DiscoverComponents}. Add to a delayed batch processor.
     */
    @Override
    public void componentDiscovered(HaID homeAssistantTopicID, AbstractComponent<?> component) {
        delayedProcessing.accept(component);
    }

    /**
     * Callback of {@link DiscoverComponents} for a device topic update.
     * Persists the full raw device config JSON on the Thing, so the handler can
     * restore device-originated components after restart and still reconcile
     * component removals/migrations when later discovery messages arrive.
     */
    @Override
    public void deviceConfigUpdated(HaID homeAssistantTopicID, String configPayload) {
        if (!config.deviceConfig.equals(configPayload)) {
            config.deviceConfig = configPayload;
            Configuration configuration = editConfiguration();
            configuration.put(HandlerConfiguration.PROPERTY_DEVICE_CONFIG, config.deviceConfig);
            updateConfiguration(configuration);
        }
    }

    @Override
    public void componentRemoved(ComponentIdentity identity) {
        delayedProcessing.accept(identity);
    }

    /**
     * Callback of {@link DelayedBatchProcessing}.
     * Add all newly discovered and removed components to the Thing and start the components.
     */
    @Override
    public void accept(List<Object> actions) {
        List<AbstractComponent<?>> discoveredComponents = new ArrayList<>();
        List<ComponentIdentity> removedComponents = new ArrayList<>();
        for (Object item : actions) {
            if (item instanceof AbstractComponent<?> component) {
                discoveredComponents.add(component);
            } else if (item instanceof ComponentIdentity removedComponent) {
                removedComponents.add(removedComponent);
            }
        }
        if (!discoveredComponents.isEmpty()) {
            addComponents(discoveredComponents);
        }
        if (!removedComponents.isEmpty()) {
            removeComponents(removedComponents);
        }
    }

    /**
     * Add all newly discovered components to the Thing and start the components.
     */
    private void addComponents(List<AbstractComponent<?>> discoveredComponentsList) {
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        synchronized (haComponents) { // sync whenever discoverComponents is started
            ThingTypeUID typeID = getThing().getThingTypeUID();
            for (AbstractComponent<?> discovered : discoveredComponentsList) {
                if (typeID.equals(HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING)
                        || typeID.getBindingId().equals(HomeAssistantBindingConstants.LEGACY_BINDING_ID)) {
                    typeID = calculateThingTypeUID(discovered);
                }
                AbstractComponent<?> known = haComponentsByUniqueId.get(discovered.getUniqueId());
                // Is component already known?
                if (known != null) {
                    if (discovered.getConfigHash() != known.getConfigHash()
                            && discovered.getUniqueId().equals(known.getUniqueId())) {
                        // Don't wait for the future to complete. We are also not interested in failures.
                        // The component will be replaced in a moment.
                        known.stop();
                        haComponentsByUniqueId.remove(discovered.getUniqueId());
                        haComponentsByDiscoveryId.remove(known.getDiscoveryIdentity());
                        haComponents.remove(known.getComponentId());
                        if (!known.getComponentId().equals(discovered.getComponentId())) {
                            discovered.setComponentId(known.getComponentId());
                        }
                    } else {
                        known.setConfigSeen();
                        continue;
                    }
                }

                // Add component to the component map
                if (addComponent(discovered)) {
                    // Start component / Subscribe to channel topics
                    discovered.start(connection, scheduler, 0).exceptionally(e -> {
                        logger.warn("Failed to start component {}", discovered.getHaID(), e);
                        return null;
                    });
                }
            }
            updateThingType(typeID);
        }
    }

    /**
     * Remove all matching deleted components.
     */
    private void removeComponents(List<ComponentIdentity> removedComponentsList) {
        synchronized (haComponents) {
            boolean componentActuallyRemoved = false;
            List<String> updatedTopics = new ArrayList<>(config.topics);
            for (ComponentIdentity removed : removedComponentsList) {
                if (removed.source().equals(removed.component())) {
                    updatedTopics.remove(removed.source().getTopic());
                }

                AbstractComponent<?> known = haComponentsByDiscoveryId.get(removed);
                if (known != null) {
                    // Don't wait for the future to complete. We are also not interested in failures.
                    known.stop();
                    haComponentsByUniqueId.remove(known.getUniqueId());
                    haComponents.remove(known.getComponentId());
                    haComponentsByDiscoveryId.remove(removed);
                    componentActuallyRemoved = true;

                    if (known.equals(updateComponent)) {
                        updateComponent = null;
                    }
                }
            }
            if (componentActuallyRemoved) {
                config.topics = List.copyOf(updatedTopics);
                Configuration configuration = editConfiguration();
                configuration.put(HandlerConfiguration.PROPERTY_TOPICS, config.topics);
                updateConfiguration(configuration);
                updateThingType(getThing().getThingTypeUID());
            }
        }
    }

    public void doUpdate() {
        Update updateComponent = this.updateComponent;
        if (updateComponent == null) {
            logger.warn(
                    "Received update command for Home Assistant device {}, but it does not have an update component.",
                    getThing().getUID());
        } else {
            updateComponent.doUpdate();
        }
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen) {
        if (availabilityTopicsSeen.orElse(messageReceived)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private boolean updateThingType(ThingTypeUID typeID) {
        if (typeID.getBindingId().equals(HomeAssistantBindingConstants.LEGACY_BINDING_ID)) {
            // Thing type needs to be migrated from legacy binding id, but we haven't yet calculated a new thing type
            // with the proper binding id, so just skip it for now. As soon as we have a proper thing type, we'll
            // perform the migration.
            return true;
        }

        // if this is a dynamic type, then we update the type
        if (!HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING.equals(typeID)
                && !HomeAssistantBindingConstants.LEGACY_MQTT_HOMEASSISTANT_THING.equals(typeID)) {
            var thingTypeBuilder = channelTypeProvider.derive(typeID,
                    HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING);

            if (getThing().getThingTypeUID().equals(HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING)
                    || getThing().getThingTypeUID().getBindingId()
                            .equals(HomeAssistantBindingConstants.LEGACY_BINDING_ID)) {
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
                channelTypeProvider.putThingType(thingTypeBuilder.build());

                removeStateDescriptions();
                sortedComponents.stream().forEach(c -> c.addStateDescriptions(stateDescriptionProvider));

                ThingBuilder thingBuilder = editThing().withChannels();
                sortedComponents.stream().map(AbstractComponent::getChannels).flatMap(List::stream)
                        .forEach(c -> thingBuilder.withChannel(c));

                channelStates.clear();
                sortedComponents.forEach(c -> c.getChannelStates(channelStates));

                Map<String, String> componentIds = new HashMap<>();
                sortedComponents
                        .forEach(component -> componentIds.put(component.getUniqueId(), component.getComponentId()));
                thingBuilder.withProperty(COMPONENT_IDS_PROPERTY, gson.toJson(componentIds));
                updateThing(thingBuilder.build());
            }
        }
        return true;
    }

    private ThingTypeUID calculateThingTypeUID(AbstractComponent<?> component) {
        return new ThingTypeUID(HomeAssistantBindingConstants.BINDING_ID,
                HomeAssistantBindingConstants.HOMEASSISTANT_DEVICE_THING.getId() + "_"
                        + component.getConfig().getThingId(component.getHaID().objectID));
    }

    private boolean validateTopics() {
        boolean hasDeviceTopic = false;
        for (String topic : config.topics) {
            String[] parts = topic.split("/");
            if (parts.length == 0 || parts[0].isBlank()) {
                continue;
            }
            if (HomeAssistantBindingConstants.DEVICE_COMPONENT.equals(parts[0])) {
                if (hasDeviceTopic) {
                    return false;
                }
                hasDeviceTopic = true;
            }
        }
        return true;
    }

    private Map<String, String> restoreComponentIds() {
        String storedIds = thing.getProperties().get(COMPONENT_IDS_PROPERTY);
        if (storedIds == null) {
            return Map.of();
        }
        try {
            JsonElement parsed = JsonParser.parseString(storedIds);
            if (parsed.isJsonObject()) {
                Map<String, String> ids = new HashMap<>();
                parsed.getAsJsonObject().entrySet().forEach(entry -> {
                    JsonElement value = entry.getValue();
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                            && value.getAsString().matches("[\\w-]+")) {
                        ids.put(entry.getKey(), value.getAsString());
                    }
                });
                return Map.copyOf(ids);
            }
        } catch (JsonParseException e) {
            logger.debug("Ignoring invalid persisted component IDs for {}", thing.getUID());
        }
        return Map.of();
    }

    private void restorePersistedDeviceConfigFromThingConfig() {
        String persistedPayload = config.deviceConfig;
        if (persistedPayload.isBlank()) {
            return;
        }

        List<HaID> deviceHaIDs = HaID.fromConfig(config).stream()
                .filter(id -> HomeAssistantBindingConstants.DEVICE_COMPONENT.equals(id.component)).toList();
        if (deviceHaIDs.size() != 1) {
            logger.debug("Ignoring persisted device config for thing {} because {} device topics are configured",
                    thing.getUID(), deviceHaIDs.size());
            return;
        }

        HaID haID = deviceHaIDs.getFirst();
        try {
            List<AbstractComponent<?>> components = ComponentFactory.createComponent(thing.getUID(), haID,
                    persistedPayload, this, this, this, scheduler, gson, python, unitProvider);
            if (!components.isEmpty()) {
                discoverComponents.restoreDeviceComponents(components);
                components.forEach(delayedProcessing::accept);
            }
        } catch (ConfigurationException e) {
            logger.debug("Ignoring invalid deviceConfig in thing {}: {}", thing.getUID(), e.getMessage());
        }
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

    // should only be called when it's safe to access haComponents
    private boolean addComponent(AbstractComponent<?> component) {
        String restoredId = restoredComponentIds.get(component.getUniqueId());
        if (restoredId != null && component.getComponentId().equals(component.getDefaultComponentId())) {
            component.setComponentId(restoredId);
        }

        List<AbstractComponent<?>> conflicts = haComponents.values().stream()
                .filter(candidate -> candidate.getComponentId().equals(component.getComponentId())
                        || candidate.getDefaultComponentId().equals(component.getDefaultComponentId()))
                .toList();
        AbstractComponent<?> mergeTarget = conflicts.stream().filter(component::mergeable).findFirst().orElse(null);
        if (mergeTarget != null) {
            MqttBrokerConnection connection = this.connection;
            if (mergeTarget.merge(component) && connection != null) {
                mergeTarget.start(connection, scheduler, 0).exceptionally(e -> {
                    logger.warn("Failed to start component {}", mergeTarget.getHaID(), e);
                    return null;
                });
            }
            haComponentsByUniqueId.put(component.getUniqueId(), component);
            haComponentsByDiscoveryId.put(component.getDiscoveryIdentity(), component);
            return false;
        }

        if (!conflicts.isEmpty()) {
            conflicts.forEach(existing -> haComponents.remove(existing.getComponentId()));
            List<AbstractComponent<?>> allConflicts = new ArrayList<>(conflicts);
            allConflicts.add(component);
            for (AbstractComponent<?> conflicting : allConflicts) {
                boolean samePlatform = allConflicts.stream()
                        .filter(other -> other.getHaID().component.equals(conflicting.getHaID().component)).limit(2)
                        .count() > 1;
                if (samePlatform) {
                    conflicting.resolveDiscoveryIdConflict();
                } else {
                    conflicting.resolveConflict();
                }
            }
            conflicts.forEach(this::reserveComponentId);
        }
        reserveComponentId(component);
        haComponentsByUniqueId.put(component.getUniqueId(), component);
        haComponentsByDiscoveryId.put(component.getDiscoveryIdentity(), component);

        if (component instanceof Update updateComponent) {
            this.updateComponent = updateComponent;
            updateComponent.setReleaseStateUpdateListener(this::releaseStateUpdated);
            if (updateComponent.isUpdatable()) {
                thingHandlerFactory.registerService(this, HomeAssistantUpdateThingActions.class);
            }
        }
        return true;
    }

    private void reserveComponentId(AbstractComponent<?> component) {
        var pending = new ArrayDeque<AbstractComponent<?>>();
        pending.add(component);
        while (!pending.isEmpty()) {
            AbstractComponent<?> candidate = pending.removeFirst();
            AbstractComponent<?> existing = haComponents.remove(candidate.getComponentId());
            if (existing == null || (existing.getHaID().equals(candidate.getHaID())
                    && existing.getUniqueId().equals(candidate.getUniqueId()))) {
                haComponents.put(candidate.getComponentId(), candidate);
            } else {
                // Topic plus unique ID distinguishes overlapping device and individual discovery configs.
                // An unchanged ID can only collide with a component that has not yet been disambiguated.
                existing.resolveUniqueIdConflict();
                candidate.resolveUniqueIdConflict();
                pending.add(existing);
                pending.add(candidate);
            }
        }
    }

    /**
     * Takes a Configuration where objectid and config are a list, and generates
     * multiple Configurations where there are single objects
     */
    private List<Configuration> flattenChannelConfiguration(Configuration multiComponentChannelConfig,
            ChannelUID channelUID) {
        Object component = multiComponentChannelConfig.get("component");
        Object nodeid = multiComponentChannelConfig.get("nodeid");
        if ((multiComponentChannelConfig.get("objectid") instanceof List<?> objectIds)
                && (multiComponentChannelConfig.get("config") instanceof List<?> configurations)) {
            if (objectIds.size() != configurations.size()) {
                logger.warn("objectid and config for channel {} do not have the same number of items; ignoring",
                        channelUID);
                return List.of();
            }
            List<Configuration> result = new ArrayList<>();
            Iterator<?> objectIdIterator = objectIds.iterator();
            Iterator<?> configIterator = configurations.iterator();
            while (objectIdIterator.hasNext()) {
                Configuration componentContext = new Configuration();
                componentContext.put("component", component);
                componentContext.put("nodeid", nodeid);
                componentContext.put("objectid", objectIdIterator.next());
                componentContext.put("config", configIterator.next());
                result.add(componentContext);
            }
            return result;
        } else {
            return List.of(multiComponentChannelConfig);
        }
    }

    // For testing
    Map<@Nullable String, AbstractComponent<?>> getComponents() {
        return haComponents;
    }

    // For components to check if a channel is linked before starting them
    @Override
    public boolean isChannelLinked(ChannelUID channelUID) {
        return isLinked(channelUID);
    }

    // A channel is newly linked; make sure it is started
    @Override
    public void channelLinked(ChannelUID channelUID) {
        MqttBrokerConnection connection = this.connection;
        // We haven't started at all yet.
        if (connection == null) {
            return;
        }
        synchronized (haComponents) {
            haComponents.forEach((id, component) -> {
                if (component.getChannels().stream().anyMatch(channel -> channel.getUID().equals(channelUID))) {
                    component.start(connection, scheduler, attributeReceiveTimeout).exceptionally(e -> {
                        updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                        return null;
                    });
                }
            });
        }
        super.channelLinked(channelUID);
    }

    // Don't bother unsubscribing on unlink; it's a relatively rare operation during normal usage,
    // and a decent amount of effort to make sure there aren't other links before stopping them, and
    // making sure not to stop other channels that are still linked.
    // A disable/re-enable of the thing will clear the subscriptions.
}
