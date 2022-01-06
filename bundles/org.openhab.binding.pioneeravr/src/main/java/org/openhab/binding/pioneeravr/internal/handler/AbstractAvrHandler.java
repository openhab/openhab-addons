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
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.openhab.binding.pioneeravr.internal.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.protocol.RequestResponseFactory;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnection;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnectionException;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrResponse;
import org.openhab.binding.pioneeravr.internal.protocol.avr.CommandTypeNotSupportedException;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrDisconnectionEvent;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrDisconnectionListener;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrStatusUpdateEvent;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrUpdateListener;
import org.openhab.binding.pioneeravr.internal.protocol.states.MuteStateValues;
import org.openhab.binding.pioneeravr.internal.protocol.states.PowerStateValues;
import org.openhab.binding.pioneeravr.internal.protocol.utils.DisplayInformationConverter;
import org.openhab.binding.pioneeravr.internal.protocol.utils.VolumeConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractAvrHandler} is responsible for handling commands, which are sent to one of the channels through an
 * AVR connection.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public abstract class AbstractAvrHandler extends BaseThingHandler
        implements AvrUpdateListener, AvrDisconnectionListener {

    private final Logger logger = LoggerFactory.getLogger(AbstractAvrHandler.class);

    private AvrConnection connection;
    private ScheduledFuture<?> statusCheckerFuture;

    public AbstractAvrHandler(Thing thing) {
        super(thing);
        this.connection = createConnection();

        this.connection.addUpdateListener(this);
        this.connection.addDisconnectionListener(this);
    }

    /**
     * Create a new connection to the AVR.
     *
     * @return
     */
    protected abstract AvrConnection createConnection();

    /**
     * Initialize the state of the AVR.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing handler for Pioneer AVR @{}", connection.getConnectionName());
        updateStatus(ThingStatus.ONLINE);

        // Start the status checker
        Runnable statusChecker = () -> {
            try {
                logger.debug("Checking status of AVR @{}", connection.getConnectionName());
                checkStatus();
            } catch (LinkageError e) {
                logger.warn(
                        "Failed to check the status for AVR @{}. If a Serial link is used to connect to the AVR, please check that the Bundle org.openhab.io.transport.serial is available. Cause: {}",
                        connection.getConnectionName(), e.getMessage());
                // Stop to check the status of this AVR.
                if (statusCheckerFuture != null) {
                    statusCheckerFuture.cancel(false);
                }
            }
        };
        statusCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, 10, TimeUnit.SECONDS);
    }

    /**
     * Close the connection and stop the status checker.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (statusCheckerFuture != null) {
            statusCheckerFuture.cancel(true);
        }
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Called when a Power ON state update is received from the AVR for the given zone.
     */
    public void onPowerOn(int zone) {
        // When the AVR is Powered ON, query the volume, the mute state and the source input of the zone
        connection.sendVolumeQuery(zone);
        connection.sendMuteQuery(zone);
        connection.sendInputSourceQuery(zone);
        connection.sendListeningModeQuery(zone);

        // Channels which are not bound to any specific zone
        connection.sendMCACCMemoryQuery();
    }

    /**
     * Called when a Power OFF state update is received from the AVR.
     */
    public void onPowerOff(int zone) {
        // When the AVR is Powered OFF, update the status of channels to Undefined
        updateState(getChannelUID(PioneerAvrBindingConstants.MUTE_CHANNEL, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.LISTENING_MODE_CHANNEL, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.PLAYING_LISTENING_MODE_CHANNEL, zone), UnDefType.UNDEF);

        // Channels which are not bound to any specific zone
        if (zone == 1) {
            updateState(PioneerAvrBindingConstants.MCACC_MEMORY_CHANNEL, UnDefType.UNDEF);
        }
    }

    /**
     * Check the status of the AVR. Return true if the AVR is online, else return false.
     *
     * @return
     */
    private void checkStatus() {
        // If the power query request has not been sent, the connection to the
        // AVR has failed. So update its status to OFFLINE.
        if (!connection.sendPowerQuery(1)) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            // If the power query has succeeded, the AVR status is ONLINE.
            updateStatus(ThingStatus.ONLINE);
            // Then send a power query for zone 2 and 3
            connection.sendPowerQuery(2);
            connection.sendPowerQuery(3);
        }
    }

    /**
     * Send a command to the AVR based on the openHAB command received.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            boolean commandSent = false;
            boolean unknownCommand = false;

            if (channelUID.getId().contains(PioneerAvrBindingConstants.POWER_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendPowerQuery(getZoneFromChannelUID(channelUID.getId()));
                } else {
                    commandSent = connection.sendPowerCommand(command, getZoneFromChannelUID(channelUID.getId()));
                }
            } else if (channelUID.getId().contains(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL)
                    || channelUID.getId().contains(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendVolumeQuery(getZoneFromChannelUID(channelUID.getId()));
                } else {
                    commandSent = connection.sendVolumeCommand(command, getZoneFromChannelUID(channelUID.getId()));
                }
            } else if (channelUID.getId().contains(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendInputSourceQuery(getZoneFromChannelUID(channelUID.getId()));
                } else {
                    commandSent = connection.sendInputSourceCommand(command, getZoneFromChannelUID(channelUID.getId()));
                }
            } else if (channelUID.getId().contains(PioneerAvrBindingConstants.LISTENING_MODE_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendListeningModeQuery(getZoneFromChannelUID(channelUID.getId()));
                } else {
                    commandSent = connection.sendListeningModeCommand(command,
                            getZoneFromChannelUID(channelUID.getId()));
                }
            } else if (channelUID.getId().contains(PioneerAvrBindingConstants.MUTE_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendMuteQuery(getZoneFromChannelUID(channelUID.getId()));
                } else {
                    commandSent = connection.sendMuteCommand(command, getZoneFromChannelUID(channelUID.getId()));
                }
            } else if (channelUID.getId().contains(PioneerAvrBindingConstants.MCACC_MEMORY_CHANNEL)) {
                if (command == RefreshType.REFRESH) {
                    commandSent = connection.sendMCACCMemoryQuery();
                } else {
                    commandSent = connection.sendMCACCMemoryCommand(command);
                }
            } else {
                unknownCommand = true;
            }

            // If the command is not unknown and has not been sent, the AVR is Offline
            if (!commandSent && !unknownCommand) {
                onDisconnection();
            }
        } catch (CommandTypeNotSupportedException e) {
            logger.warn("Unsupported command type received for channel {}.", channelUID.getId());
        }
    }

    /**
     * Called when a status update is received from the AVR.
     */
    @Override
    public void statusUpdateReceived(AvrStatusUpdateEvent event) {
        try {
            AvrResponse response = RequestResponseFactory.getIpControlResponse(event.getData());

            switch (response.getResponseType()) {
                case POWER_STATE:
                    managePowerStateUpdate(response);
                    break;

                case VOLUME_LEVEL:
                    manageVolumeLevelUpdate(response);
                    break;

                case MUTE_STATE:
                    manageMuteStateUpdate(response);
                    break;

                case INPUT_SOURCE_CHANNEL:
                    manageInputSourceChannelUpdate(response);
                    break;

                case LISTENING_MODE:
                    manageListeningModeUpdate(response);
                    break;

                case PLAYING_LISTENING_MODE:
                    managePlayingListeningModeUpdate(response);
                    break;

                case DISPLAY_INFORMATION:
                    manageDisplayedInformationUpdate(response);
                    break;

                case MCACC_MEMORY:
                    manageMCACCMemoryUpdate(response);
                    break;

                default:
                    logger.debug("Unknown response type from AVR @{}. Response discarded: {}", event.getData(),
                            event.getConnection());
            }
        } catch (AvrConnectionException e) {
            logger.debug("Unknown response type from AVR @{}. Response discarded: {}", event.getData(),
                    event.getConnection());
        }
    }

    /**
     * Called when the AVR is disconnected
     */
    @Override
    public void onDisconnection(AvrDisconnectionEvent event) {
        onDisconnection();
    }

    /**
     * Process the AVR disconnection.
     */
    private void onDisconnection() {
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Notify an AVR power state update to openHAB
     *
     * @param response
     */
    private void managePowerStateUpdate(AvrResponse response) {
        OnOffType state = PowerStateValues.ON_VALUE.equals(response.getParameterValue()) ? OnOffType.ON : OnOffType.OFF;

        // When a Power ON state update is received, call the onPowerOn method.
        if (OnOffType.ON == state) {
            onPowerOn(response.getZone());
        } else {
            onPowerOff(response.getZone());
        }

        updateState(getChannelUID(PioneerAvrBindingConstants.POWER_CHANNEL, response.getZone()), state);
    }

    /**
     * Notify an AVR volume level update to openHAB
     *
     * @param response
     */
    private void manageVolumeLevelUpdate(AvrResponse response) {
        updateState(getChannelUID(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL, response.getZone()), new DecimalType(
                VolumeConverter.convertFromIpControlVolumeToDb(response.getParameterValue(), response.getZone())));
        updateState(getChannelUID(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL, response.getZone()),
                new PercentType((int) VolumeConverter.convertFromIpControlVolumeToPercent(response.getParameterValue(),
                        response.getZone())));
    }

    /**
     * Notify an AVR mute state update to openHAB
     *
     * @param response
     */
    private void manageMuteStateUpdate(AvrResponse response) {
        updateState(getChannelUID(PioneerAvrBindingConstants.MUTE_CHANNEL, response.getZone()),
                response.getParameterValue().equals(MuteStateValues.OFF_VALUE) ? OnOffType.OFF : OnOffType.ON);
    }

    /**
     * Notify an AVR input source channel update to openHAB
     *
     * @param response
     */
    private void manageInputSourceChannelUpdate(AvrResponse response) {
        updateState(getChannelUID(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL, response.getZone()),
                new StringType(response.getParameterValue()));
    }

    /**
     * Notify an AVR now-playing, in-effect listening mode (audio output format) update to openHAB
     *
     * @param response
     */
    private void managePlayingListeningModeUpdate(AvrResponse response) {
        updateState(getChannelUID(PioneerAvrBindingConstants.PLAYING_LISTENING_MODE_CHANNEL, response.getZone()),
                new StringType(response.getParameterValue()));
    }

    /**
     * Notify an AVR set listening mode (user-selected audio mode) update to openHAB
     *
     * @param response
     */
    private void manageListeningModeUpdate(AvrResponse response) {
        updateState(getChannelUID(PioneerAvrBindingConstants.LISTENING_MODE_CHANNEL, response.getZone()),
                new StringType(response.getParameterValue()));
    }

    /**
     * Notify an AVR displayed information update to openHAB
     *
     * @param response
     */
    private void manageDisplayedInformationUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.DISPLAY_INFORMATION_CHANNEL,
                new StringType(DisplayInformationConverter.convertMessageFromIpControl(response.getParameterValue())));
    }

    /**
     * Notify an AVR MCACC Memory update to openHAB
     *
     * @param response
     */
    private void manageMCACCMemoryUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.MCACC_MEMORY_CHANNEL, new StringType(response.getParameterValue()));
    }

    /**
     * Build the channelUID from the channel name and the zone number.
     *
     * @param channelName
     * @param zone
     * @return
     */
    protected String getChannelUID(String channelName, int zone) {
        return String.format(PioneerAvrBindingConstants.GROUP_CHANNEL_PATTERN, zone, channelName);
    }

    /**
     * Return the zone from the given channelUID.
     *
     * Return 0 if the zone cannot be extracted from the channelUID.
     *
     * @param channelUID
     * @return
     */
    protected int getZoneFromChannelUID(String channelUID) {
        int zone = 0;
        Matcher matcher = PioneerAvrBindingConstants.GROUP_CHANNEL_ZONE_PATTERN.matcher(channelUID);
        if (matcher.find()) {
            zone = Integer.valueOf(matcher.group(1));
        }
        return zone;
    }
}
