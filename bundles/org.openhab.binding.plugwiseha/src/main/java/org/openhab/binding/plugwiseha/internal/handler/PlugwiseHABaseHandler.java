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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.openhab.core.thing.ThingStatus.*;

import java.lang.reflect.ParameterizedType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHAThingConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHABaseHandler} abstract class provides common methods and
 * properties for the ThingHandlers of this binding. Extends @{link
 * BaseThingHandler}
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 *
 * @param <E> entity - the Plugwise Home Automation entity class used by this
 *            thing handler
 * @param <C> config - the Plugwise Home Automation config class used by this
 *            thing handler
 */

@NonNullByDefault
public abstract class PlugwiseHABaseHandler<E, C extends PlugwiseHAThingConfig> extends BaseThingHandler {

    protected static final String STATUS_DESCRIPTION_COMMUNICATION_ERROR = "Error communicating with the Plugwise Home Automation controller";

    protected final Logger logger = LoggerFactory.getLogger(PlugwiseHABaseHandler.class);

    private Class<?> clazz;

    // Constructor
    @SuppressWarnings("null")
    public PlugwiseHABaseHandler(Thing thing) {
        super(thing);
        clazz = (Class<?>) (((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
    }

    // Abstract methods

    /**
     * Initializes the Plugwise Entity that this class handles.
     *
     * @param config the thing configuration
     * @param bridge the bridge that this thing is part of
     */
    protected abstract void initialize(C config, PlugwiseHABridgeHandler bridge);

    /**
     * Get the Plugwise Entity that belongs to this ThingHandler
     *
     * @param controller the controller for this ThingHandler
     */
    protected abstract @Nullable E getEntity(PlugwiseHAController controller) throws PlugwiseHAException;

    /**
     * Handles a {@link RefreshType} command for a given channel.
     *
     * @param entity the Plugwise Entity
     * @param channelUID the channel uid the command is for
     */
    protected abstract void refreshChannel(E entity, ChannelUID channelUID);

    /**
     * Handles a command for a given channel.
     * 
     * @param entity the Plugwise Entity
     * @param channelUID the channel uid the command is for
     * @param command the command
     */
    protected abstract void handleCommand(E entity, ChannelUID channelUID, Command command) throws PlugwiseHAException;

    // Overrides

    @Override
    public void initialize() {
        C config = getPlugwiseThingConfig();

        if (checkConfig(config)) {
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

            PlugwiseHABridgeHandler bridgeHandler = (PlugwiseHABridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                initialize(config, bridgeHandler);
            }
        } else {
            logger.debug("Invalid config for Plugwise Home Automation thing handler with config = {}", config);
        }
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);

        if (getThing().getStatus() == ONLINE) {
            PlugwiseHAController controller = getController();
            if (controller != null) {
                try {
                    @Nullable
                    E entity = getEntity(controller);
                    if (entity != null) {
                        if (this.isLinked(channelUID)) {
                            if (command instanceof RefreshType) {
                                refreshChannel(entity, channelUID);
                            } else {
                                handleCommand(entity, channelUID, command);
                            }
                        }
                    }
                } catch (PlugwiseHAException e) {
                    logger.warn("Unexpected error handling command = {} for channel = {} : {}", command, channelUID,
                            e.getMessage());
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
        return (C) getConfigAs(clazz);
    }

    // Private & protected methods

    private @Nullable PlugwiseHAController getController() {
        PlugwiseHABridgeHandler bridgeHandler = getPlugwiseHABridge();

        if (bridgeHandler != null) {
            return bridgeHandler.getController();
        }

        return null;
    }

    /**
     * Checks the configuration for validity, result is reflected in the status of
     * the Thing
     */
    private boolean checkConfig(C config) {
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration is missing or corrupted");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            setLinkedChannelsUndef();
        }
    }

    private void setLinkedChannelsUndef() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (this.isLinked(channelUID)) {
                updateState(channelUID, UnDefType.UNDEF);
            }
        }
    }

    protected final void refresh() {
        PlugwiseHABridgeHandler bridgeHandler = getPlugwiseHABridge();
        if (bridgeHandler != null) {
            if (bridgeHandler.getThing().getStatusInfo().getStatus() == ThingStatus.ONLINE) {
                PlugwiseHAController controller = getController();
                if (controller != null) {
                    @Nullable
                    E entity = null;
                    try {
                        entity = getEntity(controller);
                    } catch (PlugwiseHAException e) {
                        updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        setLinkedChannelsUndef();
                    }
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
}
