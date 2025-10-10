/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tidal.internal.handler;

import static org.openhab.binding.tidal.internal.TidalBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tidal.internal.TidalAccountHandler;
import org.openhab.binding.tidal.internal.TidalBridgeConfiguration;
import org.openhab.binding.tidal.internal.api.TidalApi;
import org.openhab.binding.tidal.internal.api.exception.TidalAuthorizationException;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
import org.openhab.binding.tidal.internal.api.model.Album;
import org.openhab.binding.tidal.internal.api.model.Artist;
import org.openhab.binding.tidal.internal.api.model.Image;
import org.openhab.binding.tidal.internal.api.model.Playlist;
import org.openhab.binding.tidal.internal.api.model.Track;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TidalBridgeHandler} is the main class to manage Tidal WebAPI connection and update status of things.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TidalBridgeHandler extends BaseBridgeHandler implements TidalAccountHandler, AccessTokenRefreshListener {

    private static final Album EMPTY_ALBUM = new Album();
    private static final Artist EMPTY_ARTIST = new Artist();
    private static final SimpleDateFormat MUSIC_TIME_FORMAT = new SimpleDateFormat("m:ss");
    private static final int MAX_IMAGE_SIZE = 500000;
    /**
     * Only poll playlist once per hour (or when refresh is called).
     */
    private static final Duration POLL_PLAY_LIST_HOURS = Duration.ofHours(1);
    /**
     * After a command is handles. With the given delay a status poll request is triggered. The delay is to give Tidal
     * some time to handle the update.
     */
    private static final int POLL_DELAY_AFTER_COMMAND_S = 2;
    /**
     * Time between track progress status updates.
     */
    private static final int PROGRESS_STEP_S = 1;
    private static final long PROGRESS_STEP_MS = TimeUnit.SECONDS.toMillis(PROGRESS_STEP_S);

    private final Logger logger = LoggerFactory.getLogger(TidalBridgeHandler.class);
    // Object to synchronize poll status on
    private final Object pollSynchronization = new Object();
    private final ProgressUpdater progressUpdater = new ProgressUpdater();
    private final AlbumUpdater albumUpdater = new AlbumUpdater();
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final ChannelUID devicesChannelUID;
    private final ChannelUID playlistsChannelUID;

    // Field members assigned in initialize method
    private @NonNullByDefault({}) Future<?> pollingFuture;
    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) TidalApi tidalApi;
    private @NonNullByDefault({}) TidalBridgeConfiguration configuration;
    private @NonNullByDefault({}) TidalHandleCommands handleCommand;

    /**
     * Keep track if this instance is disposed. This avoids new scheduling to be started after dispose is called.
     */
    private volatile boolean active;
    private volatile State lastTrackId = StringType.EMPTY;
    private volatile String lastKnownDeviceId = "";
    private volatile boolean lastKnownDeviceActive;
    private int imageChannelAlbumImageIndex;
    private int imageChannelAlbumImageUrlIndex;

    public TidalBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        devicesChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_DEVICES);
        playlistsChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_PLAYLISTS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            List<Playlist> playLists = getTidalApi().getPlaylists(0, 0);
        }
    }

    @Override
    public void dispose() {
        active = false;
        cancelSchedulers();
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
            oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
            this.oAuthService = null;
        }
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(thing.getUID().getAsString());
        super.handleRemoval();
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
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    @Override
    public String getUser() {
        return thing.getProperties().getOrDefault(PROPERTY_TIDAL_USER, "");
    }

    @Override
    public boolean isOnline() {
        return thing.getStatus() == ThingStatus.ONLINE;
    }

    @Nullable
    public TidalApi getTidalApi() {
        return tidalApi;
    }

    @Override
    public boolean equalsThingUID(String thingUID) {
        return getThing().getUID().getAsString().equals(thingUID);
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            oAuthService.addExtraAuthField("test", "toto");
            String oAuthorizationUrl = oAuthService.getAuthorizationUrl(redirectUri, null,
                    thing.getUID().getAsString());

            String cChallenge = "E9Melhoa2OwvFrEMTJguCHaoeKt8URWbuGJSstw-cM";
            oAuthorizationUrl = oAuthorizationUrl + "&code_challenge_method=S256";
            oAuthorizationUrl = oAuthorizationUrl + "&code_challenge=" + cChallenge;
            return oAuthorizationUrl;
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            logger.debug("Make call to Tidal to get access token.");
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUri);
            final String user = updateProperties(credentials);
            logger.debug("Authorized for user: {}", user);
            startPolling();
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new TidalException(e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new TidalAuthorizationException(e.getMessage(), e);
        }
    }

    private String updateProperties(AccessTokenResponse credentials) {
        if (tidalApi != null) {
        }
        return "";
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        active = true;
        configuration = getConfigAs(TidalBridgeConfiguration.class);
        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                TIDAL_API_TOKEN_URL, TIDAL_AUTHORIZE_URL, configuration.clientId, configuration.clientSecret,
                TIDAL_SCOPES, true);
        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(TidalBridgeHandler.this);
        tidalApi = new TidalApi(oAuthService, scheduler, httpClient);
        handleCommand = new TidalHandleCommands(tidalApi);
        final Duration expiringPeriod = Duration.ofSeconds(configuration.refreshPeriod);

        // Start with update status by calling Tidal. If no credentials available no polling should be started.
        scheduler.execute(() -> {
            if (pollStatus()) {
                startPolling();
            }
        });
        imageChannelAlbumImageIndex = getIntChannelParameter(CHANNEL_PLAYED_ALBUMIMAGE, CHANNEL_CONFIG_IMAGE_INDEX, 0);
        imageChannelAlbumImageUrlIndex = getIntChannelParameter(CHANNEL_PLAYED_ALBUMIMAGEURL,
                CHANNEL_CONFIG_IMAGE_INDEX, 0);
    }

    private int getIntChannelParameter(String channelName, String parameter, int _default) {
        final Channel channel = thing.getChannel(channelName);
        final BigDecimal index = channel == null ? null : (BigDecimal) channel.getConfiguration().get(parameter);

        return index == null ? _default : index.intValue();
    }

    /**
     * Scheduled method to restart polling in case polling is not running.
     */
    private void scheduledPollingRestart() {
        synchronized (pollSynchronization) {
            try {
                final boolean pollingNotRunning = pollingFuture == null || pollingFuture.isCancelled();

                if (pollStatus() && pollingNotRunning) {
                    startPolling();
                }
            } catch (final RuntimeException e) {
                logger.debug("Restarting polling failed: ", e);
            }
        }
    }

    /**
     * This method initiates a new thread for polling the available Tidal Connect devices and update the player
     * information.
     */
    private void startPolling() {
        synchronized (pollSynchronization) {
            cancelSchedulers();
            if (active) {
                // List<Album> albumns = getTidalApi().getAlbums(0, 0);
                // List<Playlist> playLists = getTidalApi().getPlaylists(0, 0);
                // List<Artist> artists = getTidalApi().getArtists(0, 0);
                List<Track> tracks = getTidalApi().getTracks(0, 0);

                pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, configuration.refreshPeriod,
                        TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Calls the Tidal API and collects user data. Returns true if method completed without errors.
     *
     * @return true if method completed without errors.
     */
    private boolean pollStatus() {

        return true;
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
     * Updates the status of all child Tidal Device Things.
     *
     * @param tidalDevices list of Tidal devices
     * @param playing true if the current active device is playing
     */
    private void updateDevicesStatus(boolean playing) {
    }

    /**
     * Update the player data.
     *
     * @param playerInfo The object with the current playing context
     * @param playlists List of available playlists
     */
    private void updatePlayerInfo(List<Playlist> playlists) {
    }

    private void updateChannelsPlayList(@Nullable List<Playlist> playlists) {
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
         * Updates the progress with its actual values as provided by Tidal. Based on if the track is running or not
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
            /*
             * final Channel imageChannel = thing.getChannel(CHANNEL_PLAYED_ALBUMIMAGE);
             * final List<Image> images = album.getImages();
             *
             * // Update album image url channel
             * final String albumImageUrlUrl = albumUrl(images, imageChannelAlbumImageUrlIndex);
             * updateChannelState(CHANNEL_PLAYED_ALBUMIMAGEURL,
             * albumImageUrlUrl == null ? UnDefType.UNDEF : StringType.valueOf(albumImageUrlUrl));
             *
             * // Trigger image refresh of album image channel
             * final String albumImageUrl = albumUrl(images, imageChannelAlbumImageIndex);
             * if (imageChannel != null && albumImageUrl != null) {
             * if (!lastAlbumImageUrl.equals(albumImageUrl)) {
             * // Download the cover art in a different thread to not delay the other operations
             * lastAlbumImageUrl = albumImageUrl;
             * refreshAlbumImage(imageChannel.getUID());
             * } // else album image still the same so nothing to do
             * } else {
             * lastAlbumImageUrl = "";
             * updateChannelState(CHANNEL_PLAYED_ALBUMIMAGE, UnDefType.UNDEF);
             * }
             */
        }

        private @Nullable String albumUrl(@Nullable List<Image> images, int index) {
            return images == null || index >= images.size() || images.isEmpty() ? null : images.get(index).getUrl();
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
            } catch (final RuntimeException e) {
                logger.debug("Async call to refresh Album image failed: ", e);
            }
        }
    }
}
