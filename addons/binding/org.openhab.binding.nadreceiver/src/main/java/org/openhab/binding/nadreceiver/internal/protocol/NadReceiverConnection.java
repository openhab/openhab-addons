/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nadreceiver.internal.protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nadreceiver.internal.NadReceiverEventListener;
import org.openhab.binding.nadreceiver.internal.net.TelnetSession;
import org.openhab.binding.nadreceiver.internal.net.TelnetSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used for all connection related challenges
 *
 * @author Marc Chételat - Initial contribution
 *
 */
public class NadReceiverConnection {
    private final Logger logger = LoggerFactory.getLogger(NadReceiverConnection.class);

    private final String hostname;
    private final int port;
    private final int heartbeatInterval;
    private final int reconnectInterval;
    private final int maxSources;

    private NadReceiverEventListener listener;
    private ScheduledExecutorService scheduler;

    private TelnetSession session;
    private BlockingQueue<NadReceiverCommand> sendQueue = new LinkedBlockingQueue<>();

    private ScheduledFuture<?> messageSender;
    private ScheduledFuture<?> keepAlive;
    private ScheduledFuture<?> keepAliveReconnect;
    private ScheduledFuture<?> connectRetryJob;

    private int minVolume = -99;
    private int maxVolume = 11;

    public NadReceiverConnection(String hostname, int port, int heartbeatInterval, int reconnectInterval,
            int maxSources, NadReceiverEventListener listener, ScheduledExecutorService scheduler)
            throws URISyntaxException {
        this.hostname = hostname;
        this.port = port;
        this.heartbeatInterval = heartbeatInterval;
        this.reconnectInterval = reconnectInterval;
        this.maxSources = maxSources;

        this.listener = listener;
        this.scheduler = scheduler;

        this.session = new TelnetSession();

        logger.debug("Starting thread to read received packages...");
        this.session.addListener(new TelnetSessionListener() {
            @Override
            public void incomingMessageAvailable() {
                parseUpdates();
            }

            @Override
            public void errorHandling(IOException exception) {
            }
        });

        logger.debug("Starting thread to connect...");
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, 0, TimeUnit.SECONDS);

    }

    private void sendKeepAlive() {
        // Reconnect if no response is received within 30 seconds.
        this.keepAliveReconnect = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }, 30, TimeUnit.SECONDS);

        sendCommand(new NadReceiverCommand(NadReceiverCommandType.MainPower, NadReceiverOperation.QUERY));
    }

    private synchronized void reconnect() {
        logger.debug("Keepalive timeout, attempting to reconnect to the NAD Receiver");

        listener.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, null);
        disconnect();
        connect();
    }

    private synchronized void connect() {
        if (this.session.isConnected()) {
            return;
        }

        logger.debug("Connecting to receiver {}", hostname);

        try {
            this.session.open(hostname, port);
        } catch (IOException e) {
            listener.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
            return;
        }

        // We need to get current statuses
        requestInitialValues();

        this.messageSender = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                sendCommands();
            }
        }, 0, TimeUnit.SECONDS);

        listener.updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
                TimeUnit.MINUTES);

    }

    public synchronized void disconnect() {
        logger.debug("Disconnecting from NAD Receiver");

        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }

        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }

        if (this.keepAliveReconnect != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            this.keepAliveReconnect.cancel(false);
        }

        if (this.messageSender != null) {
            this.messageSender.cancel(true);
        }

        try {
            this.session.close();
        } catch (IOException e) {
            logger.error("Error disconnecting", e);
        }
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private void sendCommands() {
        try {
            while (true) {
                NadReceiverCommand command = this.sendQueue.take();

                logger.debug("Sending command: {}", command);

                try {
                    this.session.writeLine(command.toString());
                } catch (IOException e) {
                    logger.error("Communication error, will try to reconnect", e);
                    listener.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null);

                    // Requeue command
                    this.sendQueue.add(command);

                    reconnect();

                    // reconnect() will start a new thread; terminate this one
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parse all received updated
     */
    private void parseUpdates() {
        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            logger.debug("Received message {}", line);

            // System is alive, cancel reconnect task.
            if (this.keepAliveReconnect != null) {
                this.keepAliveReconnect.cancel(true);
            }

            // Remove everything including and after the sign equals
            String command = null;
            try {
                command = line.substring(0, line.indexOf("="));
            } catch (Exception e) {
                logger.warn("Cannot extract command from received message", e);
                return;
            }
            String value = null;
            try {
                value = line.substring(line.indexOf("=") + 1, line.length());
            } catch (Exception e) {
                logger.warn("Cannot extract value from received message", e);
                return;
            }
            String number = null;
            if (command != null && command.matches(".*\\d+.*")) {
                number = command.replaceAll("\\D+", "");
            }

            NadReceiverCommandType nadReceiverCommand = null;
            try {
                nadReceiverCommand = NadReceiverCommandType.fromString(command);
            } catch (IllegalArgumentException iae) {
                logger.warn("This command received from the receiver is not recognized: " + command);
                return;
            }

            int intValue = 0;
            boolean boolValue = false;
            switch (nadReceiverCommand) {
                case MainPower:
                    if (value.equals("On")) {
                        boolValue = true;
                    }
                    listener.updatePowerStatus(boolValue);
                    break;

                case MainModel:
                    logger.info("NAD Receiver model: " + value);
                    listener.updateModel(value);
                    break;

                case MainVolume:
                    try {
                        intValue = Integer.parseInt(value);
                    } catch (Exception e) {
                        logger.warn("Wrong Volume value: " + value);
                        return;
                    }
                    listener.updateVolume(
                            VolumeValueConverterUtil.convertVolumeFromReal2Percentage(intValue, minVolume, maxVolume));
                    break;

                case MainMute:
                    if (value.equals("On")) {
                        boolValue = true;
                    }
                    listener.updateMute(boolValue);
                    break;

                case MainSource:
                    try {
                        intValue = Integer.parseInt(value);
                    } catch (Exception e) {
                        logger.warn("Wrong source value: " + value);
                        return;
                    }
                    listener.updateSource(intValue);
                    break;

                case SourceNUMName:
                    logger.debug("We are getting a new source with number {} and name {} ", number, value);
                    listener.addOrUpdateSourceName(number, value);
                    break;

                case SourceNUMEnabled:
                    logger.debug("We are getting source info with number {} and state {} ", number, value);
                    if (value.equals("Yes")) {
                        boolValue = true;
                    }
                    listener.addOrUpdateSourceState(number, boolValue);
                    break;

                default:
                    // Valid command (but not supported) + invalid commands
                    logger.trace("Not supported command or invalid command receiver: {}", command);
                    break;
            }
        }
    }

    void sendCommand(NadReceiverCommand command) {
        this.sendQueue.add(command);
    }

    public void getPower() {
        // Answers are read through listener and not sync after sending the message
        this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.MainPower, NadReceiverOperation.QUERY));
    }

    public void setPower(boolean power) {
        if (power) {
            this.sendQueue
                    .add(new NadReceiverCommand(NadReceiverCommandType.MainPower, NadReceiverOperation.EXECUTE, "ON"));
        } else {
            this.sendQueue
                    .add(new NadReceiverCommand(NadReceiverCommandType.MainPower, NadReceiverOperation.EXECUTE, "OFF"));
        }
    }

    public void getCurrentSource() {
        // Answers are read through listener and not sync after sending the message
        this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.MainSource, NadReceiverOperation.QUERY));
    }

    public void setSource(String input) {
        this.sendQueue
                .add(new NadReceiverCommand(NadReceiverCommandType.MainSource, NadReceiverOperation.EXECUTE, input));

    }

    public void getCurrentVolume() {
        // Answers are read through listener and not sync after sending the message
        this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.MainVolume, NadReceiverOperation.QUERY));
    }

    public void setVolume(int volume) {
        // Convert volume from percentage to real DB value:
        int volumeInDb = VolumeValueConverterUtil.convertVolumeFromPercentage2Real(volume, minVolume, maxVolume);
        this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.MainVolume, NadReceiverOperation.EXECUTE,
                String.valueOf(volumeInDb)));

    }

    public void setMute(boolean mute) {
        if (mute) {
            this.sendQueue
                    .add(new NadReceiverCommand(NadReceiverCommandType.MainMute, NadReceiverOperation.EXECUTE, "ON"));
        } else {
            this.sendQueue
                    .add(new NadReceiverCommand(NadReceiverCommandType.MainMute, NadReceiverOperation.EXECUTE, "OFF"));
        }
    }

    public void getCurrentMute() {
        // Answers are read through listener and not sync after sending the message
        this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.MainMute, NadReceiverOperation.QUERY));
    }

    public void getSourcesDetails() {
        for (int i = 0; i < this.maxSources; i++) {
            this.sendQueue.add(new NadReceiverCommand(NadReceiverCommandType.SourceNUM, NadReceiverOperation.QUERY,
                    new Integer(i)));
        }
    }

    /**
     * During first connection we need to get current state of different channels. For this we are calling manually some
     * statuses. In case we are supporting more features, we'll have to extand this method or refactor.
     */
    public void requestInitialValues() {
        this.getPower();
        this.getCurrentMute();
        this.getCurrentVolume();
        this.getSourcesDetails();
        this.getCurrentSource();
    }

    /**
     *
     * Nad receivers understand dB Volume but usually openHAB handles it with dimmer channel type and percentage. For
     * that reason, this class helps to convert both approaches.
     *
     * @author Marc Chételat
     *
     */
    private static class VolumeValueConverterUtil {
        public static int convertVolumeFromPercentage2Real(int volume, int min_volume, int max_volume) {
            return (int) Math.round(((max_volume - min_volume) * volume / 100.0) + min_volume);
        }

        public static int convertVolumeFromReal2Percentage(int volume, int min_volume, int max_volume) {
            return Math.round(100 * (volume - min_volume) / (max_volume - min_volume));
        }
    }
}
