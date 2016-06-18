/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.handler;

import static org.openhab.binding.allplay.AllPlayBindingConstants.*;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.allplay.AllPlayBindingConstants;
import org.openhab.binding.allplay.internal.CommonSpeakerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.AllPlay;
import de.kaizencode.tchaikovsky.exception.AllPlayException;
import de.kaizencode.tchaikovsky.exception.ConnectionException;
import de.kaizencode.tchaikovsky.exception.DiscoveryException;
import de.kaizencode.tchaikovsky.exception.SpeakerException;
import de.kaizencode.tchaikovsky.listener.SpeakerAnnouncedListener;
import de.kaizencode.tchaikovsky.listener.SpeakerChangedListener;
import de.kaizencode.tchaikovsky.listener.SpeakerConnectionListener;
import de.kaizencode.tchaikovsky.speaker.PlayState;
import de.kaizencode.tchaikovsky.speaker.PlayState.State;
import de.kaizencode.tchaikovsky.speaker.PlaylistItem;
import de.kaizencode.tchaikovsky.speaker.Speaker;
import de.kaizencode.tchaikovsky.speaker.Speaker.LoopMode;
import de.kaizencode.tchaikovsky.speaker.Speaker.ShuffleMode;
import de.kaizencode.tchaikovsky.speaker.VolumeRange;

/**
 * The {@link AllPlayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayHandler extends BaseThingHandler
        implements SpeakerChangedListener, SpeakerAnnouncedListener, SpeakerConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(AllPlayHandler.class);
    private final AllPlay allPlay;
    private final CommonSpeakerProperties speakerProperties;
    private Speaker speaker;
    private VolumeRange volumeRange;

    private static final String ALLPLAY_THREADPOOL_NAME = "allplayHandler";
    private ScheduledFuture<?> reconnectionJob;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(ALLPLAY_THREADPOOL_NAME);

    public AllPlayHandler(Thing thing, AllPlay allPlay, CommonSpeakerProperties properties) {
        super(thing);
        this.allPlay = allPlay;
        this.speakerProperties = properties;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AllPlay handler for speaker " + getDeviceId());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for speaker to be discovered");
        try {
            allPlay.addSpeakerAnnouncedListener(this);
            discoverSpeaker();
        } catch (DiscoveryException e) {
            logger.error("Unable to discover speaker", e);
        }
    }

    /**
     * Tries to discover the speaker which is associated with this thing.
     */
    public void discoverSpeaker() {
        try {
            logger.debug("Starting discovery for speaker " + getDeviceId());
            allPlay.discoverSpeaker(getDeviceId());
        } catch (DiscoveryException e) {
            logger.error("Unable to discover speaker " + getDeviceId(), e);
        }
    }

    @Override
    public void onSpeakerAnnounced(Speaker speaker) {
        logger.debug("Speaker announcement received for speaker " + speaker + ". Own id is " + getDeviceId());
        if (isHandledSpeaker(speaker)) {
            logger.info("Speaker announcement received for handled speaker " + speaker);
            this.speaker = speaker;
            cancelReconnectionJob();
            try {
                connectToSpeaker();
            } catch (AllPlayException e) {
                logger.error(
                        "Error while connecting to speaker" + speaker + ": " + e.getMessage() + ", scheduling retry");
                logger.debug("Connection error", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while communicating with speaker" + speaker + ": " + e.getMessage());
                scheduleReconnectionJob(speaker);
            }
        }
    }

    private void connectToSpeaker() throws ConnectionException {
        if (speaker != null) {
            logger.info("Connecting to speaker " + speaker);
            speaker.addSpeakerChangedListener(this);
            speaker.addSpeakerConnectionListener(this);
            speaker.connect();
            logger.info("Connected to speaker " + speaker);
            updateStatus(ThingStatus.ONLINE);
            try {
                initSpeakerState();
            } catch (SpeakerException e) {
                logger.error("Unable to init speaker state", e);
            }
        } else {
            logger.error("Speaker not discovered yet, cannot connect");
        }
    }

    private void initSpeakerState() throws SpeakerException {
        cacheVolumeRange();
        onMuteChanged(speaker.volume().isMute());
        onLoopModeChanged(speaker.getLoopMode());
        onShuffleModeChanged(speaker.getShuffleMode());
        onPlayStateChanged(speaker.getPlayState());
        onVolumeChanged(speaker.volume().getVolume());
        onVolumeControlChanged(speaker.volume().isControlEnabled());
    }

    /**
     * Cache the volume range as it will not change for the speaker.
     */
    private void cacheVolumeRange() throws SpeakerException {
        volumeRange = speaker.volume().getVolumeRange();
    }

    @Override
    public void onConnectionLost(String wellKnownName, int alljoynReasonCode) {
        if (isHandledSpeaker(wellKnownName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Lost connection to speaker.");
            speaker.removeSpeakerConnectionListener(this);
            speaker.removeSpeakerChangedListener(this);
            scheduleReconnectionJob(speaker);
        }
    }

    @Override
    public void dispose() {
        if (speaker != null) {
            logger.info("Disconnecting from speaker " + speaker.getName());
            cancelReconnectionJob();
            speaker.removeSpeakerChangedListener(this);
            speaker.removeSpeakerConnectionListener(this);
            allPlay.removeSpeakerAnnouncedListener(this);
            speaker.disconnect();
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel " + channelUID.getId() + " triggered with command " + command);
        if (isSpeakerReady()) {
            try {
                if (command instanceof RefreshType) {
                    handleRefreshCommand(channelUID.getId());
                } else {
                    handleSpeakerCommand(channelUID.getId(), command);
                }
            } catch (SpeakerException e) {
                logger.error("Unable to execute command " + command + " on channel " + channelUID.getId(), e);
            }
        }
    }

    private void handleSpeakerCommand(String channelId, Command command) throws SpeakerException {
        switch (channelId) {
            case CONTROL:
                handleControlCommand(command);
                break;
            case LOOP_MODE:
                speaker.setLoopMode(LoopMode.parse(command.toString()));
                break;
            case MUTE:
                speaker.volume().mute(OnOffType.ON.equals(command));
                break;
            case STOP:
                speaker.stop();
                break;
            case SHUFFLE_MODE:
                speaker.setShuffleMode(ShuffleMode.parse(command.toString()));
                break;
            case STREAM:
                logger.debug("Starting to stream URL: " + command.toString());
                speaker.playItem(command.toString());
                break;
            case VOLUME:
                handleVolumeCommand(command);
                break;
            default:
                logger.warn("Unable to handle command " + command + " on unknown channel " + channelId);
        }
    }

    private void handleRefreshCommand(String channelId) throws SpeakerException {
        switch (channelId) {
            case CURRENT_ARTIST:
            case CURRENT_ALBUM:
            case CURRENT_DURATION:
            case CURRENT_GENRE:
            case CURRENT_TITLE:
            case CURRENT_URL:
                updatePlaylistItemsState(speaker.getPlayState().getPlaylistItems());
                break;
            case CONTROL:
                updatePlayState(speaker.getPlayState());
                break;
            case LOOP_MODE:
                onLoopModeChanged(speaker.getLoopMode());
                break;
            case MUTE:
                onMuteChanged(speaker.volume().isMute());
                break;
            case SHUFFLE_MODE:
                onShuffleModeChanged(speaker.getShuffleMode());
                break;
            case VOLUME:
                onVolumeChanged(speaker.volume().getVolume());
                onVolumeControlChanged(speaker.volume().isControlEnabled());
                break;
            case ZONE_ID:
                logger.debug("Refresh of ZoneID not yet implemented");
                // TODO: Get ZoneID from speaker and update channel when implemented in allPlay library
                break;
            default:
                logger.debug("REFRESH command not implemented on channel " + channelId);
        }
    }

    private void handleControlCommand(Command command) throws SpeakerException {
        if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                speaker.resume();
            } else if (command == PlayPauseType.PAUSE) {
                speaker.pause();
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                speaker.next();
            } else if (command == NextPreviousType.PREVIOUS) {
                speaker.previous();
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                changeTrackPosition(speakerProperties.getFastForwardSkipTimeInSec() * 1000);
            } else if (command == RewindFastforwardType.REWIND) {
                changeTrackPosition(-speakerProperties.getRewindSkipTimeInSec() * 1000);
            }
        } else {
            logger.warn("Unknown control command: " + command);
        }
    }

    /**
     * Changes the position in the current track.
     *
     * @param positionOffsetInMs The offset to adjust the current position. Can be negative or positive.
     * @throws SpeakerException Exception if the position could not be changed
     */
    private void changeTrackPosition(long positionOffsetInMs) throws SpeakerException {
        long currentPosition = speaker.getPlayState().getPositionInMs();
        logger.debug("Jumping from old track position :" + currentPosition + " ms to new position " + currentPosition
                + positionOffsetInMs + " ms");
        speaker.setPosition(currentPosition + positionOffsetInMs);
    }

    private void handleVolumeCommand(Command command) throws SpeakerException {
        if (command instanceof PercentType) {
            speaker.volume().setVolume(convertPercentToAbsoluteVolume((PercentType) command));
        } else if (command instanceof IncreaseDecreaseType) {
            int stepSize = (command == IncreaseDecreaseType.DECREASE ? -getVolumeStepSize() : getVolumeStepSize());
            speaker.volume().adjustVolume(stepSize);
        }
    }

    @Override
    public void onPlayStateChanged(PlayState playState) {
        updatePlayState(playState);
        updatePlaylistItemsState(playState.getPlaylistItems());
    }

    @Override
    public void onPlaylistChanged() {
        logger.debug(speaker.getName() + ": Playlist changed: No action");
    }

    @Override
    public void onLoopModeChanged(LoopMode loopMode) {
        logger.debug(speaker.getName() + ": LoopMode changed to " + loopMode);
        updateState(LOOP_MODE, new StringType(loopMode.toString()));

    }

    @Override
    public void onShuffleModeChanged(ShuffleMode shuffleMode) {
        logger.debug(speaker.getName() + ": ShuffleMode changed to " + shuffleMode);
        updateState(SHUFFLE_MODE, new StringType(shuffleMode.toString()));
    }

    @Override
    public void onMuteChanged(boolean mute) {
        logger.debug(speaker.getName() + ": Mute changed to " + mute);
        updateState(MUTE, mute ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void onVolumeChanged(int volume) {
        logger.debug(speaker.getName() + ": Volume changed to " + volume);
        try {
            updateState(VOLUME, convertAbsoluteVolumeToPercent(volume));
        } catch (SpeakerException e) {
            logger.warn("Cannot convert new volume to percent", e);
        }
    }

    @Override
    public void onVolumeControlChanged(boolean enabled) {
        updateState(VOLUME_CONTROL, enabled ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void onZoneChanged(String zoneId, int timestamp, Map<String, Integer> slaves) {
        logger.debug(speaker.getName() + ": Zone changed to " + zoneId);
        updateState(ZONE_ID, new StringType(zoneId));
    }

    private void updatePlayState(PlayState playState) {
        logger.debug(speaker.getName() + ": PlayState changed to " + playState);
        updateState(PLAY_STATE, new StringType(playState.getState().toString()));

        if (playState.getState() == State.PLAYING) {
            updateState(CONTROL, PlayPauseType.PLAY);
        } else {
            updateState(CONTROL, PlayPauseType.PAUSE);
        }
    }

    private void updatePlaylistItemsState(List<PlaylistItem> items) {
        if (!items.isEmpty()) {
            PlaylistItem currentItem = items.iterator().next();
            updateCurrentItemState(currentItem);
        } else {
            updateState(CURRENT_ARTIST, UnDefType.UNDEF);
            updateState(CURRENT_ALBUM, UnDefType.UNDEF);
            updateState(CURRENT_TITLE, UnDefType.UNDEF);
            updateState(CURRENT_GENRE, UnDefType.UNDEF);
            updateState(CURRENT_URL, UnDefType.UNDEF);
            updateState(COVER_ART_URL, UnDefType.UNDEF);
            updateState(COVER_ART, UnDefType.UNDEF);
        }
    }

    private void updateCurrentItemState(PlaylistItem currentItem) {
        logger.debug(speaker.getName() + ": PlaylistItem changed to " + currentItem);
        updateState(CURRENT_ARTIST, new StringType(currentItem.getArtist()));
        updateState(CURRENT_ALBUM, new StringType(currentItem.getAlbum()));
        updateState(CURRENT_TITLE, new StringType(currentItem.getTitle()));
        updateState(CURRENT_GENRE, new StringType(currentItem.getGenre()));
        updateState(CURRENT_DURATION, new DecimalType(currentItem.getDurationInMs()));
        updateState(CURRENT_URL, new StringType(currentItem.getUrl()));
        updateCoverArtState(currentItem.getThumbnailUrl());

        try {
            updateState(CURRENT_USER_DATA, new StringType(String.valueOf(currentItem.getUserData())));
        } catch (SpeakerException e) {
            logger.warn("Unable to update current user data: " + e.getMessage(), e);
        }
        logger.debug("MediaType: " + currentItem.getMediaType());
    }

    private void updateCoverArtState(String coverArtUrl) {
        try {
            logger.debug(speaker.getName() + ": Cover art URL changed to " + coverArtUrl);
            updateState(COVER_ART_URL, new StringType(coverArtUrl));
            if (!coverArtUrl.isEmpty()) {
                updateState(COVER_ART, new RawType(getRawDataFromUrl(coverArtUrl)));
            } else {
                updateState(COVER_ART, UnDefType.UNDEF);
            }
        } catch (Exception e) {
            logger.warn("Error getting cover art", e);
        }
    }

    private byte[] getRawDataFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return IOUtils.toByteArray(connection.getInputStream());
    }

    private int convertPercentToAbsoluteVolume(PercentType percentVolume) throws SpeakerException {
        int range = volumeRange.getMax() - volumeRange.getMin();
        int volume = (percentVolume.shortValue() * range) / 100;
        logger.debug("Volume " + percentVolume.intValue() + "% has been converted to absolute volume " + volume);
        return volume;
    }

    private PercentType convertAbsoluteVolumeToPercent(int volume) throws SpeakerException {
        int range = volumeRange.getMax() - volumeRange.getMin();
        int percentVolume = 0;
        if (range > 0) {
            percentVolume = (volume * 100) / range;
        }
        logger.debug("Absolute volume " + volume + " has been converted to volume " + percentVolume + "%");
        return new PercentType(percentVolume);
    }

    private boolean isSpeakerReady() {
        if (speaker == null || !speaker.isConnected()) {
            logger.warn("Cannot execute command, speaker " + speaker + " is not discovered/connected!");
            return false;
        }
        return true;
    }

    /**
     * @param speaker The {@link Speaker} to check
     * @return True if the {@link Speaker} is managed by this handler, else false
     */
    private boolean isHandledSpeaker(Speaker speaker) {
        return speaker.getId().equals(getDeviceId());
    }

    private boolean isHandledSpeaker(String wellKnownName) {
        return wellKnownName.equals(speaker.details().getWellKnownName());
    }

    private String getDeviceId() {
        return (String) getConfig().get(AllPlayBindingConstants.DEVICE_ID);
    }

    private Integer getVolumeStepSize() {
        return (Integer) getConfig().get(AllPlayBindingConstants.VOLUME_STEP_SIZE);
    }

    /**
     * Schedules a reconnection job.
     */
    private void scheduleReconnectionJob(final Speaker speaker) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                discoverSpeaker();
            }
        };
        logger.info("Scheduling job to rediscover to speaker " + speaker.getName());
        // TODO: Check if it makes sense to repeat the discovery every x minutes or if the AllJoyn library is able to
        // handle re-discovery in _all_ cases.
        reconnectionJob = scheduler.scheduleAtFixedRate(runnable, 5, 600, TimeUnit.SECONDS);
    }

    /**
     * Cancels a scheduled reconnection job.
     */
    private void cancelReconnectionJob() {
        if (reconnectionJob != null) {
            reconnectionJob.cancel(true);
        }
    }

}
