/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.preset;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractThingHandler;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.bank.RioBankHandler;
import org.openhab.binding.russound.internal.rio.zone.RioZoneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thing handler for a Russound Preset. A preset provides direct access (from a bank) to a specific preset channel
 * (usually a radio frequency). This implementation must be attached to a {@link RioBankHandler} or
 * {@link RioZoneHandler} bridge.
 *
 * @author Tim Roberts
 */
public class RioPresetHandler extends AbstractThingHandler<RioPresetProtocol> {
    // Logger
    private Logger logger = LoggerFactory.getLogger(RioPresetHandler.class);

    /**
     * The preset identifier (1-6 for bank, 1-36 for a zone)
     */
    private int _preset;

    /**
     * Constructs the handler from the {@link Thing}
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public RioPresetHandler(Thing thing) {
        super(thing);

    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link RioPresetProtocol}. Basically we validate the type of command for the channel then call the
     * {@link RioPresetProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link RioPresetProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }
        final String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_PRESETNAME)) {
            if (command instanceof StringType) {
                getProtocolHandler().setName(command.toString());
            } else {
                logger.debug("Received a preset name channel command with a non StringType: {}", command);
            }
        } else if (id.equals(RioConstants.CHANNEL_PRESETCMD)) {
            if (command instanceof StringType) {
                final String cmd = command.toString().toLowerCase();
                switch (cmd) {
                    case RioConstants.CMD_PRESETSAVE:
                        getProtocolHandler().savePreset();
                        break;

                    case RioConstants.CMD_PRESETRESTORE:
                        getProtocolHandler().restorePreset();
                        break;
                    case RioConstants.CMD_PRESETDELETE:
                        getProtocolHandler().deletePreset();
                        break;
                    default:
                        logger.info("Unknown command for preset command channel: {}", cmd);
                        break;
                }
                getProtocolHandler().setName(command.toString());
            } else {
                logger.debug("Received a preset name channel command with a non StringType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link RioPresetProtocol} to
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

        if (id.equals(RioConstants.CHANNEL_PRESETNAME)) {
            getProtocolHandler().refreshName();

        } else if (id.equals(RioConstants.CHANNEL_PRESETVALID)) {
            getProtocolHandler().refreshValid();
        } else {
            // Can't refresh any others...
        }
    }

    /**
     * Initializes the thing. Confirms the configuration is valid and that our parent bridge is either a
     * {@link RioBankHandler} or {@link RioZoneHandler}. Once validated, a {@link RioPresetProtocol} is set via
     * {@link #setProtocolHandler(RioPresetProtocol)} and the thing comes online.
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

        if (!(handler instanceof RioBankHandler) && !(handler instanceof RioZoneHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Preset must be attached to either the Bank bridge or a Zone bridge: " + handler.getClass());
            return;
        }

        final RioPresetConfig config = getThing().getConfiguration().as(RioPresetConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        _preset = config.getPreset();
        if (handler instanceof RioBankHandler) {
            if (_preset < 1 || _preset > 6) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Preset must be between 1 and 6 for a bank: " + _preset);

            }
        } else if (_preset < 1 || _preset > 36) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Preset must be between 1 and 36 for a zone: " + _preset);

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

        int bankId = -1;
        int sourceId = -1;
        int zoneId = -1;
        int controllerId = -1;

        if (handler instanceof RioZoneHandler) {
            final RioZoneHandler zoneHandler = (RioZoneHandler) handler;
            controllerId = zoneHandler.getController();
            zoneId = zoneHandler.getZone();
        } else {
            final RioBankHandler bankHandler = (RioBankHandler) handler;
            bankId = bankHandler.getBank();
            sourceId = bankHandler.getSource();
        }

        setProtocolHandler(new RioPresetProtocol(_preset, bankId, sourceId, zoneId, controllerId, socketSession,
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

        if (sourceId > 0) {
            getProtocolHandler().refreshName();
            getProtocolHandler().refreshValid();
        }
    }
}
