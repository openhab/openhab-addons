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
package org.openhab.binding.lutron.internal.grxprg;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PrgBridgeHandler} is responsible for handling all bridge interactions. This includes management of the
 * connection and processing of any commands (thru the {@link PrgProtocolHandler}).
 *
 * @author Tim Roberts - Initial contribution
 */
public class PrgBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(PrgBridgeHandler.class);

    /**
     * The {@link PrgProtocolHandler} that handles the actual protocol. Will never be null
     */
    private PrgProtocolHandler protocolHandler;

    /**
     * The {@link SocketSession} to the physical devices. Will never be null
     */
    private SocketSession session;

    /**
     * The retry connection event. Null if not retrying.
     */
    private ScheduledFuture<?> retryConnectionJob;

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
        session = new SocketSession(getThing().getUID().getAsString(), config.getIpAddress(), 23);

        protocolHandler = new PrgProtocolHandler(session, new PrgHandlerCallback() {
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
        return protocolHandler;
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
            protocolHandler.setZoneLowerStop();
        } else if (id.equals(PrgConstants.CHANNEL_ZONERAISESTOP)) {
            protocolHandler.setZoneRaiseStop();
        } else if (id.equals(PrgConstants.CHANNEL_TIMECLOCK)) {
            if (command instanceof DateTimeType dateTime) {
                final ZonedDateTime zdt = dateTime.getZonedDateTime();
                protocolHandler.setTime(GregorianCalendar.from(zdt));
            } else {
                logger.error("Received a TIMECLOCK channel command with a non DateTimeType: {}", command);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_SCHEDULE)) {
            if (command instanceof DecimalType scheduleCommand) {
                final int schedule = scheduleCommand.intValue();
                protocolHandler.selectSchedule(schedule);
            } else {
                logger.error("Received a SCHEDULE channel command with a non DecimalType: {}", command);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCESTART)) {
            protocolHandler.startSuperSequence();
        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCEPAUSE)) {
            protocolHandler.pauseSuperSequence();
        } else if (id.startsWith(PrgConstants.CHANNEL_SUPERSEQUENCERESUME)) {
            protocolHandler.resumeSuperSequence();
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
            protocolHandler.refreshTime();
        } else if (id.equals(PrgConstants.CHANNEL_SCHEDULE)) {
            protocolHandler.refreshSchedule();
        } else if (id.equals(PrgConstants.CHANNEL_SUNRISE)) {
            protocolHandler.refreshSunriseSunset();
        } else if (id.equals(PrgConstants.CHANNEL_SUNSET)) {
            protocolHandler.refreshSunriseSunset();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCESTATUS)) {
            protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSTEP)) {
            protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTMIN)) {
            protocolHandler.reportSuperSequenceStatus();
        } else if (id.equals(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSEC)) {
            protocolHandler.reportSuperSequenceStatus();
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
            session.connect();

            response = protocolHandler.login(config.getUserName());
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
            session.disconnect();
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
        if (retryConnectionJob == null) {
            final PrgBridgeConfig config = getPrgBridgeConfig();
            if (config != null) {
                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                retryConnectionJob = this.scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        retryConnectionJob = null;
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
