/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.handler;

import static org.openhab.binding.allplay.AllPlayBindingConstants.*;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
import org.openhab.binding.allplay.internal.AllPlayBindingProperties;
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
import de.kaizencode.tchaikovsky.speaker.ZoneItem;

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
    private final AllPlayBindingProperties bindingProperties;
    private Speaker speaker;
    private VolumeRange volumeRange;

    private static final String ALLPLAY_THREADPOOL_NAME = "allplayHandler";
    private ScheduledFuture<?> reconnectionJob;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(ALLPLAY_THREADPOOL_NAME);

    public AllPlayHandler(Thing thing, AllPlay allPlay, AllPlayBindingProperties properties) {
        super(thing);
        this.allPlay = allPlay;
        this.bindingProperties = properties;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AllPlay handler for speaker {}", getDeviceId());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for speaker to be discovered");
        try {
            allPlay.addSpeakerAnnouncedListener(this);
            discoverSpeaker();
        } catch (DiscoveryException e) {
            logger.error("Unable to discover speaker {}", getDeviceId(), e);
        }
    }

    /**
     * Tries to discover the speaker which is associated with this thing.
     */
    public void discoverSpeaker() {
        try {
            logger.debug("Starting discovery for speaker {}", getDeviceId());
            allPlay.discoverSpeaker(getDeviceId());
        } catch (DiscoveryException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to discover speaker: " + e.getMessage());
            logger.error("Unable to discover speaker {}", getDeviceId(), e);
        }
    }

    @Override
    public void onSpeakerAnnounced(Speaker speaker) {
        logger.debug("Speaker announcement received for speaker {}. Own id is {}", speaker, getDeviceId());
        if (isHandledSpeaker(speaker)) {
            logger.debug("Speaker announcement received for handled speaker {}", speaker);
            if (this.speaker != null) {
                // Make sure to disconnect first in case the speaker is re-announced
                disconnectFromSpeaker(this.speaker);
            }
            this.speaker = speaker;
            cancelReconnectionJob();
            try {
                connectToSpeaker();
            } catch (AllPlayException e) {
                logger.debug("Connection error", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while communicating with speaker: " + e.getMessage());
                scheduleReconnectionJob(speaker);
            }
        }
    }

    private void connectToSpeaker() throws ConnectionException {
        if (speaker != null) {
            logger.debug("Connecting to speaker {}", speaker);
            speaker.addSpeakerChangedListener(this);
            speaker.addSpeakerConnectionListener(this);
            speaker.connect();
            logger.debug("Connected to speaker {}", speaker);
            updateStatus(ThingStatus.ONLINE);
            try {
                initSpeakerState();
            } catch (SpeakerException e) {
                logger.error("Unable to init speaker state", e);
            }
        } else {
            logger.error("Speaker {} not discovered yet, cannot connect", getDeviceId());
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Lost connection to speaker");
            speaker.removeSpeakerConnectionListener(this);
            speaker.removeSpeakerChangedListener(this);
            scheduleReconnectionJob(speaker);
        }
    }

    @Override
    public void dispose() {
        allPlay.removeSpeakerAnnouncedListener(this);
        if (speaker != null) {
            disconnectFromSpeaker(speaker);
        }
        super.dispose();
    }

    private void disconnectFromSpeaker(Speaker speaker) {
        logger.debug("Disconnecting from speaker {}", speaker);
        speaker.removeSpeakerChangedListener(this);
        speaker.removeSpeakerConnectionListener(this);
        cancelReconnectionJob();
        if (speaker.isConnected()) {
            speaker.disconnect();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        if (isSpeakerReady()) {
            try {
                if (command instanceof RefreshType) {
                    handleRefreshCommand(channelUID.getId());
                } else {
                    handleSpeakerCommand(channelUID.getId(), command);
                }
            } catch (SpeakerException e) {
                logger.error("Unable to execute command {} on channel {}", command, channelUID.getId(), e);
            }
        }
    }

    private void handleSpeakerCommand(String channelId, Command command) throws SpeakerException {
        switch (channelId) {
            case CLEAR_ZONE:
                if (OnOffType.ON.equals(command)) {
                    speaker.zoneManager().releaseZone();
                }
                break;
            case CONTROL:
                handleControlCommand(command);
                break;
            case INPUT:
                speaker.input().setInput(command.toString());
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
                handleShuffleModeCommand(command);
                break;
            case STREAM:
                logger.debug("Starting to stream URL: {}", command.toString());
                speaker.playItem(command.toString());
                break;
            case VOLUME:
                handleVolumeCommand(command);
                break;
            case ZONE_MEMBERS:
                handleZoneMembersCommand(command);
                break;
            default:
                logger.warn("Unable to handle command {} on unknown channel {}", command, channelId);
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
            case INPUT:
                onInputChanged(speaker.input().getActiveInput());
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
                updateState(ZONE_ID, new StringType(speaker.getPlayerInfo().getZoneInfo().getZoneId()));
                break;
            default:
                logger.debug("REFRESH command not implemented on channel {}", channelId);
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
                changeTrackPosition(bindingProperties.getFastForwardSkipTimeInSec() * 1000);
            } else if (command == RewindFastforwardType.REWIND) {
                changeTrackPosition(-bindingProperties.getRewindSkipTimeInSec() * 1000);
            }
        } else {
            logger.warn("Unknown control command: {}", command);
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
        logger.debug("Jumping from old track position {} ms to new position {} ms", currentPosition,
                currentPosition + positionOffsetInMs);
        speaker.setPosition(currentPosition + positionOffsetInMs);
    }

    /**
     * Uses the given {@link Command} to change the volume of the speaker.
     *
     * @param command The {@link Command} with the new volume
     * @throws SpeakerException Exception if the volume change failed
     */
    public void handleVolumeCommand(Command command) throws SpeakerException {
        if (command instanceof PercentType) {
            speaker.volume().setVolume(convertPercentToAbsoluteVolume((PercentType) command));
        } else if (command instanceof IncreaseDecreaseType) {
            int stepSize = (command == IncreaseDecreaseType.DECREASE ? -getVolumeStepSize() : getVolumeStepSize());
            speaker.volume().adjustVolume(stepSize);
        }
    }

    private void handleShuffleModeCommand(Command command) throws SpeakerException {
        if (OnOffType.ON.equals(command)) {
            speaker.setShuffleMode(ShuffleMode.SHUFFLE);
        } else if (OnOffType.OFF.equals(command)) {
            speaker.setShuffleMode(ShuffleMode.LINEAR);
        }
    }

    private void handleZoneMembersCommand(Command command) throws SpeakerException {
        String[] memberNames = command.toString().split(bindingProperties.getZoneMemberSeparator());
        logger.debug("{}: Creating new zone with members {}", speaker, String.join(", ", memberNames));
        List<String> memberIds = new ArrayList<>();
        for (String memberName : memberNames) {
            memberIds.add(getHandlerIdByLabel(memberName.trim()));
        }
        createZoneInNewThread(memberIds);
    }

    private void createZoneInNewThread(List<String> memberIds) {
        scheduler.execute(() -> {
            try {
                // This call blocks up to 10 seconds if one of the members is unreachable,
                // therefore it is executed in a new thread
                ZoneItem zone = speaker.zoneManager().createZone(memberIds);
                logger.debug("{}: Zone {} with member ids {} has been created", speaker, zone.getZoneId(),
                        String.join(", ", zone.getSlaves().keySet()));
            } catch (SpeakerException e) {
                logger.warn("{}: Cannot create zone", speaker, e);
            }
        });
    }

    @Override
    public void onPlayStateChanged(PlayState playState) {
        updatePlayState(playState);
        updatePlaylistItemsState(playState.getPlaylistItems());
    }

    @Override
    public void onPlaylistChanged() {
        logger.debug("{}: Playlist changed: No action", speaker.getName());
    }

    @Override
    public void onLoopModeChanged(LoopMode loopMode) {
        logger.debug("{}: LoopMode changed to {}", speaker.getName(), loopMode);
        updateState(LOOP_MODE, new StringType(loopMode.toString()));

    }

    @Override
    public void onShuffleModeChanged(ShuffleMode shuffleMode) {
        logger.debug("{}: ShuffleMode changed to {}", speaker.getName(), shuffleMode);
        OnOffType shuffleOnOff = (shuffleMode == ShuffleMode.SHUFFLE) ? OnOffType.ON : OnOffType.OFF;
        updateState(SHUFFLE_MODE, shuffleOnOff);
    }

    @Override
    public void onMuteChanged(boolean mute) {
        logger.debug("{}: Mute changed to {}", speaker.getName(), mute);
        updateState(MUTE, mute ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void onVolumeChanged(int volume) {
        logger.debug("{}: Volume changed to {}", speaker.getName(), volume);
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
        logger.debug("{}: Zone changed to {}", speaker.getName(), zoneId);
        updateState(ZONE_ID, new StringType(zoneId));
    }

    @Override
    public void onInputChanged(String input) {
        logger.debug("{}: Input changed to {}", speaker.getName(), input);
        updateState(INPUT, new StringType(input));
    }

    private void updatePlayState(PlayState playState) {
        logger.debug("{}: PlayState changed to {}", speaker.getName(), playState);
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
            updateState(CURRENT_ARTIST, UnDefType.NULL);
            updateState(CURRENT_ALBUM, UnDefType.NULL);
            updateState(CURRENT_TITLE, UnDefType.NULL);
            updateState(CURRENT_GENRE, UnDefType.NULL);
            updateState(CURRENT_URL, UnDefType.NULL);
            updateState(COVER_ART_URL, UnDefType.NULL);
            updateState(COVER_ART, UnDefType.NULL);
        }
    }

    private void updateCurrentItemState(PlaylistItem currentItem) {
        logger.debug("{}: PlaylistItem changed to {}", speaker.getName(), currentItem);
        updateState(CURRENT_ARTIST, new StringType(currentItem.getArtist()));
        updateState(CURRENT_ALBUM, new StringType(currentItem.getAlbum()));
        updateState(CURRENT_TITLE, new StringType(currentItem.getTitle()));
        updateState(CURRENT_GENRE, new StringType(currentItem.getGenre()));
        updateDuration(currentItem.getDurationInMs());
        updateState(CURRENT_URL, new StringType(currentItem.getUrl()));
        updateCoverArtState(currentItem.getThumbnailUrl());

        try {
            updateState(CURRENT_USER_DATA, new StringType(String.valueOf(currentItem.getUserData())));
        } catch (SpeakerException e) {
            logger.warn("Unable to update current user data: {}", e.getMessage(), e);
        }
        logger.debug("MediaType: {}", currentItem.getMediaType());
    }

    private void updateDuration(long durationInMs) {
        DecimalType duration = new DecimalType(durationInMs / 1000);
        duration.format("%d s");
        updateState(CURRENT_DURATION, duration);
    }

    private void updateCoverArtState(String coverArtUrl) {
        try {
            logger.debug("{}: Cover art URL changed to {}", speaker.getName(), coverArtUrl);
            updateState(COVER_ART_URL, new StringType(coverArtUrl));
            if (!coverArtUrl.isEmpty()) {
                updateState(COVER_ART, new RawType(getRawDataFromUrl(coverArtUrl)));
            } else {
                updateState(COVER_ART, UnDefType.NULL);
            }
        } catch (Exception e) {
            logger.warn("Error getting cover art", e);
        }
    }

    /**
     * Starts streaming the audio at the given URL.
     *
     * @param url The URL to stream
     * @throws SpeakerException Exception if the URL could not be streamed
     */
    public void playUrl(String url) throws SpeakerException {
        if (isSpeakerReady()) {
            speaker.playItem(url);
        } else {
            throw new SpeakerException(
                    "Cannot play audio stream, speaker " + speaker + " is not discovered/connected!");
        }
    }

    /**
     * @return The current volume of the speaker
     * @throws SpeakerException Exception if the volume could not be retrieved
     */
    public PercentType getVolume() throws SpeakerException {
        if (isSpeakerReady()) {
            return convertAbsoluteVolumeToPercent(speaker.volume().getVolume());
        } else {
            throw new SpeakerException("Cannot get volume, speaker " + speaker + " is not discovered/connected!");
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
        logger.debug("Volume {}% has been converted to absolute volume {}", percentVolume.intValue(), volume);
        return volume;
    }

    private PercentType convertAbsoluteVolumeToPercent(int volume) throws SpeakerException {
        int range = volumeRange.getMax() - volumeRange.getMin();
        int percentVolume = 0;
        if (range > 0) {
            percentVolume = (volume * 100) / range;
        }
        logger.debug("Absolute volume {} has been converted to volume {}%", volume, percentVolume);
        return new PercentType(percentVolume);
    }

    private boolean isSpeakerReady() {
        if (speaker == null || !speaker.isConnected()) {
            logger.warn("Cannot execute command, speaker {} is not discovered/connected!", speaker);
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
        logger.debug("Scheduling job to rediscover to speaker {}", speaker);
        // TODO: Check if it makes sense to repeat the discovery every x minutes or if the AllJoyn library is able to
        // handle re-discovery in _all_ cases.
        cancelReconnectionJob();
        reconnectionJob = scheduler.scheduleWithFixedDelay(runnable, 5, 600, TimeUnit.SECONDS);
    }

    /**
     * Cancels a scheduled reconnection job.
     */
    private void cancelReconnectionJob() {
        if (reconnectionJob != null) {
            reconnectionJob.cancel(true);
        }
    }

    private String getHandlerIdByLabel(String thingLabel) throws IllegalStateException {
        if (thingRegistry != null) {
            for (Thing thing : thingRegistry.getAll()) {
                if (thingLabel.equals(thing.getLabel())) {
                    return thing.getUID().getId();
                }
            }
        }
        throw new IllegalStateException("Could not find thing with label " + thingLabel);
    }
}
