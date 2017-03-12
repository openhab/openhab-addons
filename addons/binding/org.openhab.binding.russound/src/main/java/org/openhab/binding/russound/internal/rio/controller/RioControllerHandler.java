/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.controller;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.source.RioSourceHandler;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;
import org.openhab.binding.russound.internal.rio.zone.RioZoneHandler;

/**
 * The bridge handler for a Russound Controller. A controller provides access to sources ({@link RioSourceHandler}) and
 * zones ({@link RioZoneHandler}). This
 * implementation must be attached to a {@link RioSystemHandler} bridge.
 *
 * @author Tim Roberts
 */
public class RioControllerHandler extends AbstractBridgeHandler<RioControllerProtocol> {
    /**
     * The controller identifier of this instance (between 1-6)
     */
    private int _controller;

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
    public int getController() {
        return _controller;
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels - this handler has no channels to handle and
     * is a NOP
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands to implement
    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioSystemHandler}. Once validated, a {@link RioControllerProtocol} is set via
     * {@link #setProtocolHandler(RioControllerProtocol)} and the bridge comes online.
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

        _controller = config.getController();
        if (_controller < 1 || _controller > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Controller must be between 1 and 8: " + _controller);
            return;
        }

        // Get the socket session from the
        final SocketSession socketSession = getSocketSession();
        if (socketSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No socket session found");
            return;
        }

        if (getProtocolHandler() != null) {
            getProtocolHandler().dispose();
        }

        setProtocolHandler(new RioControllerProtocol(_controller, socketSession,
                new StatefulHandlerCallback(new RioHandlerCallback() {
                    @Override
                    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                        updateStatus(status, detail, msg);
                    }

                    @Override
                    public void stateChanged(String channelId, State state) {
                        updateState(channelId, state);
                    }

                    @Override
                    public void setProperty(String propertyName, String property) {
                        getThing().setProperty(propertyName, property);
                    }
                })));

        updateStatus(ThingStatus.ONLINE);
        getProtocolHandler().refreshControllerType();
        getProtocolHandler().refreshControllerIpAddress();
        getProtocolHandler().refreshControllerMacAddress();
    }
}
