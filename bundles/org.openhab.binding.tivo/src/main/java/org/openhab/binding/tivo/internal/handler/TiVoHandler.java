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
package org.openhab.binding.tivo.internal.handler;

import static org.openhab.binding.tivo.internal.TiVoBindingConstants.*;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tivo.internal.service.TivoConfigData;
import org.openhab.binding.tivo.internal.service.TivoStatusData;
import org.openhab.binding.tivo.internal.service.TivoStatusData.ConnectionStatus;
import org.openhab.binding.tivo.internal.service.TivoStatusProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TiVoHandler} is the BaseThingHandler responsible for handling commands that are
 * sent to one of the Tivo's channels.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - Updates / compilation corrections. Addition of channel scanning functionality.
 * @author Michael Lobstein - Updated for OH3
 */

@NonNullByDefault
public class TiVoHandler extends BaseThingHandler {
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("(\\d+)\\.?(\\d+)?");

    private final Logger logger = LoggerFactory.getLogger(TiVoHandler.class);
    private TivoConfigData tivoConfigData = new TivoConfigData();
    private ConnectionStatus lastConnectionStatus = ConnectionStatus.UNKNOWN;
    private Optional<TivoStatusProvider> tivoConnection = Optional.empty();
    private @Nullable ScheduledFuture<?> refreshJob;

    /**
     * Instantiates a new TiVo handler.
     *
     * @param thing the thing
     */
    public TiVoHandler(Thing thing) {
        super(thing);
        logger.debug("TiVoHandler '{}' - creating", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Handles the commands from the various TiVo channel objects
        logger.debug("handleCommand '{}', parameter: {}", channelUID, command);

        if (!isInitialized() || !tivoConnection.isPresent()) {
            logger.debug("handleCommand '{}' device is not initialized yet, command '{}' will be ignored.",
                    getThing().getUID(), channelUID + " " + command);
            return;
        }

        TivoStatusData currentStatus = tivoConnection.get().getServiceStatus();
        String commandKeyword = "";

        String commandParameter = command.toString().toUpperCase();
        if (command instanceof RefreshType) {
            // Future enhancement, if we can come up with a sensible set of actions when a REFRESH is issued
            logger.debug("TiVo '{}' skipping REFRESH command for channel: '{}'.", getThing().getUID(),
                    channelUID.getId());
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_TIVO_CHANNEL_FORCE:
                commandKeyword = "FORCECH";
                break;
            case CHANNEL_TIVO_CHANNEL_SET:
                commandKeyword = "SETCH";
                break;
            case CHANNEL_TIVO_TELEPORT:
                commandKeyword = "TELEPORT";
                break;
            case CHANNEL_TIVO_IRCMD:
                commandKeyword = "IRCODE";
                break;
            case CHANNEL_TIVO_KBDCMD:
                commandKeyword = "KEYBOARD";
                break;
        }
        try {
            sendCommand(commandKeyword, commandParameter, currentStatus);
        } catch (InterruptedException e) {
            // TiVo handler disposed or openHAB exiting, do nothing
        }
    }

    public void setStatusOffline() {
        lastConnectionStatus = ConnectionStatus.UNKNOWN;
        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Power on device or check network configuration/connection.");
    }

    private void sendCommand(String commandKeyword, String commandParameter, TivoStatusData currentStatus)
            throws InterruptedException {
        if (!tivoConnection.isPresent()) {
            return;
        }

        TivoStatusData deviceStatus = tivoConnection.get().getServiceStatus();
        TivoStatusData commandResult = null;
        logger.debug("handleCommand '{}' - {} found!", getThing().getUID(), commandKeyword);
        // Re-write command keyword if we are in STANDBY, as only IRCODE TIVO will wake the unit from
        // standby mode, otherwise just execute the commands
        if (deviceStatus.getConnectionStatus() == ConnectionStatus.STANDBY && commandKeyword.contentEquals("TELEPORT")
                && commandParameter.contentEquals("TIVO")) {
            String command = "IRCODE " + commandParameter;
            logger.debug("TiVo '{}' TELEPORT re-mapped to IRCODE as we are in standby: '{}'", getThing().getUID(),
                    command);
            commandResult = tivoConnection.get().cmdTivoSend(command);
        } else if (commandKeyword.contentEquals("FORCECH") || commandKeyword.contentEquals("SETCH")) {
            commandResult = chChannelChange(commandKeyword, commandParameter);
        } else {
            commandResult = tivoConnection.get().cmdTivoSend(commandKeyword + " " + commandParameter);
        }

        // Post processing
        if (commandResult != null && commandParameter.contentEquals("STANDBY")) {
            // Force thing state into STANDBY as this command does not return a status when executed
            commandResult.setConnectionStatus(ConnectionStatus.STANDBY);
        }

        // Push status updates
        if (commandResult != null && commandResult.isCmdOk()) {
            updateTivoStatus(currentStatus, commandResult);
        }

        if (!tivoConfigData.isKeepConnActive()) {
            // disconnect once command is complete
            tivoConnection.get().connTivoDisconnect();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing a TiVo '{}' with config options", getThing().getUID());

        tivoConfigData = getConfigAs(TivoConfigData.class);

        tivoConfigData.setCfgIdentifier(getThing().getUID().getAsString());
        tivoConnection = Optional.of(new TivoStatusProvider(tivoConfigData, this));

        updateStatus(ThingStatus.UNKNOWN);
        lastConnectionStatus = ConnectionStatus.UNKNOWN;
        logger.debug("Initializing a TiVo handler for thing '{}' - finished!", getThing().getUID());

        startPollStatus();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of a TiVo handler for thing '{}'", getThing().getUID());

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(false);
            this.refreshJob = null;
        }

        if (tivoConnection.isPresent()) {
            try {
                tivoConnection.get().connTivoDisconnect();
            } catch (InterruptedException e) {
                // TiVo handler disposed or openHAB exiting, do nothing
            }
            tivoConnection = Optional.empty();
        }
    }

    /**
     * {@link startPollStatus} scheduled job to poll for changes in state.
     */
    private void startPollStatus() {
        Runnable runnable = () -> {
            logger.debug("startPollStatus '{}' @ rate of '{}' seconds", getThing().getUID(),
                    tivoConfigData.getPollInterval());
            tivoConnection.ifPresent(connection -> {
                try {
                    connection.statusRefresh();
                } catch (InterruptedException e) {
                    // TiVo handler disposed or openHAB exiting, do nothing
                }
            });
        };

        if (tivoConfigData.isKeepConnActive()) {
            // Run once every 12 hours to keep the connection from going stale
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, INIT_POLLING_DELAY_S, POLLING_DELAY_12HR_S,
                    TimeUnit.SECONDS);
            logger.debug("Status collection '{}' will start in '{}' seconds.", getThing().getUID(),
                    INIT_POLLING_DELAY_S);
        } else if (tivoConfigData.doPollChanges()) {
            // Run at intervals
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, INIT_POLLING_DELAY_S,
                    tivoConfigData.getPollInterval(), TimeUnit.SECONDS);
            logger.debug("Status polling '{}' will start in '{}' seconds.", getThing().getUID(), INIT_POLLING_DELAY_S);
        } else {
            // Just update the status now
            tivoConnection.ifPresent(connection -> {
                try {
                    connection.statusRefresh();
                } catch (InterruptedException e) {
                    // TiVo handler disposed or openHAB exiting, do nothing
                }
            });
        }
    }

    /**
     * {@link chChannelChange} performs channel changing operations.
     *
     * @param commandKeyword the TiVo command object.
     * @param command the command parameter.
     * @return TivoStatusData status of the command.
     * @throws InterruptedException
     */
    private TivoStatusData chChannelChange(String commandKeyword, String command) throws InterruptedException {
        int channel = -1;
        int subChannel = -1;

        TivoStatusData tmpStatus = tivoConnection.get().getServiceStatus();
        try {
            // Parse the channel number and if there is a decimal, the sub-channel number (OTA channels)
            Matcher matcher = NUMERIC_PATTERN.matcher(command);
            if (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    channel = Integer.parseInt(matcher.group(1).trim());
                }
                if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                    subChannel = Integer.parseInt(matcher.group(2).trim());
                }
            } else {
                // The command string was not a number, throw exception to catch & log below
                throw new NumberFormatException();
            }

            String tmpCommand = commandKeyword + " " + channel + ((subChannel != -1) ? (" " + subChannel) : "");
            logger.debug("chChannelChange '{}' sending command to tivo: '{}'", getThing().getUID(), tmpCommand);

            // Attempt to execute the command on the tivo
            tivoConnection.get().cmdTivoSend(tmpCommand);
            TimeUnit.MILLISECONDS.sleep(tivoConfigData.getCmdWaitInterval() * 2);

            tmpStatus = tivoConnection.get().getServiceStatus();

            // Check to see if the command was successful
            if (tmpStatus.getConnectionStatus() != ConnectionStatus.INIT && tmpStatus.isCmdOk()) {
                if (tmpStatus.getMsg().contains("CH_STATUS")) {
                    return tmpStatus;
                }
            } else if (tmpStatus.getConnectionStatus() != ConnectionStatus.INIT) {
                logger.warn("TiVo'{}' set channel command failed '{}' with msg '{}'", getThing().getUID(), tmpCommand,
                        tmpStatus.getMsg());
                switch (tmpStatus.getMsg()) {
                    case "CH_FAILED NO_LIVE":
                        tmpStatus.setChannelNum(channel);
                        tmpStatus.setSubChannelNum(subChannel);
                        return tmpStatus;
                    case "CH_FAILED RECORDING":
                    case "CH_FAILED MISSING_CHANNEL":
                    case "CH_FAILED MALFORMED_CHANNEL":
                    case "CH_FAILED INVALID_CHANNEL":
                        return tmpStatus;
                    case "NO_STATUS_DATA_RETURNED":
                        tmpStatus.setChannelNum(-1);
                        tmpStatus.setSubChannelNum(-1);
                        tmpStatus.setRecording(false);
                        return tmpStatus;
                }
            }

        } catch (NumberFormatException e) {
            logger.warn("TiVo'{}' unable to parse channel integer, value sent was: '{}'", getThing().getUID(),
                    command.toString());
        }
        return tmpStatus;
    }

    /**
     * {@link updateTivoStatus} populates the items with the status / channel information.
     *
     * @param tivoStatusData the {@link TivoStatusData}
     */
    public void updateTivoStatus(TivoStatusData oldStatusData, TivoStatusData newStatusData) {
        if (newStatusData.getConnectionStatus() != ConnectionStatus.INIT) {
            // Update Item Status
            if (newStatusData.getPubToUI()) {
                if (oldStatusData.getConnectionStatus() == ConnectionStatus.INIT
                        || !(oldStatusData.getMsg().contentEquals(newStatusData.getMsg()))) {
                    updateState(CHANNEL_TIVO_STATUS, new StringType(newStatusData.getMsg()));
                }
                // If the cmd was successful, publish the channel numbers
                if (newStatusData.isCmdOk() && newStatusData.getChannelNum() != -1) {
                    if (oldStatusData.getConnectionStatus() == ConnectionStatus.INIT
                            || oldStatusData.getChannelNum() != newStatusData.getChannelNum()
                            || oldStatusData.getSubChannelNum() != newStatusData.getSubChannelNum()) {
                        if (newStatusData.getSubChannelNum() == -1) {
                            updateState(CHANNEL_TIVO_CHANNEL_FORCE, new DecimalType(newStatusData.getChannelNum()));
                            updateState(CHANNEL_TIVO_CHANNEL_SET, new DecimalType(newStatusData.getChannelNum()));
                        } else {
                            updateState(CHANNEL_TIVO_CHANNEL_FORCE, new DecimalType(
                                    newStatusData.getChannelNum() + "." + newStatusData.getSubChannelNum()));
                            updateState(CHANNEL_TIVO_CHANNEL_SET, new DecimalType(
                                    newStatusData.getChannelNum() + "." + newStatusData.getSubChannelNum()));
                        }
                    }
                    updateState(CHANNEL_TIVO_IS_RECORDING, newStatusData.isRecording() ? OnOffType.ON : OnOffType.OFF);
                }

                // Now set the pubToUI flag to false, as we have already published this status
                if (isLinked(CHANNEL_TIVO_STATUS) || isLinked(CHANNEL_TIVO_CHANNEL_FORCE)
                        || isLinked(CHANNEL_TIVO_CHANNEL_SET)) {
                    newStatusData.setPubToUI(false);
                    tivoConnection.get().setServiceStatus(newStatusData);
                }
            }

            // Update Thing status
            if (newStatusData.getConnectionStatus() != lastConnectionStatus) {
                switch (newStatusData.getConnectionStatus()) {
                    case OFFLINE:
                        this.setStatusOffline();
                        break;
                    case ONLINE:
                        updateStatus(ThingStatus.ONLINE);
                        break;
                    case STANDBY:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                "STANDBY MODE: Send command TIVO to Remote Control Button (IRCODE) item to wakeup.");
                        break;
                    case UNKNOWN:
                        updateStatus(ThingStatus.OFFLINE);
                        break;
                    case INIT:
                        break;
                }
                lastConnectionStatus = newStatusData.getConnectionStatus();
            }
        }
    }
}
