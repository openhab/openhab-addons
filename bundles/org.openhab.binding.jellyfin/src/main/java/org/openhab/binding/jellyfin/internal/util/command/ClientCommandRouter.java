/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util.command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.GeneralCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.GeneralCommandType;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.PlayCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.tick.TickConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes openHAB channel commands to the appropriate Jellyfin server API calls.
 *
 * <p>
 * This class encapsulates all command-dispatch logic that was previously embedded in
 * {@code ClientHandler.handleCommand()}. It is stateless regarding the Jellyfin API but
 * maintains a single piece of mutable state: a {@link ScheduledFuture} for delayed browse
 * commands (issued after stopping ongoing playback).
 *
 * <p>
 * Callers must invoke {@link #dispose()} when the owning handler is destroyed to cancel
 * any pending delayed commands.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class ClientCommandRouter {

    private final Logger logger = LoggerFactory.getLogger(ClientCommandRouter.class);

    private final ServerHandler serverHandler;
    private final Supplier<@Nullable SessionInfoDto> sessionSupplier;
    private final ScheduledExecutorService scheduler;

    @Nullable
    private ScheduledFuture<?> delayedCommand;

    /**
     * Creates a new router bound to the given server handler and session supplier.
     *
     * @param serverHandler the server handler to delegate API calls to
     * @param sessionSupplier provides the current session (may return {@code null})
     * @param scheduler scheduler used for the optional browse-after-stop delay
     */
    public ClientCommandRouter(ServerHandler serverHandler, Supplier<@Nullable SessionInfoDto> sessionSupplier,
            ScheduledExecutorService scheduler) {
        this.serverHandler = serverHandler;
        this.sessionSupplier = sessionSupplier;
        this.scheduler = scheduler;
    }

    /**
     * Routes the given command for the specified channel to the Jellyfin server.
     *
     * @param channelUID the channel that received the command
     * @param command the command to process
     */
    public void route(ChannelUID channelUID, Command command) {
        final String channelId = channelUID.getId();

        // Media control commands
        if (Constants.MEDIA_CONTROL_CHANNEL.equals(channelId)) {
            if (command instanceof PlayPauseType playPause) {
                if (playPause == PlayPauseType.PLAY) {
                    sendPlayStateCommand(PlaystateCommand.UNPAUSE);
                } else if (playPause == PlayPauseType.PAUSE) {
                    sendPlayStateCommand(PlaystateCommand.PAUSE);
                }
            } else if (command instanceof NextPreviousType np) {
                if (np == NextPreviousType.NEXT) {
                    sendPlayStateCommand(PlaystateCommand.NEXT_TRACK);
                } else if (np == NextPreviousType.PREVIOUS) {
                    sendPlayStateCommand(PlaystateCommand.PREVIOUS_TRACK);
                }
            } else if (command instanceof RewindFastforwardType rw) {
                if (rw == RewindFastforwardType.FASTFORWARD) {
                    sendPlayStateCommand(PlaystateCommand.FAST_FORWARD);
                } else if (rw == RewindFastforwardType.REWIND) {
                    sendPlayStateCommand(PlaystateCommand.REWIND);
                }
            } else if (command instanceof StringType stringCommand) {
                String value = stringCommand.toFullString().trim().toUpperCase();
                switch (value) {
                    case "PLAY":
                        sendPlayStateCommand(PlaystateCommand.UNPAUSE);
                        break;
                    case "PAUSE":
                        sendPlayStateCommand(PlaystateCommand.PAUSE);
                        break;
                    case "NEXT":
                        sendPlayStateCommand(PlaystateCommand.NEXT_TRACK);
                        break;
                    case "PREVIOUS":
                        sendPlayStateCommand(PlaystateCommand.PREVIOUS_TRACK);
                        break;
                    case "FASTFORWARD":
                        sendPlayStateCommand(PlaystateCommand.FAST_FORWARD);
                        break;
                    case "REWIND":
                        sendPlayStateCommand(PlaystateCommand.REWIND);
                        break;
                    default:
                        logger.debug("Ignoring unsupported media-control StringType command: {}", value);
                        break;
                }
            }
            return;
        }

        // Seek by percentage
        if (Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL.equals(channelId)) {
            if (command instanceof PercentType percent) {
                seekToPercent(percent.intValue());
            }
            return;
        }

        // Seek by seconds
        if (Constants.PLAYING_ITEM_SECOND_CHANNEL.equals(channelId)) {
            if (command instanceof DecimalType secs) {
                seekToSeconds(secs.intValue());
            }
            return;
        }

        // Play / search by terms
        if (Constants.PLAY_BY_TERMS_CHANNEL.equals(channelId) || Constants.PLAY_NEXT_BY_TERMS_CHANNEL.equals(channelId)
                || Constants.PLAY_LAST_BY_TERMS_CHANNEL.equals(channelId)) {
            if (command instanceof StringType) {
                PlayCommand playCommand = resolvePlayCommand(channelId, Constants.PLAY_BY_TERMS_CHANNEL,
                        Constants.PLAY_NEXT_BY_TERMS_CHANNEL, Constants.PLAY_LAST_BY_TERMS_CHANNEL);
                runItemSearch(command.toFullString(), playCommand);
            }
            return;
        }

        // Play by ID
        if (Constants.PLAY_BY_ID_CHANNEL.equals(channelId) || Constants.PLAY_NEXT_BY_ID_CHANNEL.equals(channelId)
                || Constants.PLAY_LAST_BY_ID_CHANNEL.equals(channelId)) {
            if (command instanceof StringType) {
                UUID id = parseItemUUID(command.toFullString());
                if (id == null) {
                    logger.warn("Cannot run item by id - invalid UUID: {}", command.toFullString());
                    return;
                }
                PlayCommand playCommand = resolvePlayCommand(channelId, Constants.PLAY_BY_ID_CHANNEL,
                        Constants.PLAY_NEXT_BY_ID_CHANNEL, Constants.PLAY_LAST_BY_ID_CHANNEL);
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
            if (command instanceof StringType) {
                runItemSearchForBrowse(command.toFullString());
            }
            return;
        }

        // Browse by ID
        if (Constants.BROWSE_ITEM_BY_ID_CHANNEL.equals(channelId)) {
            if (command instanceof StringType) {
                UUID id = parseItemUUID(command.toFullString());
                if (id != null) {
                    runBrowseById(id);
                }
            }
            return;
        }

        // Stop
        if (Constants.MEDIA_STOP_CHANNEL.equals(channelId)) {
            if (command instanceof OnOffType onOff && onOff == OnOffType.ON) {
                sendPlayStateCommand(PlaystateCommand.STOP);
            }
            return;
        }

        // Shuffle
        if (Constants.MEDIA_SHUFFLE_CHANNEL.equals(channelId)) {
            if (command instanceof OnOffType onOff) {
                sendGeneralCommand(GeneralCommandType.SET_SHUFFLE_QUEUE, "ShuffleMode",
                        onOff == OnOffType.ON ? "true" : "false");
            }
            return;
        }

        // Repeat
        if (Constants.MEDIA_REPEAT_CHANNEL.equals(channelId)) {
            if (command instanceof StringType) {
                sendGeneralCommand(GeneralCommandType.SET_REPEAT_MODE, "RepeatMode", command.toFullString());
            }
            return;
        }

        // Streaming quality
        if (Constants.MEDIA_QUALITY_CHANNEL.equals(channelId)) {
            if (command instanceof StringType) {
                sendGeneralCommand(GeneralCommandType.SET_MAX_STREAMING_BITRATE, "MaxBitrate", command.toFullString());
            }
            return;
        }

        // Audio track
        if (Constants.MEDIA_AUDIO_TRACK_CHANNEL.equals(channelId)) {
            if (command instanceof DecimalType index) {
                sendGeneralCommand(GeneralCommandType.SET_AUDIO_STREAM_INDEX, "Index",
                        String.valueOf(index.intValue()));
            }
            return;
        }

        // Subtitle track
        if (Constants.MEDIA_SUBTITLE_CHANNEL.equals(channelId)) {
            if (command instanceof DecimalType index) {
                sendGeneralCommand(GeneralCommandType.SET_SUBTITLE_STREAM_INDEX, "Index",
                        String.valueOf(index.intValue()));
            }
            return;
        }

        logger.debug("No handler defined for channel {}", channelId);
    }

    /**
     * Cancels any pending delayed browse command. Safe to call at any time.
     */
    public void cancelDelayedCommand() {
        ScheduledFuture<?> future = delayedCommand;
        if (future != null && !future.isDone()) {
            future.cancel(false);
            logger.debug("Cancelled pending delayed command");
        }
        delayedCommand = null;
    }

    /**
     * Cancels a pending delayed command and releases resources.
     * Must be called when the owning handler is destroyed.
     */
    public void dispose() {
        cancelDelayedCommand();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void sendPlayStateCommand(PlaystateCommand command) {
        sendPlayStateCommand(command, null);
    }

    private void sendPlayStateCommand(PlaystateCommand command, @Nullable Long seekPositionTick) {
        String sessionId = currentSessionId();
        serverHandler.sendPlayStateCommand(sessionId, command, seekPositionTick);
    }

    private void sendGeneralCommand(GeneralCommandType commandType, String argumentKey, String argumentValue) {
        String sessionId = currentSessionId();
        try {
            GeneralCommand command = new GeneralCommand();
            command.setName(commandType);
            Map<String, String> arguments = new HashMap<>();
            arguments.put(argumentKey, argumentValue);
            command.setArguments(arguments);
            serverHandler.sendGeneralCommand(sessionId, command);
        } catch (Exception e) {
            logger.warn("Failed to send general command {} for session {}: {}", commandType, sessionId, e.getMessage());
        }
    }

    private void seekToPercent(int percent) {
        SessionInfoDto session = sessionSupplier.get();
        if (session == null) {
            logger.warn("No session active to seek");
            return;
        }
        var playingItem = session.getNowPlayingItem();
        if (playingItem == null || playingItem.getRunTimeTicks() == null) {
            logger.warn("Cannot seek - no runtime available");
            return;
        }
        long targetTicks = TickConverter.percentToTicks(playingItem.getRunTimeTicks(), percent);
        sendPlayStateCommand(PlaystateCommand.SEEK, targetTicks);
    }

    private void seekToSeconds(int seconds) {
        SessionInfoDto session = sessionSupplier.get();
        if (session == null) {
            logger.warn("No session active to seek");
            return;
        }
        sendPlayStateCommand(PlaystateCommand.SEEK, TickConverter.secondsToTicks(seconds));
    }

    private void runItemSearch(String terms, @Nullable PlayCommand playCommand) {
        try {
            String sessionId = currentSessionId();
            BaseItemDto movie = serverHandler.searchItem(terms, BaseItemKind.MOVIE, null);
            if (movie != null && playCommand != null) {
                serverHandler.playItem(sessionId, playCommand, movie.getId().toString(), null);
                return;
            }
            BaseItemDto episode = serverHandler.searchItem(terms, BaseItemKind.EPISODE, null);
            if (episode != null && playCommand != null) {
                serverHandler.playItem(sessionId, playCommand, episode.getId().toString(), null);
            }
        } catch (Exception e) {
            logger.warn("Failed to run item search for {}: {}", terms, e.getMessage(), e);
        }
    }

    private void runItemById(UUID id, @Nullable PlayCommand playCommand) {
        if (playCommand == null) {
            logger.warn("Cannot run item by id - play command is null");
            return;
        }
        try {
            serverHandler.playItem(currentSessionId(), playCommand, id.toString(), null);
        } catch (Exception e) {
            logger.warn("Failed to run item by id {}: {}", id, e.getMessage(), e);
        }
    }

    private void sendDeviceMessage(Command command) {
        try {
            serverHandler.sendDeviceMessage(currentSessionId(), "Jellyfin OpenHAB", command.toFullString(), 15000);
        } catch (Exception e) {
            logger.warn("Failed to send device message: {}", e.getMessage(), e);
        }
    }

    private void runItemSearchForBrowse(String terms) {
        try {
            BaseItemDto movie = serverHandler.searchItem(terms, BaseItemKind.MOVIE, null);
            if (movie != null) {
                browseToItem(movie);
                return;
            }
            BaseItemDto episode = serverHandler.searchItem(terms, BaseItemKind.EPISODE, null);
            if (episode != null) {
                browseToItem(episode);
                return;
            }
            logger.debug("No items found for browse: {}", terms);
        } catch (Exception e) {
            logger.warn("Failed to browse by terms {}: {}", terms, e.getMessage(), e);
        }
    }

    private void runBrowseById(UUID id) {
        try {
            BaseItemDto item = serverHandler.getItemById(null, id);
            if (item != null) {
                browseToItem(item);
            } else {
                logger.warn("Item not found for browse: {}", id);
            }
        } catch (Exception e) {
            logger.warn("Failed to browse by id {}: {}", id, e.getMessage(), e);
        }
    }

    private void browseToItem(BaseItemDto item) {
        SessionInfoDto session = sessionSupplier.get();
        String sessionId = session == null ? null : session.getId();
        boolean isPlaying = session != null && session.getNowPlayingItem() != null;

        if (isPlaying) {
            logger.debug("Stopping playback before browse");
            sendPlayStateCommand(PlaystateCommand.STOP);

            cancelDelayedCommand();
            BaseItemKind itemType = item.getType();
            if (itemType != null) {
                delayedCommand = scheduler.schedule(() -> {
                    try {
                        serverHandler.browseToItem(sessionId, item.getId().toString(), itemType, item.getName());
                        logger.debug("Browsed to item: {}", item.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to browse to item after delay: {}", e.getMessage(), e);
                    }
                }, 3, TimeUnit.SECONDS);
            }
        } else {
            BaseItemKind itemType = item.getType();
            if (itemType != null) {
                try {
                    serverHandler.browseToItem(sessionId, item.getId().toString(), itemType, item.getName());
                    logger.debug("Browsed to item: {}", item.getName());
                } catch (Exception e) {
                    logger.warn("Failed to browse to item: {}", e.getMessage(), e);
                }
            }
        }
    }

    private @Nullable String currentSessionId() {
        SessionInfoDto session = sessionSupplier.get();
        return session == null ? null : session.getId();
    }

    private @Nullable UUID parseItemUUID(String id) {
        if (id.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID string for item id: {}", id);
            return null;
        }
    }

    private PlayCommand resolvePlayCommand(String channelId, String nowChannel, String nextChannel,
            String lastChannel) {
        if (nextChannel.equals(channelId)) {
            return PlayCommand.PLAY_NEXT;
        }
        if (lastChannel.equals(channelId)) {
            return PlayCommand.PLAY_LAST;
        }
        return PlayCommand.PLAY_NOW;
    }
}
