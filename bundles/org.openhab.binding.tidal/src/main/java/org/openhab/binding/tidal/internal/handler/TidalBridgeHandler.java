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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tidal.internal.TidalAccountHandler;
import org.openhab.binding.tidal.internal.TidalBindingConstants;
import org.openhab.binding.tidal.internal.TidalBridgeConfiguration;
import org.openhab.binding.tidal.internal.api.TidalApi;
import org.openhab.binding.tidal.internal.api.exception.TidalAuthorizationException;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
import org.openhab.binding.tidal.internal.api.model.Album;
import org.openhab.binding.tidal.internal.api.model.Artist;
import org.openhab.binding.tidal.internal.api.model.BaseEntry;
import org.openhab.binding.tidal.internal.api.model.Playlist;
import org.openhab.binding.tidal.internal.api.model.Track;
import org.openhab.binding.tidal.internal.api.model.User;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.media.MediaListenner;
import org.openhab.core.media.MediaService;
import org.openhab.core.media.model.MediaAlbum;
import org.openhab.core.media.model.MediaArtist;
import org.openhab.core.media.model.MediaCollection;
import org.openhab.core.media.model.MediaEntry;
import org.openhab.core.media.model.MediaPlayList;
import org.openhab.core.media.model.MediaRegistry;
import org.openhab.core.media.model.MediaSource;
import org.openhab.core.media.model.MediaTrack;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TidalBridgeHandler} is the main class to manage Tidal WebAPI connection and update status of things.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TidalBridgeHandler extends BaseBridgeHandler
        implements TidalAccountHandler, AccessTokenRefreshListener, MediaListenner {

    private static final Album EMPTY_ALBUM = new Album();
    private static final Artist EMPTY_ARTIST = new Artist();
    private static final SimpleDateFormat MUSIC_TIME_FORMAT = new SimpleDateFormat("m:ss");
    private static final int MAX_IMAGE_SIZE = 500000;
    private String codeChallenge = "";
    private String codeVerifier = "";

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
    private final MediaService mediaService;

    /**
     * Keep track if this instance is disposed. This avoids new scheduling to be started after dispose is called.
     */
    private volatile boolean active;
    private volatile State lastTrackId = StringType.EMPTY;
    private volatile String lastKnownDeviceId = "";
    private volatile boolean lastKnownDeviceActive;
    private int imageChannelAlbumImageIndex;
    private int imageChannelAlbumImageUrlIndex;

    public TidalBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient,
            MediaService mediaService) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        this.mediaService = mediaService;
        devicesChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_DEVICES);
        playlistsChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_PLAYLISTS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            List<Playlist> playLists = getTidalApi().getPlaylists(0, 0);
        } else if (command instanceof PlayPauseType) {

        }
    }

    @Override
    public void dispose() {
        active = false;
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
        return thing.getProperties().getOrDefault(PROPERTY_TIDAL_USER_NAME, "");
    }

    @Override
    public String getUserId() {
        return thing.getProperties().getOrDefault(PROPERTY_TIDAL_USER_ID, "");
    }

    @Override
    public String getUserCountry() {
        return thing.getProperties().getOrDefault(PROPERTY_TIDAL_USER_COUNTRY, "");
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
            String oAuthorizationUrl = oAuthService.getAuthorizationUrl(redirectUri, null,
                    thing.getUID().getAsString());

            try {
                byte[] randomBytes = new byte[32];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(randomBytes);

                codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
                System.out.println("Base64 original : " + codeVerifier);

                // 3. Hasher la séquence via SHA-256
                byte[] hashBytes = MessageDigest.getInstance("SHA-256")
                        .digest(codeVerifier.getBytes(StandardCharsets.UTF_8));

                // 4. Encoder le hash en Base64
                codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
                System.out.println("SHA-256 Base64 : " + codeChallenge);
            } catch (Exception ex) {
                logger.info(ex.toString());
            }

            oAuthorizationUrl = oAuthorizationUrl + "&code_challenge_method=S256";
            oAuthorizationUrl = oAuthorizationUrl + "&code_challenge=" + codeChallenge;
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
            oAuthService.addExtraAuthField("code_verifier", codeVerifier);
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUri);
            final String user = updateProperties();
            logger.debug("Authorized for user: {}", user);
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new TidalException(e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new TidalAuthorizationException(e.getMessage(), e);
        }
    }

    private String updateProperties() {
        if (tidalApi != null) {
            final User user = tidalApi.getMe();
            final String userName = user.getUserName() == null ? user.getId() : user.getUserName();
            final String userId = user.getId();
            final String userCountry = user.getCountry();
            final Map<String, String> props = editProperties();

            props.put(PROPERTY_TIDAL_USER_NAME, userName);
            props.put(PROPERTY_TIDAL_USER_ID, userId);
            props.put(PROPERTY_TIDAL_USER_COUNTRY, userCountry);
            updateProperties(props);
            return userName;
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
                TIDAL_SCOPES, null);
        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(this);

        tidalApi = new TidalApi(oAuthService, scheduler, httpClient, this);
        updateProperties();
        handleCommand = new TidalHandleCommands(tidalApi);
        final Duration expiringPeriod = Duration.ofSeconds(configuration.refreshPeriod);

        mediaService.addMediaListenner(TidalBindingConstants.BINDING_ID, this);

        MediaRegistry mediaRegistry = mediaService.getMediaRegistry();

        mediaRegistry.registerEntry(TidalBindingConstants.BINDING_ID, () -> {
            return new MediaSource(TidalBindingConstants.BINDING_ID, TidalBindingConstants.BINDING_LABEL,
                    "/static/Tidal.png");
        });

        imageChannelAlbumImageIndex = getIntChannelParameter(CHANNEL_PLAYED_ALBUMIMAGE, CHANNEL_CONFIG_IMAGE_INDEX, 0);
        imageChannelAlbumImageUrlIndex = getIntChannelParameter(CHANNEL_PLAYED_ALBUMIMAGEURL,
                CHANNEL_CONFIG_IMAGE_INDEX, 0);
        updateStatus(ThingStatus.ONLINE);
    }

    private int getIntChannelParameter(String channelName, String parameter, int _default) {
        final Channel channel = thing.getChannel(channelName);
        final BigDecimal index = channel == null ? null : (BigDecimal) channel.getConfiguration().get(parameter);

        return index == null ? _default : index.intValue();
    }

    @Override
    public void onAccessTokenResponse(@Nullable AccessTokenResponse tokenResponse) {
        updateChannelState(CHANNEL_ACCESSTOKEN,
                new StringType(tokenResponse == null ? null : tokenResponse.getAccessToken()));
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

    @Override
    public void refreshEntry(MediaEntry mediaEntry, long start, long size) {
        if (mediaEntry.getKey().equals(TidalBindingConstants.BINDING_ID)) {

            mediaEntry.registerEntry("Albums", () -> {
                return new MediaCollection("Albums", "Albums", "/static/Albums.png");
            });

            mediaEntry.registerEntry("Artists", () -> {
                return new MediaCollection("Artists", "Artists", "/static/Artists.png");
            });

            mediaEntry.registerEntry("Playlists", () -> {
                return new MediaCollection("Playlists", "Playlists", "/static/playlist.png");
            });

            mediaEntry.registerEntry("Tracks", () -> {
                return new MediaCollection("Tracks", "Tracks", "/static/Tracks.png");
            });
        } else if ("Playlists".equals(mediaEntry.getKey())) {
            List<Playlist> playLists = tidalApi.getPlaylists(start, size);
            RegisterCollections(mediaEntry, playLists, MediaPlayList.class);
        } else if ("Albums".equals(mediaEntry.getKey())) {
            List<Album> albums = tidalApi.getAlbums(start, size);
            RegisterCollections(mediaEntry, albums, MediaAlbum.class);
        } else if ("Artists".equals(mediaEntry.getKey())) {
            List<Artist> artists = tidalApi.getArtists(start, size);
            RegisterCollections(mediaEntry, artists, MediaArtist.class);
        } else if ("Tracks".equals(mediaEntry.getKey())) {
            List<Track> tracks = tidalApi.getTracks(start, size);
            RegisterCollections(mediaEntry, tracks, MediaArtist.class);
        } else if (mediaEntry instanceof MediaArtist) {
            Artist artist = tidalApi.getArtist(mediaEntry.getKey());

            if (artist != null) {
                List<Album> albums = artist.getAlbums();
                RegisterCollections(mediaEntry, albums, MediaAlbum.class);
            }
        } else if (mediaEntry instanceof MediaAlbum) {
            Album album = tidalApi.getAlbum(mediaEntry.getKey());

            if (album != null) {
                List<Track> tracks = album.getTracks();
                RegisterCollections(mediaEntry, tracks, MediaTrack.class);
            }
        } else if (mediaEntry instanceof MediaPlayList) {
            Playlist playList = tidalApi.getPlaylist(mediaEntry.getKey());

            if (playList != null) {
                List<Track> tracks = playList.getTracks();
                RegisterCollections(mediaEntry, tracks, MediaTrack.class);

            }
        }
    }

    private <T extends BaseEntry, R extends MediaEntry> void RegisterCollections(MediaEntry parentEntry,
            List<T> collection, Class<R> allocator) {
        for (T entry : collection) {
            if (entry == null) {
                continue;
            }
            String key = entry.getId();
            String name = entry.getName();

            parentEntry.registerEntry(key, () -> {
                try {
                    MediaEntry res = allocator.getDeclaredConstructor().newInstance();
                    res.setName(name);
                    res.setKey(key);

                    if (res instanceof MediaCollection) {
                        String artWork = "";
                        if (entry.getArtwork() != null) {
                            artWork = entry.getArtwork();
                        }
                        ((MediaCollection) res).setArtUri(artWork);
                    }
                    return res;
                } catch (Exception ex) {
                    return null;
                }
            });
        }
    }
}
