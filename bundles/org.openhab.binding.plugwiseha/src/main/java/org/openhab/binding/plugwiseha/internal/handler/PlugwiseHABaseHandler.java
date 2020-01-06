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

package org.openhab.binding.plugwiseha.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;

import java.lang.reflect.ParameterizedType;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHAThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHABaseHandler} abstract class provides common methods and
 * properties for the ThingHandlers of this binding. Extends @{link
 * BaseThingHandler}
 *
 * @author Bas van Wetten - Initial contribution
 *
 * @param <E> entity - the Plugwise Home Automation entity class used by this
 *            thing handler
 * @param <C> config - the Plugwise Home Automation config class used by this
 *            thing handler
 */
public abstract class PlugwiseHABaseHandler<E, C extends PlugwiseHAThingConfig> extends BaseThingHandler {

    // private @Nullable C config;

    protected final Logger logger = LoggerFactory.getLogger(PlugwiseHABaseHandler.class);

    // Abstract methods

    protected abstract void initialize(@NonNull C config);

    protected abstract @Nullable E getEntity(PlugwiseHAController controller);

    protected abstract void refreshChannel(E entity, ChannelUID channelUID);

    protected abstract void handleCommand(E entity, ChannelUID channelUID, Command command) throws PlugwiseHAException;

    // Constructor

    public PlugwiseHABaseHandler(Thing thing) {
        super(thing);
    }

    // Overrides

    @Override
    public void initialize() {        
        C config = getPlugwiseThingConfig();

        if (checkConfig(config)) {
            // logger.debug("Initializing Plugwise Home Automation thing handler with config = {}", config);

            Bridge bridge = getBridge();
            if (bridge == null || bridge.getHandler() == null
                    || !(bridge.getHandler() instanceof PlugwiseHABridgeHandler)) {
                updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "You must choose a Plugwise Home Automation bridge for this thing.");
                return;
            }
    
            if (bridge.getStatus() == OFFLINE) {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "The Plugwise Home Automation bridge is currently offline.");
            }
    
            initialize(config);
        } else {
            logger.warn("Invalid config for Plugwise Home Automation thing handler with config = {}", config);
        }
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);

        if (getThing().getStatus() == ONLINE) {
            PlugwiseHAController controller = getController();
            if (controller != null) {
                E entity = getEntity(controller);
                if (entity != null) {
                    if (this.isLinked(channelUID)) {
                        if (command instanceof RefreshType) {
                            refreshChannel(entity, channelUID);
                        } else {
                            try {
                                handleCommand(entity, channelUID, command);
                            } catch (PlugwiseHAException e) {
                                logger.warn("Unexpected error handling command = {} for channel = {} : {}", command,
                                        channelUID, e.getMessage());
                            }

                        }
                    }
                }
            }
        }
    }

    // Public member methods

    public @Nullable PlugwiseHABridgeHandler getPlugwiseHABridge() {
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            return (PlugwiseHABridgeHandler) bridge.getHandler();
        }
 
        return null;
    }

    @SuppressWarnings("unchecked")
    public C getPlugwiseThingConfig() {
        Class<?> clazz = (Class<?>) (((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1]);

        return (C) getConfigAs(clazz);
    }

    // Private & protected methods

    private final @Nullable PlugwiseHAController getController() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null && (bridge.getHandler() instanceof PlugwiseHABridgeHandler)) {
            return ((PlugwiseHABridgeHandler) bridge.getHandler()).getController();
        }
        return null;
    }

    /**
     * Checks the configuration for validity, result is reflected in the status of
     * the Thing
     */
    private boolean checkConfig(C config) {
        if (config == null || !config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration is missing or corrupted");
            return false;
        } else {
            return true;
        }
    }

    protected final void refresh() {
        if (getThing().getStatus() == ONLINE) {
            PlugwiseHAController controller = getController();
            if (controller != null) {
                E entity = getEntity(controller);
                if (entity != null) {
                    for (Channel channel : getThing().getChannels()) {
                        ChannelUID channelUID = channel.getUID();
                        if (this.isLinked(channelUID)) {
                            refreshChannel(entity, channelUID);
                        }
                    }
                }
            }
        }
    }
}