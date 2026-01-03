/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.events.SessionEventListener;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.GeneralCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.GeneralCommandType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlayCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.client.ClientStateUpdater;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClientHandler} is responsible for managing Jellyfin client devices.
 * It receives session updates from the parent ServerHandler bridge via event bus
 * and handles commands sent to client channels (media controls, playback position, etc.).
 *
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Maintain bridge connection to ServerHandler</li>
 * <li>Subscribe to session events from event bus</li>
 * <li>Update channels based on session state (synchronized)</li>
 * <li>Route commands to appropriate API endpoints</li>
 * <li>Handle position/seek conversions (percent/seconds to ticks)</li>
 * </ul>
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ClientHandler extends BaseThingHandler implements SessionEventListener {

    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * Lock object for synchronizing session updates.
     * Prevents concurrent modifications when processing session state changes.
     */
    private final Object sessionLock = new Object();

    /**
     * The device ID extracted from ThingUID, used to subscribe to event bus.
     */
    @Nullable
    private String deviceId;

    /**
     * The current session information for this client.
     * Updated via event bus notifications through onSessionUpdate().
     */
    @Nullable
    private SessionInfoDto currentSession;

    /**
     * Scheduled future for delayed command execution.
     * Used to delay play/browse commands after stopping current playback.
     */
    @Nullable
    private ScheduledFuture<?> delayedCommand;

    /**
     * Constructor for the client handler.
     *
     * @param thing The thing instance for this client device
     */
    public ClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ClientHandler for thing {}", thing.getUID());

        // Extract device ID from ThingUID (last segment)
        deviceId = thing.getUID().getId();
        if (deviceId == null || deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid device ID in ThingUID");
            return;
        }

        // Validate bridge connection
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured for client");
            return;
        }

        // Verify bridge is a ServerHandler
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is not a Jellyfin server");
            return;
        }

        // Check bridge online status
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Server bridge is not online");
            return;
        }

        // Subscribe to event bus for session updates
        SessionEventBus eventBus = serverHandler.getSessionEventBus();
        eventBus.subscribe(deviceId, this);
        logger.debug("ClientHandler subscribed to event bus for device ID: {}", deviceId);

        // Client is ready - will receive session updates via event bus
        updateStatus(ThingStatus.ONLINE);
        logger.debug("ClientHandler initialized successfully for thing {}", thing.getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing ClientHandler for thing {}", thing.getUID());

        // Unsubscribe from event bus
        if (deviceId != null) {
            ServerHandler serverHandler = getServerHandler();
            if (serverHandler != null) {
                SessionEventBus eventBus = serverHandler.getSessionEventBus();
                eventBus.unsubscribe(deviceId, this);
                logger.debug("ClientHandler unsubscribed from event bus for device ID: {}", deviceId);
            }
        }

        cancelDelayedCommand();

        synchronized (sessionLock) {
            currentSession = null;
        }

        deviceId = null;

        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for client {}", bridgeStatusInfo.getStatus(), thing.getUID());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // Bridge came online - client will be updated via session updates from ServerHandler
            updateStatus(ThingStatus.ONLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            // Bridge went offline - client should go offline too
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Server bridge is offline");
            clearChannelStates();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);

        // Validate bridge connection before processing commands
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot handle command - no server bridge available");
            return;
        }

        // Implement basic command routing. For complex search and browse operations
        // we delegate to ServerHandler helper methods.
        try {
            final String channelId = channelUID.getId();

            // Media control commands
            if (Constants.MEDIA_CONTROL_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.PlayPauseType) {
                    var playPause = (org.openhab.core.library.types.PlayPauseType) command;
                    if (playPause == org.openhab.core.library.types.PlayPauseType.PLAY) {
                        sendPlayStateCommand(PlaystateCommand.UNPAUSE);
                    } else if (playPause == org.openhab.core.library.types.PlayPauseType.PAUSE) {
                        sendPlayStateCommand(PlaystateCommand.PAUSE);
                    }
                    return;
                }
                if (command instanceof org.openhab.core.library.types.NextPreviousType) {
                    var np = (org.openhab.core.library.types.NextPreviousType) command;
                    if (np == org.openhab.core.library.types.NextPreviousType.NEXT) {
                        sendPlayStateCommand(PlaystateCommand.NEXT_TRACK);
                    } else if (np == org.openhab.core.library.types.NextPreviousType.PREVIOUS) {
                        sendPlayStateCommand(PlaystateCommand.PREVIOUS_TRACK);
                    }
                    return;
                }
                if (command instanceof org.openhab.core.library.types.RewindFastforwardType) {
                    var rw = (org.openhab.core.library.types.RewindFastforwardType) command;
                    if (rw == org.openhab.core.library.types.RewindFastforwardType.FASTFORWARD) {
                        sendPlayStateCommand(PlaystateCommand.FAST_FORWARD);
                    } else if (rw == org.openhab.core.library.types.RewindFastforwardType.REWIND) {
                        sendPlayStateCommand(PlaystateCommand.REWIND);
                    }
                    return;
                }
                // No StopType in core library; stop is uncommon. Use pause/seek as needed.
            }

            // Seek by percentage
            if (Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.PercentType) {
                    var percent = (org.openhab.core.library.types.PercentType) command;
                    seekToPercent(percent.intValue());
                }
                return;
            }

            // Seek by seconds (number type)
            if (Constants.PLAYING_ITEM_SECOND_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.DecimalType) {
                    var secs = (org.openhab.core.library.types.DecimalType) command;
                    seekToSeconds(secs.intValue());
                }
                return;
            }

            // Play/search commands via terms
            if (Constants.PLAY_BY_TERMS_CHANNEL.equals(channelId)
                    || Constants.PLAY_NEXT_BY_TERMS_CHANNEL.equals(channelId)
                    || Constants.PLAY_LAST_BY_TERMS_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    final String terms = ((org.openhab.core.library.types.StringType) command).toString();
                    PlayCommand playCommand = PlayCommand.PLAY_NOW;
                    if (Constants.PLAY_NEXT_BY_TERMS_CHANNEL.equals(channelId)) {
                        playCommand = PlayCommand.PLAY_NEXT;
                    } else if (Constants.PLAY_LAST_BY_TERMS_CHANNEL.equals(channelId)) {
                        playCommand = PlayCommand.PLAY_LAST;
                    }
                    runItemSearch(terms, playCommand);
                }
                return;
            }

            // Play by ID
            if (Constants.PLAY_BY_ID_CHANNEL.equals(channelId) || Constants.PLAY_NEXT_BY_ID_CHANNEL.equals(channelId)
                    || Constants.PLAY_LAST_BY_ID_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    var uuidS = ((org.openhab.core.library.types.StringType) command).toString();
                    java.util.UUID id = parseItemUUID(uuidS);
                    PlayCommand playCommand = PlayCommand.PLAY_NOW;
                    if (Constants.PLAY_NEXT_BY_ID_CHANNEL.equals(channelId)) {
                        playCommand = PlayCommand.PLAY_NEXT;
                    } else if (Constants.PLAY_LAST_BY_ID_CHANNEL.equals(channelId)) {
                        playCommand = PlayCommand.PLAY_LAST;
                    }
                    runItemById(id, playCommand);
                }
                return;
            }

            // Send notification
            if (Constants.SEND_NOTIFICATION_CHANNEL.equals(channelId)) {
                sendDeviceMessage(command);
                return;
            }

            // Browse by terms
            if (Constants.BROWSE_ITEM_BY_TERMS_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    final String terms = ((org.openhab.core.library.types.StringType) command).toString();
                    runItemSearchForBrowse(terms);
                }
                return;
            }

            // Browse by ID
            if (Constants.BROWSE_ITEM_BY_ID_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    var uuidS = ((org.openhab.core.library.types.StringType) command).toString();
                    java.util.UUID id = parseItemUUID(uuidS);
                    if (id != null) {
                        runBrowseById(id);
                    }
                }
                return;
            }

            // Stop (Switch channel)
            if (Constants.MEDIA_STOP_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.OnOffType) {
                    var onOff = (org.openhab.core.library.types.OnOffType) command;
                    if (onOff == org.openhab.core.library.types.OnOffType.ON) {
                        sendPlayStateCommand(PlaystateCommand.STOP);
                    }
                }
                return;
            }

            // Shuffle (toggle on/off)
            if (Constants.MEDIA_SHUFFLE_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.OnOffType) {
                    var onOff = (org.openhab.core.library.types.OnOffType) command;
                    sendGeneralCommand(GeneralCommandType.SET_SHUFFLE_QUEUE, "ShuffleMode",
                            onOff == org.openhab.core.library.types.OnOffType.ON ? "true" : "false");
                }
                return;
            }

            // Repeat mode (off/one/all)
            if (Constants.MEDIA_REPEAT_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    var mode = ((org.openhab.core.library.types.StringType) command).toString();
                    sendGeneralCommand(GeneralCommandType.SET_REPEAT_MODE, "RepeatMode", mode);
                }
                return;
            }

            // Streaming quality (bitrate)
            if (Constants.MEDIA_QUALITY_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.StringType) {
                    var bitrate = ((org.openhab.core.library.types.StringType) command).toString();
                    sendGeneralCommand(GeneralCommandType.SET_MAX_STREAMING_BITRATE, "MaxBitrate", bitrate);
                }
                return;
            }

            // Audio track selection
            if (Constants.MEDIA_AUDIO_TRACK_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.DecimalType) {
                    var index = ((org.openhab.core.library.types.DecimalType) command).intValue();
                    sendGeneralCommand(GeneralCommandType.SET_AUDIO_STREAM_INDEX, "Index", String.valueOf(index));
                }
                return;
            }

            // Subtitle track selection
            if (Constants.MEDIA_SUBTITLE_CHANNEL.equals(channelId)) {
                if (command instanceof org.openhab.core.library.types.DecimalType) {
                    var index = ((org.openhab.core.library.types.DecimalType) command).intValue();
                    sendGeneralCommand(GeneralCommandType.SET_SUBTITLE_STREAM_INDEX, "Index", String.valueOf(index));
                }
                return;
            }

        } catch (Exception e) {
            logger.warn("Error handling command {} for channel {}: {}", command, channelUID, e.getMessage(), e);
        }

        logger.debug("Command handling not yet implemented for channel {}", channelUID.getId());
    }

    private void sendPlayStateCommand(PlaystateCommand command) {
        sendPlayStateCommand(command, null);
    }

    private void sendPlayStateCommand(PlaystateCommand command, @Nullable Long seekPositionTick) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot send play state command - server handler not available");
            return;
        }
        String sessionId = currentSession == null ? null : currentSession.getId();
        serverHandler.sendPlayStateCommand(sessionId, command, seekPositionTick);
    }

    private void sendGeneralCommand(GeneralCommandType commandType, String argumentKey, String argumentValue) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot send general command - server handler not available");
            return;
        }
        String sessionId = currentSession == null ? null : currentSession.getId();
        try {
            GeneralCommand command = new GeneralCommand();
            command.setName(commandType);
            Map<String, String> arguments = new java.util.HashMap<>();
            arguments.put(argumentKey, argumentValue);
            command.setArguments(arguments);
            serverHandler.sendGeneralCommand(sessionId, command);
        } catch (Exception e) {
            logger.warn("Failed to send general command {} for session {}: {}", commandType, sessionId, e.getMessage());
        }
    }

    private void seekToPercent(int percent) {
        synchronized (sessionLock) {
            if (currentSession == null) {
                logger.warn("No session active to seek");
                return;
            }
            // runtime ticks are available in playing item under current session
            var playingItem = currentSession.getNowPlayingItem();
            if (playingItem == null || playingItem.getRunTimeTicks() == null) {
                logger.warn("Cannot seek - no runtime available");
                return;
            }
            long targetTicks = Math.round((playingItem.getRunTimeTicks() * percent) / 100.0);
            sendPlayStateCommand(PlaystateCommand.SEEK, targetTicks);
        }
    }

    private void seekToSeconds(int seconds) {
        synchronized (sessionLock) {
            if (currentSession == null) {
                logger.warn("No session active to seek");
                return;
            }
            long targetTicks = seconds * 10_000_000L; // Jellyfin uses 10M ticks per second
            sendPlayStateCommand(PlaystateCommand.SEEK, targetTicks);
        }
    }

    private void runItemSearch(String terms, @Nullable PlayCommand playCommand) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot run item search - server handler not available");
            return;
        }
        try {
            // Try movies and episodes
            BaseItemDto movie = serverHandler.searchItem(terms, BaseItemKind.MOVIE, null);
            if (movie != null && playCommand != null) {
                serverHandler.playItem(currentSession == null ? null : currentSession.getId(), playCommand,
                        movie.getId().toString(), null);
                return;
            }
            BaseItemDto episode = serverHandler.searchItem(terms, BaseItemKind.EPISODE, null);
            if (episode != null && playCommand != null) {
                serverHandler.playItem(currentSession == null ? null : currentSession.getId(), playCommand,
                        episode.getId().toString(), null);
            }
        } catch (Exception e) {
            logger.warn("Failed to run item search for {}: {}", terms, e.getMessage(), e);
        }
    }

    private void runItemById(java.util.UUID id, @Nullable PlayCommand playCommand) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot run item by id - server handler not available");
            return;
        }
        try {
            serverHandler.playItem(currentSession == null ? null : currentSession.getId(), playCommand, id.toString(),
                    null);
        } catch (Exception e) {
            logger.warn("Failed to run item by id {}: {}", id, e.getMessage(), e);
        }
    }

    private void sendDeviceMessage(Command command) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot send device message - server handler not available");
            return;
        }
        try {
            serverHandler.sendDeviceMessage(currentSession == null ? null : currentSession.getId(), "Jellyfin OpenHAB",
                    command == null ? "" : command.toFullString(), 15000);
        } catch (Exception e) {
            logger.warn("Failed to send device message: {}", e.getMessage(), e);
        }
    }

    private java.util.UUID parseItemUUID(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return java.util.UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID string for item id: {}", id);
            return null;
        }
    }

    /**
     * Helper to build a ChannelUID for the local thing with a specific channel ID.
     *
     * @param channelId internal ID of the channel as defined in XML
     * @return a ChannelUID instance
     */
    private ChannelUID channel(String channelId) {
        return new ChannelUID(thing.getUID(), channelId);
    }

    /**
     * Clear all channel states for this client by setting them to UnDefType.NULL.
     *
     * This is used when the session is lost or the client goes offline.
     */
    private void clearChannelStates() {
        final String[] channels = new String[] { "playing-item-id", "playing-item-name", "playing-item-series-name",
                "playing-item-season-name", "playing-item-season", "playing-item-episode", "playing-item-genres",
                "playing-item-type", "playing-item-total-seconds", "media-control", "playing-item-percentage",
                "playing-item-second",
                // command channels are write-only, not cleared here typically
        };

        for (String ch : channels) {
            updateState(new ChannelUID(thing.getUID(), ch), UnDefType.NULL);
        }
    }

    /**
     * Gets the parent ServerHandler bridge.
     *
     * @return The ServerHandler instance, or null if bridge is not available or not a ServerHandler
     */
    @Nullable
    private ServerHandler getServerHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }

        if (bridge.getHandler() instanceof ServerHandler serverHandler) {
            return serverHandler;
        }

        return null;
    }

    /**
     * Receives session update notifications from the event bus.
     * This method implements the SessionEventListener interface.
     *
     * <p>
     * Catches and logs any exceptions to prevent disruption of the event bus.
     * Delegates to updateStateFromSession() for actual state update logic.
     *
     * @param session The updated session information, or null if session ended/offline
     */
    @Override
    public void onSessionUpdate(@Nullable SessionInfoDto session) {
        try {
            logger.trace("Received session update event for device: {}", deviceId);
            updateStateFromSession(session);
        } catch (Exception e) {
            logger.warn("Error processing session update for device {}: {}", deviceId, e.getMessage());
            logger.debug("Session update exception", e);
        }
    }

    /**
     * Updates the client state based on a new session information object.
     * This method processes session updates received from the event bus.
     *
     * <p>
     * This method is synchronized to prevent concurrent modifications and ensure
     * consistent channel updates. All channel state changes are performed within
     * this synchronized block to maintain data integrity.
     *
     * @param session The session information to update from, or null to clear state
     */
    public synchronized void updateStateFromSession(@Nullable SessionInfoDto session) {
        Map<String, State> states;
        synchronized (sessionLock) {
            currentSession = session;
            states = ClientStateUpdater.calculateChannelStates(session);
        }

        if (session == null) {
            logger.debug("Clearing client state - session is null");
            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.debug("Updating client state from session: {}", session.getId());
        }

        states.forEach((channelId, state) -> {
            if (isLinked(channelId)) {
                updateState(channel(channelId), state);
            }
        });
    }

    /**
     * Gets the current session information for this client.
     * Used for testing and internal state queries.
     *
     * @return The current session, or null if no session is active
     */
    @Nullable
    public SessionInfoDto getCurrentSession() {
        synchronized (sessionLock) {
            return currentSession;
        }
    }

    /**
     * Cancels any pending delayed command.
     */
    private void cancelDelayedCommand() {
        ScheduledFuture<?> future = delayedCommand;
        if (future != null && !future.isDone()) {
            future.cancel(false);
            logger.debug("Cancelled pending delayed command");
        }
        delayedCommand = null;
    }

    /**
     * Runs a search for items to browse to based on search terms.
     *
     * @param terms The search terms
     */
    private void runItemSearchForBrowse(String terms) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot run browse search - server handler not available");
            return;
        }
        try {
            // Try movies first
            BaseItemDto movie = serverHandler.searchItem(terms, BaseItemKind.MOVIE, null);
            if (movie != null) {
                browseToItem(movie);
                return;
            }
            // Then try episodes
            BaseItemDto episode = serverHandler.searchItem(terms, BaseItemKind.EPISODE, null);
            if (episode != null) {
                browseToItem(episode);
                return;
            }
            logger.debug("No items found for browse search: {}", terms);
        } catch (Exception e) {
            logger.warn("Failed to run browse search for {}: {}", terms, e.getMessage(), e);
        }
    }

    /**
     * Runs a browse command for an item by ID.
     *
     * @param id The item ID
     */
    private void runBrowseById(java.util.UUID id) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot browse by id - server handler not available");
            return;
        }
        try {
            BaseItemDto item = serverHandler.getItemById(null, id);
            if (item != null) {
                browseToItem(item);
            } else {
                logger.warn("Item not found for browse by id: {}", id);
            }
        } catch (Exception e) {
            logger.warn("Failed to browse by id {}: {}", id, e.getMessage(), e);
        }
    }

    /**
     * Browses to a specific item on the client.
     * If the client is currently playing, stops playback before browsing.
     *
     * @param item The item to browse to
     */
    private void browseToItem(BaseItemDto item) {
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            logger.warn("Cannot browse to item - server handler not available");
            return;
        }

        String sessionId = currentSession == null ? null : currentSession.getId();
        boolean isPlaying = currentSession != null && currentSession.getNowPlayingItem() != null;

        if (isPlaying) {
            // Stop current playback first, then browse after delay
            logger.debug("Stopping playback before browse");
            sendPlayStateCommand(PlaystateCommand.STOP);

            cancelDelayedCommand();
            delayedCommand = scheduler.schedule(() -> {
                try {
                    serverHandler.browseToItem(sessionId, item.getId().toString(), item.getType(), item.getName());
                    logger.debug("Browsed to item: {}", item.getName());
                } catch (Exception e) {
                    logger.warn("Failed to browse to item after delay: {}", e.getMessage(), e);
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            // Browse immediately if not playing
            try {
                serverHandler.browseToItem(sessionId, item.getId().toString(), item.getType(), item.getName());
                logger.debug("Browsed to item: {}", item.getName());
            } catch (Exception e) {
                logger.warn("Failed to browse to item: {}", e.getMessage(), e);
            }
        }
    }
}
