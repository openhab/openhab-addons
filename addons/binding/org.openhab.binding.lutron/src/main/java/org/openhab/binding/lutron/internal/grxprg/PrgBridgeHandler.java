/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.grxprg;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PrgBridgeHandler} is responsible for handling all bridge interactions. This includes management of the
 * connection and processing of any commands (thru the {@link PrgProtocolHandler}).
 *
 * @author Tim Roberts
 */
public class PrgBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(PrgBridgeHandler.class);

    /**
     * The {@link PrgProtocolHandler} that handles the actual protocol. Will never be null
     */
    private PrgProtocolHandler _protocolHandler;

    /**
     * The {@link SocketSession} to the physical devices. Will never be null
     */
    private SocketSession _session;

    /**
     * The retry connection event. Null if not retrying.
     */
    private ScheduledFuture<?> _retryConnection;

    /**
     * Constructs the handler from the {@link Bridge}. Simply calls the super constructor with the {@link Bridge},
     * creates the session (unconnected) and the protocol handler.
     *
     * @param bridge a non-null {@link Bridge} the handler is for
     */
    public PrgBridgeHandler(Bridge bridge) {
        super(bridge);

        if (bridge == null) {
            throw new IllegalArgumentException("thing cannot be null");
        }

        final PrgBridgeConfig config = getPrgBridgeConfig();
        _session = new SocketSession(config.getIpAddress(), 23);

        _protocolHandler = new PrgProtocolHandler(_session, new PrgHandlerCallback() {
            @Override
            public void stateChanged(String channelId, State state) {
                updateState(channelId, state);
            }

            @Override
            public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                updateStatus(status, detail, msg);

                if (status != ThingStatus.ONLINE) {
                    disconnect(true);
                }
            }

            @Override
            public void stateChanged(int controlUnit, String channelId, State state) {
                getGrafikEyeHandler(controlUnit).stateChanged(channelId, state);
            }

            @Override
            public boolean isShade(int controlUnit, int zone) {
                return getGrafikEyeHandler(controlUnit).isShade(zone);
            }
        });
    }

    /**
     * Internal method to retrieve the {@link PrgProtocolHandler} used by the bridge
     *
     * @return a non-null protocol handler to use
     */
    PrgProtocolHandler getProtocolHandler() {
        return _protocolHandler;
    }

    /**
     * Helper method used to retrieve the {@link GrafikEyeHandler} for a given control unit number. If not found, an
     * IllegalArgumentException will be thrown.
     *
     * @param controlUnit a control number to retrieve
     * @return a non-null {@link GrafikEyeHandler}
     * @throws IllegalArgumentException if the {@link GrafikEyeHandler} for the given controlUnit was not found.
     */
    private GrafikEyeHandler getGrafikEyeHandler(int controlUnit) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof GrafikEyeHandler) {
                final GrafikEyeHandler handler = (GrafikEyeHandler) thing.getHandler();
                if (handler.getControlUnit() == controlUnit) {
                    return handler;
                }
            } else {
                logger.warn("Should not be a non-GrafikEyeHandler as a thing to this bridge - ignoring: {}", thing);
            }
        }

        throw new IllegalArgumentException("Could not find a GrafikEyeHandler for control unit : " + controlUnit);
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link PrgProtocolHandler}. Basically we validate the type of command for the channel then call the
     * {@link PrgProtocolHandler} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link PrgProtocolHandler} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        // if (getThing().getStatus() != ThingStatus.ONLINE) {
        // // Ignore any command if not online
        // return;
        // }

        String id = channelUID.getId();

        if (id == null) {
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(PrgConstants.CHANNEL_ZONELOWERSTOP)) {
            _protocolHandler.setZoneLowerStop();

        } else if (id.equals(PrgConstants.CHANNEL_ZONERAISESTOP)) {
            _protocolHandler.setZoneRaiseStop();

        } else if (id.equals(PrgConstants.CHANNEL_TIMECLOCK)) {
            if (command instanceof DateTimeType) {
                final Calendar c = ((DateTimeType) command).getCalendar();
                _protocolHandler.setTime(c);
            } else {
                logger.error("Received a TIMECLOCK channel command with a non DateTimeType: {}", command);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_SCHEDULE)) {
            if (command instanceof DecimalType) {
                final int schedule = ((DecimalType) command).intValue();
                _protocolHandler.selectSchedule(schedule);
            } else {
                logger.error("Received a SCHEDULE channel command with a non DecimalType: {}", command);
            }

        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCESTART)) {
            _protocolHandler.startSuperSequence();

        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCEPAUSE)) {
            _protocolHandler.pauseSuperSequence();
        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCERESUME)) {
            _protocolHandler.resumeSuperSequence();

        } else {
            logger.error("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link PrgProtocolHandler} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (id.equals(PrgConstants.CHANNEL_TIMECLOCK)) {
            _protocolHandler.refreshTime();

        } else if (id.equals(PrgConstants.CHANNEL_SCHEDULE)) {
            _protocolHandler.refreshSchedule();

        } else if (id.equals(PrgConstants.CHANNEL_SUNRISE)) {
            _protocolHandler.refreshSunriseSunset();

        } else if (id.equals(PrgConstants.CHANNEL_SUNSET)) {
            _protocolHandler.refreshSunriseSunset();

        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCESTATUS)) {
            _protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSTEP)) {
            _protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTMIN)) {
            _protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSEC)) {
            _protocolHandler.reportSuperSequenceStatus();

        } else {
            // Can't refresh any others...
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration and will attempt to connect to
     * the switch (via {{@link #retryConnect()}.
     */
    @Override
    public void initialize() {
        final PrgBridgeConfig config = getPrgBridgeConfig();

        if (config == null) {
            return;
        }

        if (config.getIpAddress() == null || config.getIpAddress().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address/Host Name of GRX-PRG/GRX-CI-PRG is missing from configuration");
            return;
        }

        // Try initial connection in a scheduled task
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }

        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Attempts to connect to the PRG unit. If successfully connect, the {@link PrgProtocolHandler#login()} will be
     * called to log into the unit. If a connection cannot be established (or login failed), the connection attempt will
     * be retried later (via {@link #retryConnect()})
     */
    private void connect() {

        final PrgBridgeConfig config = getPrgBridgeConfig();

        String response = "Server is offline - will try to reconnect later";
        try {
            logger.info("Attempting connection ...");
            _session.connect();

            response = _protocolHandler.login(config.getUserName());
            if (response == null) {
                if (config != null) {
                    updateStatus(ThingStatus.ONLINE);
                    return;
                }
            }

        } catch (Exception e) {
            logger.error("Exception during connection attempt", e);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
        retryConnect();
    }

    /**
     * Attempts to disconnect from the session and will optionally retry the connection attempt.
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        try {
            _session.disconnect();
        } catch (IOException e) {
            // ignore - we don't care
        }

        if (retryConnection) {
            retryConnect();
        }
    }

    /**
     * Retries the connection attempt - schedules a job in {@link PrgBridgeConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    private void retryConnect() {
        if (_retryConnection == null) {
            final PrgBridgeConfig config = getPrgBridgeConfig();
            if (config != null) {

                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                _retryConnection = this.scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        _retryConnection = null;
                        connect();
                    }

                }, config.getRetryPolling(), TimeUnit.SECONDS);
            }
        } else {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        }
    }

    /**
     * Simplu gets the {@link PrgBridgeConfig} from the {@link Thing} and will set the status to offline if not
     * found.
     *
     * @return a possible null {@link PrgBridgeConfig}
     */
    private PrgBridgeConfig getPrgBridgeConfig() {
        final PrgBridgeConfig config = getThing().getConfiguration().as(PrgBridgeConfig.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
        }

        return config;
    }

    /**
     * {@inheritDoc}
     *
     * Disposes of the handler. Will simply call {@link #disconnect(boolean)} to disconnect and NOT retry the
     * connection
     */
    @Override
    public void dispose() {
        disconnect(false);
    }
}
