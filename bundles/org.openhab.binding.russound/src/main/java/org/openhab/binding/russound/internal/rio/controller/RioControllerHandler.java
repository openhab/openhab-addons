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
package org.openhab.binding.russound.internal.rio.controller;

import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.internal.rio.AbstractRioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioHandlerCallbackListener;
import org.openhab.binding.russound.internal.rio.RioNamedHandler;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.source.RioSourceHandler;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;
import org.openhab.binding.russound.internal.rio.zone.RioZoneHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * The bridge handler for a Russound Controller. A controller provides access to sources ({@link RioSourceHandler}) and
 * zones ({@link RioZoneHandler}). This
 * implementation must be attached to a {@link RioSystemHandler} bridge.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioControllerHandler extends AbstractBridgeHandler<RioControllerProtocol> implements RioNamedHandler {
    /**
     * The controller identifier of this instance (between 1-6)
     */
    private final AtomicInteger controller = new AtomicInteger(0);

    /**
     * {@link Gson} used for JSON operations
     */
    private final Gson gson = GsonUtilities.createGson();

    /**
     * Callback listener to use when zone name changes - will call {@link #refreshNamedHandler(Gson, Class, String)} to
     * refresh the {@link RioConstants#CHANNEL_CTLZONES} channel
     */
    private final RioHandlerCallbackListener handlerCallbackListener = new RioHandlerCallbackListener() {
        @Override
        public void stateUpdate(String channelId, State state) {
            refreshNamedHandler(gson, RioZoneHandler.class, RioConstants.CHANNEL_CTLZONES);
        }
    };

    /**
     * Constructs the handler from the {@link Bridge}
     *
     * @param bridge a non-null {@link Bridge} the handler is for
     */
    public RioControllerHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Returns the controller identifier
     *
     * @return the controller identifier
     */
    @Override
    public int getId() {
        return controller.get();
    }

    /**
     * Returns the controller name
     *
     * @return a non-empty, non-null controller name
     */
    @Override
    public String getName() {
        return "Controller " + getId();
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. The only command this handles is a {@link RefreshType} and that's handled
     * by {{@link #handleRefresh(String)}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (id.equals(RioConstants.CHANNEL_CTLZONES)) {
            refreshNamedHandler(gson, RioZoneHandler.class, RioConstants.CHANNEL_CTLZONES);
        }
        // Can't refresh any others...
    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioSystemHandler}. Once validated, a {@link RioControllerProtocol} is set via
     * {@link #setProtocolHandler(AbstractRioProtocol)} and the bridge comes online.
     */
    @Override
    public void initialize() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot be initialized without a bridge");
            return;
        }

        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No handler specified (null) for the bridge!");
            return;
        }

        if (!(handler instanceof RioSystemHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Controller must be attached to a system bridge: " + handler.getClass());
            return;
        }

        final RioControllerConfig config = getThing().getConfiguration().as(RioControllerConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        final int configController = config.getController();
        if (configController < 1 || configController > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Controller must be between 1 and 8: " + configController);
            return;
        }
        controller.set(configController);

        // Get the socket session from the
        final SocketSession socketSession = getSocketSession();
        if (socketSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No socket session found");
            return;
        }

        if (getProtocolHandler() != null) {
            getProtocolHandler().dispose();
        }

        setProtocolHandler(new RioControllerProtocol(configController, socketSession,
                new StatefulHandlerCallback(new AbstractRioHandlerCallback() {
                    @Override
                    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                        updateStatus(status, detail, msg);
                    }

                    @Override
                    public void stateChanged(String channelId, State state) {
                        updateState(channelId, state);
                        fireStateUpdated(channelId, state);
                    }

                    @Override
                    public void setProperty(String propertyName, String property) {
                        getThing().setProperty(propertyName, property);
                    }
                })));

        updateStatus(ThingStatus.ONLINE);
        getProtocolHandler().postOnline();

        refreshNamedHandler(gson, RioZoneHandler.class, RioConstants.CHANNEL_CTLZONES);
    }

    /**
     * Overrides the base to call {@link #childChanged(ThingHandler, boolean)} to recreate the zone names
     */
    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        childChanged(childHandler, true);
    }

    /**
     * Overrides the base to call {@link #childChanged(ThingHandler, boolean)} to recreate the zone names
     */
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        childChanged(childHandler, false);
    }

    /**
     * Helper method to recreate the {@link RioConstants#CHANNEL_CTLZONES} channel
     *
     * @param childHandler a non-null child handler that changed
     * @param added true if the handler was added, false otherwise
     * @throw IllegalArgumentException if childHandler is null
     */
    private void childChanged(ThingHandler childHandler, boolean added) {
        if (childHandler == null) {
            throw new IllegalArgumentException("childHandler cannot be null");
        }
        if (childHandler instanceof RioZoneHandler zoneHandler) {
            final RioHandlerCallback callback = zoneHandler.getRioHandlerCallback();
            if (callback != null) {
                if (added) {
                    callback.addListener(RioConstants.CHANNEL_ZONENAME, handlerCallbackListener);
                } else {
                    callback.removeListener(RioConstants.CHANNEL_ZONENAME, handlerCallbackListener);
                }
            }
            refreshNamedHandler(gson, RioZoneHandler.class, RioConstants.CHANNEL_CTLZONES);
        }
    }
}
