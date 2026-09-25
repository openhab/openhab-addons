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
package org.openhab.binding.hasslink.internal.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.HassLinkDynamicCommandDescriptionProvider;
import org.openhab.binding.hasslink.internal.HassLinkDynamicStateDescriptionProvider;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.config.HassLinkDeviceConfiguration;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityId;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.registry.EntityRegistryEntry;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.binding.hasslink.internal.util.HassLinkEntityFilter;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles communication for Home Assistant devices or entity groupings,
 * auto-populating initial channels from device or standalone entity configurations,
 * updating states, and providing dynamic state options via {@link HassLinkDynamicStateDescriptionProvider}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkDeviceHandler extends BaseThingHandler {

    public static final String CONFIG_ENTITY_ID = "entityId";
    public static final String CONFIG_ATTRIBUTE = "attribute";
    public static final String CONFIG_AUTO_CREATED = "autoCreated";

    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(HassLinkDeviceHandler.class);

    private final HassLinkDynamicStateDescriptionProvider stateDescriptionProvider;
    private final HassLinkDynamicCommandDescriptionProvider commandDescriptionProvider;

    // Maps entityId -> Set of channel UIDs configured for it
    private final Map<String, Set<ChannelUID>> entityToChannelsMap = new ConcurrentHashMap<>();

    private final Object registeredBridgeHandlerLock = new Object();
    private @Nullable HassLinkBridgeHandler registeredBridgeHandler;
    private final Set<String> requiredEntityIds = new HashSet<>();

    private Set<String> lastLoggedMissingEntities = Set.of();

    private final AtomicBoolean isBuildingChannels = new AtomicBoolean(false);
    private volatile boolean channelsInitialized = false;
    private volatile boolean disposed = false;

    public HassLinkDeviceHandler(Thing thing, HassLinkDynamicStateDescriptionProvider stateDescriptionProvider,
            HassLinkDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void initialize() {
        this.disposed = false;
        HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Home Assistant bridge is not available");
            return;
        }
        buildChannels(true);
    }

    @Override
    public void dispose() {
        this.disposed = true;

        HassLinkBridgeHandler handlerToUnregister = null;
        try {
            synchronized (this.registeredBridgeHandlerLock) {
                handlerToUnregister = registeredBridgeHandler;
                registeredBridgeHandler = null;
                requiredEntityIds.clear();
                entityToChannelsMap.clear();
            }

            if (handlerToUnregister != null) {
                handlerToUnregister.unregisterDeviceHandler(this);
            }

            stateDescriptionProvider.removeThing(getThing().getUID());
            commandDescriptionProvider.removeThing(getThing().getUID());
        } finally {
            super.dispose();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (getCallback() == null) {
            return;
        }
        HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && bridgeHandler != null) {
            buildChannels(false);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Called by {@link HassLinkBridgeHandler} when the entity/device registry snapshots are updated.
     */
    public void onRegistryUpdated() {
        if (getCallback() == null) {
            return;
        }
        buildChannels(false);
    }

    public void onEntityStateUpdate(EntityState entityState) {
        if (getCallback() == null) {
            return;
        }
        // Fallback: If init was deferred due to missing initial state, attempt build once
        if (!channelsInitialized) {
            buildChannels(false);
        }

        if (!entityToChannelsMap.containsKey(entityState.entityId())) {
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        applyStateUpdates(entityState);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getCallback() == null) {
            return;
        }
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            return;
        }

        if (command instanceof RefreshType) {
            handleRefreshCommand(channel);
            return;
        }

        Object entityIdConfig = channel.getConfiguration().get(CONFIG_ENTITY_ID);
        if (entityIdConfig instanceof String entityId && !entityId.isBlank()) {
            String attribute = getAttribute(channel);
            executeEntityCommand(entityId, attribute, command);
        }
    }

    private void handleRefreshCommand(Channel channel) {
        Object entityIdConfig = channel.getConfiguration().get(CONFIG_ENTITY_ID);
        if (!(entityIdConfig instanceof String entityId) || entityId.isBlank()) {
            return;
        }

        HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        // Invalidate caches and push state updates back to openHAB channels
        bridgeHandler.refreshEntity(entityId);
    }

    /**
     * Builds channels for the Thing by discovering entities of the configured deviceId
     * and configure channels based on cached entity states.
     *
     * If forceRebuild is true, existing auto-created channels will be removed and rebuilt.
     *
     * @param forceRebuild whether to force a rebuild of channels
     */
    private void buildChannels(boolean forceRebuild) {
        if (disposed || getCallback() == null || !isBuildingChannels.compareAndSet(false, true)) {
            return;
        }

        try {
            HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                return;
            }

            HassLinkDeviceConfiguration config = getConfigAs(HassLinkDeviceConfiguration.class);
            boolean hasDeviceId = config.deviceId instanceof String deviceId && !deviceId.isBlank();

            if (hasDeviceId && !bridgeHandler.isRegistryLoaded()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Waiting for Home Assistant device registry snapshot");
                return;
            }

            // 1. Resolve raw candidate entity IDs configured via Thing configuration (pre-filter)
            Set<String> allCandidateEntityIds = getAllConfiguredEntityIds(bridgeHandler, config);

            // 2. Filter raw candidates in-memory using HassLinkEntityFilter (post-filter)
            Set<String> filteredEntityIds = filterEntities(allCandidateEntityIds, bridgeHandler, config);

            // 3. Combine allowed configured entity IDs with entity IDs from existing manually created channels
            Set<String> targetEntityIds = new HashSet<>(filteredEntityIds);
            for (Channel channel : getThing().getChannels()) {
                if (!isChannelAutoCreated(channel) //
                        && channel.getConfiguration().get(CONFIG_ENTITY_ID) instanceof String entityId //
                        && !entityId.isBlank()) {
                    targetEntityIds.add(entityId);
                }
            }

            ThingBuilder builder = editThing();
            boolean modified = false;
            Set<ChannelUID> activeUids = new HashSet<>();

            // 4. Reconcile existing channels (remove obsolete auto-created channels)
            for (Channel channel : getThing().getChannels()) {
                boolean isAuto = isChannelAutoCreated(channel);
                String channelEntityId = (String) channel.getConfiguration().get(CONFIG_ENTITY_ID);
                ChannelUID channelUID = channel.getUID();

                if (forceRebuild && isAuto) {
                    // Hard rebuild: strip existing auto-created channels
                    builder.withoutChannel(channelUID);
                    stateDescriptionProvider.removeChannel(channelUID);
                    commandDescriptionProvider.removeChannel(channelUID);
                    modified = true;
                } else if (isAuto && channelEntityId != null && !filteredEntityIds.contains(channelEntityId)) {
                    // Dynamic sync: auto-created channel entity is no longer configured/allowed -> drop channel
                    builder.withoutChannel(channelUID);
                    stateDescriptionProvider.removeChannel(channelUID);
                    commandDescriptionProvider.removeChannel(channelUID);
                    modified = true;
                } else {
                    activeUids.add(channelUID);
                }
            }

            // 5. Build candidate channels for active configured entities
            if (!filteredEntityIds.isEmpty()) {
                List<EntityState> allCandidateEntityStates = allCandidateEntityIds.stream() //
                        .map(bridgeHandler::getCachedEntityState) //
                        .filter(Objects::nonNull) //
                        .toList();

                // Defer if HA hasn't delivered state snapshots yet
                if (allCandidateEntityStates.isEmpty()) {
                    if (modified) {
                        updateThing(builder.build());
                    }

                    // Register required entity IDs with the bridge so it knows to track/fetch them
                    syncRegistrations(bridgeHandler, targetEntityIds, Set.of());

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Waiting for Home Assistant entity states");
                    return;
                }

                // Pass all candidate states to factory so prefix calculation runs on the full set
                // so the LCP (Longest Common Prefix) results are consistent and not affected by filtering.
                List<Channel> candidateChannels = HassLinkChannelFactory.buildChannels(allCandidateEntityStates,
                        getThing().getUID(), getThing().getLabel(), bridgeHandler);

                for (Channel candidate : candidateChannels) {
                    Object entityIdObj = candidate.getConfiguration().get(CONFIG_ENTITY_ID);
                    // Finally we refer back to filteredEntityIds to ensure only allowed entities are added to the Thing
                    if (entityIdObj instanceof String channelEntityId && filteredEntityIds.contains(channelEntityId)) {
                        if (!activeUids.contains(candidate.getUID())) {
                            builder.withChannel(candidate);
                            activeUids.add(candidate.getUID());
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                updateThing(builder.build());
            }

            // 6. Map active channels to entity IDs
            Map<String, Set<ChannelUID>> newEntityToChannelsMap = buildEntityMappings();

            if (disposed) {
                return;
            }

            // 7. Synchronize entity handler registrations and notify bridge of mapping changes
            syncRegistrations(bridgeHandler, targetEntityIds, newEntityToChannelsMap.keySet());

            this.entityToChannelsMap.clear();
            this.entityToChannelsMap.putAll(newEntityToChannelsMap);

            if (entityToChannelsMap.isEmpty()) {
                if (filteredEntityIds.isEmpty()) {
                    if (hasDeviceId && !bridgeHandler.isRegistryLoaded()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Waiting for Home Assistant device registry snapshot");
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Configured Home Assistant device or entity ID does not exist");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No matching channels could be created for configured entities");
                }
            } else {
                this.channelsInitialized = true;

                // Identify missing/unresolved entity IDs from the filtered set
                Set<String> resolvedEntities = entityToChannelsMap.keySet();
                Set<String> missingEntities = filteredEntityIds.stream().filter(id -> !resolvedEntities.contains(id))
                        .collect(Collectors.toSet());

                if (!missingEntities.isEmpty()) {
                    if (!missingEntities.equals(lastLoggedMissingEntities)) {
                        logger.warn("Thing {} initialized with missing entity states: {}", getThing().getUID(),
                                missingEntities);
                        lastLoggedMissingEntities = missingEntities;
                    }
                } else {
                    lastLoggedMissingEntities = Set.of();
                }

                updateStatus(ThingStatus.ONLINE);
                syncEntityStates(bridgeHandler);
            }
        } finally {
            isBuildingChannels.set(false);
        }
    }

    /**
     * Resolves all raw candidate entity IDs from the configuration before filtering (pre-filter).
     */
    private Set<String> getAllConfiguredEntityIds(HassLinkBridgeHandler bridgeHandler,
            HassLinkDeviceConfiguration config) {
        Set<String> rawEntityIds = new HashSet<>();

        if (config.deviceId instanceof String deviceId && !deviceId.isBlank()) {
            rawEntityIds.addAll(bridgeHandler.getEntitiesForDevice(deviceId.trim()));
        }

        for (String entityId : config.entityIds) {
            if (!entityId.isBlank()) {
                rawEntityIds.add(entityId.trim());
            }
        }

        return rawEntityIds;
    }

    /**
     * Filters candidate entity IDs against device inclusion/exclusion rules (post-filter).
     */
    private Set<String> filterEntities(Set<String> candidateEntityIds, HassLinkBridgeHandler bridgeHandler,
            HassLinkDeviceConfiguration config) {
        Set<String> filteredEntityIds = new HashSet<>();
        for (String entityId : candidateEntityIds) {
            EntityRegistryEntry entityEntry = bridgeHandler.getRegistry().getEntity(entityId).orElse(null);

            // Skip entities that are explicitly disabled in Home Assistant
            if (entityEntry != null && entityEntry.isDisabled()) {
                logger.debug("Entity {} is disabled in Home Assistant, skipping for Thing {}", entityId,
                        getThing().getUID());
                continue;
            }

            if (HassLinkEntityFilter.isEntityAllowedForDevice(entityId, entityEntry, config)) {
                filteredEntityIds.add(entityId);
            } else {
                logger.debug("Entity {} filtered out for Thing {}", entityId, getThing().getUID());
            }
        }
        return filteredEntityIds;
    }

    private boolean isChannelAutoCreated(Channel channel) {
        Object autoCreated = channel.getConfiguration().get(CONFIG_AUTO_CREATED);
        return Boolean.TRUE.equals(autoCreated) || "true".equalsIgnoreCase(String.valueOf(autoCreated));
    }

    private void syncRegistrations(HassLinkBridgeHandler bridgeHandler, Set<String> targetEntityIds,
            Set<String> mappedEntityIds) {

        Set<String> requiredEntities = new HashSet<>(targetEntityIds);
        requiredEntities.addAll(mappedEntityIds);

        boolean bridgeChanged;
        boolean entitiesChanged;
        HassLinkBridgeHandler oldBridgeHandler = null;

        synchronized (this.registeredBridgeHandlerLock) {
            if (disposed) {
                return;
            }

            bridgeChanged = !Objects.equals(registeredBridgeHandler, bridgeHandler);
            entitiesChanged = !this.requiredEntityIds.equals(requiredEntities);

            if (bridgeChanged) {
                oldBridgeHandler = registeredBridgeHandler;
                registeredBridgeHandler = bridgeHandler;
            }

            if (entitiesChanged) {
                this.requiredEntityIds.clear();
                this.requiredEntityIds.addAll(requiredEntities);
            }
        }

        // Call bridge methods outside synchronized block to prevent lock inversion deadlock
        if (bridgeChanged) {
            if (oldBridgeHandler != null) {
                oldBridgeHandler.unregisterDeviceHandler(this);
            }
            bridgeHandler.registerDeviceHandler(this);
        } else if (entitiesChanged) {
            bridgeHandler.notifyEntityMappingsChanged();
        }
    }

    /**
     * Returns the set of Home Assistant entity IDs required by this handler.
     * Called by {@link HassLinkBridgeHandler} during subscription reconciliation.
     *
     * @return set of required entity IDs
     */
    public Set<String> getRequiredEntityIds() {
        synchronized (registeredBridgeHandlerLock) {
            return Set.copyOf(requiredEntityIds);
        }
    }

    private Map<String, Set<ChannelUID>> buildEntityMappings() {
        Map<String, Set<ChannelUID>> newEntityToChannelsMap = new HashMap<>();
        for (Channel channel : getThing().getChannels()) {
            Object entityIdConfig = channel.getConfiguration().get(CONFIG_ENTITY_ID);
            if (entityIdConfig instanceof String entityId && !entityId.isBlank()) {
                if (EntityUtils.isValidEntityId(entityId)) {
                    Objects.requireNonNull(newEntityToChannelsMap.computeIfAbsent(entityId, k -> new HashSet<>()))
                            .add(channel.getUID());
                }
            }
        }
        return newEntityToChannelsMap;
    }

    private void syncEntityStates(HassLinkBridgeHandler bridgeHandler) {
        for (String entityId : entityToChannelsMap.keySet()) {
            EntityState state = bridgeHandler.getCachedEntityState(entityId);
            if (state != null) {
                applyStateUpdates(state);
            }
        }
    }

    private void applyStateUpdates(EntityState entityState) {
        List<Channel> channels = entityToChannelsMap.getOrDefault(entityState.entityId(), Set.of()).stream() //
                .map(channelUID -> getThing().getChannel(channelUID)) //
                .filter(Objects::nonNull) //
                .toList();

        if (channels.isEmpty()) {
            return;
        }

        HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
        EntityType entityType = bridgeHandler != null ? bridgeHandler.getEntityType(entityState) : null;
        if (entityType == null) {
            return;
        }

        EntityContext context = createEntityContext(bridgeHandler, channels);

        // Process state updates
        Map<String, ParsedData> parsedStates = entityType.parseState(entityState, context);

        // All entities get a special "json_attributes" channel if any attribute is linked,
        // which contains a JSON string of all attributes
        if (!parsedStates.containsKey("json_attributes") && context.isLinked("json_attributes")) {
            String json = GSON.toJson(entityState.attributes());
            Map<String, ParsedData> newParsedStates = new HashMap<>(parsedStates);
            newParsedStates.put("json_attributes", new ParsedData.StateData(StringType.valueOf(json)));
            parsedStates = newParsedStates;
        }

        // Dispatch synchronous states
        for (Channel channel : channels) {
            String attribute = getAttribute(channel);
            ChannelUID channelUID = channel.getUID();

            ParsedData data = parsedStates.get(attribute);
            if (data != null && isLinked(channelUID)) {
                dispatchChannelData(channel, data);
            }

            StateDescriptionFragment fragment = entityType.getStateDescriptionFragment(entityState, attribute, context);
            if (fragment != null) {
                applyStateDescriptionFragment(channelUID, fragment);
            }

            List<CommandOption> commandOptions = entityType.getCommandOptions(entityState, attribute, context);
            if (commandOptions != null) {
                commandDescriptionProvider.setCommandOptions(channelUID, commandOptions);
            }
        }
    }

    private void dispatchToAttributeChannels(List<Channel> channels, String attribute, ParsedData data) {
        for (Channel channel : channels) {
            if (attribute.equals(getAttribute(channel)) && isLinked(channel.getUID())) {
                dispatchChannelData(channel, data);
            }
        }
    }

    /**
     * Dispatches state updates, commands, channel trigger events, or time series data
     * to openHAB based on data type and dynamic channel configuration.
     */
    private void dispatchChannelData(Channel channel, ParsedData data) {
        switch (data) {
            case ParsedData.StateData(State newState) -> {
                if (channel.getKind() == ChannelKind.TRIGGER) {
                    triggerChannel(channel.getUID(), newState.toFullString());
                } else {
                    updateState(channel.getUID(), newState);
                }
            }
            case ParsedData.TimeSeriesData(TimeSeries timeSeries) -> sendTimeSeries(channel.getUID(), timeSeries);
        }
    }

    private void applyStateDescriptionFragment(ChannelUID channelUID, StateDescriptionFragment fragment) {
        BigDecimal min = fragment.getMinimum();
        BigDecimal max = fragment.getMaximum();
        BigDecimal step = fragment.getStep();
        if (min != null || max != null || step != null) {
            stateDescriptionProvider.setRange(channelUID, min, max, step);
        }

        List<StateOption> options = fragment.getOptions();
        if (options != null) {
            stateDescriptionProvider.setStateOptions(channelUID, options);
        }

        String pattern = fragment.getPattern();
        if (pattern != null) {
            stateDescriptionProvider.setStatePattern(channelUID, pattern);
        }
    }

    private void executeEntityCommand(String entityId, String property, Command command) {
        HassLinkBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        EntityId parsedEntityId = EntityUtils.parseEntityId(entityId);
        if (parsedEntityId == null) {
            return;
        }

        // Don't derive entityType from entityState, because we may not have a cached state yet
        // (e.g., for a newly created entity).
        // Instead, get the entityType directly from the registry.
        EntityType entityType = bridgeHandler.getEntityTypeByDomain(parsedEntityId.domain());
        EntityState entityState = bridgeHandler.getCachedEntityState(entityId);

        EntityContext context = createEntityContext(bridgeHandler);

        entityType.toServiceCall(entityId, property, command, entityState, context)
                .ifPresent(bridgeHandler::callService);
    }

    /**
     * Constructs an {@link EntityContext} tailored for state updates on specific channels.
     */
    private EntityContext createEntityContext(@Nullable HassLinkBridgeHandler bridgeHandler, List<Channel> channels) {
        Predicate<String> isAttributeLinked = attr -> channels.stream().filter(c -> attr.equals(getAttribute(c)))
                .anyMatch(c -> isLinked(c.getUID()));
        return new EntityContext(bridgeHandler, isAttributeLinked,
                (attribute, data) -> dispatchToAttributeChannels(channels, attribute, data));
    }

    /**
     * Constructs a lightweight {@link EntityContext} for simple execution scenarios (e.g., commanding).
     */
    private EntityContext createEntityContext(@Nullable HassLinkBridgeHandler bridgeHandler) {
        return new EntityContext(bridgeHandler, attr -> true, null);
    }

    private String getAttribute(Channel channel) {
        Object propertyConfig = channel.getConfiguration().get(CONFIG_ATTRIBUTE);
        return propertyConfig == null ? EntityType.PRIMARY_ATTR : String.valueOf(propertyConfig);
    }

    private @Nullable HassLinkBridgeHandler getBridgeHandler() {
        if (getCallback() == null) {
            return null; // Prevents calling super.getBridge() when uninitialized/disposed, avoiding logger.warn
        }
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof HassLinkBridgeHandler handler ? handler : null;
    }
}
