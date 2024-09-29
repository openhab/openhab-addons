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
package org.openhab.binding.mpd.internal.handler;

import static org.openhab.binding.mpd.internal.MPDBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mpd.internal.MPDBindingConstants;
import org.openhab.binding.mpd.internal.MPDConfiguration;
import org.openhab.binding.mpd.internal.action.MPDActions;
import org.openhab.binding.mpd.internal.protocol.MPDConnection;
import org.openhab.binding.mpd.internal.protocol.MPDSong;
import org.openhab.binding.mpd.internal.protocol.MPDStatus;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MPDHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDHandler extends BaseThingHandler implements MPDEventListener {

    private final Logger logger = LoggerFactory.getLogger(MPDHandler.class);

    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    private final MPDConnection connection;
    private int volume = 0;

    private @Nullable ScheduledFuture<?> futureUpdateStatus;
    private @Nullable ScheduledFuture<?> futureUpdateCurrentSong;

    public MPDHandler(Thing thing) {
        super(thing);
        connection = new MPDConnection(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleCommandRefresh(channelUID.getId());
        } else {
            handlePlayerCommand(channelUID.getId(), command);
        }
    }

    @Override
    public void initialize() {
        MPDConfiguration config = getConfigAs(MPDConfiguration.class);
        String uniquePropVal = String.format("%s-%d", config.getIpAddress(), config.getPort());
        updateProperty(MPDBindingConstants.UNIQUE_ID, uniquePropVal);

        updateStatus(ThingStatus.UNKNOWN);
        connection.start(config.getIpAddress(), config.getPort(), config.getPassword(),
                "OH-binding-" + getThing().getUID().getAsString());
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> future = this.futureUpdateStatus;
        if (future != null) {
            future.cancel(true);
        }

        future = this.futureUpdateCurrentSong;
        if (future != null) {
            future.cancel(true);
        }

        connection.dispose();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MPDActions.class);
    }

    /**
     * send a command to the music player daemon
     *
     * @param command command to send
     * @param parameter parameter of command
     */
    public void sendCommand(@Nullable String command, String... parameter) {
        if (command != null) {
            connection.sendCommand(command, parameter);
        } else {
            logger.warn("can't send null command");
        }
    }

    private void handleCommandRefresh(String channelId) {
        stateMap.remove(channelId);
        switch (channelId) {
            case CHANNEL_CONTROL:
            case CHANNEL_STOP:
            case CHANNEL_VOLUME:
                scheduleUpdateStatus();
                break;
            case CHANNEL_CURRENT_ALBUM:
            case CHANNEL_CURRENT_ARTIST:
            case CHANNEL_CURRENT_NAME:
            case CHANNEL_CURRENT_SONG:
            case CHANNEL_CURRENT_SONG_ID:
            case CHANNEL_CURRENT_TITLE:
            case CHANNEL_CURRENT_TRACK:
                scheduleUpdateCurrentSong();
                break;
        }
    }

    private synchronized void scheduleUpdateStatus() {
        logger.debug("scheduleUpdateStatus");
        ScheduledFuture<?> future = this.futureUpdateStatus;
        if (future == null || future.isCancelled() || future.isDone()) {
            this.futureUpdateStatus = scheduler.schedule(this::doUpdateStatus, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void doUpdateStatus() {
        connection.updateStatus();
    }

    private synchronized void scheduleUpdateCurrentSong() {
        logger.debug("scheduleUpdateCurrentSong");
        ScheduledFuture<?> future = this.futureUpdateCurrentSong;
        if (future == null || future.isCancelled() || future.isDone()) {
            this.futureUpdateCurrentSong = scheduler.schedule(this::doUpdateCurrentSong, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void doUpdateCurrentSong() {
        connection.updateCurrentSong();
    }

    private void handlePlayerCommand(String channelId, Command command) {
        switch (channelId) {
            case CHANNEL_CONTROL:
                handleCommandControl(command);
                break;
            case CHANNEL_STOP:
                handleCommandStop(command);
                break;
            case CHANNEL_VOLUME:
                handleCommandVolume(command);
                break;
        }
    }

    private void handleCommandControl(Command command) {
        if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                connection.play();
            } else if (command == PlayPauseType.PAUSE) {
                connection.pause();
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connection.playNext();
            } else if (command == NextPreviousType.PREVIOUS) {
                connection.playPrevious();
            }
        } else {
            // Rewind and Fast Forward are currently not implemented by the binding
            logger.debug("Control command {} is not supported", command);
        }
    }

    private void handleCommandStop(Command command) {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                connection.stop();
            } else if (command == OnOffType.OFF) {
                connection.play();
            }
        } else {
            logger.debug("Stop Command {} is not supported", command);
            return;
        }
    }

    private void handleCommandVolume(Command command) {
        int newValue = 0;
        if (command instanceof IncreaseDecreaseType) {
            if (command == IncreaseDecreaseType.INCREASE) {
                newValue = Math.min(100, volume + 1);
            } else if (command == IncreaseDecreaseType.DECREASE) {
                newValue = Math.max(0, volume - 1);
            }
        } else if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                newValue = 100;
            } else if (command == OnOffType.OFF) {
                newValue = 0;
            }
        } else if (command instanceof DecimalType decimalCommand) {
            newValue = decimalCommand.intValue();
        } else if (command instanceof PercentType percentCommand) {
            newValue = percentCommand.intValue();
        } else {
            logger.debug("Command {} is not supported to change volume", command);
            return;
        }

        connection.setVolume(newValue);
    }

    private void updateChannel(String channelID, State state) {
        State previousState = stateMap.put(channelID, state);
        if (previousState == null || !previousState.equals(state)) {
            updateState(channelID, state);
        }
    }

    @Override
    public void updateMPDStatus(MPDStatus status) {
        volume = status.getVolume();
        if (volume < 0 || volume > 100) {
            updateChannel(CHANNEL_VOLUME, UnDefType.UNDEF);
        } else {
            updateChannel(CHANNEL_VOLUME, new PercentType(volume));
        }

        State newControlState = UnDefType.UNDEF;
        switch (status.getState()) {
            case PLAY:
                newControlState = PlayPauseType.PLAY;
                break;
            case STOP:
            case PAUSE:
                newControlState = PlayPauseType.PAUSE;
                break;
        }
        updateChannel(CHANNEL_CONTROL, newControlState);

        State newStopState = OnOffType.OFF;
        if (status.getState() == MPDStatus.State.STOP) {
            newStopState = OnOffType.ON;
        }
        updateChannel(CHANNEL_STOP, newStopState);
    }

    @Override
    public void updateMPDSong(MPDSong song) {
        updateChannel(CHANNEL_CURRENT_ALBUM, new StringType(song.getAlbum()));
        updateChannel(CHANNEL_CURRENT_ARTIST, new StringType(song.getArtist()));
        updateChannel(CHANNEL_CURRENT_NAME, new StringType(song.getName()));
        updateChannel(CHANNEL_CURRENT_SONG, new DecimalType(song.getSong()));
        updateChannel(CHANNEL_CURRENT_SONG_ID, new DecimalType(song.getSongId()));
        updateChannel(CHANNEL_CURRENT_TITLE, new StringType(song.getTitle()));
        updateChannel(CHANNEL_CURRENT_TRACK, new DecimalType(song.getTrack()));
    }

    @Override
    public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }
}
