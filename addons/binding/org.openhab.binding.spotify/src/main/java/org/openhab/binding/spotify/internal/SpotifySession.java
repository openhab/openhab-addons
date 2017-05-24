/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.spotify.SpotifyBindingConstants;
import org.openhab.binding.spotify.handler.SpotifyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SpotifySession} maintains an active session with Spotify WebAPI.
 * The refreshToken is used to retrieve an accessToken which is used through its validity time.
 * The class also provides wrappers for various API calls.
 *
 * @author Andreas Stenlund - Initial contribution
 */
public class SpotifySession implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SpotifySession.class);

    final int HTTP_CLIENT_RETRY_COUNT = 5;
    final int HTTP_CLIENT_TIMEOUT = 20;

    // Instantiate and configure the SslContextFactory
    private SslContextFactory sslContextFactory = new SslContextFactory();

    // Instantiate HttpClient with the SslContextFactory
    private HttpClient httpClient = new HttpClient(sslContextFactory);

    private String clientId = null;
    private String clientSecret = null;
    private String refreshToken = null;
    private String accessToken = null;
    private int tokenValidity = 3600;

    private SpotifyHandler spotifyPlayer = null;
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    @SuppressWarnings({ "rawtypes" })
    private ScheduledFuture future = null;

    private SpotifySession(String clientId, String clientSecret, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;

        httpClient.setFollowRedirects(true);
        if (!httpClient.isStarted()) {
            try {
                httpClient.start();
            } catch (Exception e) {
                logger.error("Error starting HttpClient", e);
            }

        }
    }

    public static SpotifySession getInstance(String clientId, String clientSecret, String refreshToken) {

        SpotifySession session = new SpotifySession(clientId, clientSecret, refreshToken);
        return session;
    }

    /**
     *
     */
    public void dispose() {
        if (future != null) {
            future.cancel(true);
        }

        // TODO: Not really need? Causing bad exceptions when refreshing SpotifySession in Controller check later....
        // if (httpClient != null && httpClient.isStarted()) {
        // try {
        // httpClient.stop();
        // } catch (Exception e) {
        // logger.error("Error stopping HttpClient", e);
        // }
        // }

    }

    /**
     * Call the Spotify WebAPI to authorize client and retrieve access and refresh tokens.
     *
     * This is step 6 in Spotify Web API authorization code flow.
     * See https://developer.spotify.com/web-api/authorization-guide/#authorization-code-flow
     *
     * @param callbackUrl
     * @param reqCode
     * @return
     */
    public SpotifyWebAPIAuthResult authenticate(String callbackUrl, String reqCode) {

        final String authString = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes());

        logger.debug("Sending Spotify Web API autorization request");

        String content = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s", reqCode, callbackUrl);

        String contentType = "application/x-www-form-urlencoded";

        for (int i = 0; i < HTTP_CLIENT_RETRY_COUNT; i++) {
            try {
                ContentResponse response = httpClient.POST("https://accounts.spotify.com/api/token")
                        .header("Authorization", "Basic " + authString)
                        .content(new StringContentProvider(content), contentType)
                        .timeout(HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS).send();

                logger.trace("Response Code: {}", response.getStatus());
                logger.trace("Response Data: {}", response.getContentAsString());

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    SpotifyWebAPIAuthResult test = gson.fromJson(response.getContentAsString(),
                            SpotifyWebAPIAuthResult.class);
                    accessToken = test.accessToken;
                    tokenValidity = test.getExpiresIn();
                    return test;
                } else if (response.getStatus() == 400) {
                    logger.error("Response: {} - verify that Spotify Client ID and Client Secret are correct!",
                            response.getContentAsString());

                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Error calling Spotify Web API for authorization - no accessToken!", e);
            }
            logger.debug("Attempt {} failed.", i + 1);
        }
        logger.error("Giving up on accessing Spotify WebAPI. Check network connectivity!");
        return null;
    }

    /**
     * Call WebAPI to get accessToken using the refreshToken.
     *
     * This is step 7 of the Spotify WebAPI authorization code flow
     * See https://developer.spotify.com/web-api/authorization-guide/#authorization-code-flow
     */
    private void refreshToken() {
        final String authString = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes());
        logger.debug("Sending Spotify Web API token refresh request");

        String content = "grant_type=refresh_token&refresh_token=" + refreshToken;
        String contentType = "application/x-www-form-urlencoded";

        for (int i = 0; i < HTTP_CLIENT_RETRY_COUNT; i++) {
            try {
                String url = "https://accounts.spotify.com/api/token";
                ContentResponse response = httpClient.POST(url).header("Authorization", "Basic " + authString)
                        .content(new StringContentProvider(content), contentType)
                        .timeout(HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS).send();

                logger.trace("Response Code: {}", response.getStatus());
                logger.trace("Response Data: {}", response.getContentAsString());

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    SpotifyWebAPIRefreshResult test = gson.fromJson(response.getContentAsString(),
                            SpotifyWebAPIRefreshResult.class);
                    accessToken = test.accessToken;
                    tokenValidity = test.getExpiresIn();
                    return;
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Error calling Spotify Web API for token refresh - no accessToken!", e);
            }
            logger.debug("Attempt {} failed.", i + 1);
        }
        logger.error("Giving up on accessing Spotify WebAPI. Check network connectivity!");
    }

    /**
     * Schedule timely refreshes of the accessToken.
     *
     * @return false if refresh is not successful
     */
    public boolean scheduleAccessTokenRefresh() {

        if (future != null && !future.isCancelled()) {
            // stop previous refresh thread
            future.cancel(true);
        }

        try {
            refreshToken();
        } catch (java.lang.NullPointerException npe) {
            // ignore
        }

        if (accessToken == null) {
            return false;
        }

        // TODO: Find a more suitable to retrieve token validity if it changes after being scheduled? Scheduling refresh
        // 10 seconds before expiring. Can this make use of existing thread/threadpools?
        tokenValidity -= 10;
        future = scheduledExecutorService.scheduleWithFixedDelay(this, tokenValidity, tokenValidity, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public void run() {
        spotifyPlayer.setChannelValue(SpotifyBindingConstants.CHANNEL_REFRESHTOKEN, OnOffType.ON);
        refreshToken();
        spotifyPlayer.setChannelValue(SpotifyBindingConstants.CHANNEL_REFRESHTOKEN, OnOffType.OFF);
    }

    /**
     * This method is a simple wrapper for Spotify WebAPI calls
     *
     * @param method the http method to use (GET, PUT, POST ..)
     * @param url the WebAPI url to call
     * @param requestData the body of the request, if any.
     * @return response from call
     */
    private String callWebAPI(String method, String url, String requestData) {
        logger.trace("Calling Spotify WebAPI at {}:{} with:\n{}", method, url, requestData);
        Properties headers = new Properties();
        headers.setProperty("Authorization", "Bearer " + accessToken);
        headers.setProperty("Accept", "application/json");

        String contentType = "application/json";
        ContentResponse response = null;

        // TODO: manage http timeout exceptions in a better way, currently crash the polling thread
        for (int i = 0; i < HTTP_CLIENT_RETRY_COUNT; i++) {
            try {
                response = httpClient.newRequest(url).method(HttpMethod.fromString(method))
                        .header("Authorization", "Bearer " + accessToken).header("Accept", "application/json")
                        .content(new StringContentProvider(requestData), contentType)
                        .timeout(HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS).send();

                logger.trace("Response Code: {}", response.getStatus());
                logger.trace("Response Data: {}", response.getContentAsString());

                // Response Code 429 means requests rate limits exceeded.
                if (response.getStatus() == 429) {
                    String retryAfter = response.getHeaders().get("Retry-After");
                    logger.warn(
                            "Spotify Web API returned code 429 (rate limit exceeded). Retry After {} seconds. Decrease polling interval of bridge! Going to sleep...",
                            retryAfter);

                    Thread.sleep(Integer.parseInt(retryAfter));
                } else {
                    return response.getContentAsString();
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Error refreshing spotify web api token!", e);
            }
            logger.debug("Attempt {} failed.", i + 1);

        }
        logger.error("Giving up on accessing Spotify WebAPI. Check network connectivity!");
        return new String();
    }

    /*
     * Spotify WebAPI calls
     */

    public void playTrack(String trackId) {
        String url = "https://api.spotify.com/v1/me/player/play";
        String jsonRequest = "{\"context_uri\":\"%s\",\"offset\":{\"position\":0}}";
        callWebAPI("PUT", url, String.format(jsonRequest, trackId));
    }

    public void playTrack(String deviceId, String trackId) {
        String url = "https://api.spotify.com/v1/me/player/play?device_id=%s";
        String jsonRequest = "{\"context_uri\":\"%s\",\"offset\":{\"position\":0}}";
        callWebAPI("PUT", String.format(url, deviceId), String.format(jsonRequest, trackId));
    }

    public void playActiveTrack() {
        String url = "https://api.spotify.com/v1/me/player/play";
        callWebAPI("PUT", url, "");
    }

    public void playActiveTrack(String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/play?device_id=%s";
        String jsonRequest = "";
        callWebAPI("PUT", String.format(url, deviceId), jsonRequest);
    }

    public void pauseActiveTrack() {
        String url = "https://api.spotify.com/v1/me/player/pause";
        callWebAPI("PUT", url, "");
    }

    public void pauseActiveTrack(String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/pause?device_id=%s";
        callWebAPI("PUT", String.format(url, deviceId), "");
    }

    public void nextTrack() {
        String url = "https://api.spotify.com/v1/me/player/next";
        callWebAPI("POST", url, "");
    }

    public void nextTrack(String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/next?device_id=%s";
        callWebAPI("POST", String.format(url, deviceId), "");
    }

    public void previousTrack() {
        String url = "https://api.spotify.com/v1/me/player/previous";
        callWebAPI("POST", url, "");
    }

    public void previousTrack(String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/previous?device_id=%s";
        callWebAPI("POST", String.format(url, deviceId), "");
    }

    public void setVolume(int volume) {
        String url = "https://api.spotify.com/v1/me/player/volume?volume_percent=%1d";
        String jsonRequest = "";
        callWebAPI("PUT", String.format(url, volume), jsonRequest);

    }

    public void setDeviceVolume(String deviceId, int volume) {
        String url = "https://api.spotify.com/v1/me/player/volume?device_id=%s&volume_percent=%1d";
        String jsonRequest = "";
        callWebAPI("PUT", String.format(url, deviceId, volume), jsonRequest);

    }

    public void setShuffleState(String state) {
        String url = "https://api.spotify.com/v1/me/player/shuffle?state=%s";
        String jsonRequest = "";
        callWebAPI("PUT", String.format(url, state), jsonRequest);
    }

    public void setShuffleState(String deviceId, String state) {
        String url = "https://api.spotify.com/v1/me/player/shuffle?state=%s&device_id=%s";
        String jsonRequest = "";
        callWebAPI("PUT", String.format(url, state, deviceId), jsonRequest);
    }

    public List<SpotifyWebAPIDeviceList.Device> listDevices() {
        String url = "https://api.spotify.com/v1/me/player/devices";
        String result = callWebAPI("GET", url, "");
        Gson gson = new Gson();
        SpotifyWebAPIDeviceList deviceList = gson.fromJson(result, SpotifyWebAPIDeviceList.class);
        if (deviceList == null || deviceList.getDevices() == null) {
            return Collections.emptyList();
        }
        return deviceList.getDevices();
    }

    public SpotifyWebAPIPlayerInfo getPlayerInfo() {
        String url = "https://api.spotify.com/v1/me/player";
        String result = callWebAPI("GET", url, "");
        Gson gson = new Gson();
        SpotifyWebAPIPlayerInfo playerInfo = gson.fromJson(result, SpotifyWebAPIPlayerInfo.class);
        return playerInfo;
    }

    /*
     * Inner classes used to parse JSON data
     */

    /**
     * This class and its inner classes represents the SpotifyWebAPI response of an authorization request
     *
     * @author Andreas Stenlund
     *
     */
    public class SpotifyWebAPIAuthResult {

        @SerializedName("access_token")
        @Expose
        private String accessToken;
        @SerializedName("refresh_token")
        @Expose
        private String refreshToken;
        @SerializedName("token_type")
        @Expose
        private String tokenType;
        @SerializedName("scope")
        @Expose
        private String scope;
        @SerializedName("expires_in")
        @Expose
        private Integer expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public Integer getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }

    }

    public class SpotifyWebAPIRefreshResult {

        @SerializedName("access_token")
        @Expose
        private String accessToken;
        @SerializedName("token_type")
        @Expose
        private String tokenType;
        @SerializedName("scope")
        @Expose
        private String scope;
        @SerializedName("expires_in")
        @Expose
        private Integer expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public Integer getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }

    }

    /**
     * This class and its inner classes represents the SpotifyWebAPI response with Player Information
     *
     * @author Andreas Stenlund
     *
     */
    public class SpotifyWebAPIPlayerInfo {

        @SerializedName("timestamp")
        @Expose
        private Long timestamp;
        @SerializedName("progress_ms")
        @Expose
        private Long progressMs;
        @SerializedName("is_playing")
        @Expose
        private Boolean isPlaying;
        @SerializedName("item")
        @Expose
        private Item item;
        @SerializedName("context")
        @Expose
        private Object context;
        @SerializedName("device")
        @Expose
        private Device device;
        @SerializedName("repeat_state")
        @Expose
        private String repeatState;
        @SerializedName("shuffle_state")
        @Expose
        private Boolean shuffleState;

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Long getProgressMs() {
            return progressMs;
        }

        public void setProgressMs(Long progressMs) {
            this.progressMs = progressMs;
        }

        public Boolean getIsPlaying() {
            return isPlaying;
        }

        public void setIsPlaying(Boolean isPlaying) {
            this.isPlaying = isPlaying;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public Object getContext() {
            return context;
        }

        public void setContext(Object context) {
            this.context = context;
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        public String getRepeatState() {
            return repeatState;
        }

        public void setRepeatState(String repeatState) {
            this.repeatState = repeatState;
        }

        public Boolean getShuffleState() {
            return shuffleState;
        }

        public void setShuffleState(Boolean shuffleState) {
            this.shuffleState = shuffleState;
        }

        /*
         * Inner classes of the SpotifyWebAPIPlayerInfo
         */
        public class Album {

            @SerializedName("album_type")
            @Expose
            private String albumType;
            @SerializedName("artists")
            @Expose
            private List<Artist> artists = null;
            @SerializedName("available_markets")
            @Expose
            private List<String> availableMarkets = null;
            @SerializedName("external_urls")
            @Expose
            private ExternalUrls externalUrls;
            @SerializedName("href")
            @Expose
            private String href;
            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("images")
            @Expose
            private List<Image> images = null;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("uri")
            @Expose
            private String uri;

            public String getAlbumType() {
                return albumType;
            }

            public void setAlbumType(String albumType) {
                this.albumType = albumType;
            }

            public List<Artist> getArtists() {
                return artists;
            }

            public void setArtists(List<Artist> artists) {
                this.artists = artists;
            }

            public List<String> getAvailableMarkets() {
                return availableMarkets;
            }

            public void setAvailableMarkets(List<String> availableMarkets) {
                this.availableMarkets = availableMarkets;
            }

            public ExternalUrls getExternalUrls() {
                return externalUrls;
            }

            public void setExternalUrls(ExternalUrls externalUrls) {
                this.externalUrls = externalUrls;
            }

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public List<Image> getImages() {
                return images;
            }

            public void setImages(List<Image> images) {
                this.images = images;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

        }

        public class Artist {

            @SerializedName("external_urls")
            @Expose
            private ExternalUrls externalUrls;
            @SerializedName("href")
            @Expose
            private String href;
            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("uri")
            @Expose
            private String uri;

            public ExternalUrls getExternalUrls() {
                return externalUrls;
            }

            public void setExternalUrls(ExternalUrls externalUrls) {
                this.externalUrls = externalUrls;
            }

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

        }

        public class Device {

            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("is_active")
            @Expose
            private Boolean isActive;
            @SerializedName("is_restricted")
            @Expose
            private Boolean isRestricted;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("volume_percent")
            @Expose
            private Integer volumePercent;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public Boolean getIsActive() {
                return isActive;
            }

            public void setIsActive(Boolean isActive) {
                this.isActive = isActive;
            }

            public Boolean getIsRestricted() {
                return isRestricted;
            }

            public void setIsRestricted(Boolean isRestricted) {
                this.isRestricted = isRestricted;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Integer getVolumePercent() {
                return volumePercent;
            }

            public void setVolumePercent(Integer volumePercent) {
                this.volumePercent = volumePercent;
            }

        }

        public class ExternalIds {

            @SerializedName("isrc")
            @Expose
            private String isrc;

            public String getIsrc() {
                return isrc;
            }

            public void setIsrc(String isrc) {
                this.isrc = isrc;
            }

        }

        public class ExternalUrls {

            @SerializedName("spotify")
            @Expose
            private String spotify;

            public String getSpotify() {
                return spotify;
            }

            public void setSpotify(String spotify) {
                this.spotify = spotify;
            }

        }

        public class Image {

            @SerializedName("height")
            @Expose
            private Integer height;
            @SerializedName("url")
            @Expose
            private String url;
            @SerializedName("width")
            @Expose
            private Integer width;

            public Integer getHeight() {
                return height;
            }

            public void setHeight(Integer height) {
                this.height = height;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public Integer getWidth() {
                return width;
            }

            public void setWidth(Integer width) {
                this.width = width;
            }

        }

        public class Item {

            @SerializedName("album")
            @Expose
            private Album album;
            @SerializedName("artists")
            @Expose
            private List<Artist> artists = null;
            @SerializedName("available_markets")
            @Expose
            private List<String> availableMarkets = null;
            @SerializedName("disc_number")
            @Expose
            private Integer discNumber;
            @SerializedName("duration_ms")
            @Expose
            private Long durationMs;
            @SerializedName("explicit")
            @Expose
            private Boolean explicit;
            @SerializedName("external_ids")
            @Expose
            private ExternalIds externalIds;
            @SerializedName("external_urls")
            @Expose
            private ExternalUrls externalUrls;
            @SerializedName("href")
            @Expose
            private String href;
            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("popularity")
            @Expose
            private Integer popularity;
            @SerializedName("preview_url")
            @Expose
            private String previewUrl;
            @SerializedName("track_number")
            @Expose
            private Integer trackNumber;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("uri")
            @Expose
            private String uri;

            public Album getAlbum() {
                return album;
            }

            public void setAlbum(Album album) {
                this.album = album;
            }

            public List<Artist> getArtists() {
                return artists;
            }

            public void setArtists(List<Artist> artists) {
                this.artists = artists;
            }

            public List<String> getAvailableMarkets() {
                return availableMarkets;
            }

            public void setAvailableMarkets(List<String> availableMarkets) {
                this.availableMarkets = availableMarkets;
            }

            public Integer getDiscNumber() {
                return discNumber;
            }

            public void setDiscNumber(Integer discNumber) {
                this.discNumber = discNumber;
            }

            public Long getDurationMs() {
                return durationMs;
            }

            public void setDurationMs(Long durationMs) {
                this.durationMs = durationMs;
            }

            public Boolean getExplicit() {
                return explicit;
            }

            public void setExplicit(Boolean explicit) {
                this.explicit = explicit;
            }

            public ExternalIds getExternalIds() {
                return externalIds;
            }

            public void setExternalIds(ExternalIds externalIds) {
                this.externalIds = externalIds;
            }

            public ExternalUrls getExternalUrls() {
                return externalUrls;
            }

            public void setExternalUrls(ExternalUrls externalUrls) {
                this.externalUrls = externalUrls;
            }

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Integer getPopularity() {
                return popularity;
            }

            public void setPopularity(Integer popularity) {
                this.popularity = popularity;
            }

            public String getPreviewUrl() {
                return previewUrl;
            }

            public void setPreviewUrl(String previewUrl) {
                this.previewUrl = previewUrl;
            }

            public Integer getTrackNumber() {
                return trackNumber;
            }

            public void setTrackNumber(Integer trackNumber) {
                this.trackNumber = trackNumber;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

        }
    }

    /**
     *
     * @author Andreas Stenlund
     *
     */
    public class SpotifyWebAPIDeviceList {

        public class Device {

            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("is_active")
            @Expose
            private Boolean isActive;
            @SerializedName("is_restricted")
            @Expose
            private Boolean isRestricted;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("volume_percent")
            @Expose
            private Integer volumePercent;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public Boolean getIsActive() {
                return isActive;
            }

            public void setIsActive(Boolean isActive) {
                this.isActive = isActive;
            }

            public Boolean getIsRestricted() {
                return isRestricted;
            }

            public void setIsRestricted(Boolean isRestricted) {
                this.isRestricted = isRestricted;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Integer getVolumePercent() {
                return volumePercent;
            }

            public void setVolumePercent(Integer volumePercent) {
                this.volumePercent = volumePercent;
            }

        }

        @SerializedName("devices")
        @Expose
        private List<Device> devices = null;

        public List<Device> getDevices() {
            return devices;
        }

        public void setDevices(List<Device> devices) {
            this.devices = devices;
        }

    }

}
