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
package org.openhab.binding.oppo.internal.handler;

import static org.openhab.binding.oppo.internal.OppoBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.oppo.internal.OppoException;
import org.openhab.binding.oppo.internal.OppoStateDescriptionOptionProvider;
import org.openhab.binding.oppo.internal.communication.OppoCommand;
import org.openhab.binding.oppo.internal.communication.OppoConnector;
import org.openhab.binding.oppo.internal.communication.OppoDefaultConnector;
import org.openhab.binding.oppo.internal.communication.OppoIpConnector;
import org.openhab.binding.oppo.internal.communication.OppoMessageEvent;
import org.openhab.binding.oppo.internal.communication.OppoMessageEventListener;
import org.openhab.binding.oppo.internal.communication.OppoSerialConnector;
import org.openhab.binding.oppo.internal.communication.OppoStatusCodes;
import org.openhab.binding.oppo.internal.configuration.OppoThingConfiguration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OppoHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * Based on the Rotel binding by Laurent Garnier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class OppoHandler extends BaseThingHandler implements OppoMessageEventListener {
    private static final long RECON_POLLING_INTERVAL_SEC = 60;
    private static final long POLLING_INTERVAL_SEC = 10;
    private static final long INITIAL_POLLING_DELAY_SEC = 5;
    private static final long SLEEP_BETWEEN_CMD_MS = 100;

    private static final Pattern TIME_CODE_PATTERN = Pattern
            .compile("^(\\d{3}) (\\d{3}) ([A-Z]{1}) (\\d{2}:\\d{2}:\\d{2})$");

    private final Logger logger = LoggerFactory.getLogger(OppoHandler.class);

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private OppoStateDescriptionOptionProvider stateDescriptionProvider;
    private SerialPortManager serialPortManager;
    private OppoConnector connector = new OppoDefaultConnector();

    private List<StateOption> inputSourceOptions = new ArrayList<>();
    private List<StateOption> hdmiModeOptions = new ArrayList<>();

    private long lastEventReceived = System.currentTimeMillis();
    private String verboseMode = VERBOSE_2;
    private String currentChapter = BLANK;
    private String currentTimeMode = T;
    private String currentPlayMode = BLANK;
    private String currentDiscType = BLANK;
    private boolean isPowerOn = false;
    private boolean isUDP20X = false;
    private boolean isBdpIP = false;
    private boolean isVbModeSet = false;
    private boolean isInitialQuery = false;
    private Object sequenceLock = new Object();

    /**
     * Constructor
     */
    public OppoHandler(Thing thing, OppoStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        OppoThingConfiguration config = getConfigAs(OppoThingConfiguration.class);
        final String uid = this.getThing().getUID().getAsString();

        // Check configuration settings
        String configError = null;
        boolean override = false;

        Integer model = config.model;
        String serialPort = config.serialPort;
        String host = config.host;
        Integer port = config.port;

        if (model == null) {
            configError = "player model must be specified";
            return;
        }

        if ((serialPort == null || serialPort.isEmpty()) && (host == null || host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (serialPort != null && (host == null || host.isEmpty())) {
            if (serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (port == null) {
                if (model == MODEL83) {
                    port = BDP83_PORT;
                    override = true;
                    this.isBdpIP = true;
                } else if (model == MODEL103 || model == MODEL105) {
                    port = BDP10X_PORT;
                    override = true;
                    this.isBdpIP = true;
                } else {
                    port = BDP20X_PORT;
                }
            } else if (port <= 0) {
                configError = "invalid port configuration setting";
            }
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
            return;
        }

        if (serialPort != null) {
            connector = new OppoSerialConnector(serialPortManager, serialPort, uid);
        } else if (port != null) {
            connector = new OppoIpConnector(host, port, uid);
            connector.overrideCmdPreamble(override);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Either Serial port or Host & Port must be specifed");
            return;
        }

        if (config.verboseMode) {
            this.verboseMode = VERBOSE_3;
        }

        if (model == MODEL203 || model == MODEL205) {
            this.isUDP20X = true;
        }

        this.buildOptionDropdowns(model);
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SOURCE),
                inputSourceOptions);
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_HDMI_MODE),
                hdmiModeOptions);

        // remove channels not needed for this model
        List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

        if (model == MODEL83) {
            channels.removeIf(c -> (c.getUID().getId().equals(CHANNEL_SUB_SHIFT)
                    || c.getUID().getId().equals(CHANNEL_OSD_POSITION)));
        }

        if (model == MODEL83 || model == MODEL103 || model == MODEL105) {
            channels.removeIf(c -> (c.getUID().getId().equals(CHANNEL_ASPECT_RATIO)
                    || c.getUID().getId().equals(CHANNEL_HDR_MODE)));
        }

        // no query to determine this, so set the default value at startup
        updateChannelState(CHANNEL_TIME_MODE, currentTimeMode);

        updateThing(editThing().withChannels(channels).build());

        scheduleReconnectJob();
        schedulePollingJob();

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        cancelReconnectJob();
        cancelPollingJob();
        closeConnection();
        super.dispose();
    }

    /**
     * Handle a command the UI
     *
     * @param channelUID the channel sending the command
     * @param command the command received
     *
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }

        if (!connector.isConnected()) {
            logger.debug("Command {} from channel {} is ignored: connection not established", command, channel);
            return;
        }

        synchronized (sequenceLock) {
            try {
                String commandStr = command.toString();
                switch (channel) {
                    case CHANNEL_POWER:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(
                                    command == OnOffType.ON ? OppoCommand.POWER_ON : OppoCommand.POWER_OFF);

                            // set the power flag to false only, will be set true by QPW or UPW messages
                            if (command == OnOffType.OFF) {
                                isPowerOn = false;
                                isInitialQuery = false;
                            }
                        }
                        break;
                    case CHANNEL_VOLUME:
                        if (command instanceof PercentType) {
                            connector.sendCommand(OppoCommand.SET_VOLUME_LEVEL, commandStr);
                        }
                        break;
                    case CHANNEL_MUTE:
                        if (command instanceof OnOffType) {
                            if (command == OnOffType.ON) {
                                connector.sendCommand(OppoCommand.SET_VOLUME_LEVEL, MUTE);
                            } else {
                                connector.sendCommand(OppoCommand.MUTE);
                            }
                        }
                        break;
                    case CHANNEL_SOURCE:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            connector.sendCommand(OppoCommand.SET_INPUT_SOURCE, String.valueOf(value));
                        }
                        break;
                    case CHANNEL_CONTROL:
                        this.handleControlCommand(command);
                        break;
                    case CHANNEL_TIME_MODE:
                        if (command instanceof StringType) {
                            connector.sendCommand(OppoCommand.SET_TIME_DISPLAY, commandStr);
                            currentTimeMode = commandStr;
                        }
                        break;
                    case CHANNEL_REPEAT_MODE:
                        if (command instanceof StringType) {
                            // this one is lame, the response code when querying repeat mode is two digits,
                            // but setting it is a 2-3 letter code.
                            connector.sendCommand(OppoCommand.SET_REPEAT, OppoStatusCodes.REPEAT_MODE.get(commandStr));
                        }
                        break;
                    case CHANNEL_ZOOM_MODE:
                        if (command instanceof StringType) {
                            // again why could't they make the query code and set code the same?
                            connector.sendCommand(OppoCommand.SET_ZOOM_RATIO,
                                    OppoStatusCodes.ZOOM_MODE.get(commandStr));
                        }
                        break;
                    case CHANNEL_SUB_SHIFT:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            connector.sendCommand(OppoCommand.SET_SUBTITLE_SHIFT, String.valueOf(value));
                        }
                        break;
                    case CHANNEL_OSD_POSITION:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            connector.sendCommand(OppoCommand.SET_OSD_POSITION, String.valueOf(value));
                        }
                        break;
                    case CHANNEL_HDMI_MODE:
                        if (command instanceof StringType) {
                            connector.sendCommand(OppoCommand.SET_HDMI_MODE, commandStr);
                        }
                        break;
                    case CHANNEL_HDR_MODE:
                        if (command instanceof StringType) {
                            connector.sendCommand(OppoCommand.SET_HDR_MODE, commandStr);
                        }
                        break;
                    case CHANNEL_REMOTE_BUTTON:
                        if (command instanceof StringType) {
                            connector.sendCommand(commandStr);
                        }
                        break;
                    default:
                        logger.warn("Unknown Command {} from channel {}", command, channel);
                        break;
                }
            } catch (OppoException e) {
                logger.warn("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Sending command failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }
    }

    /**
     * Open the connection with the Oppo player
     *
     * @return true if the connection is opened successfully or false if not
     */
    private synchronized boolean openConnection() {
        connector.addEventListener(this);
        try {
            connector.open();
        } catch (OppoException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the Oppo player
     */
    private synchronized void closeConnection() {
        if (connector.isConnected()) {
            connector.close();
            connector.removeEventListener(this);
            logger.debug("closeConnection(): disconnected");
        }
    }

    /**
     * Handle an event received from the Oppo player
     *
     * @param event the event to process
     */
    @Override
    public void onNewMessageEvent(OppoMessageEvent evt) {
        logger.debug("onNewMessageEvent: key {} = {}", evt.getKey(), evt.getValue());
        lastEventReceived = System.currentTimeMillis();

        String key = evt.getKey();
        String updateData = evt.getValue().trim();
        if (this.getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        }

        synchronized (sequenceLock) {
            try {
                switch (key) {
                    case NOP: // ignore
                        break;
                    case UTC:
                        // Player sent a time code update ie: 000 000 T 00:00:01
                        // g1 = title(movie only; cd always 000), g2 = chapter(movie)/track(cd), g3 = time display code,
                        // g4 = time
                        Matcher matcher = TIME_CODE_PATTERN.matcher(updateData);
                        if (matcher.find()) {
                            // only update these when chapter/track changes to prevent spamming the channels with
                            // unnecessary updates
                            if (!currentChapter.equals(matcher.group(2))) {
                                currentChapter = matcher.group(2);
                                // for CDs this will get track 1/x also
                                connector.sendCommand(OppoCommand.QUERY_TITLE_TRACK);
                                // for movies shows chapter 1/x; always 0/0 for CDs
                                connector.sendCommand(OppoCommand.QUERY_CHAPTER);
                            }

                            if (!currentTimeMode.equals(matcher.group(3))) {
                                currentTimeMode = matcher.group(3);
                                updateChannelState(CHANNEL_TIME_MODE, currentTimeMode);
                            }
                            updateChannelState(CHANNEL_TIME_DISPLAY, matcher.group(4));
                        } else {
                            logger.debug("no match on message: {}", updateData);
                        }
                        break;
                    case QTE:
                    case QTR:
                    case QCE:
                    case QCR:
                        // these are used with verbose mode 2
                        updateChannelState(CHANNEL_TIME_DISPLAY, updateData);
                        break;
                    case QVR:
                        thing.setProperty(PROPERTY_FIRMWARE_VERSION, updateData);
                        break;
                    case QPW:
                        updateChannelState(CHANNEL_POWER, updateData);
                        if (OFF.equals(updateData)) {
                            currentPlayMode = BLANK;
                            isPowerOn = false;
                        } else {
                            isPowerOn = true;
                        }
                        break;
                    case UPW:
                        updateChannelState(CHANNEL_POWER, ONE.equals(updateData) ? ON : OFF);
                        if (ZERO.equals(updateData)) {
                            currentPlayMode = BLANK;
                            isPowerOn = false;
                            isInitialQuery = false;
                        } else {
                            isPowerOn = true;
                        }
                        break;
                    case QVL:
                    case UVL:
                    case VUP:
                    case VDN:
                        if (MUTE.equals(updateData) || MUT.equals(updateData)) { // query sends MUTE, update sends MUT
                            updateChannelState(CHANNEL_MUTE, ON);
                        } else if (UMT.equals(updateData)) {
                            updateChannelState(CHANNEL_MUTE, OFF);
                        } else {
                            updateChannelState(CHANNEL_VOLUME, updateData);
                            updateChannelState(CHANNEL_MUTE, OFF);
                        }
                        break;
                    case QIS:
                    case UIS:
                        // example: 0 BD-PLAYER, split off just the number
                        updateChannelState(CHANNEL_SOURCE, updateData.split(SPACE)[0]);
                        break;
                    case QTK:
                        // example: 02/10, split off both numbers
                        String[] track = updateData.split(SLASH);
                        if (track.length == 2) {
                            updateChannelState(CHANNEL_CURRENT_TITLE, track[0]);
                            updateChannelState(CHANNEL_TOTAL_TITLE, track[1]);
                        }
                        break;
                    case QCH:
                        // example: 03/03, split off the both numbers
                        String[] chapter = updateData.split(SLASH);
                        if (chapter.length == 2) {
                            updateChannelState(CHANNEL_CURRENT_CHAPTER, chapter[0]);
                            updateChannelState(CHANNEL_TOTAL_CHAPTER, chapter[1]);
                        }
                        break;
                    case UPL:
                    case QPL:
                        // try to normalize the slightly different responses between UPL and QPL
                        String playStatus = OppoStatusCodes.PLAYBACK_STATUS.get(updateData);
                        if (playStatus == null) {
                            playStatus = updateData;
                        }

                        // if playback has stopped, we have to zero out Time, Title and Track info and so on manually
                        if (NO_DISC.equals(playStatus) || LOADING.equals(playStatus) || OPEN.equals(playStatus)
                                || CLOSE.equals(playStatus) || STOP.equals(playStatus)) {
                            updateChannelState(CHANNEL_CURRENT_TITLE, ZERO);
                            updateChannelState(CHANNEL_TOTAL_TITLE, ZERO);
                            updateChannelState(CHANNEL_CURRENT_CHAPTER, ZERO);
                            updateChannelState(CHANNEL_TOTAL_CHAPTER, ZERO);
                            updateChannelState(CHANNEL_TIME_DISPLAY, UNDEF);
                            updateChannelState(CHANNEL_AUDIO_TYPE, UNDEF);
                            updateChannelState(CHANNEL_SUBTITLE_TYPE, UNDEF);
                        }
                        updateChannelState(CHANNEL_PLAY_MODE, playStatus);

                        // ejecting the disc does not produce a UDT message, so clear disc type manually
                        if (OPEN.equals(playStatus) || NO_DISC.equals(playStatus)) {
                            updateChannelState(CHANNEL_DISC_TYPE, UNKNOW_DISC);
                            currentDiscType = BLANK;
                        }

                        // if switching to play mode and not a CD then query the subtitle type...
                        // because if subtitles were on when playback stopped, they got nulled out above
                        // and the subtitle update message ("UST") is not sent when play starts like it is for audio
                        if (PLAY.equals(playStatus) && !CDDA.equals(currentDiscType)) {
                            connector.sendCommand(OppoCommand.QUERY_SUBTITLE_TYPE);
                        }
                        currentPlayMode = playStatus;
                        break;
                    case QRP:
                        updateChannelState(CHANNEL_REPEAT_MODE, updateData);
                        break;
                    case QZM:
                        updateChannelState(CHANNEL_ZOOM_MODE, updateData);
                        break;
                    case UDT:
                    case QDT:
                        // try to normalize the slightly different responses between UDT and QDT
                        final String discType = OppoStatusCodes.DISC_TYPE.get(updateData);
                        currentDiscType = (discType != null ? discType : updateData);
                        updateChannelState(CHANNEL_DISC_TYPE, currentDiscType);
                        break;
                    case UAT:
                        // we got the audio type status update, throw it away
                        // and call the query because the text output is better
                        // wait before sending the command to give the player time to catch up
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        connector.sendCommand(OppoCommand.QUERY_AUDIO_TYPE);
                        break;
                    case QAT:
                        updateChannelState(CHANNEL_AUDIO_TYPE, updateData);
                        break;
                    case UST:
                        // we got the subtitle type status update, throw it away
                        // and call the query because the text output is better
                        // wait before sending the command to give the player time to catch up
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        connector.sendCommand(OppoCommand.QUERY_SUBTITLE_TYPE);
                        break;
                    case QST:
                        updateChannelState(CHANNEL_SUBTITLE_TYPE, updateData);
                        break;
                    case UAR: // 203 & 205 only
                        updateChannelState(CHANNEL_ASPECT_RATIO, updateData);
                        break;
                    case UVO:
                        // example: _480I60 1080P60 - 1st source res, 2nd output res
                        String[] resolution = updateData.replace(UNDERSCORE, BLANK).split(SPACE);
                        if (resolution.length == 2) {
                            updateChannelState(CHANNEL_SOURCE_RESOLUTION, resolution[0]);
                            updateChannelState(CHANNEL_OUTPUT_RESOLUTION, resolution[1]);
                        }
                        break;
                    case U3D:
                        updateChannelState(CHANNEL_3D_INDICATOR, updateData);
                        break;
                    case QSH:
                        updateChannelState(CHANNEL_SUB_SHIFT, updateData);
                        break;
                    case QOP:
                        updateChannelState(CHANNEL_OSD_POSITION, updateData);
                        break;
                    case QHD:
                        if (this.isUDP20X) {
                            updateChannelState(CHANNEL_HDMI_MODE, updateData);
                        } else {
                            handleHdmiModeUpdate(updateData);
                        }
                        break;
                    case QHR: // 203 & 205 only
                        updateChannelState(CHANNEL_HDR_MODE, updateData);
                        break;
                    default:
                        logger.debug("onNewMessageEvent: unhandled key {}, value: {}", key, updateData);
                        break;
                }
            } catch (OppoException | InterruptedException e) {
                logger.debug("Exception processing event from player: {}", e.getMessage());
            }
        }
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();

        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!connector.isConnected()) {
                logger.debug("Trying to reconnect...");
                closeConnection();
                String error = null;
                synchronized (sequenceLock) {
                    if (openConnection()) {
                        try {
                            long prevUpdateTime = lastEventReceived;

                            connector.sendCommand(OppoCommand.QUERY_POWER_STATUS);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);

                            // if the player is off most of these won't really do much...
                            OppoCommand.QUERY_COMMANDS.forEach(cmd -> {
                                try {
                                    connector.sendCommand(cmd);
                                    Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                                } catch (OppoException | InterruptedException e) {
                                    logger.debug("Exception sending initial commands: {}", e.getMessage());
                                }
                            });

                            // prevUpdateTime should have changed if a message was received from the player
                            if (prevUpdateTime == lastEventReceived) {
                                error = "Player not responding to status requests";
                            }
                        } catch (OppoException | InterruptedException e) {
                            error = "First command after connection failed";
                            logger.debug("{}: {}", error, e.getMessage());
                        }
                    } else {
                        error = "Reconnection failed";
                    }
                    if (error != null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                        closeConnection();
                    } else {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                        isInitialQuery = false;
                        isVbModeSet = false;
                    }
                }
            }
        }, 1, RECON_POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the reconnection job
     */
    private void cancelReconnectJob() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
            reconnectJob.cancel(true);
            this.reconnectJob = null;
        }
    }

    /**
     * Schedule the polling job
     */
    private void schedulePollingJob() {
        logger.debug("Schedule polling job");
        cancelPollingJob();

        // when the Oppo is off, this will keep the connection (esp Serial over IP) alive and
        // detect if the connection goes down
        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (connector.isConnected()) {
                logger.debug("Polling the player for updated status...");

                synchronized (sequenceLock) {
                    try {
                        // Verbose mode 2 & 3 only do once until power comes on OR always for BDP direct IP
                        if ((!isPowerOn && !isInitialQuery) || isBdpIP) {
                            connector.sendCommand(OppoCommand.QUERY_POWER_STATUS);
                        }

                        if (isPowerOn) {
                            // the verbose mode must be set while the player is on
                            if (!isVbModeSet && !isBdpIP) {
                                connector.sendCommand(OppoCommand.SET_VERBOSE_MODE, this.verboseMode);
                                isVbModeSet = true;
                                Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            }

                            // Verbose mode 2 & 3 only do once OR always for BDP direct IP
                            if (!isInitialQuery || isBdpIP) {
                                isInitialQuery = true;
                                OppoCommand.QUERY_COMMANDS.forEach(cmd -> {
                                    try {
                                        connector.sendCommand(cmd);
                                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                                    } catch (OppoException | InterruptedException e) {
                                        logger.debug("Exception sending polling commands: {}", e.getMessage());
                                    }
                                });
                            }

                            // for Verbose mode 2 get the current play back time if we are playing, otherwise just do
                            // NO_OP
                            if ((VERBOSE_2.equals(this.verboseMode) && PLAY.equals(currentPlayMode)) || isBdpIP) {
                                switch (currentTimeMode) {
                                    case T:
                                        connector.sendCommand(OppoCommand.QUERY_TITLE_ELAPSED);
                                        break;
                                    case X:
                                        connector.sendCommand(OppoCommand.QUERY_TITLE_REMAIN);
                                        break;
                                    case C:
                                        connector.sendCommand(OppoCommand.QUERY_CHAPTER_ELAPSED);
                                        break;
                                    case K:
                                        connector.sendCommand(OppoCommand.QUERY_CHAPTER_REMAIN);
                                        break;
                                }
                                Thread.sleep(SLEEP_BETWEEN_CMD_MS);

                                // make queries to refresh total number of titles/tracks & chapters
                                connector.sendCommand(OppoCommand.QUERY_TITLE_TRACK);
                                Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                                connector.sendCommand(OppoCommand.QUERY_CHAPTER);
                            } else if (!isBdpIP) {
                                // verbose mode 3
                                connector.sendCommand(OppoCommand.NO_OP);
                            }
                        }

                    } catch (OppoException | InterruptedException e) {
                        logger.warn("Polling error: {}", e.getMessage());
                    }

                    // if the last event received was more than 1.25 intervals ago,
                    // the player is not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastEventReceived) > (POLLING_INTERVAL_SEC * 1.25 * 1000)) {
                        logger.debug("Player not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Player not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    }
                }
            }
        }, INITIAL_POLLING_DELAY_SEC, POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    /**
     * Update the state of a channel
     *
     * @param channel the channel
     * @param value the value to be updated
     */
    private void updateChannelState(String channel, String value) {
        if (!isLinked(channel)) {
            return;
        }

        if (UNDEF.equals(value)) {
            updateState(channel, UnDefType.UNDEF);
            return;
        }

        State state = UnDefType.UNDEF;

        switch (channel) {
            case CHANNEL_TIME_DISPLAY:
                String[] timeArr = value.split(COLON);
                if (timeArr.length == 3) {
                    int seconds = (Integer.parseInt(timeArr[0]) * 3600) + (Integer.parseInt(timeArr[1]) * 60)
                            + Integer.parseInt(timeArr[2]);
                    state = new QuantityType<>(seconds, Units.SECOND);
                } else {
                    state = UnDefType.UNDEF;
                }
                break;
            case CHANNEL_POWER:
            case CHANNEL_MUTE:
                state = ON.equals(value) ? OnOffType.ON : OnOffType.OFF;
                break;
            case CHANNEL_SOURCE:
            case CHANNEL_SUB_SHIFT:
            case CHANNEL_OSD_POSITION:
            case CHANNEL_CURRENT_TITLE:
            case CHANNEL_TOTAL_TITLE:
            case CHANNEL_CURRENT_CHAPTER:
            case CHANNEL_TOTAL_CHAPTER:
                state = new DecimalType(value);
                break;
            case CHANNEL_VOLUME:
                state = new PercentType(BigDecimal.valueOf(Integer.parseInt(value)));
                break;
            case CHANNEL_PLAY_MODE:
            case CHANNEL_TIME_MODE:
            case CHANNEL_REPEAT_MODE:
            case CHANNEL_ZOOM_MODE:
            case CHANNEL_DISC_TYPE:
            case CHANNEL_AUDIO_TYPE:
            case CHANNEL_SUBTITLE_TYPE:
            case CHANNEL_ASPECT_RATIO:
            case CHANNEL_SOURCE_RESOLUTION:
            case CHANNEL_OUTPUT_RESOLUTION:
            case CHANNEL_3D_INDICATOR:
            case CHANNEL_HDMI_MODE:
            case CHANNEL_HDR_MODE:
                state = new StringType(value);
                break;
            default:
                break;
        }
        updateState(channel, state);
    }

    /**
     * Handle a button press from a UI Player item
     *
     * @param command the control button press command received
     */
    private void handleControlCommand(Command command) throws OppoException {
        if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                connector.sendCommand(OppoCommand.PLAY);
            } else if (command == PlayPauseType.PAUSE) {
                connector.sendCommand(OppoCommand.PAUSE);
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connector.sendCommand(OppoCommand.NEXT);
            } else if (command == NextPreviousType.PREVIOUS) {
                connector.sendCommand(OppoCommand.PREV);
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                connector.sendCommand(OppoCommand.FFORWARD);
            } else if (command == RewindFastforwardType.REWIND) {
                connector.sendCommand(OppoCommand.REWIND);
            }
        } else {
            logger.warn("Unknown control command: {}", command);
        }
    }

    private void buildOptionDropdowns(int model) {
        if (model == MODEL83 || model == MODEL103 || model == MODEL105) {
            hdmiModeOptions.add(new StateOption("AUTO", "Auto"));
            hdmiModeOptions.add(new StateOption("SRC", "Source Direct"));
            if (!(model == MODEL83)) {
                hdmiModeOptions.add(new StateOption("4K2K", "4K*2K"));
            }
            hdmiModeOptions.add(new StateOption("1080P", "1080P"));
            hdmiModeOptions.add(new StateOption("1080I", "1080I"));
            hdmiModeOptions.add(new StateOption("720P", "720P"));
            hdmiModeOptions.add(new StateOption("SDP", "480P"));
            hdmiModeOptions.add(new StateOption("SDI", "480I"));
        }

        if (model == MODEL103 || model == MODEL105) {
            inputSourceOptions.add(new StateOption("0", "Blu-Ray Player"));
            inputSourceOptions.add(new StateOption("1", "HDMI/MHL IN-Front"));
            inputSourceOptions.add(new StateOption("2", "HDMI IN-Back"));
            inputSourceOptions.add(new StateOption("3", "ARC"));

            if (model == MODEL105) {
                inputSourceOptions.add(new StateOption("4", "Optical In"));
                inputSourceOptions.add(new StateOption("5", "Coaxial In"));
                inputSourceOptions.add(new StateOption("6", "USB Audio In"));
            }
        }

        if (model == MODEL203 || model == MODEL205) {
            hdmiModeOptions.add(new StateOption("AUTO", "Auto"));
            hdmiModeOptions.add(new StateOption("SRC", "Source Direct"));
            hdmiModeOptions.add(new StateOption("UHD_AUTO", "UHD Auto"));
            hdmiModeOptions.add(new StateOption("UHD24", "UHD24"));
            hdmiModeOptions.add(new StateOption("UHD50", "UHD50"));
            hdmiModeOptions.add(new StateOption("UHD60", "UHD60"));
            hdmiModeOptions.add(new StateOption("1080P_AUTO", "1080P Auto"));
            hdmiModeOptions.add(new StateOption("1080P24", "1080P24"));
            hdmiModeOptions.add(new StateOption("1080P50", "1080P50"));
            hdmiModeOptions.add(new StateOption("1080P60", "1080P60"));
            hdmiModeOptions.add(new StateOption("1080I50", "1080I50"));
            hdmiModeOptions.add(new StateOption("1080I60", "1080I60"));
            hdmiModeOptions.add(new StateOption("720P50", "720P50"));
            hdmiModeOptions.add(new StateOption("720P60", "720P60"));
            hdmiModeOptions.add(new StateOption("576P", "567P"));
            hdmiModeOptions.add(new StateOption("576I", "567I"));
            hdmiModeOptions.add(new StateOption("480P", "480P"));
            hdmiModeOptions.add(new StateOption("480I", "480I"));

            inputSourceOptions.add(new StateOption("0", "Blu-Ray Player"));
            inputSourceOptions.add(new StateOption("1", "HDMI IN"));
            inputSourceOptions.add(new StateOption("2", "ARC"));

            if (model == MODEL205) {
                inputSourceOptions.add(new StateOption("3", "Optical In"));
                inputSourceOptions.add(new StateOption("4", "Coaxial In"));
                inputSourceOptions.add(new StateOption("5", "USB Audio In"));
            }
        }
    }

    private void handleHdmiModeUpdate(String updateData) {
        // ugly... a couple of the query hdmi mode response codes on the earlier models don't match the code to set it
        // some of this protocol is weird like that...
        if ("480I".equals(updateData)) {
            updateChannelState(CHANNEL_HDMI_MODE, "SDI");
        } else if ("480P".equals(updateData)) {
            updateChannelState(CHANNEL_HDMI_MODE, "SDP");
        } else if ("4K*2K".equals(updateData)) {
            updateChannelState(CHANNEL_HDMI_MODE, "4K2K");
        } else {
            updateChannelState(CHANNEL_HDMI_MODE, updateData);
        }
    }
}
