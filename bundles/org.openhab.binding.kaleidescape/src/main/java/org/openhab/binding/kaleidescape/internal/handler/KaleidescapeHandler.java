/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.kaleidescape.internal.handler;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.openhab.binding.kaleidescape.internal.KaleidescapeThingActions;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeDefaultConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeIpConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeMessageEvent;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeMessageEventListener;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeSerialConnector;
import org.openhab.binding.kaleidescape.internal.configuration.KaleidescapeThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KaleidescapeHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * Based on the Rotel binding by Laurent Garnier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeHandler extends BaseThingHandler implements KaleidescapeMessageEventListener {
    private static final long RECON_POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(60);
    private static final long POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(20);
    private static final long SLEEP_BETWEEN_CMD = TimeUnit.MILLISECONDS.toMillis(100);

    private final Logger logger = LoggerFactory.getLogger(KaleidescapeHandler.class);

    protected final Unit<Time> apiSecondUnit = SmartHomeUnits.SECOND;
    protected int metaRuntimeMultiple = 1;
    protected String friendlyName = "";

    protected boolean volumeEnabled = false;
    protected int volume = 0;
    protected boolean isMuted = false;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable KaleidescapeThingConfiguration config;

    private SerialPortManager serialPortManager;

    protected KaleidescapeConnector connector = new KaleidescapeDefaultConnector();

    private long lastPollingUpdate = System.currentTimeMillis();

    private Object sequenceLock = new Object();

    protected final HttpClient httpClient;

    public KaleidescapeHandler(Thing thing, SerialPortManager serialPortManager, HttpClient httpClient) {
        super(thing);
        this.serialPortManager = serialPortManager;
        this.httpClient = httpClient;
    }

    public void updateChannel(String channelUID, State state) {
        this.updateState(channelUID, state);
    }

    public void updateDetailChannel(String channelUID, State state) {
        this.updateState(DETAIL + channelUID, state);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        this.config = getConfigAs(KaleidescapeThingConfiguration.class);

        // Check configuration settings
        String configError = null;
        if ((config.serialPort == null || config.serialPort.isEmpty())
                && (config.host == null || config.host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (config.host == null || config.host.isEmpty()) {
            if (config.serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (config.port == null) {
                configError = "undefined port configuration setting";
            } else if (config.port <= 0) {
                configError = "invalid port configuration setting";
            }
        }

        List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

        // check if volume is enabled, if not remove the volume & mute channels
        if (config.volumeEnabled) {
            this.volumeEnabled = true;
            this.volume = config.initialVolume;
            this.updateState(KaleidescapeBindingConstants.VOLUME, new PercentType(BigDecimal.valueOf(this.volume)));
        } else {
            channels.removeIf(c -> (c.getUID().getId().equals(VOLUME)));
            channels.removeIf(c -> (c.getUID().getId().equals(MUTE)));
        }

        // remove music channels if we are not a Premiere Player or Cinema One
        if (!(PLAYER.equals(config.componentType) || CINEMA_ONE.equals(config.componentType))) {
            channels.removeIf(c -> (c.getUID().getId().contains(MUSIC)));
            channels.removeIf(c -> (c.getUID().getId().equals(DETAIL + DETAIL_ALBUM_TITLE)));
            channels.removeIf(c -> (c.getUID().getId().equals(DETAIL + DETAIL_ARTIST)));
            channels.removeIf(c -> (c.getUID().getId().equals(DETAIL + DETAIL_REVIEW)));
        }

        // premiere players do not support SYSTEM_READINESS_STATE
        if (PLAYER.equals(config.componentType)) {
            channels.removeIf(c -> (c.getUID().getId().equals(SYSTEM_READINESS_STATE)));
        }

        // remove VIDEO_COLOR and CONTENT_COLOR if not a Strato
        if (!STRATO.equals(config.componentType)) {
            channels.removeIf(c -> (c.getUID().getId().equals(VIDEO_COLOR)));
            channels.removeIf(c -> (c.getUID().getId().equals(VIDEO_COLOR_EOTF)));
            channels.removeIf(c -> (c.getUID().getId().equals(CONTENT_COLOR)));
            channels.removeIf(c -> (c.getUID().getId().equals(CONTENT_COLOR_EOTF)));
        }

        updateThing(editThing().withChannels(channels).build());

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
        } else {
            if (config.serialPort != null) {
                connector = new KaleidescapeSerialConnector(serialPortManager, config.serialPort);
            } else {
                connector = new KaleidescapeIpConnector(config.host, config.port);
            }

            updateStatus(ThingStatus.UNKNOWN);

            scheduleReconnectJob();
            schedulePollingJob();
        }
    }

    @Override
    public void dispose() {
        cancelReconnectJob();
        cancelPollingJob();
        closeConnection();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(KaleidescapeThingActions.class);
    }

    public void handleRawCommand(@Nullable String command) {
        synchronized (sequenceLock) {
            try {
                connector.sendCommand(command);
            } catch (KaleidescapeException e) {
                logger.warn("K Command: {} failed", command);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }
        synchronized (sequenceLock) {
            if (!connector.isConnected()) {
                logger.debug("Command {} from channel {} is ignored: connection not established", command, channel);
                return;
            }

            boolean success = true;

            try {
                switch (channel) {
                    case POWER:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand("LEAVE_STANDBY");
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand("ENTER_STANDBY");
                        }
                        break;
                    case VOLUME:
                        if (command instanceof PercentType) {
                            this.volume = (int) ((PercentType) command).doubleValue();
                            logger.debug("Got volume command {}", this.volume);
                            connector.sendCommand("SEND_EVENT:VOLUME_LEVEL=" + this.volume);
                        }
                        break;
                    case MUTE:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            this.isMuted = true;
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            this.isMuted = false;
                        }
                        connector.sendCommand("SEND_EVENT:MUTE_" + (this.isMuted ? "ON" : "OFF") + "_FB");
                        break;
                    case MUSIC_REPEAT:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand("MUSIC_REPEAT_ON");
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand("MUSIC_REPEAT_OFF");
                        }
                        break;
                    case MUSIC_RANDOM:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand("MUSIC_RANDOM_ON");
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand("MUSIC_RANDOM_OFF");
                        }
                        break;
                    case CONTROL:
                    case MUSIC_CONTROL:
                        handleControlCommand(command);
                        break;
                    default:
                        success = false;
                        logger.warn("Command {} from channel {} failed: unexpected command", command, channel);
                        break;
                }

                if (success) {
                    logger.debug("Command {} from channel {} succeeded", command, channel);
                }
            } catch (KaleidescapeException e) {
                logger.warn("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Sending command failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }
    }

    /**
     * Open the connection with the Kaleidescape component
     *
     * @return true if the connection is opened successfully or false if not
     */
    private synchronized boolean openConnection() {
        connector.addEventListener(this);
        try {
            connector.open();
        } catch (KaleidescapeException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the Kaleidescape component
     */
    private synchronized void closeConnection() {
        if (connector.isConnected()) {
            connector.close();
            connector.removeEventListener(this);
            logger.debug("closeConnection(): disconnected");
        }
    }

    @Override
    public void onNewMessageEvent(EventObject event) {
        KaleidescapeMessageEvent evt = (KaleidescapeMessageEvent) event;
        lastPollingUpdate = System.currentTimeMillis();

        // check if we are in standby
        if (KaleidescapeConnector.STANDBY_MSG.equals(evt.getKey())) {
            if (!ThingStatusDetail.BRIDGE_OFFLINE.equals(thing.getStatusInfo().getStatusDetail())) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.BRIDGE_OFFLINE, KaleidescapeConnector.STANDBY_MSG);
            }
            return;
        } else {
            try {
                // Use the Enum valueOf to handle the message based on the event key. Otherwise there would be a huge
                // case statement here
                KaleidescapeMessageHandler.valueOf(evt.getKey()).handleMessage(evt.getValue(), this);

                if (ThingStatusDetail.BRIDGE_OFFLINE.equals(thing.getStatusInfo().getStatusDetail())) {
                    // no longer in standby, update the status
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.friendlyName);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Unhandled message: key {} = {}", evt.getKey(), evt.getValue());
            }
        }
    }

    /**
     * Schedule the reconnection job
     */
    @SuppressWarnings("null")
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                if (!connector.isConnected()) {
                    logger.debug("Trying to reconnect...");
                    closeConnection();
                    String error = null;
                    if (openConnection()) {
                        try {
                            long prevUpdateTime = lastPollingUpdate;

                            ArrayList<String> initialCommands = new ArrayList<String>(
                                    Arrays.asList("GET_DEVICE_TYPE_NAME", "GET_FRIENDLY_NAME", "GET_DEVICE_INFO",
                                            "GET_SYSTEM_VERSION", "GET_DEVICE_POWER_STATE", "GET_CINEMASCAPE_MASK",
                                            "GET_CINEMASCAPE_MODE", "GET_SCALE_MODE", "GET_SCREEN_MASK",
                                            "GET_SCREEN_MASK2", "GET_VIDEO_MODE", "GET_UI_STATE",
                                            "GET_HIGHLIGHTED_SELECTION", "GET_CHILD_MODE_STATE", "GET_MOVIE_LOCATION",
                                            "GET_MOVIE_MEDIA_TYPE", "GET_PLAYING_TITLE_NAME"));

                            // Premiere Players and Cinema One support music
                            if (PLAYER.equals(config.componentType) || CINEMA_ONE.equals(config.componentType)) {
                                initialCommands
                                        .addAll(new ArrayList<String>(Arrays.asList("GET_MUSIC_NOW_PLAYING_STATUS",
                                                "GET_MUSIC_PLAY_STATUS", "GET_MUSIC_TITLE")));
                            }

                            // everything after Premiere Player supports GET_SYSTEM_READINESS_STATE
                            if (!PLAYER.equals(config.componentType)) {
                                initialCommands.add("GET_SYSTEM_READINESS_STATE");
                            }

                            // only Strato supports the GET_*_COLOR commands
                            if (STRATO.equals(config.componentType)) {
                                initialCommands.addAll(
                                        new ArrayList<String>(Arrays.asList("GET_VIDEO_COLOR", "GET_CONTENT_COLOR")));
                            }

                            initialCommands.forEach(command -> {
                                try {
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                    connector.sendCommand(command);
                                } catch (InterruptedException | KaleidescapeException e) {
                                    logger.debug("{}: {}", "Error sending initial commands", e.getMessage());
                                }
                            });

                            if (config.updatePeriod == 1) {
                                Thread.sleep(SLEEP_BETWEEN_CMD);
                                connector.sendCommand("SET_STATUS_CUE_PERIOD:1");
                            }

                            // prevUpdateTime should have changed if a response was received
                            if (lastPollingUpdate == prevUpdateTime) {
                                error = "Component not responding to status requests";
                            }

                        } catch (InterruptedException | KaleidescapeException e) {
                            error = "First command after connection failed";
                            logger.debug("{}: {}", error, e.getMessage());
                            closeConnection();
                        }
                    } else {
                        error = "Reconnection failed";
                    }
                    if (error != null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                    } else {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.friendlyName);
                        lastPollingUpdate = System.currentTimeMillis();
                    }
                }
            }
        }, 1, RECON_POLLING_INTERVAL, TimeUnit.SECONDS);
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

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                if (connector.isConnected()) {
                    logger.debug("Polling the component for updated status...");
                    try {
                        connector.ping();

                    } catch (KaleidescapeException e) {
                        logger.debug("Polling error: {}", e.getMessage());
                    }

                    // if the last successful polling update was more than 1.25 intervals ago,
                    // the component is not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastPollingUpdate) > (POLLING_INTERVAL * 1.25 * 1000)) {
                        logger.warn("Component not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Component not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    }
                }
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL, TimeUnit.SECONDS);
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

    private void handleControlCommand(Command command) throws KaleidescapeException {
        if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                connector.sendCommand("PLAY");
            } else if (command == PlayPauseType.PAUSE) {
                connector.sendCommand("PAUSE");
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connector.sendCommand("NEXT");
            } else if (command == NextPreviousType.PREVIOUS) {
                connector.sendCommand("PREVIOUS");
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                connector.sendCommand("SCAN_FORWARD");
            } else if (command == RewindFastforwardType.REWIND) {
                connector.sendCommand("SCAN_REVERSE");
            }
        } else {
            logger.warn("Unknown control command: {}", command);
        }
    }
}
