/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.handler;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.squeezebox.SqueezeBoxBindingConstants;
import org.openhab.binding.squeezebox.internal.config.SqueezeBoxPlayerConfig;
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
 */
public class SqueezeBoxPlayerHandler extends BaseThingHandler implements SqueezeBoxPlayerEventListener {

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SQUEEZEBOXPLAYER_THING_TYPE);

    /**
     * We need to remember some states to change offsets in volume, time index,
     * etc..
     */
    protected Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<String, State>());

    /**
     * Keeps current track time
     */
    ScheduledFuture<?> timeCounterJob;

    /**
     * Local reference to our bridge
     */
    private SqueezeBoxServerHandler squeezeBoxServerHandler;

    /**
     * Our mac address, needed everywhere
     */
    private String mac;

    /**
     * Before we mute or recieve a mute event, store our current volume
     */
    private int unmuteVolume = 0;

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
    private int notificationSoundVolume = -1;

    private String callbackUrl;

    private static final ExpiringCacheMap<String, RawType> IMAGE_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(15)); // 15min

    /**
     * Creates SqueezeBox Player Handler
     *
     * @param thing
     */
    public SqueezeBoxPlayerHandler(@NonNull Thing thing, String callbackUrl) {
        super(thing);
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void initialize() {
        mac = getConfig().as(SqueezeBoxPlayerConfig.class).mac;
        timeCounter();
        updateBridgeStatus();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus();
    }

    private void updateBridgeStatus() {
        ThingStatus bridgeStatus = getBridge().getStatus();
        if (bridgeStatus == ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            squeezeBoxServerHandler = (SqueezeBoxServerHandler) getBridge().getHandler();
        } else if (bridgeStatus == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
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
        logger.debug("player thing {} disposed.", getThing().getUID());
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (squeezeBoxServerHandler == null) {
            logger.info("player thing {} has no server configured, ignoring command: {}", getThing().getUID(), command);
            return;
        }
        String mac = getConfigAs(SqueezeBoxPlayerConfig.class).mac;

        // Some of the code below is not designed to handle REFRESH
        if (command == RefreshType.REFRESH) {
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
                    mute();
                } else {
                    squeezeBoxServerHandler.unMute(mac, unmuteVolume);
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
                if (command instanceof PercentType) {
                    squeezeBoxServerHandler.setVolume(mac, ((PercentType) command).intValue());
                } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    squeezeBoxServerHandler.volumeUp(mac, currentVolume());
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    squeezeBoxServerHandler.volumeDown(mac, currentVolume());
                } else if (command.equals(OnOffType.OFF)) {
                    mute();
                } else if (command.equals(OnOffType.ON)) {
                    squeezeBoxServerHandler.unMute(mac, unmuteVolume);
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
                if (StringUtils.isBlank(command.toString())) {
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
            case CHANNEL_NOTIFICATION_SOUND_VOLUME:
                setNotificationSoundVolume(((PercentType) command));
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
        updateChannel(mac, CHANNEL_STOP, mode.equals("stop") ? OnOffType.ON : OnOffType.OFF);
        if (isMe(mac)) {
            playing = "play".equalsIgnoreCase(mode);
        }
    }

    @Override
    public void volumeChangeEvent(String mac, int volume) {
        volume = Math.min(100, volume);
        volume = Math.max(0, volume);
        updateChannel(mac, CHANNEL_VOLUME, new PercentType(volume));
    }

    @Override
    public void muteChangeEvent(String mac, boolean mute) {
        unmuteVolume = currentVolume();
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
    public void coverArtChangeEvent(String mac, String coverArtUrl) {
        updateChannel(mac, CHANNEL_COVERART_DATA, createImage(downloadImage(coverArtUrl)));
    }

    /**
     * Download and cache the image data from an URL.
     *
     * @param url The URL of the image to be downloaded.
     * @return A RawType object containing the image, null if the content type could not be found or the content type is
     *         not an image.
     */
    private RawType downloadImage(String url) {
        if (StringUtils.isNotEmpty(url)) {
            if (!IMAGE_CACHE.containsKey(url)) {
                IMAGE_CACHE.put(url, () -> {
                    logger.debug("Trying to download the content of URL {}", url);
                    return HttpUtil.downloadImage(url);
                });
            }
            RawType image = IMAGE_CACHE.get(url);
            if (image == null) {
                logger.debug("Failed to download the content of URL {}", url);
                return null;
            } else {
                return image;
            }
        }
        return null;
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
                try {
                    updateState(channelID, state);
                } catch (Exception e) {
                    logger.error("Could not update channel", e);
                }
            }
        }
    }

    /**
     * Helper method to mute a player
     */
    private void mute() {
        unmuteVolume = currentVolume();
        squeezeBoxServerHandler.mute(mac);
    }

    /**
     * Helper methods to get the current state of the player
     *
     * @return
     */
    private int currentVolume() {
        if (stateMap.containsKey(CHANNEL_VOLUME)) {
            return ((DecimalType) stateMap.get(CHANNEL_VOLUME)).intValue();
        } else {
            return 0;
        }
    }

    private int currentPlayingTime() {
        if (stateMap.containsKey(CHANNEL_CURRENT_PLAYING_TIME)) {
            return ((DecimalType) stateMap.get(CHANNEL_CURRENT_PLAYING_TIME)).intValue();
        } else {
            return 0;
        }
    }

    private int currentNumberPlaylistTracks() {
        if (stateMap.containsKey(CHANNEL_NUMBER_PLAYLIST_TRACKS)) {
            return ((DecimalType) stateMap.get(CHANNEL_NUMBER_PLAYLIST_TRACKS)).intValue();
        } else {
            return 0;
        }
    }

    private int currentPlaylistIndex() {
        if (stateMap.containsKey(CHANNEL_PLAYLIST_INDEX)) {
            return ((DecimalType) stateMap.get(CHANNEL_PLAYLIST_INDEX)).intValue();
        } else {
            return 0;
        }
    }

    private boolean currentPower() {
        if (stateMap.containsKey(CHANNEL_POWER)) {
            return (stateMap.get(CHANNEL_POWER).equals(OnOffType.ON) ? true : false);
        } else {
            return false;
        }
    }

    private boolean currentStop() {
        if (stateMap.containsKey(CHANNEL_STOP)) {
            return (stateMap.get(CHANNEL_STOP).equals(OnOffType.ON) ? true : false);
        } else {
            return false;
        }
    }

    private boolean currentControl() {
        if (stateMap.containsKey(CHANNEL_CONTROL)) {
            return (stateMap.get(CHANNEL_CONTROL).equals(PlayPauseType.PLAY) ? true : false);
        } else {
            return false;
        }
    }

    private boolean currentMute() {
        if (stateMap.containsKey(CHANNEL_MUTE)) {
            return (stateMap.get(CHANNEL_MUTE).equals(OnOffType.ON) ? true : false);
        } else {
            return false;
        }
    }

    private int currentShuffle() {
        if (stateMap.containsKey(CHANNEL_CURRENT_PLAYLIST_SHUFFLE)) {
            return ((DecimalType) stateMap.get(CHANNEL_CURRENT_PLAYLIST_SHUFFLE)).intValue();
        } else {
            return 0;
        }
    }

    private int currentRepeat() {
        if (stateMap.containsKey(CHANNEL_CURRENT_PLAYLIST_REPEAT)) {
            return ((DecimalType) stateMap.get(CHANNEL_CURRENT_PLAYLIST_REPEAT)).intValue();
        } else {
            return 0;
        }
    }

    /**
     * Ticks away when in a play state to keep current track time
     */
    private void timeCounter() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (playing) {
                    updateChannel(mac, CHANNEL_CURRENT_PLAYING_TIME, new DecimalType(currentTime++));
                }
            }
        };

        timeCounterJob = scheduler.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.SECONDS);
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
     * The following methods were added to enable notifications using the ESH AudioSink
     */
    public PercentType getNotificationSoundVolume() {
        if (notificationSoundVolume == -1) {
            // Initialize the value for the first time
            logger.debug("Initializing notification volume to current player volume");
            notificationSoundVolume = currentVolume();
            if (notificationSoundVolume != 0) {
                updateState(SqueezeBoxBindingConstants.CHANNEL_NOTIFICATION_SOUND_VOLUME,
                        new PercentType(notificationSoundVolume));
            }
        }
        return PercentType.valueOf(String.valueOf(notificationSoundVolume));
    }

    public void setNotificationSoundVolume(PercentType volume) {
        if (volume != null) {
            logger.debug("Set notification volume to: {}", volume.toString());
            notificationSoundVolume = volume.intValue();
        }
    }

    /*
     * Play the notification by 1) saving the state of the player, 2) stopping the current
     * playlist item, 3) adding the notification as a new playlist item, 4) playing the
     * new playlist item, and 5) restoring the player to its previous state.
     */
    public void playNotificationSoundURI(StringType uri) {
        logger.debug("Play notification sound on player {} at URI {}", mac, uri);

        SqueezeBoxPlayerState playerState = new SqueezeBoxPlayerState();
        playNotification(playerState, uri);
    }

    private void playNotification(SqueezeBoxPlayerState playerState, StringType uri) {
        if (squeezeBoxServerHandler == null) {
            logger.warn("Server handler is null in playNotification");
            return;
        }

        logger.debug("Setting up player for notification");
        if (!playerState.isPoweredOn()) {
            logger.debug("Powering on the player");
            squeezeBoxServerHandler.powerOn(mac);
        }
        if (playerState.isShuffling()) {
            logger.debug("Turning off shuffle");
            squeezeBoxServerHandler.setShuffleMode(mac, 0);
        }
        if (playerState.isRepeating()) {
            logger.debug("Turning off repeat");
            squeezeBoxServerHandler.setRepeatMode(mac, 0);
        }
        if (playerState.isPlaying()) {
            squeezeBoxServerHandler.stop(mac);
        }

        int notificationVolume = getNotificationSoundVolume().intValue();
        squeezeBoxServerHandler.setVolume(mac, notificationVolume);
        waitForVolume(notificationVolume);

        // Add the notification uri to the playlist, get the playlist item index, then play
        logger.debug("Playing notification");
        squeezeBoxServerHandler.addPlaylistItem(mac, uri.toString());
        if (!waitForPlaylistUpdate()) {
            // Give up since we timed out waiting for playlist to update
            squeezeBoxServerHandler.setVolume(mac, playerState.getVolume());
            waitForVolume(playerState.getVolume());
            return;
        }
        int newNumberPlaylistTracks = currentNumberPlaylistTracks();
        squeezeBoxServerHandler.playPlaylistItem(mac, newNumberPlaylistTracks - 1);
        waitForNotification();

        logger.debug("Restoring player state");
        // Mute the player to prevent any noise during the transition to previous state
        squeezeBoxServerHandler.setVolume(mac, 0);
        waitForVolume(0);
        // Remove the notification uri from the playlist
        squeezeBoxServerHandler.deletePlaylistItem(mac, newNumberPlaylistTracks - 1);
        waitForPlaylistUpdate();

        // Resume playing save playlist item if player wasn't stopped
        if (!playerState.isStopped()) {
            logger.debug("Resuming last item playing");
            squeezeBoxServerHandler.playPlaylistItem(mac, playerState.getPlaylistIndex());
            waitForPlaylistUpdate();
            // Note that setting the time doesn't work for remote streams
            squeezeBoxServerHandler.setPlayingTime(mac, playerState.getPlayingTime());
        }

        if (playerState.isStopped()) {
            logger.debug("Stopping the player");
            squeezeBoxServerHandler.stop(mac);
        } else if (playerState.isPlaying()) {
            logger.debug("Playing the playlist item");
            // Nothing to do; should already be playing due to call to playPlaylistItem above
        } else {
            logger.debug("Pausing the player");
            // Sometimes the first couple pauses don't work (really!)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            int count;
            final int maxPauseAttempts = 4;
            for (count = 0; count < maxPauseAttempts; count++) {
                squeezeBoxServerHandler.pause(mac);
                if (waitForPause()) {
                    break;
                }
            }
            if (count == maxPauseAttempts) {
                // Unable to pause, try to stop
                squeezeBoxServerHandler.stop(mac);
            }
        }
        // Now we can restore the volume and the remaining state items
        squeezeBoxServerHandler.setVolume(mac, playerState.getVolume());
        waitForVolume(playerState.getVolume());

        if (playerState.isShuffling()) {
            logger.debug("Restoring shuffle mode");
            squeezeBoxServerHandler.setShuffleMode(mac, playerState.getShuffle());
        }
        if (playerState.isRepeating()) {
            logger.debug("Restoring repeat mode");
            squeezeBoxServerHandler.setRepeatMode(mac, playerState.getRepeat());
        }
        if (playerState.isMuted()) {
            logger.debug("Re-muting the player");
            squeezeBoxServerHandler.mute(mac);
        }
        if (!playerState.isPoweredOn()) {
            logger.debug("Powering off the player");
            squeezeBoxServerHandler.powerOff(mac);
        }
    }

    /*
     * Monitor the number of playlist entries. When it changes, then we know the playlist
     * has been updated with the notification URL. There's probably an edge case here where
     * someone is updating the playlist at the same time, but that should be rare.
     */
    private boolean waitForPlaylistUpdate() {
        final int timeoutMaxCount = 50;

        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);

        logger.trace("Waiting up to {} ms for playlist to be updated...", timeoutMaxCount * 100);
        listener.resetPlaylistUpdated();
        int timeoutCount = 0;
        while (!listener.isPlaylistUpdated() && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        listener = null;
        return checkForTimeout(timeoutCount, timeoutMaxCount, "playlist to update");
    }

    /*
     * Monitor the status of the notification so that we know when it has finished playing
     */
    private boolean waitForNotification() {
        final int timeoutMaxCount = 300;

        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);

        logger.trace("Waiting up to {} ms for stop...", timeoutMaxCount * 100);
        listener.resetStopped();
        int timeoutCount = 0;
        while (!listener.isStopped() && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        listener = null;
        return checkForTimeout(timeoutCount, timeoutMaxCount, "stop");
    }

    /*
     * Wait for the volume status to equal the targetVolume
     */
    private boolean waitForVolume(int targetVolume) {
        final int timeoutMaxCount = 40;

        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);

        logger.trace("Waiting up to {} ms for volume to update...", timeoutMaxCount * 100);
        listener.resetVolumeUpdated();
        int timeoutCount = 0;
        while (!listener.isVolumeUpdated(targetVolume) && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        listener = null;
        return checkForTimeout(timeoutCount, timeoutMaxCount, "volume to update");
    }

    /*
     * Wait for the mode to reflect that the player is paused
     */
    private boolean waitForPause() {
        final int timeoutMaxCount = 25;

        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);

        logger.trace("Waiting up to {} ms for player to pause...", timeoutMaxCount * 100);
        listener.resetPaused();
        int timeoutCount = 0;
        while (!listener.isPaused() && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        listener = null;
        return checkForTimeout(timeoutCount, timeoutMaxCount, "player to pause");
    }

    private boolean checkForTimeout(int timeoutCount, int timeoutLimit, String message) {
        if (timeoutCount >= timeoutLimit) {
            logger.warn("TIMEOUT after {} waiting for {}!", timeoutCount * 100, message);
            return false;
        }
        logger.debug("Done waiting {} ms for {}", timeoutCount * 100, message);
        return true;
    }

    /*
     * Return the IP and port of the OH2 web server
     */
    public String getHostAndPort() {
        return callbackUrl;
    }

    /**
     * The {@link SqueezeBoxPlayerState} is responsible for saving the state of a player.
     *
     * @author Mark Hilbush - Added support for AudioSink and notifications
     */
    public class SqueezeBoxPlayerState {
        int savedVolume;
        boolean savedMute;
        boolean savedPower;
        boolean savedStop;
        boolean savedControl;
        int savedShuffle;
        int savedRepeat;
        int savedPlaylistIndex;
        int savedNumberPlaylistTracks;
        int savedPlayingTime;

        public SqueezeBoxPlayerState() {
            save();
        }

        private boolean isMuted() {
            return savedMute;
        }

        private boolean isPoweredOn() {
            return savedPower;
        }

        private boolean isStopped() {
            return savedStop;
        }

        private boolean isPlaying() {
            return savedControl;
        }

        private boolean isShuffling() {
            return savedShuffle == 0 ? false : true;
        }

        private int getShuffle() {
            return savedShuffle;
        }

        private boolean isRepeating() {
            return savedRepeat == 0 ? false : true;
        }

        private int getRepeat() {
            return savedRepeat;
        }

        private int getVolume() {
            return savedVolume;
        }

        private int getPlaylistIndex() {
            return savedPlaylistIndex;
        }

        private int getNumberPlaylistTracks() {
            return savedNumberPlaylistTracks;
        }

        private int getPlayingTime() {
            return savedPlayingTime;
        }

        private void save() {
            savedVolume = currentVolume();
            savedMute = currentMute();
            savedPower = currentPower();
            savedStop = currentStop();
            savedControl = currentControl();
            savedShuffle = currentShuffle();
            savedRepeat = currentRepeat();
            savedPlaylistIndex = currentPlaylistIndex();
            savedNumberPlaylistTracks = currentNumberPlaylistTracks();
            savedPlayingTime = currentPlayingTime();

            logger.debug("Cur State: vol={}, mut={}, pwr={}, stp={}, ctl={}, shf={}, rpt={}, tix={}, tnm={}, tim={}",
                    savedVolume, muteAsString(), powerAsString(), stopAsString(), controlAsString(), shuffleAsString(),
                    repeatAsString(), getPlaylistIndex(), getNumberPlaylistTracks(), getPlayingTime());
        }

        private String muteAsString() {
            return isMuted() ? "MUTED" : "NOT MUTED";
        }

        private String powerAsString() {
            return isPoweredOn() ? "ON" : "OFF";
        }

        private String stopAsString() {
            return isStopped() ? "STOPPED" : "NOT STOPPED";
        }

        private String controlAsString() {
            return isPlaying() ? "PLAYING" : "PAUSED";
        }

        private String shuffleAsString() {
            String shuffle = "OFF";
            if (getShuffle() == 1) {
                shuffle = "SONG";
            } else if (getShuffle() == 2) {
                shuffle = "ALBUM";
            }
            return shuffle;
        }

        private String repeatAsString() {
            String repeat = "OFF";
            if (getRepeat() == 1) {
                repeat = "SONG";
            } else if (getRepeat() == 2) {
                repeat = "PLAYLIST";
            }
            return repeat;
        }
    }
}
