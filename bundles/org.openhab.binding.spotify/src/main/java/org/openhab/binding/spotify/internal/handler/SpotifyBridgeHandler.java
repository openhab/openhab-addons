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
package org.openhab.binding.spotify.internal.handler;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.spotify.internal.SpotifyAccountHandler;
import org.openhab.binding.spotify.internal.SpotifyBridgeConfiguration;
import org.openhab.binding.spotify.internal.api.SpotifyApi;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
import org.openhab.binding.spotify.internal.api.model.Album;
import org.openhab.binding.spotify.internal.api.model.Artist;
import org.openhab.binding.spotify.internal.api.model.Context;
import org.openhab.binding.spotify.internal.api.model.CurrentlyPlayingContext;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Image;
import org.openhab.binding.spotify.internal.api.model.Item;
import org.openhab.binding.spotify.internal.api.model.Me;
import org.openhab.binding.spotify.internal.api.model.Playlist;
import org.openhab.binding.spotify.internal.discovery.SpotifyDeviceDiscoveryService;
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
        implements SpotifyAccountHandler, AccessTokenRefreshListener {

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
    // Object to synchronize poll status on
    private final Object pollSynchronization = new Object();
    private final ProgressUpdater progressUpdater = new ProgressUpdater();
    private final AlbumUpdater albumUpdater = new AlbumUpdater();
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final SpotifyDynamicStateDescriptionProvider spotifyDynamicStateDescriptionProvider;
    private final ChannelUID devicesChannelUID;
    private final ChannelUID playlistsChannelUID;

    // Field members assigned in initialize method
    private @NonNullByDefault({}) Future<?> pollingFuture;
    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private @NonNullByDefault({}) SpotifyApi spotifyApi;
    private @NonNullByDefault({}) SpotifyBridgeConfiguration configuration;
    private @NonNullByDefault({}) SpotifyHandleCommands handleCommand;
    private @NonNullByDefault({}) ExpiringCache<CurrentlyPlayingContext> playingContextCache;
    private @NonNullByDefault({}) ExpiringCache<List<Playlist>> playlistCache;
    private @NonNullByDefault({}) ExpiringCache<List<Device>> devicesCache;

    /**
     * Keep track if this instance is disposed. This avoids new scheduling to be started after dispose is called.
     */
    private volatile boolean active;
    private volatile State lastTrackId = StringType.EMPTY;
    private volatile String lastKnownDeviceId = "";
    private volatile boolean lastKnownDeviceActive;

    public SpotifyBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient,
            SpotifyDynamicStateDescriptionProvider spotifyDynamicStateDescriptionProvider) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        this.spotifyDynamicStateDescriptionProvider = spotifyDynamicStateDescriptionProvider;
        devicesChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_DEVICES);
        playlistsChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_PLAYLISTS);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SpotifyDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (CHANNEL_PLAYED_ALBUMIMAGE.equals(channelUID.getId())) {
                albumUpdater.refreshAlbumImage(channelUID);
            } else if (CHANNEL_ACCESSTOKEN.equals(channelUID.getId())) {
                onAccessTokenResponse(getAccessTokenResponse());
            } else {
                lastTrackId = StringType.EMPTY;
            }
        } else {
            try {
                if (handleCommand != null
                        && handleCommand.handleCommand(channelUID, command, lastKnownDeviceActive, lastKnownDeviceId)) {
                    scheduler.schedule(this::scheduledPollingRestart, POLL_DELAY_AFTER_COMMAND_S, TimeUnit.SECONDS);
                }
            } catch (SpotifyException e) {
                logger.debug("Handle Spotify command failed: ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        active = false;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
        }
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
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
    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            return oAuthService == null ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
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
        try {
            return oAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
        } catch (OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) {
        try {
            logger.debug("Make call to Spotify to get access token.");
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUri);
            final String user = updateProperties(credentials);
            logger.debug("Authorized for user: {}", user);
            startPolling();
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SpotifyException(e.getMessage(), e);
        } catch (OAuthResponseException e) {
            throw new SpotifyAuthorizationException(e.getMessage(), e);
        }
    }

    private String updateProperties(AccessTokenResponse credentials) {
        if (spotifyApi != null) {
            final Me me = spotifyApi.getMe();
            final String user = me.getDisplayName() == null ? me.getId() : me.getDisplayName();
            final Map<String, String> props = editProperties();

            props.put(PROPERTY_SPOTIFY_USER, user);
            updateProperties(props);
            return user;
        }
        return "";
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        active = true;
        configuration = getConfigAs(SpotifyBridgeConfiguration.class);
        oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), SPOTIFY_API_TOKEN_URL,
                SPOTIFY_AUTHORIZE_URL, configuration.clientId, configuration.clientSecret, SPOTIFY_SCOPES, true);
        oAuthService.addAccessTokenRefreshListener(SpotifyBridgeHandler.this);
        spotifyApi = new SpotifyApi(oAuthService, scheduler, httpClient);
        handleCommand = new SpotifyHandleCommands(spotifyApi);
        playingContextCache = new ExpiringCache<>(configuration.refreshPeriod, spotifyApi::getPlayerInfo);
        playlistCache = new ExpiringCache<>(configuration.refreshPeriod, spotifyApi::getPlaylists);
        devicesCache = new ExpiringCache<>(configuration.refreshPeriod, spotifyApi::getDevices);

        // Start with update status by calling Spotify. If no credentials available no polling should be started.
        if (pollStatus()) {
            startPolling();
        }
    }

    @Override
    public List<Device> listDevices() {
        final List<Device> listDevices = devicesCache.getValue();

        return listDevices == null ? Collections.emptyList() : listDevices;
    }

    /**
     * Scheduled method to restart polling in case polling is not running.
     */
    private void scheduledPollingRestart() {
        synchronized (pollSynchronization) {
            try {
                final boolean pollingNotRunning = pollingFuture == null || pollingFuture.isCancelled();

                expireCache();
                if (pollStatus() && pollingNotRunning) {
                    startPolling();
                }
            } catch (RuntimeException e) {
                logger.debug("Restarting polling failed: ", e);
            }
        }
    }

    /**
     * This method initiates a new thread for polling the available Spotify Connect devices and update the player
     * information.
     */
    private void startPolling() {
        synchronized (pollSynchronization) {
            cancelSchedulers();
            if (active) {
                expireCache();
                pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, configuration.refreshPeriod,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void expireCache() {
        playingContextCache.invalidateValue();
        playlistCache.invalidateValue();
        devicesCache.invalidateValue();
    }

    /**
     * Calls the Spotify API and collects user data. Returns true if method completed without errors.
     *
     * @return true if method completed without errors.
     */
    private boolean pollStatus() {
        synchronized (pollSynchronization) {
            try {
                onAccessTokenResponse(getAccessTokenResponse());
                // Collect devices and populate selection with available devices.
                final List<Device> ld = devicesCache.getValue();
                final List<Device> devices = ld == null ? Collections.emptyList() : ld;
                spotifyDynamicStateDescriptionProvider.setDevices(devicesChannelUID, devices);
                // Collect currently playing context.
                final CurrentlyPlayingContext pc = playingContextCache.getValue();
                final CurrentlyPlayingContext playingContext = pc == null ? EMPTY_CURRENTLYPLAYINGCONTEXT : pc;
                final List<Playlist> lp = playlistCache.getValue();
                final List<Playlist> playlists = lp == null ? Collections.emptyList() : lp;
                updateStatus(ThingStatus.ONLINE);

                handleCommand.setLists(devices, playlists);
                updatePlayerInfo(playingContext, playlists);
                spotifyDynamicStateDescriptionProvider.setPlayLists(playlistsChannelUID, playlists);

                updateDevicesStatus(devices, playingContext.isPlaying());
                return true;
            } catch (SpotifyAuthorizationException e) {
                logger.debug("Authorization error during polling: ", e);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                cancelSchedulers();
                devicesCache.invalidateValue();
            } catch (SpotifyException e) {
                logger.info("Spotify returned an error during polling: {}", e.getMessage());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (RuntimeException e) {
                // This only should catch RuntimeException as the apiCall don't throw other exceptions.
                logger.info("Unexpected error during polling status, please report if this keeps occurring: ", e);

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
    public void onAccessTokenResponse(@Nullable AccessTokenResponse tokenResponse) {
        updateChannelState(CHANNEL_ACCESSTOKEN,
                new StringType(tokenResponse == null ? null : tokenResponse.getAccessToken()));
    }

    /**
     * Updates the status of all child Spotify Device Things.
     *
     * @param spotifyDevices list of Spotify devices
     * @param playing true if the current active device is playing
     */
    private void updateDevicesStatus(List<Device> spotifyDevices, boolean playing) {
        getThing().getThings().stream() //
                .filter(thing -> thing.getHandler() instanceof SpotifyDeviceHandler) //
                .filter(thing -> !spotifyDevices.stream()
                        .anyMatch(sd -> ((SpotifyDeviceHandler) thing.getHandler()).updateDeviceStatus(sd, playing)))
                .forEach(thing -> ((SpotifyDeviceHandler) thing.getHandler()).setStatusGone());
    }

    /**
     * Update the player data.
     *
     * @param playerInfo The object with the current playing context
     * @param playlists List of available playlists
     */
    private void updatePlayerInfo(CurrentlyPlayingContext playerInfo, List<Playlist> playlists) {
        updateChannelState(CHANNEL_TRACKPLAYER, playerInfo.isPlaying() ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateChannelState(CHANNEL_DEVICESHUFFLE, playerInfo.isShuffleState() ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_TRACKREPEAT, playerInfo.getRepeatState());

        final boolean hasItem = playerInfo.getItem() != null;
        final Item item = hasItem ? playerInfo.getItem() : EMPTY_ITEM;
        final State trackId = valueOrEmpty(item.getId());

        progressUpdater.updateProgress(active, playerInfo.isPlaying(), item.getDurationMs(),
                playerInfo.getProgressMs());
        if (!lastTrackId.equals(trackId)) {
            lastTrackId = trackId;
            updateChannelState(CHANNEL_PLAYED_TRACKDURATION_MS, new DecimalType(item.getDurationMs()));
            final String formattedProgress;
            synchronized (MUSIC_TIME_FORMAT) {
                // synchronize because SimpleDateFormat is not thread safe
                formattedProgress = MUSIC_TIME_FORMAT.format(new Date(item.getDurationMs()));
            }
            updateChannelState(CHANNEL_PLAYED_TRACKDURATION_FMT, formattedProgress);

            updateChannelsPlayList(playerInfo, playlists);
            updateChannelState(CHANNEL_PLAYED_TRACKID, lastTrackId);
            updateChannelState(CHANNEL_PLAYED_TRACKHREF, valueOrEmpty(item.getHref()));
            updateChannelState(CHANNEL_PLAYED_TRACKURI, valueOrEmpty(item.getUri()));
            updateChannelState(CHANNEL_PLAYED_TRACKNAME, valueOrEmpty(item.getName()));
            updateChannelState(CHANNEL_PLAYED_TRACKTYPE, valueOrEmpty(item.getType()));
            updateChannelState(CHANNEL_PLAYED_TRACKNUMBER, valueOrZero(item.getTrackNumber()));
            updateChannelState(CHANNEL_PLAYED_TRACKDISCNUMBER, valueOrZero(item.getDiscNumber()));
            updateChannelState(CHANNEL_PLAYED_TRACKPOPULARITY, valueOrZero(item.getPopularity()));

            final boolean hasAlbum = hasItem && item.getAlbum() != null;
            final Album album = hasAlbum ? item.getAlbum() : EMPTY_ALBUM;
            updateChannelState(CHANNEL_PLAYED_ALBUMID, valueOrEmpty(album.getId()));
            updateChannelState(CHANNEL_PLAYED_ALBUMHREF, valueOrEmpty(album.getHref()));
            updateChannelState(CHANNEL_PLAYED_ALBUMURI, valueOrEmpty(album.getUri()));
            updateChannelState(CHANNEL_PLAYED_ALBUMNAME, valueOrEmpty(album.getName()));
            updateChannelState(CHANNEL_PLAYED_ALBUMTYPE, valueOrEmpty(album.getType()));
            albumUpdater.updateAlbumImage(album);

            final Artist firstArtist = hasItem && item.getArtists() != null && !item.getArtists().isEmpty()
                    ? item.getArtists().get(0)
                    : EMPTY_ARTIST;

            updateChannelState(CHANNEL_PLAYED_ARTISTID, valueOrEmpty(firstArtist.getId()));
            updateChannelState(CHANNEL_PLAYED_ARTISTHREF, valueOrEmpty(firstArtist.getHref()));
            updateChannelState(CHANNEL_PLAYED_ARTISTURI, valueOrEmpty(firstArtist.getUri()));
            updateChannelState(CHANNEL_PLAYED_ARTISTNAME, valueOrEmpty(firstArtist.getName()));
            updateChannelState(CHANNEL_PLAYED_ARTISTTYPE, valueOrEmpty(firstArtist.getType()));
        }
        final Device device = playerInfo.getDevice() == null ? EMPTY_DEVICE : playerInfo.getDevice();
        // Only update activeDeviceId if it has a value, otherwise keep old value.
        if (device.getId() != null) {
            lastKnownDeviceId = device.getId();
            updateChannelState(CHANNEL_DEVICEID, valueOrEmpty(lastKnownDeviceId));
            updateChannelState(CHANNEL_DEVICES, valueOrEmpty(lastKnownDeviceId));
            updateChannelState(CHANNEL_DEVICENAME, valueOrEmpty(device.getName()));
        }
        lastKnownDeviceActive = device.isActive();
        updateChannelState(CHANNEL_DEVICEACTIVE, lastKnownDeviceActive ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_DEVICETYPE, valueOrEmpty(device.getType()));

        // experienced situations where volume seemed to be undefined...
        updateChannelState(CHANNEL_DEVICEVOLUME,
                device.getVolumePercent() == null ? UnDefType.UNDEF : new PercentType(device.getVolumePercent()));
    }

    private void updateChannelsPlayList(CurrentlyPlayingContext playerInfo, @Nullable List<Playlist> playlists) {
        final Context context = playerInfo.getContext();
        final String playlistId;
        String playlistName = "";

        if (context != null && "playlist".equals(context.getType())) {
            playlistId = "spotify:playlist" + context.getUri().substring(context.getUri().lastIndexOf(':'));

            if (playlists != null) {
                final Optional<Playlist> optionalPlaylist = playlists.stream()
                        .filter(pl -> playlistId.equals(pl.getUri())).findFirst();

                playlistName = optionalPlaylist.isPresent() ? optionalPlaylist.get().getName() : "";
            }
        } else {
            playlistId = "";
        }
        updateChannelState(CHANNEL_PLAYLISTS, valueOrEmpty(playlistId));
        updateChannelState(CHANNEL_PLAYLISTNAME, valueOrEmpty(playlistName));
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
        final Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }

    }

    /**
     * Class that manages the current progress of a track. The actual progress is tracked with the user specified
     * interval, This class fills the in between seconds so the status will show a continues updating of the progress.
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
            } else if ((progressFuture == null || progressFuture.isCancelled()) && active) {
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
            final String formattedProgress;

            synchronized (MUSIC_TIME_FORMAT) {
                formattedProgress = MUSIC_TIME_FORMAT.format(new Date(progress));
            }
            updateChannelState(CHANNEL_PLAYED_TRACKPROGRESS_MS, new DecimalType(progress));
            updateChannelState(CHANNEL_PLAYED_TRACKPROGRESS_FMT, formattedProgress);
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
            final Channel channel = thing.getChannel(CHANNEL_PLAYED_ALBUMIMAGE);
            final List<Image> images = album.getImages();

            if (channel != null && images != null && !images.isEmpty()) {
                final String imageUrl = images.get(0).getUrl();

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
                final String imageUrl = lastAlbumImageUrl;
                scheduler.execute(() -> refreshAlbumAsynced(channelUID, imageUrl));
            }
        }

        private void refreshAlbumAsynced(ChannelUID channelUID, String imageUrl) {
            try {
                if (lastAlbumImageUrl.equals(imageUrl) && isLinked(channelUID)) {
                    final RawType image = HttpUtil.downloadImage(imageUrl, true, MAX_IMAGE_SIZE);
                    updateChannelState(CHANNEL_PLAYED_ALBUMIMAGE, image == null ? UnDefType.UNDEF : image);
                }
            } catch (RuntimeException e) {
                logger.debug("Async call to refresh Album image failed: ", e);
            }
        }
    }
}
