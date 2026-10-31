/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volumio.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.volumio.internal.mapping.VolumioData;
import org.openhab.binding.volumio.internal.mapping.VolumioEvents;
import org.openhab.binding.volumio.internal.mapping.VolumioServiceTypes;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * The {@link VolumioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioHandler extends BaseThingHandler {

    /** Upper bound for a downloaded cover art image. */
    private static final int MAX_COVER_ART_BYTES = 5 * 1024 * 1024;

    private final Logger logger = LoggerFactory.getLogger(VolumioHandler.class);

    private final VolumioData state = new VolumioData();

    private final Object lifecycleLock = new Object();

    private volatile @Nullable VolumioService volumio;
    private volatile @Nullable ScheduledFuture<?> initJob;
    private volatile @Nullable ScheduledFuture<?> coverArtJob;

    private volatile boolean disposed;
    private volatile String baseUrl = "";
    private volatile int timeout = 5000;

    public VolumioHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        if (command instanceof RefreshType) {
            refreshChannel(channelId);
            return;
        }

        VolumioService volumioLocal = volumio;
        if (volumioLocal == null || !volumioLocal.isConnected()) {
            // Do not queue commands while offline, they would be replayed (e.g. a reboot) on reconnect.
            logger.debug("Volumio is not connected, ignoring command {} for channel {}", command, channelId);
            return;
        }

        switch (channelId) {
            case VolumioBindingConstants.CHANNEL_PLAYER:
                handlePlaybackCommands(volumioLocal, command);
                break;
            case VolumioBindingConstants.CHANNEL_VOLUME:
                handleVolumeCommand(volumioLocal, command);
                break;
            case VolumioBindingConstants.CHANNEL_MUTE:
                if (command instanceof OnOffType onOff) {
                    volumioLocal.setMute(onOff == OnOffType.ON);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_RADIO_STREAM:
                if (command instanceof StringType) {
                    volumioLocal.replacePlay(command.toFullString(), "Radio", VolumioServiceTypes.WEBRADIO);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_URI:
                if (command instanceof StringType) {
                    volumioLocal.replacePlay(command.toFullString(), "URI", VolumioServiceTypes.WEBRADIO);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_FILE:
                if (command instanceof StringType) {
                    volumioLocal.replacePlay(command.toFullString(), "", VolumioServiceTypes.MPD);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_PLAYLIST:
                if (command instanceof StringType) {
                    volumioLocal.playPlaylist(command.toFullString());
                }
                break;
            case VolumioBindingConstants.CHANNEL_CLEAR_QUEUE:
                if (command == OnOffType.ON) {
                    volumioLocal.clearQueue();
                    // Make it feel like a toggle button ...
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_RANDOM:
                if (command instanceof OnOffType onOff) {
                    volumioLocal.setRandom(onOff == OnOffType.ON);
                }
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_REPEAT:
                if (command instanceof OnOffType onOff) {
                    volumioLocal.setRepeat(onOff == OnOffType.ON);
                }
                break;
            case VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND:
                if (command instanceof StringType) {
                    if (!volumioLocal.sendSystemCommand(command.toString())) {
                        logger.debug("Unknown system command: {}", command);
                    }
                    updateState(channelUID, UnDefType.UNDEF);
                }
                break;
            case VolumioBindingConstants.CHANNEL_STOP:
                if (command instanceof StringType) {
                    volumioLocal.stop();
                    updateState(channelUID, UnDefType.UNDEF);
                }
                break;
            case VolumioBindingConstants.CHANNEL_SEEK:
                handleSeekCommand(volumioLocal, command);
                break;
            case VolumioBindingConstants.CHANNEL_ADD_TO_QUEUE:
                if (command instanceof StringType) {
                    volumioLocal.addToQueue(command.toFullString());
                }
                break;
            case VolumioBindingConstants.CHANNEL_REMOVE_FROM_QUEUE:
                if (command instanceof DecimalType index) {
                    volumioLocal.removeFromQueue(index.intValue());
                }
                break;
            case VolumioBindingConstants.CHANNEL_ENQUEUE_PLAYLIST:
                if (command instanceof StringType) {
                    volumioLocal.enqueuePlaylist(command.toFullString());
                }
                break;
            case VolumioBindingConstants.CHANNEL_ADD_TO_FAVOURITES:
            case VolumioBindingConstants.CHANNEL_REMOVE_FROM_FAVOURITES:
                if (command == OnOffType.ON) {
                    handleFavouriteCommand(volumioLocal, channelId);
                    // Make it feel like a toggle button ...
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
            case VolumioBindingConstants.CHANNEL_ARTIST:
            case VolumioBindingConstants.CHANNEL_ALBUM:
            case VolumioBindingConstants.CHANNEL_TRACK_TYPE:
            case VolumioBindingConstants.CHANNEL_TITLE:
            case VolumioBindingConstants.CHANNEL_COVER_ART:
            case VolumioBindingConstants.CHANNEL_DURATION:
            case VolumioBindingConstants.CHANNEL_QUEUE_POSITION:
            case VolumioBindingConstants.CHANNEL_QUEUE_SIZE:
            case VolumioBindingConstants.CHANNEL_CURRENT_URI:
            case VolumioBindingConstants.CHANNEL_SERVICE:
            case VolumioBindingConstants.CHANNEL_SAMPLE_RATE:
            case VolumioBindingConstants.CHANNEL_BIT_DEPTH:
            case VolumioBindingConstants.CHANNEL_AUDIO_CHANNELS:
            case VolumioBindingConstants.CHANNEL_STREAMING:
            case VolumioBindingConstants.CHANNEL_LIBRARY_UPDATE:
            case VolumioBindingConstants.CHANNEL_REPEAT_SINGLE:
                // read-only channels
                break;
            default:
                logger.debug("Unknown channel: {}", channelId);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // Updates are only sent for linked channels, so bring a newly linked channel up to date.
        refreshChannel(channelUID.getId());
    }

    /**
     * Publish the last known state to a channel. Does not talk to the device; the state is requested once on
     * connect and kept up to date by the {@code pushState} event.
     */
    private void refreshChannel(String channelId) {
        switch (channelId) {
            case VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND:
            case VolumioBindingConstants.CHANNEL_STOP:
            case VolumioBindingConstants.CHANNEL_SEEK:
            case VolumioBindingConstants.CHANNEL_ADD_TO_QUEUE:
            case VolumioBindingConstants.CHANNEL_REMOVE_FROM_QUEUE:
            case VolumioBindingConstants.CHANNEL_ENQUEUE_PLAYLIST:
                updateState(channelId, UnDefType.UNDEF);
                return;
            case VolumioBindingConstants.CHANNEL_CLEAR_QUEUE:
            case VolumioBindingConstants.CHANNEL_ADD_TO_FAVOURITES:
            case VolumioBindingConstants.CHANNEL_REMOVE_FROM_FAVOURITES:
                updateState(channelId, OnOffType.OFF);
                return;
            case VolumioBindingConstants.CHANNEL_QUEUE_SIZE:
                if (state.isQueueInitialized()) {
                    updateState(channelId, state.getQueueSize());
                }
                return;
            default:
                break;
        }

        if (!state.isInitialized()) {
            return;
        }

        switch (channelId) {
            case VolumioBindingConstants.CHANNEL_TITLE:
                updateState(channelId, state.getTitle());
                break;
            case VolumioBindingConstants.CHANNEL_ARTIST:
                updateState(channelId, state.getArtist());
                break;
            case VolumioBindingConstants.CHANNEL_ALBUM:
                updateState(channelId, state.getAlbum());
                break;
            case VolumioBindingConstants.CHANNEL_VOLUME:
                updateState(channelId, state.getVolume());
                break;
            case VolumioBindingConstants.CHANNEL_MUTE:
                updateState(channelId, state.getMute());
                break;
            case VolumioBindingConstants.CHANNEL_PLAYER:
                updateState(channelId, state.getState());
                break;
            case VolumioBindingConstants.CHANNEL_TRACK_TYPE:
                updateState(channelId, state.getTrackType());
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_RANDOM:
                updateState(channelId, state.getRandom());
                break;
            case VolumioBindingConstants.CHANNEL_PLAY_REPEAT:
                updateState(channelId, state.getRepeat());
                break;
            case VolumioBindingConstants.CHANNEL_COVER_ART:
                updateCoverArt();
                break;
            case VolumioBindingConstants.CHANNEL_DURATION:
                updateState(channelId, state.getDuration());
                break;
            case VolumioBindingConstants.CHANNEL_QUEUE_POSITION:
                updateState(channelId, state.getPosition());
                break;
            case VolumioBindingConstants.CHANNEL_CURRENT_URI:
                updateState(channelId, state.getUri());
                break;
            case VolumioBindingConstants.CHANNEL_SERVICE:
                updateState(channelId, state.getService());
                break;
            case VolumioBindingConstants.CHANNEL_SAMPLE_RATE:
                updateState(channelId, state.getSampleRate());
                break;
            case VolumioBindingConstants.CHANNEL_BIT_DEPTH:
                updateState(channelId, state.getBitDepth());
                break;
            case VolumioBindingConstants.CHANNEL_AUDIO_CHANNELS:
                updateState(channelId, state.getAudioChannels());
                break;
            case VolumioBindingConstants.CHANNEL_STREAMING:
                updateState(channelId, state.getStream());
                break;
            case VolumioBindingConstants.CHANNEL_LIBRARY_UPDATE:
                updateState(channelId, state.getUpdateDb());
                break;
            case VolumioBindingConstants.CHANNEL_REPEAT_SINGLE:
                updateState(channelId, state.getRepeatSingle());
                break;
            default:
                break;
        }
    }

    /**
     * Jump to a position in the current track. The Volumio {@code seek} event takes seconds; a plain number is
     * interpreted as seconds.
     */
    private void handleSeekCommand(VolumioService volumioLocal, Command command) {
        int seconds = -1;
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> inSeconds = quantity.toUnit(Units.SECOND);
            if (inSeconds != null) {
                seconds = inSeconds.intValue();
            }
        } else if (command instanceof DecimalType decimal) {
            seconds = decimal.intValue();
        }
        if (seconds >= 0) {
            volumioLocal.seek(seconds);
        } else {
            logger.debug("Seek command not handled: {}", command);
        }
    }

    /** Add or remove the currently playing item to or from the Volumio favourites. */
    private void handleFavouriteCommand(VolumioService volumioLocal, String channelId) {
        String uri = state.getUriText();
        String service = state.getServiceText();
        if (uri.isEmpty() || service.isEmpty()) {
            logger.debug("Nothing is playing, cannot change favourites");
            return;
        }
        if (VolumioBindingConstants.CHANNEL_ADD_TO_FAVOURITES.equals(channelId)) {
            volumioLocal.addToFavourites(uri, state.getTitleText(), service);
        } else {
            volumioLocal.removeFromFavourites(uri, service);
        }
    }

    private void handleVolumeCommand(VolumioService volumioLocal, Command command) {
        if (command instanceof PercentType percent) {
            volumioLocal.setVolume(percent);
        } else if (command instanceof IncreaseDecreaseType increaseDecrease) {
            volumioLocal.changeVolume(increaseDecrease == IncreaseDecreaseType.INCREASE);
        } else {
            logger.debug("Volume command not handled: {}", command);
        }
    }

    private void handlePlaybackCommands(VolumioService volumioLocal, Command command) {
        if (command instanceof PlayPauseType playPauseCmd) {
            switch (playPauseCmd) {
                case PLAY -> volumioLocal.play();
                case PAUSE -> volumioLocal.pause();
            }
        } else if (command instanceof NextPreviousType nextPreviousType) {
            switch (nextPreviousType) {
                case PREVIOUS -> volumioLocal.previous();
                case NEXT -> volumioLocal.next();
            }
        } else if (command instanceof RewindFastforwardType) {
            logger.debug("Rewind/fast-forward is not supported");
        } else {
            logger.debug("Playback command not handled: {}", command);
        }
    }

    /**
     * Read the configuration and connect to the Volumio device. Host name resolution can block, so the connection is
     * set up on the scheduler instead of the calling thread.
     */
    @Override
    public void initialize() {
        VolumioConfiguration config = getConfigAs(VolumioConfiguration.class);

        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration incomplete, missing hostname");
            return;
        }
        if (config.protocol.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration incomplete, missing protocol");
            return;
        }

        disposed = false;
        timeout = config.timeout;
        baseUrl = String.format("%s://%s:%d", config.protocol, config.hostname, config.port);
        updateStatus(ThingStatus.UNKNOWN);

        initJob = scheduler.schedule(() -> connect(config), 0, TimeUnit.SECONDS);
    }

    private void connect(VolumioConfiguration config) {
        logger.debug("Trying to connect to Volumio on {}", baseUrl);
        VolumioService service;
        try {
            service = new VolumioService(config.protocol, config.hostname, config.port, config.timeout);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String.valueOf(e.getMessage()));
            return;
        }

        // Registration and connect() stay inside the lifecycle guard, so dispose() cannot close the service in between.
        synchronized (lifecycleLock) {
            if (disposed) {
                service.close();
                return;
            }
            volumio = service;
            service.on(Socket.EVENT_CONNECT, connectListener());
            service.on(Socket.EVENT_DISCONNECT, disconnectListener());
            service.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener());
            service.on(VolumioEvents.PUSH_STATE, pushStateListener());
            service.on(VolumioEvents.PUSH_QUEUE, pushQueueListener());
            service.connect();
        }
    }

    @Override
    public void dispose() {
        VolumioService volumioLocal;
        synchronized (lifecycleLock) {
            disposed = true;
            volumioLocal = volumio;
            volumio = null;
        }

        ScheduledFuture<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        job = coverArtJob;
        if (job != null) {
            job.cancel(true);
            coverArtJob = null;
        }

        if (volumioLocal != null) {
            volumioLocal.disconnect();
            volumioLocal.close();
        }
    }

    /** Listener **/

    private Emitter.Listener connectListener() {
        return arg -> {
            if (!disposed) {
                updateStatus(ThingStatus.ONLINE);
            }
        };
    }

    private Emitter.Listener disconnectListener() {
        return arg -> {
            if (!disposed) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Disconnected from Volumio");
            }
        };
    }

    private Emitter.Listener connectErrorListener() {
        return arg -> {
            if (!disposed) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to Volumio on " + baseUrl);
            }
        };
    }

    /**
     * On received a pushState Event, the ThingChannels are
     * updated if there is a change and they are linked.
     */
    private Emitter.Listener pushStateListener() {
        return data -> {
            if (disposed) {
                return;
            }
            if (data.length == 0 || !(data[0] instanceof JSONObject jsonObject)) {
                logger.debug("Ignoring pushState event without a JSON payload");
                return;
            }
            logger.trace("pushState: {}", jsonObject);
            state.update(jsonObject);

            updateIfChanged(VolumioBindingConstants.CHANNEL_TITLE, state.isTitleDirty(), state.getTitle());
            updateIfChanged(VolumioBindingConstants.CHANNEL_ARTIST, state.isArtistDirty(), state.getArtist());
            updateIfChanged(VolumioBindingConstants.CHANNEL_ALBUM, state.isAlbumDirty(), state.getAlbum());
            updateIfChanged(VolumioBindingConstants.CHANNEL_VOLUME, state.isVolumeDirty(), state.getVolume());
            updateIfChanged(VolumioBindingConstants.CHANNEL_MUTE, state.isMuteDirty(), state.getMute());
            updateIfChanged(VolumioBindingConstants.CHANNEL_PLAYER, state.isStateDirty(), state.getState());
            updateIfChanged(VolumioBindingConstants.CHANNEL_TRACK_TYPE, state.isTrackTypeDirty(), state.getTrackType());
            updateIfChanged(VolumioBindingConstants.CHANNEL_PLAY_RANDOM, state.isRandomDirty(), state.getRandom());
            updateIfChanged(VolumioBindingConstants.CHANNEL_PLAY_REPEAT, state.isRepeatDirty(), state.getRepeat());
            updateIfChanged(VolumioBindingConstants.CHANNEL_REPEAT_SINGLE, state.isRepeatSingleDirty(),
                    state.getRepeatSingle());
            updateIfChanged(VolumioBindingConstants.CHANNEL_DURATION, state.isDurationDirty(), state.getDuration());
            updateIfChanged(VolumioBindingConstants.CHANNEL_QUEUE_POSITION, state.isPositionDirty(),
                    state.getPosition());
            updateIfChanged(VolumioBindingConstants.CHANNEL_CURRENT_URI, state.isUriDirty(), state.getUri());
            updateIfChanged(VolumioBindingConstants.CHANNEL_SERVICE, state.isServiceDirty(), state.getService());
            updateIfChanged(VolumioBindingConstants.CHANNEL_SAMPLE_RATE, state.isSampleRateDirty(),
                    state.getSampleRate());
            updateIfChanged(VolumioBindingConstants.CHANNEL_BIT_DEPTH, state.isBitDepthDirty(), state.getBitDepth());
            updateIfChanged(VolumioBindingConstants.CHANNEL_AUDIO_CHANNELS, state.isAudioChannelsDirty(),
                    state.getAudioChannels());
            updateIfChanged(VolumioBindingConstants.CHANNEL_STREAMING, state.isStreamDirty(), state.getStream());
            updateIfChanged(VolumioBindingConstants.CHANNEL_LIBRARY_UPDATE, state.isUpdateDbDirty(),
                    state.getUpdateDb());

            if (state.isCoverArtDirty()) {
                updateCoverArt();
            }
        };
    }

    /**
     * The play queue is pushed as a JSON array; only its size is published.
     */
    private Emitter.Listener pushQueueListener() {
        return data -> {
            if (disposed) {
                return;
            }
            if (data.length == 0 || !(data[0] instanceof JSONArray queue)) {
                logger.debug("Ignoring pushQueue event without a JSON array payload");
                return;
            }
            state.setQueueSize(queue.length());
            updateIfChanged(VolumioBindingConstants.CHANNEL_QUEUE_SIZE, state.isQueueSizeDirty(), state.getQueueSize());
        };
    }

    private void updateIfChanged(String channelId, boolean changed, State newState) {
        if (changed && isLinked(channelId)) {
            updateState(channelId, newState);
        }
    }

    /**
     * Download the current cover art in the background and publish it. The {@code albumart} value is either an
     * absolute URL or a path on the Volumio host.
     */
    private void updateCoverArt() {
        if (!isLinked(VolumioBindingConstants.CHANNEL_COVER_ART)) {
            return;
        }
        String art = state.getCoverArtUrl();
        if (art.isEmpty()) {
            updateState(VolumioBindingConstants.CHANNEL_COVER_ART, UnDefType.UNDEF);
            return;
        }
        String url = art.startsWith("http://") || art.startsWith("https://") ? art
                : baseUrl + (art.startsWith("/") ? "" : "/") + art;

        synchronized (lifecycleLock) {
            if (disposed) {
                return;
            }
            ScheduledFuture<?> previous = coverArtJob;
            if (previous != null) {
                previous.cancel(true);
            }
            coverArtJob = scheduler.schedule(() -> fetchCoverArt(art, url), 0, TimeUnit.SECONDS);
        }
    }

    private void fetchCoverArt(String art, String url) {
        State result = UnDefType.UNDEF;
        try {
            URLConnection connection = URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            if (connection instanceof HttpURLConnection http && http.getResponseCode() != 200) {
                logger.debug("Cover art request for {} failed with HTTP {}", url, http.getResponseCode());
            } else {
                String contentType = connection.getContentType();
                try (InputStream in = connection.getInputStream()) {
                    byte[] bytes = in.readNBytes(MAX_COVER_ART_BYTES + 1);
                    if (bytes.length > 0 && bytes.length <= MAX_COVER_ART_BYTES) {
                        result = new RawType(bytes,
                                contentType != null && contentType.startsWith("image/") ? contentType : "image/jpeg");
                    } else {
                        logger.debug("Ignoring cover art from {}: unexpected size", url);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            logger.debug("Could not load cover art from {}: {}", url, e.getMessage());
        }

        // The track may have changed while the image was loading
        if (!disposed && art.equals(state.getCoverArtUrl())) {
            updateState(VolumioBindingConstants.CHANNEL_COVER_ART, result);
        }
    }
}
