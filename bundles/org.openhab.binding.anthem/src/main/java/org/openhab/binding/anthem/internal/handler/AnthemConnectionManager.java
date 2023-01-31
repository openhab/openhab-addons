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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anthem.internal.AnthemConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AnthemConnectionManager} is responsible for handling the connection
 * to the Anthem AV processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(AnthemConnectionManager.class);

    private static final long POLLING_INTERVAL_SECONDS = 900L;
    private static final long POLLING_DELAY_SECONDS = 10L;

    private ScheduledExecutorService scheduler;

    private final AnthemHandler handler;
    private AnthemConfiguration configuration;

    private @Nullable Socket socket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private AnthemCommandParser messageParser;

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

    public AnthemConnectionManager(AnthemHandler handler, AnthemConfiguration configuration) {
        messageParser = new AnthemCommandParser(this);
        this.configuration = configuration;
        this.handler = handler;
        this.scheduler = handler.getScheduler();
        initalize();
    }

    public void dispose() {
        Future<?> localAsyncInitializeTask = this.asyncInitializeTask;
        if (localAsyncInitializeTask != null && !localAsyncInitializeTask.isDone()) {
            localAsyncInitializeTask.cancel(true);
            this.asyncInitializeTask = null;
        }
        disconnect();
    }

    public void setModel(String model) {
        handler.setThingProperty("Model", model);
    }

    public void setRegion(String region) {
        handler.setThingProperty("Region", region);
    }

    public void setSoftwareVersion(String version) {
        handler.setThingProperty("Software Version", version);
    }

    public void setSoftwareBuildDate(String date) {
        handler.setThingProperty("Software Build Date", date);
    }

    public void setHardwareVersion(String version) {
        handler.setThingProperty("Hardware Version", version);
    }

    public void setMacAddress(String mac) {
        handler.setThingProperty("Mac Address", mac);
    }

    public void setNumAvailableInputs(int numInputs) {
        // Request the names for all the inputs
        for (int input = 1; input <= numInputs; input++) {
            sendCommand(AnthemCommand.queryInputShortName(input));
            sendCommand(AnthemCommand.queryInputLongName(input));
        }
        handler.setThingProperty("Number of Inputs", String.valueOf(numInputs));
    }

    public void updateChannelState(String zone, String channelId, State state) {
        handler.updateChannelState(zone, channelId, state);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command {} received for channel {}", command, channelUID.getId().toString());
        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            return;
        }
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
            case CHANNEL_MUTE_TOGGLE:
                if (command instanceof OnOffType && command == OnOffType.ON) {
                    sendCommand(AnthemCommand.muteToggle(zone));
                }
                break;
            case CHANNEL_ACTIVE_INPUT:
                if (command instanceof DecimalType) {
                    sendCommand(AnthemCommand.activeInput(zone, ((DecimalType) command).intValue()));
                }
                break;
            default:
                logger.debug("Received command '{}' for unhandled channel '{}'", command, channelUID.getId());
                break;
        }
    }

    public void checkPowerStatusChange(String zone, String power) {
        // Zone 1
        if (Zone.MAIN.equals(Zone.fromValue(zone))) {
            boolean newZone1PowerState = "1".equals(power) ? true : false;
            if (!zone1PreviousPowerState && newZone1PowerState) {
                scheduler.submit(() -> queryAdditionalInformation(Zone.MAIN));
            }
            zone1PreviousPowerState = newZone1PowerState;
        }
        // Zone 2
        else if (Zone.ZONE2.equals(Zone.fromValue(zone))) {
            boolean newZone2PowerState = "1".equals(power) ? true : false;
            if (!zone2PreviousPowerState && newZone2PowerState) {
                scheduler.submit(() -> queryAdditionalInformation(Zone.ZONE2));
            }
            zone2PreviousPowerState = newZone2PowerState;
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

    private void sendCommand(@Nullable AnthemCommand command) {
        if (command != null) {
            logger.debug("Adding command to queue: {}", command);
            sendQueue.add(command);
        }
    }

    private void initalize() {
        reconnectIntervalMinutes = configuration.reconnectIntervalMinutes;
        commandDelayMsec = configuration.commandDelayMsec;
        asyncInitializeTask = scheduler.submit(this::connect);
    }

    private synchronized void connect() {
        handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Initializing");
        logger.debug("Opening connection to Anthem host {} on port {}", configuration.host, configuration.port);
        try {
            Socket socket = new Socket(configuration.host, configuration.port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            this.socket = socket;
        } catch (UnknownHostException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
            return;
        } catch (IllegalArgumentException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid port number");
            return;
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while establishing Anthem connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error opening Anthem connection. Check log.");
            logger.info("Error opening Anthem connection: {}", e.getMessage());
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

        handler.updateThingStatus(ThingStatus.ONLINE);

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
        if (localPollingJob != null && !localPollingJob.isDone()) {
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
        handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "reconnecting");
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
                    handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Interrupted");
                    break;
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
                    messageParser.parseMessage(command);
                    sbReader.setLength(0);
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from socket: {}", e.getMessage());
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "I/O Error");
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Runtime exception");
        } finally {
            logger.debug("Reader thread exiting");
        }
    }
}
