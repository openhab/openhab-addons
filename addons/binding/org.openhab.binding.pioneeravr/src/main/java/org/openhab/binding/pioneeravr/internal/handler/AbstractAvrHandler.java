/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.protocol.RequestResponseFactory;
import org.openhab.binding.pioneeravr.protocol.AvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;
import org.openhab.binding.pioneeravr.protocol.CommandTypeNotSupportedException;
import org.openhab.binding.pioneeravr.protocol.event.AvrDisconnectionEvent;
import org.openhab.binding.pioneeravr.protocol.event.AvrDisconnectionListener;
import org.openhab.binding.pioneeravr.protocol.event.AvrStatusUpdateEvent;
import org.openhab.binding.pioneeravr.protocol.event.AvrUpdateListener;
import org.openhab.binding.pioneeravr.protocol.states.MuteStateValues;
import org.openhab.binding.pioneeravr.protocol.states.PowerStateValues;
import org.openhab.binding.pioneeravr.protocol.utils.DisplayInformationConverter;
import org.openhab.binding.pioneeravr.protocol.utils.VolumeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractAvrHandler} is responsible for handling commands, which are sent to one of the channels through an
 * AVR connection.
 * 
 * @author Antoine Besnard - Initial contribution
 */
public abstract class AbstractAvrHandler extends BaseThingHandler
        implements AvrUpdateListener, AvrDisconnectionListener {

    private Logger logger = LoggerFactory.getLogger(AbstractAvrHandler.class);

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
        super.initialize();

        // Start the status checker
        Runnable statusChecker = new Runnable() {
            @Override
            public void run() {
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
     * Called when a Power ON state update is received from the AVR.
     */
    public void onPowerOn() {
        // When the AVR is Powered ON, query the volume, the mute state and the source input
        connection.sendVolumeQuery();
        connection.sendMuteQuery();
        connection.sendSourceInputQuery();
    }

    /**
     * Called when a Power OFF state update is received from the AVR.
     */
    public void onPowerOff() {
        // When the AVR is Powered OFF, update the status of channels to Undefined
        updateState(PioneerAvrBindingConstants.MUTE_CHANNEL, UnDefType.UNDEF);
        updateState(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL, UnDefType.UNDEF);
        updateState(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL, UnDefType.UNDEF);
        updateState(PioneerAvrBindingConstants.DISPLAY_INFORMATION_CHANNEL, new StringType(StringUtils.EMPTY));
        updateState(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL, new StringType(StringUtils.EMPTY));
    }

    /**
     * Check the status of the AVR. Return true if the AVR is online, else return false.
     * 
     * @return
     */
    private void checkStatus() {
        // If the power query request has not been sent, the connection to the
        // AVR has failed. So update its status to OFFLINE.
        if (!connection.sendPowerQuery()) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            // IF the power query has succeeded, the AVR status is ONLINE.
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Send a command to the AVR based on the OpenHAB command received.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            boolean commandSent = false;
            boolean unknownCommand = false;

            if (channelUID.getId().equals(PioneerAvrBindingConstants.POWER_CHANNEL)) {
                commandSent = connection.sendPowerCommand(command);
            } else if (channelUID.getId().equals(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL)
                    || channelUID.getId().equals(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL)) {
                commandSent = connection.sendVolumeCommand(command);
            } else if (channelUID.getId().equals(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL)) {
                commandSent = connection.sendInputSourceCommand(command);
            } else if (channelUID.getId().equals(PioneerAvrBindingConstants.MUTE_CHANNEL)) {
                commandSent = connection.sendMuteCommand(command);
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

                case DISPLAY_INFORMATION:
                    manageDisplayedInformationUpdate(response);
                    break;

                default:
                    logger.debug("Unkown response type from AVR @{}. Response discarded: {}", event.getData(),
                            event.getConnection());

            }
        } catch (AvrConnectionException e) {
            logger.debug("Unkown response type from AVR @{}. Response discarded: {}", event.getData(),
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
     * Notify an AVR power state update to OpenHAB
     * 
     * @param response
     */
    private void managePowerStateUpdate(AvrResponse response) {
        OnOffType state = PowerStateValues.ON_VALUE.equals(response.getParameterValue()) ? OnOffType.ON : OnOffType.OFF;

        // When a Power ON state update is received, call the onPowerOn method.
        if (OnOffType.ON == state) {
            onPowerOn();
        } else {
            onPowerOff();
        }

        updateState(PioneerAvrBindingConstants.POWER_CHANNEL, state);
    }

    /**
     * Notify an AVR volume level update to OpenHAB
     * 
     * @param response
     */
    private void manageVolumeLevelUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.VOLUME_DB_CHANNEL,
                new DecimalType(VolumeConverter.convertFromIpControlVolumeToDb(response.getParameterValue())));
        updateState(PioneerAvrBindingConstants.VOLUME_DIMMER_CHANNEL, new PercentType(
                (int) VolumeConverter.convertFromIpControlVolumeToPercent(response.getParameterValue())));
    }

    /**
     * Notify an AVR mute state update to OpenHAB
     * 
     * @param response
     */
    private void manageMuteStateUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.MUTE_CHANNEL,
                response.getParameterValue().equals(MuteStateValues.OFF_VALUE) ? OnOffType.OFF : OnOffType.ON);
    }

    /**
     * Notify an AVR input source channel update to OpenHAB
     * 
     * @param response
     */
    private void manageInputSourceChannelUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.SET_INPUT_SOURCE_CHANNEL, new StringType(response.getParameterValue()));
    }

    /**
     * Notify an AVR displayed information update to OpenHAB
     * 
     * @param response
     */
    private void manageDisplayedInformationUpdate(AvrResponse response) {
        updateState(PioneerAvrBindingConstants.DISPLAY_INFORMATION_CHANNEL,
                new StringType(DisplayInformationConverter.convertMessageFromIpControl(response.getParameterValue())));
    }

}
