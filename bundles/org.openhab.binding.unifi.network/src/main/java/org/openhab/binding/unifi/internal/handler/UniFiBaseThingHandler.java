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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.internal.api.UniFiCommunicationException;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidCredentialsException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
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
 * @author Matthew Bowman - Initial contribution
 * @author Dan Cunningham - Refactored onto shared parent bridge session + refresh coordinator
 *
 * @param <E> entity - the UniFi entity class used by this thing handler
 * @param <C> config - the UniFi config class used by this thing handler
 */
@NonNullByDefault
public abstract class UniFiBaseThingHandler<E, C> extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(UniFiBaseThingHandler.class);

    private @Nullable NetworkRefreshCoordinator coordinator;

    public UniFiBaseThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void initialize() {
        final Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof UniFiControllerBridgeHandler bridgeHandler)) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.offline.configuration_error");
            return;
        }
        // mgb: derive the config class from the generic type
        @SuppressWarnings("null")
        final Class<?> clazz = (Class<?>) (((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1]);
        final C config = (C) getConfigAs(clazz);
        if (initialize(config)) {
            coordinator = NetworkRefreshCoordinator.attach(bridgeHandler, this);
            if (bridge.getStatus() == OFFLINE) {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.thing.offline.bridge_offline");
                return;
            } else {
                updateStatus(UNKNOWN);
            }
        }
    }

    @Override
    public void dispose() {
        NetworkRefreshCoordinator c = coordinator;
        if (c != null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                NetworkRefreshCoordinator.detach(bridge.getUID(), this);
            }
            coordinator = null;
        }
        super.dispose();
    }

    /**
     * Utility method to access the {@link UniFiController} instance associated with this thing's bridge. Returns
     * {@code null} if no coordinator is attached or if the shared session has not yet been established.
     */
    public final @Nullable UniFiController getController() {
        NetworkRefreshCoordinator c = coordinator;
        return c != null ? c.getController() : null;
    }

    @Nullable
    public E getEntity() {
        final UniFiController controller = getController();
        return controller == null ? null : getEntity(controller.getCache());
    }

    @Override
    public final void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        // mgb: only handle commands if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            final @Nullable E entity = getEntity();
            final UniFiController controller = getController();

            if (command == REFRESH) {
                updateState(entity, channelUID);
            } else {
                if (entity != null && controller != null) {
                    try {
                        if (!handleCommand(controller, entity, channelUID, command)) {
                            logger.debug("Ignoring unsupported command = {} for channel = {}", command, channelUID);
                        }
                    } catch (final UniFiException e) {
                        logger.debug("Unexpected error handling command = {} for channel = {} : {}", command,
                                channelUID, e.getMessage());
                    }
                } else {
                    logger.debug(
                            "Could not handle command {} for channel = {} because no entity/controller data available.",
                            command, channelUID);
                }
            }
        } else {
            logger.debug("Could not handle command {} for channel = {} because thing not online.", command, channelUID);
        }
    }

    /**
     * Called by {@link NetworkRefreshCoordinator} each refresh tick. Reflects the coordinator's latest state into
     * this thing's status and channels.
     */
    public final void onCoordinatorRefresh() {
        NetworkRefreshCoordinator c = coordinator;
        if (c == null) {
            return;
        }
        Throwable err = c.getLastError();
        if (err != null) {
            if (err instanceof UniFiInvalidCredentialsException) {
                updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, err.getMessage());
            } else if (err instanceof UniFiCommunicationException) {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, err.getMessage());
            } else {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, err.getMessage());
            }
            return;
        }
        // Refresh succeeded — reflect any updated entity data into the channels.
        if (getThing().getStatus() != ONLINE) {
            updateStatus(ONLINE);
        }
        refresh();
    }

    protected final void refresh() {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            final @Nullable E entity = getEntity();

            getThing().getChannels().forEach(channel -> updateState(entity, channel.getUID()));

            updateProperties(entity);
        }
    }

    private void updateState(final @Nullable E entity, final ChannelUID channelUID) {
        final String channelId = channelUID.getId();
        final State state = Optional.ofNullable(entity).map(e -> getChannelState(e, channelId))
                .orElseGet(() -> getDefaultState(channelId));

        if (state != UnDefType.NULL) {
            updateState(channelUID, state);
        }
    }

    /**
     * Additional sub class specific initialization.
     * If initialization is unsuccessful it should set the thing status and return false.
     * if it was successful it should return true
     *
     * @param config thing configuration
     * @return true if initialization was successful
     */
    protected abstract boolean initialize(C config);

    /**
     * Returns the default state if no data available. Default implementation return {@link UnDefType#UNDEF}.
     *
     * @param channelId channel to update
     * @return default state
     */
    protected State getDefaultState(final String channelId) {
        return UnDefType.UNDEF;
    }

    /**
     * Returns the cached UniFi entity object related to this thing.
     *
     * @param cache cache to get the cached entity from
     * @return cached entry or null if not exists
     */
    protected abstract @Nullable E getEntity(UniFiControllerCache cache);

    /**
     * Returns the state to set for the given channel. If {@link UnDefType#NULL} is returned it means the channel should
     * not be updated.
     *
     * @param entity UniFi entity object to get the state information from
     * @param channelId Channel to update
     * @return state to set or {@link UnDefType#NULL} if channel state should not be updated.
     */
    protected abstract State getChannelState(E entity, String channelId);

    /**
     * Updates relevant Thing properties from the UniFi entity object.
     * Default implementation does not update any properties.
     *
     * @param entity UniFi entity object to get the properties information from
     */
    protected void updateProperties(E entity) {
    }

    /**
     * Send the given command to the UniFi controller.
     *
     * @param controller controller object to use to send the command to the UniFi controller
     * @param entity data object of the thing to send command to
     * @param channelUID channel the command is from
     * @param command command to send
     * @return true if command was send
     * @throws UniFiException
     */
    protected abstract boolean handleCommand(UniFiController controller, E entity, ChannelUID channelUID,
            Command command) throws UniFiException;
}
