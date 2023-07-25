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
package org.openhab.binding.anthem.internal.handler;

import static org.openhab.binding.anthem.internal.AnthemBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anthem.internal.AnthemConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AnthemHandler} is responsible for handling commands, which are
 * sent to one of the channels. It also manages the connection to the AV processor.
 * The reader thread receives solicited and unsolicited commands from the processor.
 * The sender thread is used to send commands to the processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AnthemHandler.class);

    private static final long POLLING_INTERVAL_SECONDS = 900L;
    private static final long POLLING_DELAY_SECONDS = 10L;

    private @Nullable Socket socket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private AnthemCommandParser commandParser;

    private final BlockingQueue<AnthemCommand> sendQueue = new LinkedBlockingQueue<>();

    private @Nullable Future<?> asyncInitializeTask;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable Thread senderThread;
    private @Nullable Thread readerThread;

    private int reconnectIntervalMinutes;
    private int commandDelayMsec;

    private boolean zone1PreviousPowerState;
    private boolean zone2PreviousPowerState;

    public AnthemHandler(Thing thing) {
        super(thing);
        commandParser = new AnthemCommandParser();
    }

    @Override
    public void initialize() {
        AnthemConfiguration configuration = getConfig().as(AnthemConfiguration.class);
        logger.debug("AnthemHandler: Configuration of thing {} is {}", thing.getUID().getId(), configuration);

        if (!configuration.isValid()) {
            logger.debug("AnthemHandler: Config of thing '{}' is invalid", thing.getUID().getId());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status-detail-invalidconfig");
            return;
        }
        reconnectIntervalMinutes = configuration.reconnectIntervalMinutes;
        commandDelayMsec = configuration.commandDelayMsec;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/thing-status-detail-connecting");
        asyncInitializeTask = scheduler.submit(this::connect);
    }

    @Override
    public void dispose() {
        Future<?> localAsyncInitializeTask = this.asyncInitializeTask;
        if (localAsyncInitializeTask != null) {
            localAsyncInitializeTask.cancel(true);
            this.asyncInitializeTask = null;
        }
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command {} received for channel {}", command, channelUID.getId().toString());
        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            return;
        }

        if (CHANNEL_GROUP_GENERAL.equals(groupId)) {
            handleGeneralCommand(channelUID, command);
        } else {
            handleZoneCommand(groupId, channelUID, command);
        }
    }

    private void handleGeneralCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_COMMAND:
                if (command instanceof StringType) {
                    sendCommand(AnthemCommand.customCommand(command.toString()));
                }
                break;
            default:
                logger.debug("Received general command '{}' for unhandled channel '{}'", command, channelUID.getId());
                break;
        }
    }

    private void handleZoneCommand(String groupId, ChannelUID channelUID, Command command) {
        Zone zone = Zone.fromValue(groupId);

        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        // Power on the device
                        sendCommand(AnthemCommand.powerOn(zone));
                    } else if (command == OnOffType.OFF) {
                        sendCommand(AnthemCommand.powerOff(zone));
                    }
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof OnOffType || command instanceof IncreaseDecreaseType) {
                    if (command == OnOffType.ON || command == IncreaseDecreaseType.INCREASE) {
                        sendCommand(AnthemCommand.volumeUp(zone, 1));
                    } else if (command == OnOffType.OFF || command == IncreaseDecreaseType.DECREASE) {
                        sendCommand(AnthemCommand.volumeDown(zone, 1));
                    }
                }
                break;
            case CHANNEL_VOLUME_DB:
                if (command instanceof DecimalType) {
                    sendCommand(AnthemCommand.volume(zone, ((DecimalType) command).intValue()));
                }
                break;
            case CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        sendCommand(AnthemCommand.muteOn(zone));
                    } else if (command == OnOffType.OFF) {
                        sendCommand(AnthemCommand.muteOff(zone));
                    }
                }
                break;
            case CHANNEL_ACTIVE_INPUT:
                if (command instanceof DecimalType) {
                    sendCommand(AnthemCommand.activeInput(zone, ((DecimalType) command).intValue()));
                }
                break;
            default:
                logger.debug("Received zone command '{}' for unhandled channel '{}'", command, channelUID.getId());
                break;
        }
    }

    private void queryAdditionalInformation(Zone zone) {
        // Request information about the device
        sendCommand(AnthemCommand.queryNumAvailableInputs());
        sendCommand(AnthemCommand.queryModel());
        sendCommand(AnthemCommand.queryRegion());
        sendCommand(AnthemCommand.querySoftwareVersion());
        sendCommand(AnthemCommand.querySoftwareBuildDate());
        sendCommand(AnthemCommand.queryHardwareVersion());
        sendCommand(AnthemCommand.queryMacAddress());
        sendCommand(AnthemCommand.queryVolume(zone));
        sendCommand(AnthemCommand.queryMute(zone));
        // Give some time for the input names to populate before requesting the active input
        scheduler.schedule(() -> queryActiveInput(zone), 5L, TimeUnit.SECONDS);
    }

    private void queryActiveInput(Zone zone) {
        sendCommand(AnthemCommand.queryActiveInput(zone));
    }

    private void sendCommand(AnthemCommand command) {
        logger.debug("Adding command to queue: {}", command);
        sendQueue.add(command);
    }

    private synchronized void connect() {
        try {
            AnthemConfiguration configuration = getConfig().as(AnthemConfiguration.class);
            logger.debug("Opening connection to Anthem host {} on port {}", configuration.host, configuration.port);
            Socket socket = new Socket(configuration.host, configuration.port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            this.socket = socket;
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status-detail-unknownhost");
            return;
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status-detail-invalidport");
            return;
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while establishing Anthem connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-status-detail-openerror");
            logger.debug("Error opening Anthem connection: {}", e.getMessage());
            disconnect();
            scheduleConnectRetry(reconnectIntervalMinutes);
            return;
        }
        Thread localReaderThread = new Thread(this::readerThreadJob, "Anthem reader");
        localReaderThread.setDaemon(true);
        localReaderThread.start();
        this.readerThread = localReaderThread;

        Thread localSenderThread = new Thread(this::senderThreadJob, "Anthem sender");
        localSenderThread.setDaemon(true);
        localSenderThread.start();
        this.senderThread = localSenderThread;

        updateStatus(ThingStatus.ONLINE);

        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob == null) {
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::poll, POLLING_DELAY_SECONDS,
                    POLLING_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void poll() {
        logger.debug("Polling...");
        sendCommand(AnthemCommand.queryPower(Zone.MAIN));
        sendCommand(AnthemCommand.queryPower(Zone.ZONE2));
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from Anthem");

        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            this.pollingJob = null;
        }

        ScheduledFuture<?> localConnectRetryJob = this.connectRetryJob;
        if (localConnectRetryJob != null) {
            localConnectRetryJob.cancel(true);
            this.connectRetryJob = null;
        }

        Thread localSenderThread = this.senderThread;
        if (localSenderThread != null && localSenderThread.isAlive()) {
            localSenderThread.interrupt();
        }

        Thread localReaderThread = this.readerThread;
        if (localReaderThread != null && localReaderThread.isAlive()) {
            localReaderThread.interrupt();
        }
        Socket localSocket = this.socket;
        if (localSocket != null) {
            try {
                localSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing socket: {}", e.getMessage());
            }
            this.socket = null;
        }
        BufferedReader localReader = this.reader;
        if (localReader != null) {
            try {
                localReader.close();
            } catch (IOException e) {
                logger.debug("Error closing reader: {}", e.getMessage());
            }
            this.reader = null;
        }
        BufferedWriter localWriter = this.writer;
        if (localWriter != null) {
            try {
                localWriter.close();
            } catch (IOException e) {
                logger.debug("Error closing writer: {}", e.getMessage());
            }
            this.writer = null;
        }
    }

    private synchronized void reconnect() {
        logger.debug("Attempting to reconnect to the Anthem");
        disconnect();
        connect();
    }

    private void senderThreadJob() {
        logger.debug("Sender thread started");
        try {
            while (!Thread.currentThread().isInterrupted() && writer != null) {
                AnthemCommand command = sendQueue.take();
                logger.debug("Sender thread writing command: {}", command);
                try {
                    BufferedWriter localWriter = this.writer;
                    if (localWriter != null) {
                        localWriter.write(command.toString());
                        localWriter.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending command");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/thing-status-detail-interrupted");
                    break;
                } catch (IOException e) {
                    logger.debug("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    // Requeue the command and try to reconnect
                    sendQueue.add(command);
                    reconnect();
                    break;
                }
                // Introduce delay to throttle the send rate
                if (commandDelayMsec > 0) {
                    Thread.sleep(commandDelayMsec);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("Sender thread exiting");
        }
    }

    private void readerThreadJob() {
        logger.debug("Reader thread started");
        StringBuffer sbReader = new StringBuffer();
        try {
            char c;
            String command;
            BufferedReader localReader = this.reader;
            while (!Thread.interrupted() && localReader != null) {
                c = (char) localReader.read();
                sbReader.append(c);
                if (c == COMMAND_TERMINATION_CHAR) {
                    command = sbReader.toString();
                    logger.debug("Reader thread sending command to parser: {}", command);
                    AnthemUpdate update = commandParser.parseCommand(command);
                    if (update != null) {
                        processUpdate(update);
                    }
                    sbReader.setLength(0);
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-status-detail-interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from socket: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-status-detail-ioexception");
        } finally {
            logger.debug("Reader thread exiting");
        }
    }

    private void processUpdate(AnthemUpdate update) {
        // State update
        if (update.isStateUpdate()) {
            StateUpdate stateUpdate = update.getStateUpdate();
            updateState(stateUpdate.getGroupId() + ChannelUID.CHANNEL_GROUP_SEPARATOR + stateUpdate.getChannelId(),
                    stateUpdate.getState());
            postProcess(stateUpdate);
        }
        // Property update
        else if (update.isPropertyUpdate()) {
            PropertyUpdate propertyUpdate = update.getPropertyUpdate();
            updateProperty(propertyUpdate.getName(), propertyUpdate.getValue());
            postProcess(propertyUpdate);
        }
    }

    private void postProcess(StateUpdate stateUpdate) {
        switch (stateUpdate.getChannelId()) {
            case CHANNEL_POWER:
                checkPowerStatusChange(stateUpdate);
                break;
            case CHANNEL_ACTIVE_INPUT:
                updateInputNameChannels(stateUpdate);
                break;
        }
    }

    private void checkPowerStatusChange(StateUpdate stateUpdate) {
        String zone = stateUpdate.getGroupId();
        State power = stateUpdate.getState();
        // Zone 1
        if (Zone.MAIN.equals(Zone.fromValue(zone))) {
            boolean newZone1PowerState = (power instanceof OnOffType && power == OnOffType.ON) ? true : false;
            if (!zone1PreviousPowerState && newZone1PowerState) {
                // Power turned on for main zone.
                // This will cause the main zone channel states to be updated
                scheduler.submit(() -> queryAdditionalInformation(Zone.MAIN));
            }
            zone1PreviousPowerState = newZone1PowerState;
        }
        // Zone 2
        else if (Zone.ZONE2.equals(Zone.fromValue(zone))) {
            boolean newZone2PowerState = (power instanceof OnOffType && power == OnOffType.ON) ? true : false;
            if (!zone2PreviousPowerState && newZone2PowerState) {
                // Power turned on for zone 2.
                // This will cause zone 2 channel states to be updated
                scheduler.submit(() -> queryAdditionalInformation(Zone.ZONE2));
            }
            zone2PreviousPowerState = newZone2PowerState;
        }
    }

    private void updateInputNameChannels(StateUpdate stateUpdate) {
        State state = stateUpdate.getState();
        String groupId = stateUpdate.getGroupId();
        if (state instanceof StringType) {
            updateState(groupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ACTIVE_INPUT_SHORT_NAME,
                    new StringType(commandParser.getInputShortName(state.toString())));
            updateState(groupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ACTIVE_INPUT_LONG_NAME,
                    new StringType(commandParser.getInputLongName(state.toString())));
        }
    }

    private void postProcess(PropertyUpdate propertyUpdate) {
        switch (propertyUpdate.getName()) {
            case PROPERTY_NUM_AVAILABLE_INPUTS:
                queryAllInputNames(propertyUpdate);
                break;
        }
    }

    private void queryAllInputNames(PropertyUpdate propertyUpdate) {
        try {
            int numInputs = Integer.parseInt(propertyUpdate.getValue());
            for (int input = 1; input <= numInputs; input++) {
                sendCommand(AnthemCommand.queryInputShortName(input));
                sendCommand(AnthemCommand.queryInputLongName(input));
            }
        } catch (NumberFormatException e) {
            logger.debug("Unable to convert property '{}' to integer: {}", propertyUpdate.getName(),
                    propertyUpdate.getValue());
        }
    }
}
