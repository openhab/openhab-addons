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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.api.CacheChangeListener;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.binding.ddwrt.internal.api.DDWRTThingUpdater;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base handler for all DD-WRT thing types. Provides bridge lookup,
 * config deserialization, refresh dispatch, and command delegation.
 *
 * Modeled after UniFi's {@code UniFiBaseThingHandler<E, C>}.
 *
 * @param <E> entity - the API data object class used by this handler
 * @param <C> config - the configuration class used by this handler
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public abstract class DDWRTBaseHandler<E, C> extends BaseThingHandler
        implements DDWRTThingUpdater, CacheChangeListener {

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTBaseHandler.class));

    public DDWRTBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void initialize() {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null
                || !(bridge.getHandler() instanceof DDWRTNetworkBridgeHandler)) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is missing or wrong type");
            return;
        }

        // Derive the config class from the generic type parameter
        final Class<?> clazz = (Class<?>) ((ParameterizedType) Objects
                .requireNonNull(getClass().getGenericSuperclass())).getActualTypeArguments()[1]; // Expected unchecked
                                                                                                 // cast
        final C config = (C) getConfigAs(clazz);

        if (initialize(config)) {
            if (bridge.getStatus() == OFFLINE) {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is offline");
            } else {
                updateStatus(ONLINE);
            }
            // Register as cache listener for this entity's keys
            registerCacheListeners();
        }
    }

    @Override
    public void dispose() {
        unregisterCacheListeners();
        super.dispose();
    }

    // ---- CacheChangeListener ----

    @Override
    public void onCacheChanged() {
        refresh();
    }

    private void registerCacheListeners() {
        final DDWRTNetworkCache cache = getCache();
        if (cache != null) {
            for (String key : getCacheKeys()) {
                cache.addChangeListener(key, this);
                logger.debug("Registered cache listener for key '{}'", key);
            }
        }
    }

    private void unregisterCacheListeners() {
        final DDWRTNetworkCache cache = getCache();
        if (cache != null) {
            for (String key : getCacheKeys()) {
                cache.removeChangeListener(key, this);
            }
        }
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);

        if (getThing().getStatus() != ONLINE) {
            logger.debug("Ignoring command {} for channel {} — thing not online", command, channelUID);
            return;
        }

        final @Nullable E entity = getEntity();

        if (command == REFRESH) {
            updateState(entity, channelUID);
        } else {
            final DDWRTNetwork network = getNetwork();
            if (entity != null && network != null) {
                try {
                    if (!handleCommand(network, entity, channelUID, command)) {
                        logger.info("Ignoring unsupported command = {} for channel = {}", command, channelUID);
                    }
                } catch (Exception e) {
                    logger.info("Error handling command = {} for channel = {}: {}", command, channelUID,
                            e.getMessage());
                }
            } else {
                logger.debug("No entity/network available for command {} on channel {}", command, channelUID);
            }
        }
    }

    // ---- DDWRTThingUpdater implementation ----

    @Override
    public void updateChannel(String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), channelId), state);
    }

    @Override
    public void reportOffline(@Nullable String detail) {
        updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
    }

    @Override
    public void reportOnline() {
        if (getThing().getStatus() != ONLINE) {
            updateStatus(ONLINE);
        }
    }

    @Override
    public void fireTrigger(String channelId, String event) {
        triggerChannel(channelId, event);
    }

    // ---- Refresh ----

    /**
     * Refresh all channels from the entity. Called by the bridge during its refresh cycle.
     */
    public final void refresh() {
        if (getThing().getStatus() == ONLINE) {
            final @Nullable E entity = getEntity();
            getThing().getChannels().forEach(channel -> updateState(entity, channel.getUID()));
            if (entity != null) {
                updateProperties(entity);
            }
        }
    }

    private void updateState(@Nullable E entity, ChannelUID channelUID) {
        final String channelId = channelUID.getId();
        State state;
        if (entity != null) {
            state = getChannelState(entity, channelId);
        } else {
            state = getDefaultState(channelId);
        }
        if (state != UnDefType.NULL) {
            updateState(channelUID, state);
        }
    }

    // ---- Bridge / Network access ----

    public @Nullable DDWRTNetwork getNetwork() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof DDWRTNetworkBridgeHandler bh) {
            return bh.getNetwork();
        }
        return null;
    }

    public @Nullable DDWRTNetworkCache getCache() {
        final DDWRTNetwork net = getNetwork();
        return net != null ? net.getCache() : null;
    }

    /**
     * Look up the API entity for this thing from the network cache.
     */
    public @Nullable E getEntity() {
        final DDWRTNetworkCache cache = getCache();
        return cache != null ? getEntity(cache) : null;
    }

    // ---- Abstract methods for subclasses ----

    /**
     * Subclass-specific initialization. Should validate config and return true if successful.
     * If unsuccessful, should call updateStatus and return false.
     */
    protected abstract boolean initialize(C config);

    /**
     * Look up the entity from the cache.
     */
    protected abstract @Nullable E getEntity(DDWRTNetworkCache cache);

    /**
     * Return the channel state for the given entity and channel ID.
     * Return {@link UnDefType#NULL} to skip updating the channel.
     */
    protected abstract State getChannelState(E entity, String channelId);

    /**
     * Handle a non-REFRESH command. Return true if handled.
     */
    protected abstract boolean handleCommand(DDWRTNetwork network, E entity, ChannelUID channelUID, Command command);

    /**
     * Return the default state when no entity is available.
     */
    protected State getDefaultState(String channelId) {
        return UnDefType.UNDEF;
    }

    /**
     * Return the cache keys this handler should listen to for changes.
     * Subclasses override to provide entity-specific keys (hostname, MAC, interfaceId).
     * Default returns empty list (no cache listening).
     */
    protected List<String> getCacheKeys() {
        return Collections.emptyList();
    }

    /**
     * Update thing properties from the entity. Default is no-op.
     */
    protected void updateProperties(@Nullable E entity) {
        // no-op by default
    }
}
