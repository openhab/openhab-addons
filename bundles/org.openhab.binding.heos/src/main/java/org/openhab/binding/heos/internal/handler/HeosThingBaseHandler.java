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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;
import static org.openhab.binding.heos.internal.json.dto.HeosCommandGroup.*;
import static org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;
import static org.openhab.core.thing.ThingStatus.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.exception.HeosFunctionalException;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.dto.HeosCommandTuple;
import org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute;
import org.openhab.binding.heos.internal.json.dto.HeosError;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosThingBaseHandler} class is the base Class all HEOS handler have to extend.
 * It provides basic command handling and common needed methods.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public abstract class HeosThingBaseHandler extends BaseThingHandler implements HeosEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosThingBaseHandler.class);
    private final HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;
    private final ChannelUID favoritesChannelUID;
    private final ChannelUID playlistsChannelUID;
    private final ChannelUID queueChannelUID;

    private @Nullable HeosChannelHandlerFactory channelHandlerFactory;
    protected @Nullable HeosBridgeHandler bridgeHandler;

    private String notificationVolume = "0";

    private int failureCount;
    private @Nullable Future<?> scheduleQueueFetchFuture;
    private @Nullable Future<?> handleDynamicStatesFuture;

    HeosThingBaseHandler(Thing thing, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(thing);
        this.heosDynamicStateDescriptionProvider = heosDynamicStateDescriptionProvider;
        favoritesChannelUID = new ChannelUID(thing.getUID(), CH_ID_FAVORITES);
        playlistsChannelUID = new ChannelUID(thing.getUID(), CH_ID_PLAYLISTS);
        queueChannelUID = new ChannelUID(thing.getUID(), CH_ID_QUEUE);
    }

    @Override
    public void initialize() {
        @Nullable
        Bridge bridge = getBridge();
        @Nullable
        HeosBridgeHandler localBridgeHandler;
        if (bridge != null) {
            localBridgeHandler = (HeosBridgeHandler) bridge.getHandler();
            if (localBridgeHandler != null) {
                bridgeHandler = localBridgeHandler;
                channelHandlerFactory = localBridgeHandler.getChannelHandlerFactory();
            } else {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
        } else {
            logger.warn("No Bridge set within child handler");
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        try {
            getApiConnection().registerForChangeEvents(this);
            cancel(scheduleQueueFetchFuture);
            scheduleQueueFetchFuture = scheduler.submit(this::fetchQueueFromPlayer);

            if (localBridgeHandler.isLoggedIn()) {
                scheduleImmediatelyHandleDynamicStatesSignedIn();
            }
        } catch (HeosNotConnectedException e) {
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    void handleSuccess() {
        failureCount = 0;
        updateStatus(ONLINE);
    }

    void handleError(Exception e) {
        logger.debug("Failed to handle player/group command", e);
        failureCount++;

        if (failureCount > FAILURE_COUNT_LIMIT) {
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to handle command: " + e.getMessage());
        }
    }

    public HeosFacade getApiConnection() throws HeosNotConnectedException {
        @Nullable
        HeosBridgeHandler localBridge = bridgeHandler;
        if (localBridge != null) {
            return localBridge.getApiConnection();
        }
        throw new HeosNotConnectedException();
    }

    public abstract String getId() throws HeosNotFoundException;

    public abstract void setStatusOffline();

    public abstract void setStatusOnline();

    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(notificationVolume);
    }

    public void setNotificationSoundVolume(PercentType volume) {
        notificationVolume = volume.toString();
    }

    @Nullable
    HeosChannelHandler getHeosChannelHandler(ChannelUID channelUID) {
        @Nullable
        HeosChannelHandlerFactory localChannelHandlerFactory = this.channelHandlerFactory;
        return localChannelHandlerFactory != null ? localChannelHandlerFactory.getChannelHandler(channelUID, this, null)
                : null;
    }

    @Override
    public void bridgeChangeEvent(String event, boolean success, Object command) {
        logger.debug("BridgeChangeEvent: {}", command);
        if (HeosEvent.USER_CHANGED == command) {
            handleDynamicStatesSignedIn();
        }

        if (EVENT_TYPE_EVENT.equals(event)) {
            if (HeosEvent.GROUPS_CHANGED == command) {
                fetchQueueFromPlayer();
            } else if (CONNECTION_RESTORED.equals(command)) {
                try {
                    refreshPlayState(getId());
                } catch (IOException | ReadException e) {
                    logger.debug("Failed to refreshPlayState", e);
                }
            }
        }
    }

    void scheduleImmediatelyHandleDynamicStatesSignedIn() {
        cancel(handleDynamicStatesFuture);
        handleDynamicStatesFuture = scheduler.submit(this::handleDynamicStatesSignedIn);
    }

    void handleDynamicStatesSignedIn() {
        try {
            heosDynamicStateDescriptionProvider.setFavorites(favoritesChannelUID, getApiConnection().getFavorites());
            heosDynamicStateDescriptionProvider.setPlaylists(playlistsChannelUID, getApiConnection().getPlaylists());
        } catch (IOException | ReadException e) {
            logger.debug("Failed to set favorites / playlists, rescheduling", e);
            cancel(handleDynamicStatesFuture, false);
            handleDynamicStatesFuture = scheduler.schedule(this::handleDynamicStatesSignedIn, 30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        } else if (ThingStatus.UNINITIALIZED.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Dispose the handler and unregister the handler
     * form Change Events
     */
    @Override
    public void dispose() {
        try {
            logger.debug("Disposing this: {}", this);
            getApiConnection().unregisterForChangeEvents(this);
        } catch (HeosNotConnectedException e) {
            logger.trace("No connection available while trying to unregister");
        }

        cancel(scheduleQueueFetchFuture);
        cancel(handleDynamicStatesFuture);
    }

    /**
     * Plays a media file from an external source. Can be
     * used for audio sink function
     *
     * @param urlStr The external URL where the file is located
     * @throws ReadException
     * @throws IOException
     */
    public void playURL(String urlStr) throws IOException, ReadException {
        try {
            URL url = new URL(urlStr);
            getApiConnection().playURL(getId(), url);
        } catch (MalformedURLException e) {
            logger.debug("Command '{}' is not a proper URL. Error: {}", urlStr, e.getMessage());
        }
    }

    /**
     * Handles the updates send from the HEOS system to
     * the binding. To receive updates the handler has
     * to register itself via {@link HeosFacade} via the method:
     * {@link HeosFacade#registerForChangeEvents(HeosEventListener)}
     *
     * @param eventObject containing information about the even which was sent to us by the HEOS device
     */
    protected void handleThingStateUpdate(HeosEventObject eventObject) {
        updateStatus(ONLINE, ThingStatusDetail.NONE, "Receiving events");

        @Nullable
        HeosEvent command = eventObject.command;

        if (command == null) {
            logger.debug("Ignoring event with null command");
            return;
        }

        switch (command) {
            case PLAYER_STATE_CHANGED:
                playerStateChanged(eventObject);
                break;

            case PLAYER_VOLUME_CHANGED:
            case GROUP_VOLUME_CHANGED:
                @Nullable
                String level = eventObject.getAttribute(LEVEL);
                if (level != null) {
                    notificationVolume = level;
                    updateState(CH_ID_VOLUME, PercentType.valueOf(level));
                    updateState(CH_ID_MUTE, OnOffType.from(eventObject.getBooleanAttribute(MUTE)));
                }
                break;

            case SHUFFLE_MODE_CHANGED:
                handleShuffleMode(eventObject);
                break;

            case PLAYER_NOW_PLAYING_PROGRESS:
                @Nullable
                Long position = eventObject.getNumericAttribute(CURRENT_POSITION);
                @Nullable
                Long duration = eventObject.getNumericAttribute(DURATION);
                if (position != null && duration != null) {
                    updateState(CH_ID_CUR_POS, quantityFromMilliSeconds(position));
                    updateState(CH_ID_DURATION, quantityFromMilliSeconds(duration));
                }
                break;

            case REPEAT_MODE_CHANGED:
                handleRepeatMode(eventObject);
                break;

            case PLAYER_PLAYBACK_ERROR:
                updateStatus(UNKNOWN, ThingStatusDetail.NONE, eventObject.getAttribute(ERROR));
                break;

            case PLAYER_QUEUE_CHANGED:
                fetchQueueFromPlayer();
                break;

            case SOURCES_CHANGED:
                // we are not yet handling the actual sources, although we might want to do that in the future
                logger.trace("Ignoring {}, support might be added in the future", command);
                break;

            case GROUPS_CHANGED:
            case PLAYERS_CHANGED:
            case PLAYER_NOW_PLAYING_CHANGED:
            case USER_CHANGED:
                logger.trace("Ignoring {}, will be handled inside HeosEventController", command);
                break;
        }
    }

    private QuantityType<Time> quantityFromMilliSeconds(long position) {
        return new QuantityType<>(position / 1000, Units.SECOND);
    }

    private void handleShuffleMode(HeosObject eventObject) {
        updateState(CH_ID_SHUFFLE_MODE,
                OnOffType.from(eventObject.getBooleanAttribute(HeosCommunicationAttribute.SHUFFLE)));
    }

    void refreshPlayState(String id) throws IOException, ReadException {
        handleThingStateUpdate(getApiConnection().getPlayMode(id));
        handleThingStateUpdate(getApiConnection().getPlayState(id));
        handleThingStateUpdate(getApiConnection().getNowPlayingMedia(id));
    }

    protected <T> void handleThingStateUpdate(HeosResponseObject<T> responseObject) throws HeosFunctionalException {
        handleResponseError(responseObject);

        @Nullable
        HeosCommandTuple cmd = responseObject.heosCommand;

        if (cmd == null) {
            logger.debug("Ignoring response with null command");
            return;
        }

        if (cmd.commandGroup == PLAYER || cmd.commandGroup == GROUP) {
            switch (cmd.command) {
                case GET_PLAY_STATE:
                    playerStateChanged(responseObject);
                    break;

                case GET_MUTE:
                    updateState(CH_ID_MUTE, OnOffType.from(responseObject.getBooleanAttribute(MUTE)));
                    break;

                case GET_VOLUME:
                    @Nullable
                    String level = responseObject.getAttribute(LEVEL);
                    if (level != null) {
                        notificationVolume = level;
                        updateState(CH_ID_VOLUME, PercentType.valueOf(level));
                    }
                    break;

                case GET_PLAY_MODE:
                    handleRepeatMode(responseObject);
                    handleShuffleMode(responseObject);
                    break;

                case GET_NOW_PLAYING_MEDIA:
                    @Nullable
                    T mediaPayload = responseObject.payload;
                    if (mediaPayload instanceof Media) {
                        handleThingMediaUpdate((Media) mediaPayload);
                    }
                    break;

                case GET_PLAYER_INFO:
                    @Nullable
                    T playerPayload = responseObject.payload;
                    if (playerPayload instanceof Player) {
                        handlePlayerInfo((Player) playerPayload);
                    }
                    break;
            }
        }
    }

    private <T> void handleResponseError(HeosResponseObject<T> responseObject) throws HeosFunctionalException {
        @Nullable
        HeosError error = responseObject.getError();
        if (error != null) {
            throw new HeosFunctionalException(error.code);
        }
    }

    private void handleRepeatMode(HeosObject eventObject) {
        @Nullable
        String repeatMode = eventObject.getAttribute(REPEAT);
        if (repeatMode == null) {
            updateState(CH_ID_REPEAT_MODE, UnDefType.NULL);
            return;
        }

        switch (repeatMode) {
            case REPEAT_ALL:
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ALL));
                break;

            case REPEAT_ONE:
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ONE));
                break;

            case OFF:
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_OFF));
                break;
        }
    }

    private void playerStateChanged(HeosObject eventObject) {
        @Nullable
        String attribute = eventObject.getAttribute(STATE);
        if (attribute == null) {
            updateState(CH_ID_CONTROL, UnDefType.NULL);
            return;
        }
        switch (attribute) {
            case PLAY:
                updateState(CH_ID_CONTROL, PlayPauseType.PLAY);
                break;
            case PAUSE:
            case STOP:
                updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
                break;
        }
    }

    private synchronized void fetchQueueFromPlayer() {
        try {
            List<Media> queue = getApiConnection().getQueue(getId());
            heosDynamicStateDescriptionProvider.setQueue(queueChannelUID, queue);
            return;
        } catch (HeosNotFoundException e) {
            logger.debug("HEOS player/group is not found, rescheduling");
        } catch (IOException | ReadException e) {
            logger.debug("Failed to set queue, rescheduling", e);
        }
        cancel(scheduleQueueFetchFuture, false);
        scheduleQueueFetchFuture = scheduler.schedule(this::fetchQueueFromPlayer, 30, TimeUnit.SECONDS);
    }

    protected void handleThingMediaUpdate(Media info) {
        logger.debug("Received updated media state: {}", info);

        updateState(CH_ID_SONG, StringType.valueOf(info.song));
        updateState(CH_ID_ARTIST, StringType.valueOf(info.artist));
        updateState(CH_ID_ALBUM, StringType.valueOf(info.album));
        if (SONG.equals(info.type)) {
            updateState(CH_ID_QUEUE, StringType.valueOf(String.valueOf(info.queueId)));
            updateState(CH_ID_FAVORITES, UnDefType.UNDEF);
        } else if (STATION.equals(info.type)) {
            updateState(CH_ID_QUEUE, UnDefType.UNDEF);
            updateState(CH_ID_FAVORITES, StringType.valueOf(info.albumId));
        } else {
            updateState(CH_ID_QUEUE, UnDefType.UNDEF);
            updateState(CH_ID_FAVORITES, UnDefType.UNDEF);
        }
        handleImageUrl(info);
        handleStation(info);
        handleSourceId(info);
    }

    private void handleImageUrl(Media info) {
        String imageUrl = info.imageUrl;
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                URL url = new URL(imageUrl); // checks if String is proper URL
                RawType cover = HttpUtil.downloadImage(url.toString());
                if (cover != null) {
                    updateState(CH_ID_COVER, cover);
                    return;
                }
            } catch (MalformedURLException e) {
                logger.debug("Cover can't be loaded. No proper URL: {}", imageUrl, e);
            }
        }
        updateState(CH_ID_COVER, UnDefType.NULL);
    }

    private void handleStation(Media info) {
        if (STATION.equals(info.type)) {
            updateState(CH_ID_STATION, StringType.valueOf(info.station));
        } else {
            updateState(CH_ID_STATION, UnDefType.UNDEF);
        }
    }

    private void handleSourceId(Media info) {
        if (info.sourceId == INPUT_SID && info.mediaId != null) {
            String inputName = info.mediaId.substring(info.mediaId.indexOf("/") + 1);
            updateState(CH_ID_INPUTS, StringType.valueOf(inputName));
            updateState(CH_ID_TYPE, StringType.valueOf(info.station));
        } else {
            updateState(CH_ID_TYPE, StringType.valueOf(info.type));
            updateState(CH_ID_INPUTS, UnDefType.UNDEF);
        }
    }

    private void handlePlayerInfo(Player player) {
        Map<String, String> prop = new HashMap<>();
        HeosPlayerHandler.propertiesFromPlayer(prop, player);
        updateProperties(prop);
    }
}
