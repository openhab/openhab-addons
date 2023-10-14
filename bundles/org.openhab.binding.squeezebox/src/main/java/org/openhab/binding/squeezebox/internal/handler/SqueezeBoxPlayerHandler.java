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
package org.openhab.binding.squeezebox.internal.handler;

import static org.openhab.binding.squeezebox.internal.SqueezeBoxBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.squeezebox.internal.SqueezeBoxStateDescriptionOptionsProvider;
import org.openhab.binding.squeezebox.internal.config.SqueezeBoxPlayerConfig;
import org.openhab.binding.squeezebox.internal.model.Favorite;
import org.openhab.binding.squeezebox.internal.utils.SqueezeBoxTimeoutException;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SqueezeBoxPlayerHandler} is responsible for handling states, which
 * are sent to/from channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Improved handling of player status, prevent REFRESH from causing exception
 * @author Mark Hilbush - Implement AudioSink and notifications
 * @author Mark Hilbush - Added duration channel
 * @author Patrik Gfeller - Timeout for TTS messages increased from 30 to 90s.
 * @author Mark Hilbush - Get favorites from server and play favorite
 * @author Mark Hilbush - Convert sound notification volume from channel to config parameter
 * @author Mark Hilbush - Add like/unlike functionality
 */
public class SqueezeBoxPlayerHandler extends BaseThingHandler implements SqueezeBoxPlayerEventListener {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(SQUEEZEBOXPLAYER_THING_TYPE);

    /**
     * We need to remember some states to change offsets in volume, time index,
     * etc..
     */
    protected Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Keeps current track time
     */
    private ScheduledFuture<?> timeCounterJob;

    /**
     * Local reference to our bridge
     */
    private SqueezeBoxServerHandler squeezeBoxServerHandler;

    /**
     * Our mac address, needed everywhere
     */
    private String mac;

    /**
     * The server sends us the current time on play/pause/stop events, we
     * increment it locally from there on
     */
    private int currentTime = 0;

    /**
     * Our we playing something right now or not, need to keep current track
     * time
     */
    private boolean playing;

    /**
     * Separate volume level for notifications
     */
    private Integer notificationSoundVolume = null;

    private String callbackUrl;

    private SqueezeBoxStateDescriptionOptionsProvider stateDescriptionProvider;

    private static final ExpiringCacheMap<String, RawType> IMAGE_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(15)); // 15min

    private String likeCommand;
    private String unlikeCommand;
    private boolean connected = false;

    /**
     * Creates SqueezeBox Player Handler
     *
     * @param thing
     * @param stateDescriptionProvider
     */
    public SqueezeBoxPlayerHandler(@NonNull Thing thing, String callbackUrl,
            SqueezeBoxStateDescriptionOptionsProvider stateDescriptionProvider) {
        super(thing);
        this.callbackUrl = callbackUrl;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        mac = getConfig().as(SqueezeBoxPlayerConfig.class).mac;
        timeCounter();
        updateThingStatus();
        logger.debug("player thing {} initialized with mac {}", getThing().getUID(), mac);
        if (squeezeBoxServerHandler != null) {
            // ensure we get an up-to-date connection state
            squeezeBoxServerHandler.requestPlayers();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateThingStatus();
    }

    private void updateThingStatus() {
        Thing bridge = getBridge();
        if (bridge != null) {
            squeezeBoxServerHandler = (SqueezeBoxServerHandler) bridge.getHandler();
            ThingStatus bridgeStatus = bridge.getStatus();

            if (bridgeStatus == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else if (!this.connected) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
            } else if (bridgeStatus == ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not found");
        }
    }

    @Override
    public void dispose() {
        // stop our duration counter
        if (timeCounterJob != null && !timeCounterJob.isCancelled()) {
            timeCounterJob.cancel(true);
            timeCounterJob = null;
        }

        if (squeezeBoxServerHandler != null) {
            squeezeBoxServerHandler.removePlayerCache(mac);
        }
        logger.debug("player thing {} disposed for mac {}", getThing().getUID(), mac);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (squeezeBoxServerHandler == null) {
            logger.debug("Player {} has no server configured, ignoring command: {}", getThing().getUID(), command);
            return;
        }
        // Some of the code below is not designed to handle REFRESH, only reply to channels where cached values exist
        if (command == RefreshType.REFRESH) {
            String channelID = channelUID.getId();
            State newState = stateMap.get(channelID);
            if (newState != null) {
                updateState(channelID, newState);
            }
            return;
        }

        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_POWER:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.powerOn(mac);
                } else {
                    squeezeBoxServerHandler.powerOff(mac);
                }
                break;
            case CHANNEL_MUTE:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.mute(mac);
                } else {
                    squeezeBoxServerHandler.unMute(mac);
                }
                break;
            case CHANNEL_STOP:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.stop(mac);
                } else if (command.equals(OnOffType.OFF)) {
                    squeezeBoxServerHandler.play(mac);
                }
                break;
            case CHANNEL_PLAY_PAUSE:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.play(mac);
                } else if (command.equals(OnOffType.OFF)) {
                    squeezeBoxServerHandler.pause(mac);
                }
                break;
            case CHANNEL_PREV:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.prev(mac);
                }
                break;
            case CHANNEL_NEXT:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.next(mac);
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof PercentType percentCommand) {
                    squeezeBoxServerHandler.setVolume(mac, percentCommand.intValue());
                } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    squeezeBoxServerHandler.volumeUp(mac, currentVolume());
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    squeezeBoxServerHandler.volumeDown(mac, currentVolume());
                } else if (command.equals(OnOffType.OFF)) {
                    squeezeBoxServerHandler.mute(mac);
                } else if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.unMute(mac);
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        squeezeBoxServerHandler.play(mac);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        squeezeBoxServerHandler.pause(mac);
                    }
                }
                if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        squeezeBoxServerHandler.next(mac);
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        squeezeBoxServerHandler.prev(mac);
                    }
                }
                if (command instanceof RewindFastforwardType) {
                    if (command.equals(RewindFastforwardType.REWIND)) {
                        squeezeBoxServerHandler.setPlayingTime(mac, currentPlayingTime() - 5);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        squeezeBoxServerHandler.setPlayingTime(mac, currentPlayingTime() + 5);
                    }
                }
                break;
            case CHANNEL_STREAM:
                squeezeBoxServerHandler.playUrl(mac, command.toString());
                break;
            case CHANNEL_SYNC:
                if (command.toString().isBlank()) {
                    squeezeBoxServerHandler.unSyncPlayer(mac);
                } else {
                    squeezeBoxServerHandler.syncPlayer(mac, command.toString());
                }
                break;
            case CHANNEL_UNSYNC:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.unSyncPlayer(mac);
                }
                break;
            case CHANNEL_PLAYLIST_INDEX:
                squeezeBoxServerHandler.playPlaylistItem(mac, ((DecimalType) command).intValue());
                break;
            case CHANNEL_CURRENT_PLAYING_TIME:
                squeezeBoxServerHandler.setPlayingTime(mac, ((DecimalType) command).intValue());
                break;
            case CHANNEL_CURRENT_PLAYLIST_SHUFFLE:
                squeezeBoxServerHandler.setShuffleMode(mac, ((DecimalType) command).intValue());
                break;
            case CHANNEL_CURRENT_PLAYLIST_REPEAT:
                squeezeBoxServerHandler.setRepeatMode(mac, ((DecimalType) command).intValue());
                break;
            case CHANNEL_FAVORITES_PLAY:
                squeezeBoxServerHandler.playFavorite(mac, command.toString());
                break;
            case CHANNEL_RATE:
                if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.rate(mac, likeCommand);
                } else if (command.equals(OnOffType.OFF)) {
                    squeezeBoxServerHandler.rate(mac, unlikeCommand);
                }
                break;
            case CHANNEL_SLEEP:
                if (command instanceof DecimalType decimalCommand) {
                    Duration sleepDuration = Duration.ofMinutes(decimalCommand.longValue());
                    if (sleepDuration.isNegative() || sleepDuration.compareTo(Duration.ofDays(1)) > 0) {
                        logger.debug("Sleep timer of {} minutes must be >= 0 and <= 1 day", sleepDuration.toMinutes());
                        return;
                    }
                    squeezeBoxServerHandler.sleep(mac, sleepDuration);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void playerAdded(SqueezeBoxPlayer player) {
        // Player properties are saved in SqueezeBoxPlayerDiscoveryParticipant
    }

    @Override
    public void powerChangeEvent(String mac, boolean power) {
        updateChannel(mac, CHANNEL_POWER, power ? OnOffType.ON : OnOffType.OFF);
        if (!power && isMe(mac)) {
            playing = false;
        }
    }

    @Override
    public synchronized void modeChangeEvent(String mac, String mode) {
        updateChannel(mac, CHANNEL_CONTROL, "play".equals(mode) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateChannel(mac, CHANNEL_PLAY_PAUSE, "play".equals(mode) ? OnOffType.ON : OnOffType.OFF);
        updateChannel(mac, CHANNEL_STOP, "stop".equals(mode) ? OnOffType.ON : OnOffType.OFF);
        if (isMe(mac)) {
            playing = "play".equalsIgnoreCase(mode);
        }
    }

    @Override
    public void sourceChangeEvent(String mac, String source) {
        updateChannel(mac, CHANNEL_SOURCE, StringType.valueOf(source));
    }

    @Override
    public void absoluteVolumeChangeEvent(String mac, int volume) {
        int newVolume = volume;
        newVolume = Math.min(100, newVolume);
        newVolume = Math.max(0, newVolume);
        updateChannel(mac, CHANNEL_VOLUME, new PercentType(newVolume));
    }

    @Override
    public void relativeVolumeChangeEvent(String mac, int volumeChange) {
        int newVolume = currentVolume() + volumeChange;
        newVolume = Math.min(100, newVolume);
        newVolume = Math.max(0, newVolume);
        updateChannel(mac, CHANNEL_VOLUME, new PercentType(newVolume));

        if (isMe(mac)) {
            logger.trace("Volume changed [{}] for player {}. New volume: {}", volumeChange, mac, newVolume);
        }
    }

    @Override
    public void muteChangeEvent(String mac, boolean mute) {
        updateChannel(mac, CHANNEL_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void currentPlaylistIndexEvent(String mac, int index) {
        updateChannel(mac, CHANNEL_PLAYLIST_INDEX, new DecimalType(index));
    }

    @Override
    public void currentPlayingTimeEvent(String mac, int time) {
        updateChannel(mac, CHANNEL_CURRENT_PLAYING_TIME, new DecimalType(time));
        if (isMe(mac)) {
            currentTime = time;
        }
    }

    @Override
    public void durationEvent(String mac, int duration) {
        if (getThing().getChannel(CHANNEL_DURATION) == null) {
            logger.debug("Channel 'duration' does not exist.  Delete and readd player thing to pick up channel.");
            return;
        }
        updateChannel(mac, CHANNEL_DURATION, new DecimalType(duration));
    }

    @Override
    public void numberPlaylistTracksEvent(String mac, int track) {
        updateChannel(mac, CHANNEL_NUMBER_PLAYLIST_TRACKS, new DecimalType(track));
    }

    @Override
    public void currentPlaylistShuffleEvent(String mac, int shuffle) {
        updateChannel(mac, CHANNEL_CURRENT_PLAYLIST_SHUFFLE, new DecimalType(shuffle));
    }

    @Override
    public void currentPlaylistRepeatEvent(String mac, int repeat) {
        updateChannel(mac, CHANNEL_CURRENT_PLAYLIST_REPEAT, new DecimalType(repeat));
    }

    @Override
    public void titleChangeEvent(String mac, String title) {
        updateChannel(mac, CHANNEL_TITLE, new StringType(title));
    }

    @Override
    public void albumChangeEvent(String mac, String album) {
        updateChannel(mac, CHANNEL_ALBUM, new StringType(album));
    }

    @Override
    public void artistChangeEvent(String mac, String artist) {
        updateChannel(mac, CHANNEL_ARTIST, new StringType(artist));
    }

    @Override
    public void albumArtistChangeEvent(String mac, String albumArtist) {
        updateChannel(mac, CHANNEL_ALBUM_ARTIST, new StringType(albumArtist));
    }

    @Override
    public void trackArtistChangeEvent(String mac, String trackArtist) {
        updateChannel(mac, CHANNEL_TRACK_ARTIST, new StringType(trackArtist));
    }

    @Override
    public void bandChangeEvent(String mac, String band) {
        updateChannel(mac, CHANNEL_BAND, new StringType(band));
    }

    @Override
    public void composerChangeEvent(String mac, String composer) {
        updateChannel(mac, CHANNEL_COMPOSER, new StringType(composer));
    }

    @Override
    public void conductorChangeEvent(String mac, String conductor) {
        updateChannel(mac, CHANNEL_CONDUCTOR, new StringType(conductor));
    }

    @Override
    public void coverArtChangeEvent(String mac, String coverArtUrl) {
        updateChannel(mac, CHANNEL_COVERART_DATA, createImage(downloadImage(mac, coverArtUrl)));
    }

    /**
     * Download and cache the image data from an URL.
     *
     * @param url The URL of the image to be downloaded.
     * @return A RawType object containing the image, null if the content type could not be found or the content type is
     *         not an image.
     */
    private RawType downloadImage(String mac, String url) {
        // Only get the image if this is my PlayerHandler instance
        if (isMe(mac)) {
            if (url != null && !url.isEmpty()) {
                String sanitizedUrl = sanitizeUrl(url);
                RawType image = IMAGE_CACHE.putIfAbsentAndGet(url, () -> {
                    logger.debug("Trying to download the content of URL {}", sanitizedUrl);
                    try {
                        return HttpUtil.downloadImage(url);
                    } catch (IllegalArgumentException e) {
                        logger.debug("IllegalArgumentException when downloading image from {}", sanitizedUrl, e);
                        return null;
                    }
                });
                if (image == null) {
                    logger.debug("Failed to download the content of URL {}", sanitizedUrl);
                    return null;
                } else {
                    return image;
                }
            }
        }
        return null;
    }

    /*
     * Replaces the password in the URL, if present
     */
    private String sanitizeUrl(String url) {
        String sanitizedUrl = url;
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String[] userInfoParts = userInfo.split(":");
                if (userInfoParts.length == 2) {
                    sanitizedUrl = url.replace(userInfoParts[1], "**********");
                }
            }
        } catch (URISyntaxException e) {
            // Just return what was passed in
        }
        return sanitizedUrl;
    }

    /**
     * Wrap the given RawType and return it as {@link State} or return {@link UnDefType#UNDEF} if the RawType is null.
     */
    private State createImage(RawType image) {
        if (image == null) {
            return UnDefType.UNDEF;
        } else {
            return image;
        }
    }

    @Override
    public void yearChangeEvent(String mac, String year) {
        updateChannel(mac, CHANNEL_YEAR, new StringType(year));
    }

    @Override
    public void genreChangeEvent(String mac, String genre) {
        updateChannel(mac, CHANNEL_GENRE, new StringType(genre));
    }

    @Override
    public void remoteTitleChangeEvent(String mac, String title) {
        updateChannel(mac, CHANNEL_REMOTE_TITLE, new StringType(title));
    }

    @Override
    public void irCodeChangeEvent(String mac, String ircode) {
        if (isMe(mac)) {
            postCommand(CHANNEL_IRCODE, new StringType(ircode));
        }
    }

    @Override
    public void buttonsChangeEvent(String mac, String likeCommand, String unlikeCommand) {
        if (isMe(mac)) {
            this.likeCommand = likeCommand;
            this.unlikeCommand = unlikeCommand;
            logger.trace("Player {} got a button change event: like='{}' unlike='{}'", mac, likeCommand, unlikeCommand);
        }
    }

    @Override
    public void connectedStateChangeEvent(String mac, boolean connected) {
        if (isMe(mac)) {
            this.connected = connected;
            updateThingStatus();
        }
    }

    @Override
    public void updateFavoritesListEvent(List<Favorite> favorites) {
        logger.trace("Player {} updating favorites list with {} favorites", mac, favorites.size());
        List<StateOption> options = new ArrayList<>();
        for (Favorite favorite : favorites) {
            options.add(new StateOption(favorite.shortId, favorite.name));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FAVORITES_PLAY), options);
    }

    /**
     * Update a channel if the mac matches our own
     *
     * @param mac
     * @param channelID
     * @param state
     */
    private void updateChannel(String mac, String channelID, State state) {
        if (isMe(mac)) {
            State prevState = stateMap.put(channelID, state);
            if (prevState == null || !prevState.equals(state)) {
                logger.trace("Updating channel {} for thing {} with mac {} to state {}", channelID, getThing().getUID(),
                        mac, state);
                updateState(channelID, state);
            }
        }
    }

    /**
     * Helper methods to get the current state of the player
     *
     * @return
     */
    int currentVolume() {
        return cachedStateAsInt(CHANNEL_VOLUME);
    }

    int currentPlayingTime() {
        return cachedStateAsInt(CHANNEL_CURRENT_PLAYING_TIME);
    }

    int currentNumberPlaylistTracks() {
        return cachedStateAsInt(CHANNEL_NUMBER_PLAYLIST_TRACKS);
    }

    int currentPlaylistIndex() {
        return cachedStateAsInt(CHANNEL_PLAYLIST_INDEX);
    }

    boolean currentPower() {
        return cachedStateAsBoolean(CHANNEL_POWER, OnOffType.ON);
    }

    boolean currentStop() {
        return cachedStateAsBoolean(CHANNEL_STOP, OnOffType.ON);
    }

    boolean currentControl() {
        return cachedStateAsBoolean(CHANNEL_CONTROL, PlayPauseType.PLAY);
    }

    boolean currentMute() {
        return cachedStateAsBoolean(CHANNEL_MUTE, OnOffType.ON);
    }

    int currentShuffle() {
        return cachedStateAsInt(CHANNEL_CURRENT_PLAYLIST_SHUFFLE);
    }

    int currentRepeat() {
        return cachedStateAsInt(CHANNEL_CURRENT_PLAYLIST_REPEAT);
    }

    private boolean cachedStateAsBoolean(String key, @NonNull State activeState) {
        return activeState.equals(stateMap.get(key));
    }

    private int cachedStateAsInt(String key) {
        State state = stateMap.get(key);
        return state instanceof DecimalType decimalValue ? decimalValue.intValue() : 0;
    }

    /**
     * Ticks away when in a play state to keep current track time
     */
    private void timeCounter() {
        timeCounterJob = scheduler.scheduleWithFixedDelay(() -> {
            if (playing) {
                updateChannel(mac, CHANNEL_CURRENT_PLAYING_TIME, new DecimalType(currentTime++));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private boolean isMe(String mac) {
        return mac.equals(this.mac);
    }

    /**
     * Returns our server handler if set
     *
     * @return
     */
    public SqueezeBoxServerHandler getSqueezeBoxServerHandler() {
        return this.squeezeBoxServerHandler;
    }

    /**
     * Returns the MAC address for this player
     *
     * @return
     */
    public String getMac() {
        return this.mac;
    }

    /*
     * Give the notification player access to the notification timeout
     */
    public int getNotificationTimeout() {
        return getConfigAs(SqueezeBoxPlayerConfig.class).notificationTimeout;
    }

    /*
     * Used by the AudioSink to get the volume level that should be used for the notification.
     * Priority for determining volume is:
     * - volume is provided in the say/playSound actions
     * - volume is contained in the player thing's configuration
     * - current player volume setting
     */
    public PercentType getNotificationSoundVolume() {
        // Get the notification sound volume from this player thing's configuration
        Integer configNotificationSoundVolume = getConfigAs(SqueezeBoxPlayerConfig.class).notificationVolume;

        // Determine which volume to use
        Integer currentNotificationSoundVolume;
        if (notificationSoundVolume != null) {
            currentNotificationSoundVolume = notificationSoundVolume;
        } else if (configNotificationSoundVolume != null) {
            currentNotificationSoundVolume = configNotificationSoundVolume;
        } else {
            currentNotificationSoundVolume = Integer.valueOf(currentVolume());
        }
        return new PercentType(currentNotificationSoundVolume.intValue());
    }

    /*
     * Used by the AudioSink to set the volume level that should be used to play the notification
     */
    public void setNotificationSoundVolume(PercentType newNotificationSoundVolume) {
        if (newNotificationSoundVolume != null) {
            notificationSoundVolume = Integer.valueOf(newNotificationSoundVolume.intValue());
        }
    }

    /*
     * Play the notification.
     */
    public void playNotificationSoundURI(StringType uri) {
        logger.debug("Play notification sound on player {} at URI {}", mac, uri);

        try (SqueezeBoxNotificationPlayer notificationPlayer = new SqueezeBoxNotificationPlayer(this,
                squeezeBoxServerHandler, uri)) {
            notificationPlayer.play();
        } catch (InterruptedException e) {
            logger.warn("Notification playback was interrupted", e);
        } catch (SqueezeBoxTimeoutException e) {
            logger.debug("SqueezeBoxTimeoutException during notification: {}", e.getMessage());
        } finally {
            notificationSoundVolume = null;
        }
    }

    /*
     * Return the IP and port of the OH2 web server
     */
    public String getHostAndPort() {
        return callbackUrl;
    }
}
