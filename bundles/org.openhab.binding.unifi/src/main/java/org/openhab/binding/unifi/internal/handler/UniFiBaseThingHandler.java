/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Bowman - Initial contribution
 *
 * @param <E> entity - the UniFi entity class used by this thing handler
 * @param <C> config - the UniFi config class used by this thing handler
 */
@NonNullByDefault
public abstract class UniFiBaseThingHandler<E, C> extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(UniFiBaseThingHandler.class);

    public UniFiBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null
                || !(bridge.getHandler() instanceof UniFiControllerThingHandler)) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "You must choose a UniFi Controller for this thing.");
            return;
        }
        if (bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "The UniFi Controller is currently offline.");
            return;
        }
        // mgb: derive the config class from the generic type
        Class<?> clazz = (Class<?>) (((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1]);
        C config = (C) getConfigAs(clazz);
        initialize(config);
    }

    /**
     * Utility method to access the {@link UniFiController} instance associated with this thing.
     *
     * @return
     */
    @SuppressWarnings("null")
    private final @Nullable UniFiController getController() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null
                && (bridge.getHandler() instanceof UniFiControllerThingHandler)) {
            return ((UniFiControllerThingHandler) bridge.getHandler()).getController();
        }
        return null;
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        // mgb: only handle commands if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            UniFiController controller = getController();
            if (controller != null) {
                E entity = getEntity(controller);
                if (entity != null) {
                    if (command == REFRESH) {
                        refreshChannel(entity, channelUID);
                    } else {
                        try {
                            handleCommand(entity, channelUID, command);
                        } catch (UniFiException e) {
                            logger.warn("Unexpected error handling command = {} for channel = {} : {}", command,
                                    channelUID, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    protected final void refresh() {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            UniFiController controller = getController();
            if (controller != null) {
                E entity = getEntity(controller);
                if (entity != null) {
                    for (Channel channel : getThing().getChannels()) {
                        ChannelUID channelUID = channel.getUID();
                        refreshChannel(entity, channelUID);
                    }
                }
            }
        }
    }

    protected abstract void initialize(@NonNull C config);

    protected abstract @Nullable E getEntity(UniFiController controller);

    protected abstract void refreshChannel(E entity, ChannelUID channelUID);

    protected abstract void handleCommand(E entity, ChannelUID channelUID, Command command) throws UniFiException;
}
