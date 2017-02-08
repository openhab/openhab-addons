/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.bank;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.source.RioSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a Russound Bank. A bank provides access to presets and is generally associated with a tuner
 * source. A bank is similar to "FM-1", "FM-2" or "AM" on radios where the presets are different stations. This
 * implementation must be attached to a {@link RioSourceHandler} bridge.
 *
 * @author Tim Roberts
 */
public class RioBankHandler extends AbstractBridgeHandler<RioBankProtocol> {
    private Logger logger = LoggerFactory.getLogger(RioBankHandler.class);

    /**
     * The bank identifier of this instance
     */
    private int _bank;

    /**
     * The parent source identifier
     */
    private int _source;

    /**
     * Constructs the handler from the {@link Bridge}
     *
     * @param bridge a non-null {@link Bridge} the handler is for
     */
    public RioBankHandler(Bridge bridge) {
        super(bridge);

    }

    /**
     * Returns the bank identifier for this handler
     *
     * @return a bank identifier from 1-6
     */
    public int getBank() {
        return _bank;
    }

    /**
     * Returns the source identifier this handler is related to
     *
     * @return a source identifier from 1-12
     */
    public int getSource() {
        return _source;
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link RioBankProtocol}. Basically we validate the type of command for the channel then call the
     * {@link RioBankProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link RioBankProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }
        String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_BANKNAME)) {
            if (command instanceof StringType) {
                getProtocolHandler().setName(command.toString());
            } else {
                logger.debug("Received a favorite name channel command with a non StringType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link RioBankProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (getProtocolHandler() == null) {
            return;
        }

        // Remove the cache'd value to force a refreshed value
        ((StatefulHandlerCallback) getProtocolHandler().getCallback()).removeState(id);

        if (id.equals(RioConstants.CHANNEL_BANKNAME)) {
            getProtocolHandler().refreshName();

        } else {
            // Can't refresh any others...
        }
    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioSourceHandler}. Once validated, a {@link RioBankProtocol} is set via
     * {@link #setProtocolHandler(RioBankProtocol)} and the bridge comes online.
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

        if (!(handler instanceof RioSourceHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bank must be attached to a Source bridge: " + handler.getClass());
            return;
        }

        final RioBankConfig config = getThing().getConfiguration().as(RioBankConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        _bank = config.getBank();
        if (_bank < 1 || _bank > 6) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bank must be between 1 and 6: " + config.getBank());

        }

        // Get the socket session from the
        final SocketSession socketSession = getSocketSession();
        if (socketSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No socket session found");
            return;
        }

        if (getProtocolHandler() != null) {
            setProtocolHandler(null);
        }

        _source = ((RioSourceHandler) handler).getSource();

        setProtocolHandler(new RioBankProtocol(_bank, _source, socketSession,
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
                    public void setProperty(String propertyName, String propertyValue) {
                        getThing().setProperty(propertyName, propertyValue);
                    }
                })));

        updateStatus(ThingStatus.ONLINE);

        getProtocolHandler().refreshName();
    }
}
