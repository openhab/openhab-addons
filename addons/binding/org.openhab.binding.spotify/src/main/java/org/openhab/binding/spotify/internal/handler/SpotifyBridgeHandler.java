/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.handler;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.spotify.internal.SpotifyAccountHandler;
import org.openhab.binding.spotify.internal.SpotifyBridgeConfiguration;
import org.openhab.binding.spotify.internal.SpotifyHandleCommands;
import org.openhab.binding.spotify.internal.api.SpotifyAccessTokenChangeHandler;
import org.openhab.binding.spotify.internal.api.SpotifyApi;
import org.openhab.binding.spotify.internal.api.SpotifyAuthorizer;
import org.openhab.binding.spotify.internal.api.SpotifyConnector;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
import org.openhab.binding.spotify.internal.api.model.Album;
import org.openhab.binding.spotify.internal.api.model.Artist;
import org.openhab.binding.spotify.internal.api.model.AuthorizationCodeCredentials;
import org.openhab.binding.spotify.internal.api.model.CurrentlyPlayingContext;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Image;
import org.openhab.binding.spotify.internal.api.model.Item;
import org.openhab.binding.spotify.internal.api.model.Me;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyBridgeHandler} is the main class to manage Spotify WebAPI connection and update status of things.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Just a lot of refactoring
 */
@NonNullByDefault
public class SpotifyBridgeHandler extends BaseBridgeHandler
        implements SpotifyAccountHandler, SpotifyAccessTokenChangeHandler {

    private static final CurrentlyPlayingContext EMPTY_CURRENTLYPLAYINGCONTEXT = new CurrentlyPlayingContext();
    private static final Album EMPTY_ALBUM = new Album();
    private static final Artist EMPTY_ARTIST = new Artist();
    private static final Item EMPTY_ITEM = new Item();
    private static final Device EMPTY_DEVICE = new Device();
    private static final SimpleDateFormat MUSIC_TIME_FORMAT = new SimpleDateFormat("m:ss");
    private static final int MAX_IMAGE_SIZE = 500000;
    /**
     * After a command is handles. With the given delay a status poll request is triggered. The delay is to give Spotify
     * some time to handle the update.
     */
    private static final int POLL_DELAY_AFTER_COMMAND_S = 2;
    /**
     * Time between track progress status updates.
     */
    private static final int PROGRESS_STEP_S = 1;
    private static final long PROGRESS_STEP_MS = TimeUnit.SECONDS.toMillis(PROGRESS_STEP_S);

    private final Logger logger = LoggerFactory.getLogger(SpotifyBridgeHandler.class);
    private final SpotifyConnector spotifyConnector;
    private final ProgressUpdater progressUpdater = new ProgressUpdater();
    private final AlbumUpdater albumUpdater = new AlbumUpdater();

    // Field members assigned in initialize method
    private @NonNullByDefault({}) Future<?> pollingFuture;
    private @NonNullByDefault({}) SpotifyAuthorizer spotifyAuthorizer;
    private @NonNullByDefault({}) SpotifyApi spotifyApi;
    private @NonNullByDefault({}) SpotifyBridgeConfiguration configuration;
    private @NonNullByDefault({}) SpotifyHandleCommands handleCommand;
    private @NonNullByDefault({}) ExpiringCache<CurrentlyPlayingContext> playingContextCache;
    private @NonNullByDefault({}) ExpiringCache<List<Device>> devicesCache;

    /**
     * Keep track if this instance is disposed, so that now new scheduling can be started after dispose is called.
     */
    private volatile boolean active;
    private State lastTrackId = StringType.EMPTY;

    public SpotifyBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        spotifyConnector = new SpotifyConnector(scheduler, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && CHANNEL_PLAYED_ALBUMIMAGE.equals(channelUID.getId())) {
            albumUpdater.refreshAlbumImage(channelUID);
        } else {
            try {
                if (handleCommand != null && handleCommand.handleCommand(channelUID, command, true)) {
                    scheduler.schedule(() -> {
                        boolean statusNoPolling = pollingFuture == null || pollingFuture.isCancelled();
                        playingContextCache.invalidateValue();
                        devicesCache.invalidateValue();

                        if (pollStatus() && statusNoPolling) {
                            startPolling();
                        }
                    }, POLL_DELAY_AFTER_COMMAND_S, TimeUnit.SECONDS);
                }
            } catch (SpotifyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        active = false;
        cancelSchedulers();
    }

    @Override
    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public String getLabel() {
        return thing.getLabel() == null ? "" : thing.getLabel().toString();
    }

    @Override
    public String getUser() {
        return thing.getProperties().get(PROPERTY_SPOTIFY_USER);
    }

    @Override
    public boolean isOnline() {
        return thing.getStatus() == ThingStatus.ONLINE;
    }

    @Nullable
    SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    @Override
    public boolean equalsThingUID(String thingUID) {
        return getThing().getUID().getAsString().equals(thingUID);
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        return spotifyAuthorizer.formatAuthorizationUrl(redirectUri, configuration.clientId,
                thing.getUID().getAsString());
    }

    @Override
    public AuthorizationCodeCredentials authorize(String redirectUri, String reqCode) {
        try {
            AuthorizationCodeCredentials credentials = spotifyAuthorizer.requestTokens(redirectUri, reqCode);

            updateConfiguration(credentials);
            updateProperties(credentials);
            startPolling();
            return credentials;
        } catch (RuntimeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw e;
        }
    }

    private void updateConfiguration(AuthorizationCodeCredentials credentials) {
        if (spotifyApi != null) {
            spotifyApi.setAuthorizationCodeCredentials(credentials);
            Configuration conf = editConfiguration();

            conf.put(CONFIGURATION_CLIENT_REFRESH_TOKEN, credentials.getRefreshToken());
            updateConfiguration(conf);
        }
    }

    private void updateProperties(AuthorizationCodeCredentials credentials) {
        if (spotifyApi != null) {
            Me me = spotifyApi.getMe();
            credentials.setUser(me.getDisplayName() == null ? me.getId() : me.getDisplayName());
            Map<String, String> props = editProperties();

            props.put(PROPERTY_SPOTIFY_USER, credentials.getUser());
            updateProperties(props);
        }
    }

    @Override
    public void initialize() {
        active = true;
        configuration = getConfigAs(SpotifyBridgeConfiguration.class);
        spotifyAuthorizer = new SpotifyAuthorizer(spotifyConnector, configuration.clientId, configuration.clientSecret);
        spotifyApi = new SpotifyApi(spotifyConnector, spotifyAuthorizer, this);
        handleCommand = new SpotifyHandleCommands(spotifyApi, "");
        spotifyApi.setAuthorizationCodeCredentials(new AuthorizationCodeCredentials(
                getThing().getProperties().get(PROPERTY_SPOTIFY_USER), configuration.refreshToken));
        playingContextCache = new ExpiringCache<>(configuration.refreshPeriod, spotifyApi::getPlayerInfo);
        devicesCache = new ExpiringCache<>(configuration.refreshPeriod, spotifyApi::listDevices);
        updateStatus(ThingStatus.UNKNOWN);
        // start with update status by calling Spotify. If no credentials available no polling should be started.
        if (pollStatus()) {
            startPolling();
        }
    }

    @Override
    public List<Device> listDevices() {
        List<Device> listDevices = devicesCache.getValue();

        return listDevices == null ? Collections.emptyList() : listDevices;
    }

    /**
     * This method initiates a new thread for polling the available Spotify Connect devices and update the player
     * information.
     */
    private synchronized void startPolling() {
        cancelSchedulers();
        if (active) {
            playingContextCache.invalidateValue();
            devicesCache.invalidateValue();
            pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, configuration.refreshPeriod,
                    TimeUnit.SECONDS);
        }
    }

    private boolean pollStatus() {
        // No synchronation on this because method calls asynchronously other methods that also call synchronized
        // methods, which could result in a deadlock.
        synchronized (spotifyConnector) {
            try {
                CurrentlyPlayingContext pc = playingContextCache.getValue();
                CurrentlyPlayingContext playingContext = pc == null ? EMPTY_CURRENTLYPLAYINGCONTEXT : pc;
                updateStatus(ThingStatus.ONLINE);
                updatePlayerInfo(playingContext);
                List<Device> ld = devicesCache.getValue();
                List<Device> listDevices = ld == null ? Collections.emptyList() : ld;

                updateDevicesStatus(listDevices, playingContext.isPlaying());
                return true;
            } catch (SpotifyAuthorizationException e) {
                logger.debug(e.getMessage());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                cancelSchedulers();
                devicesCache.invalidateValue();
            } catch (SpotifyException e) {
                logger.info(e.getMessage());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (RuntimeException e) {
                logger.info("Unexpected error during polling status, please report if this keeps ocurring: ", e);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Cancels all running schedulers.
     */
    private synchronized void cancelSchedulers() {
        if (pollingFuture != null) {
            pollingFuture.cancel(true);
        }
        progressUpdater.cancelProgressScheduler();
    }

    @Override
    public void onAccessTokenChanged(String accessToken) {
        updateChannelState(CHANNEL_ACCESSTOKEN, new StringType(accessToken));
    }

    /**
     * Updates the status of all child Spotify Device Things.
     *
     * @param playing true if the current active device is playing
     */
    private void updateDevicesStatus(List<Device> spotifyDevices, boolean playing) {
        getThing().getThings().stream().filter(thing -> !spotifyDevices.stream().anyMatch(sd -> {
            SpotifyDeviceHandler handler = (SpotifyDeviceHandler) thing.getHandler();

            return handler == null ? false : handler.updateDeviceStatus(sd, playing);
        })).forEach(thing -> ((SpotifyDeviceHandler) thing.getHandler()).setStatusGone());
    }

    /**
     * Update the player data.
     */
    private void updatePlayerInfo(CurrentlyPlayingContext playerInfo) {
        updateChannelState(CHANNEL_TRACKPLAYER, playerInfo.isPlaying() ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateChannelState(CHANNEL_DEVICESHUFFLE, playerInfo.isShuffleState() ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_TRACKREPEAT, playerInfo.getRepeatState());

        boolean hasItem = playerInfo.getItem() != null;
        Item item = hasItem ? playerInfo.getItem() : EMPTY_ITEM;
        State trackId = valueOrEmpty(item.getId());

        progressUpdater.updateProgress(active, playerInfo.isPlaying(), item.getDurationMs(),
                playerInfo.getProgressMs());
        if (!lastTrackId.equals(trackId)) {
            lastTrackId = trackId;
            updateChannelState(CHANNEL_PLAYED_TRACKDURATION_MS, new DecimalType(item.getDurationMs()));
            synchronized (MUSIC_TIME_FORMAT) {
                // synchronize because SimpleDateFormat is not thread safe
                updateChannelState(CHANNEL_PLAYED_TRACKDURATION_FMT,
                        MUSIC_TIME_FORMAT.format(new Date(item.getDurationMs())));
            }
            updateChannelState(CHANNEL_PLAYED_TRACKID, lastTrackId);
            updateChannelState(CHANNEL_PLAYED_TRACKHREF, valueOrEmpty(item.getHref()));
            updateChannelState(CHANNEL_PLAYED_TRACKURI, valueOrEmpty(item.getUri()));
            updateChannelState(CHANNEL_PLAYED_TRACKNAME, valueOrEmpty(item.getName()));
            updateChannelState(CHANNEL_PLAYED_TRACKTYPE, valueOrEmpty(item.getType()));
            updateChannelState(CHANNEL_PLAYED_TRACKNUMBER, valueOrZero(item.getTrackNumber()));
            updateChannelState(CHANNEL_PLAYED_TRACKDISCNUMBER, valueOrZero(item.getDiscNumber()));
            updateChannelState(CHANNEL_PLAYED_TRACKPOPULARITY, valueOrZero(item.getPopularity()));

            boolean hasAlbum = hasItem && item.getAlbum() != null;
            Album album = hasAlbum ? item.getAlbum() : EMPTY_ALBUM;
            updateChannelState(CHANNEL_PLAYED_ALBUMID, valueOrEmpty(album.getId()));
            updateChannelState(CHANNEL_PLAYED_ALBUMHREF, valueOrEmpty(album.getHref()));
            updateChannelState(CHANNEL_PLAYED_ALBUMURI, valueOrEmpty(album.getUri()));
            updateChannelState(CHANNEL_PLAYED_ALBUMNAME, valueOrEmpty(album.getName()));
            updateChannelState(CHANNEL_PLAYED_ALBUMTYPE, valueOrEmpty(album.getType()));
            albumUpdater.updateAlbumImage(album);

            Artist firstArtist = hasItem && item.getArtists() != null && !item.getArtists().isEmpty()
                    ? item.getArtists().get(0)
                    : EMPTY_ARTIST;

            updateChannelState(CHANNEL_PLAYED_ARTISTID, valueOrEmpty(firstArtist.getId()));
            updateChannelState(CHANNEL_PLAYED_ARTISTHREF, valueOrEmpty(firstArtist.getHref()));
            updateChannelState(CHANNEL_PLAYED_ARTISTURI, valueOrEmpty(firstArtist.getUri()));
            updateChannelState(CHANNEL_PLAYED_ARTISTNAME, valueOrEmpty(firstArtist.getName()));
            updateChannelState(CHANNEL_PLAYED_ARTISTTYPE, valueOrEmpty(firstArtist.getType()));
        }
        Device device = playerInfo.getDevice() == null ? EMPTY_DEVICE : playerInfo.getDevice();
        updateChannelState(CHANNEL_DEVICEID, valueOrEmpty(device.getId()));
        updateChannelState(CHANNEL_DEVICEACTIVE, device.isActive() ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_DEVICENAME, valueOrEmpty(device.getName()));
        updateChannelState(CHANNEL_DEVICETYPE, valueOrEmpty(device.getType()));

        // experienced situations where volume seemed to be undefined...
        updateChannelState(CHANNEL_DEVICEVOLUME,
                device.getVolumePercent() == null ? UnDefType.UNDEF : new PercentType(device.getVolumePercent()));
    }

    /**
     * @param value Integer value to return as {@link DecimalType}
     * @return value as {@link DecimalType} or ZERO if the value is null
     */
    private DecimalType valueOrZero(@Nullable Integer value) {
        return value == null ? DecimalType.ZERO : new DecimalType(value);
    }

    /**
     * @param value String value to return as {@link StringType}
     * @return value as {@link StringType} or EMPTY if the value is null or empty
     */
    private StringType valueOrEmpty(@Nullable String value) {
        return value == null || value.isEmpty() ? StringType.EMPTY : new StringType(value);
    }

    /**
     * Convenience method to update the channel state as {@link StringType} with a {@link String} value
     *
     * @param channelId id of the channel to update
     * @param value String value to set as {@link StringType}
     */
    private void updateChannelState(String channelId, String value) {
        updateChannelState(channelId, new StringType(value));
    }

    /**
     * Convenience method to update the channel state but only if the channel is linked.
     *
     * @param channelId id of the channel to update
     * @param state State to set on the channel
     */
    private void updateChannelState(String channelId, State state) {
        Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }

    /**
     * Class that manages the current progress of a track. The actual progress is tracked with the user specified
     * interval, This class fills the inbetween seconds so the status will show a continues updating of the progress.
     *
     * @author Hilbrand Bouwkamp - Initial contribution
     */
    private class ProgressUpdater {
        private long progress;
        private long duration;
        private @NonNullByDefault({}) Future<?> progressFuture;

        /**
         * Updates the progress with its actual values as provided by Spotify. Based on if the track is running or not
         * update the progress scheduler.
         *
         * @param active true if this instance is not disposed
         * @param playing true if the track if playing
         * @param duration duration of the track
         * @param progress current progress of the track
         */
        public synchronized void updateProgress(boolean active, boolean playing, long duration, long progress) {
            this.duration = duration;
            setProgress(progress);
            if (!playing || !active) {
                cancelProgressScheduler();
            } else if (progressFuture == null && active) {
                progressFuture = scheduler.scheduleWithFixedDelay(this::incrementProgress, PROGRESS_STEP_S,
                        PROGRESS_STEP_S, TimeUnit.SECONDS);
            }
        }

        /**
         * Increments the progress with PROGRESS_STEP_MS, but limits it on the duration.
         */
        private synchronized void incrementProgress() {
            setProgress(Math.min(duration, progress + PROGRESS_STEP_MS));
        }

        /**
         * Sets the progress on the channels.
         *
         * @param progress progress value to set
         */
        private void setProgress(long progress) {
            this.progress = progress;
            synchronized (MUSIC_TIME_FORMAT) {
                updateChannelState(CHANNEL_PLAYED_TRACKPROGRESS_MS, new DecimalType(progress));
                updateChannelState(CHANNEL_PLAYED_TRACKPROGRESS_FMT, MUSIC_TIME_FORMAT.format(new Date(progress)));
            }
        }

        /**
         * Cancels the progress future.
         */
        public synchronized void cancelProgressScheduler() {
            if (progressFuture != null) {
                progressFuture.cancel(true);
                progressFuture = null;
            }
        }
    }

    /**
     * Class to manager Album image updates.
     *
     * @author Hilbrand Bouwkamp - Initial contribution
     */
    private class AlbumUpdater {
        private String lastAlbumImageUrl = "";

        /**
         * Updates the album image status, but only refreshes the image when a new image should be shown.
         *
         * @param album album data
         */
        public void updateAlbumImage(Album album) {
            Channel channel = thing.getChannel(CHANNEL_PLAYED_ALBUMIMAGE);
            List<Image> images = album.getImages();

            if (channel != null && images != null && !images.isEmpty()) {
                String imageUrl = images.get(0).getUrl();

                if (!lastAlbumImageUrl.equals(imageUrl)) {
                    // Download the cover art in a different thread to not delay the other operations
                    lastAlbumImageUrl = imageUrl == null ? "" : imageUrl;
                    refreshAlbumImage(channel.getUID());
                }
            } else {
                updateChannelState(CHANNEL_PLAYED_ALBUMIMAGE, UnDefType.UNDEF);
            }
        }

        /**
         * Refreshes the image asynchronously, but only downloads the image if the channel is linked to avoid
         * unnecessary downloading of the image.
         *
         * @param channelUID UID of the album channel
         */
        public void refreshAlbumImage(ChannelUID channelUID) {
            if (!lastAlbumImageUrl.isEmpty() && isLinked(channelUID)) {
                String imageUrl = lastAlbumImageUrl;
                scheduler.execute(() -> {
                    if (lastAlbumImageUrl.equals(imageUrl) && isLinked(channelUID)) {
                        RawType image = HttpUtil.downloadImage(imageUrl, true, MAX_IMAGE_SIZE);
                        updateChannelState(CHANNEL_PLAYED_ALBUMIMAGE, image == null ? UnDefType.UNDEF : image);
                    }
                });
            }
        }
    }
}
