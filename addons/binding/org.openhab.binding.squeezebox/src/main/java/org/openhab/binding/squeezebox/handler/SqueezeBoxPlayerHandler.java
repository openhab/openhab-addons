/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.openhab.binding.squeezebox.config.SqueezeBoxPlayerConfig;
import org.openhab.binding.squeezebox.internal.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SqueezeBoxPlayerHandler} is responsible for handling states, which
 * are sent to/from channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Improved handling of player status, prevent REFRESH from causing exception
 */
public class SqueezeBoxPlayerHandler extends BaseThingHandler implements SqueezeBoxPlayerEventListener {

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
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
     * Creates SqueezeBox Player Handler
     *
     * @param thing
     */
    public SqueezeBoxPlayerHandler(Thing thing) {
        super(thing);
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
                        squeezeBoxServerHandler.setPlayingTime(mac, currentTime() - 5);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        squeezeBoxServerHandler.setPlayingTime(mac, currentTime() + 5);
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
    public void modeChangeEvent(String mac, String mode) {
        updateChannel(mac, CHANNEL_CONTROL, "play".equals(mode) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateChannel(mac, CHANNEL_STOP, mode.equals("stop") ? OnOffType.ON : OnOffType.OFF);
        if (isMe(mac)) {
            logger.trace("Mode: {} for mac: {}", mode, mac);
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
        try {
            byte[] data = HttpUtils.getData(coverArtUrl);
            updateChannel(mac, CHANNEL_COVERART_DATA, new RawType(data));
        } catch (Exception e) {
            logger.debug("Coul not get album art data", e);
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
        updateChannel(mac, CHANNEL_TITLE, new StringType(title));
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
     * Helper method to mute a players
     */
    private void mute() {
        unmuteVolume = currentVolume();
        squeezeBoxServerHandler.mute(mac);
    }

    /**
     * Helper method to get the current volume
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

    /**
     * Helper method to get the current track time
     *
     * @return
     */
    private int currentTime() {
        if (stateMap.containsKey(CHANNEL_CURRENT_PLAYING_TIME)) {
            return ((DecimalType) stateMap.get(CHANNEL_CURRENT_PLAYING_TIME)).intValue();
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

        timeCounterJob = scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
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
}
