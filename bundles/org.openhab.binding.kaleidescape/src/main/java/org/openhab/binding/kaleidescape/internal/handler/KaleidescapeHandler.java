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
package org.openhab.binding.kaleidescape.internal.handler;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.openhab.binding.kaleidescape.internal.KaleidescapeThingActions;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeDefaultConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeIpConnector;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeMessageEvent;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeMessageEventListener;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeSerialConnector;
import org.openhab.binding.kaleidescape.internal.configuration.KaleidescapeThingConfiguration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    private static final long RECON_POLLING_INTERVAL_S = 60;
    private static final long POLLING_INTERVAL_S = 20;

    private final Logger logger = LoggerFactory.getLogger(KaleidescapeHandler.class);
    private final SerialPortManager serialPortManager;
    private final Map<String, String> cache = new HashMap<String, String>();

    protected final HttpClient httpClient;
    protected final Unit<Time> apiSecondUnit = Units.SECOND;

    private ThingTypeUID thingTypeUID = THING_TYPE_PLAYER;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private long lastEventReceived = 0;
    private int updatePeriod = 0;

    protected KaleidescapeConnector connector = new KaleidescapeDefaultConnector();
    protected int metaRuntimeMultiple = 1;
    protected int volume = 0;
    protected boolean volumeEnabled = false;
    protected boolean isMuted = false;
    protected boolean isLoadHighlightedDetails = false;
    protected boolean isLoadAlbumDetails = false;
    protected String friendlyName = EMPTY;
    protected Object sequenceLock = new Object();

    public KaleidescapeHandler(Thing thing, SerialPortManager serialPortManager, HttpClient httpClient) {
        super(thing);
        this.serialPortManager = serialPortManager;
        this.httpClient = httpClient;
    }

    protected void updateChannel(String channelUID, State state) {
        this.updateState(channelUID, state);
    }

    protected void updateDetailChannel(String channelUID, State state) {
        this.updateState(DETAIL + channelUID, state);
    }

    protected void updateThingProperty(String name, String value) {
        thing.setProperty(name, value);
    }

    @Override
    public void initialize() {
        final String uid = this.getThing().getUID().getAsString();
        KaleidescapeThingConfiguration config = getConfigAs(KaleidescapeThingConfiguration.class);

        this.thingTypeUID = thing.getThingTypeUID();

        // Check configuration settings
        String configError = null;
        final String serialPort = config.serialPort;
        final String host = config.host;
        final Integer port = config.port;
        final Integer updatePeriod = config.updatePeriod;
        this.isLoadHighlightedDetails = config.loadHighlightedDetails;
        this.isLoadAlbumDetails = config.loadAlbumDetails;

        if ((serialPort == null || serialPort.isEmpty()) && (host == null || host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (host == null || host.isEmpty()) {
            if (serialPort != null && serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (port == null) {
                configError = "undefined port configuration setting";
            } else if (port <= 0) {
                configError = "invalid port configuration setting";
            }
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
            return;
        }

        if (updatePeriod != null) {
            this.updatePeriod = updatePeriod;
        }

        // check if volume is enabled
        if (config.volumeEnabled) {
            this.volumeEnabled = true;
            this.volume = config.initialVolume;
            this.updateState(VOLUME, new PercentType(this.volume));
            this.updateState(MUTE, OnOffType.OFF);
        }

        if (serialPort != null) {
            connector = new KaleidescapeSerialConnector(serialPortManager, serialPort, uid);
        } else if (port != null) {
            connector = new KaleidescapeIpConnector(host, port, uid);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Either Serial port or Host & Port must be specifed");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduleReconnectJob();
        schedulePollingJob();
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

            try {
                if (command instanceof RefreshType) {
                    handleRefresh(channel);
                    return;
                }

                switch (channel) {
                    case POWER:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(command == OnOffType.ON ? LEAVE_STANDBY : ENTER_STANDBY);
                        }
                        break;
                    case VOLUME:
                        if (command instanceof PercentType) {
                            this.volume = (int) ((PercentType) command).doubleValue();
                            logger.debug("Got volume command {}", this.volume);
                            connector.sendCommand(SEND_EVENT_VOLUME_LEVEL_EQ + this.volume);
                        }
                        break;
                    case MUTE:
                        if (command instanceof OnOffType) {
                            this.isMuted = command == OnOffType.ON ? true : false;
                        }
                        connector.sendCommand(SEND_EVENT_MUTE + (this.isMuted ? MUTE_ON : MUTE_OFF));
                        break;
                    case MUSIC_REPEAT:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(command == OnOffType.ON ? MUSIC_REPEAT_ON : MUSIC_REPEAT_OFF);
                        }
                        break;
                    case MUSIC_RANDOM:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(command == OnOffType.ON ? MUSIC_RANDOM_ON : MUSIC_RANDOM_OFF);
                        }
                        break;
                    case CONTROL:
                    case MUSIC_CONTROL:
                        handleControlCommand(command);
                        break;
                    default:
                        logger.debug("Command {} from channel {} failed: unexpected command", command, channel);
                        break;
                }
            } catch (KaleidescapeException e) {
                logger.debug("Command {} from channel {} failed: {}", command, channel, e.getMessage());
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
    public void onNewMessageEvent(KaleidescapeMessageEvent evt) {
        lastEventReceived = System.currentTimeMillis();

        // check if we are in standby
        if (STANDBY_MSG.equals(evt.getKey())) {
            if (!ThingStatusDetail.BRIDGE_OFFLINE.equals(thing.getStatusInfo().getStatusDetail())) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.BRIDGE_OFFLINE, STANDBY_MSG);
            }
            return;
        }
        try {
            // Use the Enum valueOf to handle the message based on the event key. Otherwise there would be a huge
            // case statement here
            KaleidescapeMessageHandler.valueOf(evt.getKey()).handleMessage(evt.getValue(), this);

            if (!evt.isCached()) {
                cache.put(evt.getKey(), evt.getValue());
            }

            if (ThingStatusDetail.BRIDGE_OFFLINE.equals(thing.getStatusInfo().getStatusDetail())) {
                // no longer in standby, update the status
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.friendlyName);
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Unhandled message: key {} = {}", evt.getKey(), evt.getValue());
        }
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                if (!connector.isConnected()) {
                    logger.debug("Trying to reconnect...");
                    closeConnection();
                    String error = EMPTY;
                    if (openConnection()) {
                        try {
                            cache.clear();
                            Set<String> initialCommands = new HashSet<>(Arrays.asList(GET_DEVICE_TYPE_NAME,
                                    GET_FRIENDLY_NAME, GET_DEVICE_INFO, GET_SYSTEM_VERSION, GET_DEVICE_POWER_STATE,
                                    GET_CINEMASCAPE_MASK, GET_CINEMASCAPE_MODE, GET_SCALE_MODE, GET_SCREEN_MASK,
                                    GET_SCREEN_MASK2, GET_VIDEO_MODE, GET_UI_STATE, GET_HIGHLIGHTED_SELECTION,
                                    GET_CHILD_MODE_STATE, GET_PLAY_STATUS, GET_MOVIE_LOCATION, GET_MOVIE_MEDIA_TYPE,
                                    GET_PLAYING_TITLE_NAME));

                            // Premiere Players and Cinema One support music
                            if (thingTypeUID.equals(THING_TYPE_PLAYER) || thingTypeUID.equals(THING_TYPE_CINEMA_ONE)) {
                                initialCommands.addAll(Arrays.asList(GET_MUSIC_NOW_PLAYING_STATUS,
                                        GET_MUSIC_PLAY_STATUS, GET_MUSIC_TITLE));
                            }

                            // everything after Premiere Player supports GET_SYSTEM_READINESS_STATE
                            if (!thingTypeUID.equals(THING_TYPE_PLAYER)) {
                                initialCommands.add(GET_SYSTEM_READINESS_STATE);
                            }

                            // only Strato supports the GET_*_COLOR commands
                            if (thingTypeUID.equals(THING_TYPE_STRATO)) {
                                initialCommands.addAll(Arrays.asList(GET_VIDEO_COLOR, GET_CONTENT_COLOR));
                            }

                            initialCommands.forEach(command -> {
                                try {
                                    connector.sendCommand(command);
                                } catch (KaleidescapeException e) {
                                    logger.debug("{}: {}", "Error sending initial commands", e.getMessage());
                                }
                            });

                            if (this.updatePeriod == 1) {
                                connector.sendCommand(SET_STATUS_CUE_PERIOD_1);
                            }
                        } catch (KaleidescapeException e) {
                            error = "First command after connection failed";
                            logger.debug("{}: {}", error, e.getMessage());
                            closeConnection();
                        }
                    } else {
                        error = "Reconnection failed";
                    }
                    if (!error.equals(EMPTY)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                        return;
                    }
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.friendlyName);
                    lastEventReceived = System.currentTimeMillis();
                }
            }
        }, 1, RECON_POLLING_INTERVAL_S, TimeUnit.SECONDS);
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
                        cache.clear();
                    } catch (KaleidescapeException e) {
                        logger.debug("Polling error: {}", e.getMessage());
                    }

                    // if the last successful polling update was more than 1.25 intervals ago,
                    // the component is not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastEventReceived) > (POLLING_INTERVAL_S * 1.25 * 1000)) {
                        logger.warn("Component not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Component not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    }
                }
            }
        }, POLLING_INTERVAL_S, POLLING_INTERVAL_S, TimeUnit.SECONDS);
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
                connector.sendCommand(PLAY);
            } else if (command == PlayPauseType.PAUSE) {
                connector.sendCommand(PAUSE);
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connector.sendCommand(NEXT);
            } else if (command == NextPreviousType.PREVIOUS) {
                connector.sendCommand(PREVIOUS);
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                connector.sendCommand(SCAN_FORWARD);
            } else if (command == RewindFastforwardType.REWIND) {
                connector.sendCommand(SCAN_REVERSE);
            }
        } else {
            logger.warn("Unknown control command: {}", command);
        }
    }

    private void handleRefresh(String channel) throws KaleidescapeException {
        switch (channel) {
            case POWER:
                connector.sendCommand(GET_DEVICE_POWER_STATE, cache.get("DEVICE_POWER_STATE"));
                break;
            case VOLUME:
                updateState(channel, new PercentType(this.volume));
                break;
            case MUTE:
                updateState(channel, this.isMuted ? OnOffType.ON : OnOffType.OFF);
                break;
            case TITLE_NAME:
                connector.sendCommand(GET_PLAYING_TITLE_NAME, cache.get("TITLE_NAME"));
                break;
            case PLAY_MODE:
            case PLAY_SPEED:
            case TITLE_NUM:
            case TITLE_LENGTH:
            case TITLE_LOC:
            case CHAPTER_NUM:
            case CHAPTER_LENGTH:
            case CHAPTER_LOC:
                connector.sendCommand(GET_PLAY_STATUS, cache.get("PLAY_STATUS"));
                break;
            case MOVIE_MEDIA_TYPE:
                connector.sendCommand(GET_MOVIE_MEDIA_TYPE, cache.get("MOVIE_MEDIA_TYPE"));
                break;
            case MOVIE_LOCATION:
                connector.sendCommand(GET_MOVIE_LOCATION, cache.get("MOVIE_LOCATION"));
                break;
            case VIDEO_MODE:
            case VIDEO_MODE_COMPOSITE:
            case VIDEO_MODE_COMPONENT:
            case VIDEO_MODE_HDMI:
                connector.sendCommand(GET_VIDEO_MODE, cache.get("VIDEO_MODE"));
                break;
            case VIDEO_COLOR:
            case VIDEO_COLOR_EOTF:
                connector.sendCommand(GET_VIDEO_COLOR, cache.get("VIDEO_COLOR"));
                break;
            case CONTENT_COLOR:
            case CONTENT_COLOR_EOTF:
                connector.sendCommand(GET_CONTENT_COLOR, cache.get("CONTENT_COLOR"));
                break;
            case SCALE_MODE:
                connector.sendCommand(GET_SCALE_MODE, cache.get("SCALE_MODE"));
                break;
            case ASPECT_RATIO:
            case SCREEN_MASK:
                connector.sendCommand(GET_SCREEN_MASK, cache.get("SCREEN_MASK"));
                break;
            case SCREEN_MASK2:
                connector.sendCommand(GET_SCREEN_MASK2, cache.get("SCREEN_MASK2"));
                break;
            case CINEMASCAPE_MASK:
                connector.sendCommand(GET_CINEMASCAPE_MASK, cache.get("GET_CINEMASCAPE_MASK"));
                break;
            case CINEMASCAPE_MODE:
                connector.sendCommand(GET_CINEMASCAPE_MODE, cache.get("CINEMASCAPE_MODE"));
                break;
            case UI_STATE:
                connector.sendCommand(GET_UI_STATE, cache.get("UI_STATE"));
                break;
            case CHILD_MODE_STATE:
                connector.sendCommand(GET_CHILD_MODE_STATE, cache.get("CHILD_MODE_STATE"));
                break;
            case SYSTEM_READINESS_STATE:
                connector.sendCommand(GET_SYSTEM_READINESS_STATE, cache.get("SYSTEM_READINESS_STATE"));
                break;
            case HIGHLIGHTED_SELECTION:
                connector.sendCommand(GET_HIGHLIGHTED_SELECTION, cache.get("HIGHLIGHTED_SELECTION"));
                break;
            case USER_DEFINED_EVENT:
            case USER_INPUT:
            case USER_INPUT_PROMPT:
                updateState(channel, StringType.EMPTY);
                break;
            case MUSIC_REPEAT:
            case MUSIC_RANDOM:
                connector.sendCommand(GET_MUSIC_NOW_PLAYING_STATUS, cache.get("MUSIC_NOW_PLAYING_STATUS"));
                break;
            case MUSIC_TRACK:
            case MUSIC_ARTIST:
            case MUSIC_ALBUM:
            case MUSIC_TRACK_HANDLE:
            case MUSIC_ALBUM_HANDLE:
            case MUSIC_NOWPLAY_HANDLE:
                connector.sendCommand(GET_MUSIC_TITLE, cache.get("MUSIC_TITLE"));
                break;
            case MUSIC_PLAY_MODE:
            case MUSIC_PLAY_SPEED:
            case MUSIC_TRACK_LENGTH:
            case MUSIC_TRACK_POSITION:
            case MUSIC_TRACK_PROGRESS:
                connector.sendCommand(GET_MUSIC_PLAY_STATUS, cache.get("MUSIC_PLAY_STATUS"));
                break;
            case DETAIL_TYPE:
            case DETAIL_TITLE:
            case DETAIL_ALBUM_TITLE:
            case DETAIL_COVER_ART:
            case DETAIL_COVER_URL:
            case DETAIL_HIRES_COVER_URL:
            case DETAIL_RATING:
            case DETAIL_YEAR:
            case DETAIL_RUNNING_TIME:
            case DETAIL_ACTORS:
            case DETAIL_ARTIST:
            case DETAIL_DIRECTORS:
            case DETAIL_GENRES:
            case DETAIL_RATING_REASON:
            case DETAIL_SYNOPSIS:
            case DETAIL_REVIEW:
            case DETAIL_COLOR_DESCRIPTION:
            case DETAIL_COUNTRY:
            case DETAIL_ASPECT_RATIO:
            case DETAIL_DISC_LOCATION:
                updateState(channel, StringType.EMPTY);
                break;
        }
    }
}
