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
package org.openhab.binding.jellyfin.internal.handler;

import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.BROWSE_ITEM_BY_ID_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.BROWSE_ITEM_BY_TERMS_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.MEDIA_CONTROL_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_EPISODE_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_GENRES_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_ID_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_NAME_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_PERCENTAGE_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_SEASON_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_SEASON_NAME_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_SECOND_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_SERIES_NAME_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAYING_ITEM_TYPE_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_BY_ID_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_BY_TERMS_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_LAST_BY_ID_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_LAST_BY_TERMS_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_NEXT_BY_ID_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.PLAY_NEXT_BY_TERMS_CHANNEL;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.SEND_NOTIFICATION_CHANNEL;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jellyfin.sdk.api.client.exception.ApiClientException;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.jellyfin.sdk.model.api.BaseItemKind;
import org.jellyfin.sdk.model.api.PlayCommand;
import org.jellyfin.sdk.model.api.PlayerStateInfo;
import org.jellyfin.sdk.model.api.PlaystateCommand;
import org.jellyfin.sdk.model.api.SessionInfo;
import org.openhab.binding.jellyfin.internal.util.SyncCallback;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JellyfinClientHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class JellyfinClientHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(JellyfinClientHandler.class);
    private final Pattern typeSearchPattern = Pattern.compile("<type:(?<type>movie|series|episode)>\\s?(?<terms>.*)");
    private final Pattern seriesSearchPattern = Pattern
            .compile("(<type:series>)?<season:(?<season>[0-9]*)><episode:(?<episode>[0-9]*)>\\s?(?<terms>.*)");
    private @Nullable ScheduledFuture<?> delayedCommand;
    private String lastSessionId = "";
    private boolean lastPlayingState = false;
    private long lastRunTimeTicks = 0L;

    public JellyfinClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::refreshState);
    }

    public synchronized void updateStateFromSession(@Nullable SessionInfo session) {
        if (session != null) {
            lastSessionId = Objects.requireNonNull(session.getId());
            updateStatus(ThingStatus.ONLINE);
            updateChannelStates(session.getNowPlayingItem(), session.getPlayState());
        } else {
            lastPlayingState = false;
            cleanChannels();
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                refreshState();
                return;
            }
            switch (channelUID.getId()) {
                case SEND_NOTIFICATION_CHANNEL -> sendDeviceMessage(command);
                case MEDIA_CONTROL_CHANNEL -> handleMediaControlCommand(channelUID, command);
                case PLAY_BY_TERMS_CHANNEL -> runItemSearch(command.toFullString(), PlayCommand.PLAY_NOW);
                case PLAY_NEXT_BY_TERMS_CHANNEL -> runItemSearch(command.toFullString(), PlayCommand.PLAY_NEXT);
                case PLAY_LAST_BY_TERMS_CHANNEL -> runItemSearch(command.toFullString(), PlayCommand.PLAY_LAST);
                case BROWSE_ITEM_BY_TERMS_CHANNEL -> runItemSearch(command.toFullString(), null);
                case PLAY_BY_ID_CHANNEL -> runItemById(parseItemUUID(command), PlayCommand.PLAY_NOW);
                case PLAY_NEXT_BY_ID_CHANNEL -> runItemById(parseItemUUID(command), PlayCommand.PLAY_NEXT);
                case PLAY_LAST_BY_ID_CHANNEL -> runItemById(parseItemUUID(command), PlayCommand.PLAY_LAST);
                case BROWSE_ITEM_BY_ID_CHANNEL -> runItemById(parseItemUUID(command), null);
                case PLAYING_ITEM_SECOND_CHANNEL -> seekToSecond(command);
                case PLAYING_ITEM_PERCENTAGE_CHANNEL -> seekToPercentage(command);
            }
        } catch (NumberFormatException numberFormatException) {
            logger.warn("NumberFormatException error while running channel {}: {}", channelUID.getId(),
                    numberFormatException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("IllegalArgumentException error while running channel {}: {}", channelUID.getId(),
                    illegalArgumentException.getMessage());
        } catch (SyncCallback.SyncCallbackError syncCallbackError) {
            logger.warn("Unexpected error while running channel {}: {}", channelUID.getId(),
                    syncCallbackError.getMessage());
        } catch (ApiClientException e) {
            getServerHandler().handleApiException(e);
        }
    }

    private UUID parseItemUUID(Command command) throws IllegalArgumentException {
        try {
            var itemId = command.toFullString().replace("-", "");
            return new UUID(new BigInteger(itemId.substring(0, 16), 16).longValue(),
                    new BigInteger(itemId.substring(16), 16).longValue());
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Unable to parse item UUID in command " + command.toFullString() + ".");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelDelayedCommand();
    }

    private void cancelDelayedCommand() {
        var delayedCommand = this.delayedCommand;
        if (delayedCommand != null) {
            delayedCommand.cancel(true);
        }
    }

    private void refreshState() {
        getServerHandler().updateClientState(this);
    }

    private void updateChannelStates(@Nullable BaseItemDto playingItem, @Nullable PlayerStateInfo playState) {
        lastPlayingState = playingItem != null;
        lastRunTimeTicks = playingItem != null ? Objects.requireNonNull(playingItem.getRunTimeTicks()) : 0L;
        var positionTicks = playState != null ? playState.getPositionTicks() : null;
        var runTimeTicks = playingItem != null ? playingItem.getRunTimeTicks() : null;
        if (isLinked(MEDIA_CONTROL_CHANNEL)) {
            updateState(new ChannelUID(this.thing.getUID(), MEDIA_CONTROL_CHANNEL),
                    playingItem != null && playState != null && !playState.isPaused() ? PlayPauseType.PLAY
                            : PlayPauseType.PAUSE);
        }
        if (isLinked(PLAYING_ITEM_PERCENTAGE_CHANNEL)) {
            if (positionTicks != null && runTimeTicks != null) {
                int percentage = (int) Math.round((positionTicks * 100.0) / runTimeTicks);
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_PERCENTAGE_CHANNEL),
                        new PercentType(percentage));
            } else {
                cleanChannel(PLAYING_ITEM_PERCENTAGE_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_SECOND_CHANNEL)) {
            if (positionTicks != null) {
                var second = Math.round((float) positionTicks / 10000000.0);
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_SECOND_CHANNEL), new DecimalType(second));
            } else {
                cleanChannel(PLAYING_ITEM_SECOND_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_TOTAL_SECOND_CHANNEL)) {
            if (runTimeTicks != null) {
                var seconds = Math.round((float) runTimeTicks / 10000000.0);
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_TOTAL_SECOND_CHANNEL),
                        new DecimalType(seconds));
            } else {
                cleanChannel(PLAYING_ITEM_TOTAL_SECOND_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_ID_CHANNEL)) {
            if (playingItem != null) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_ID_CHANNEL),
                        new StringType(playingItem.getId().toString()));
            } else {
                cleanChannel(PLAYING_ITEM_ID_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_NAME_CHANNEL)) {
            if (playingItem != null) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_NAME_CHANNEL),
                        new StringType(playingItem.getName()));
            } else {
                cleanChannel(PLAYING_ITEM_NAME_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_SERIES_NAME_CHANNEL)) {
            if (playingItem != null) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_SERIES_NAME_CHANNEL),
                        new StringType(playingItem.getSeriesName()));
            } else {
                cleanChannel(PLAYING_ITEM_SERIES_NAME_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_SEASON_NAME_CHANNEL)) {
            if (playingItem != null && BaseItemKind.EPISODE.equals(playingItem.getType())) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_SEASON_NAME_CHANNEL),
                        new StringType(playingItem.getSeasonName()));
            } else {
                cleanChannel(PLAYING_ITEM_SEASON_NAME_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_SEASON_CHANNEL)) {
            if (playingItem != null && BaseItemKind.EPISODE.equals(playingItem.getType())) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_SEASON_CHANNEL),
                        new DecimalType(Objects.requireNonNull(playingItem.getParentIndexNumber())));
            } else {
                cleanChannel(PLAYING_ITEM_SEASON_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_EPISODE_CHANNEL)) {
            if (playingItem != null && BaseItemKind.EPISODE.equals(playingItem.getType())) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_EPISODE_CHANNEL),
                        new DecimalType(Objects.requireNonNull(playingItem.getIndexNumber())));
            } else {
                cleanChannel(PLAYING_ITEM_EPISODE_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_GENRES_CHANNEL)) {
            if (playingItem != null) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_GENRES_CHANNEL),
                        new StringType(String.join(",", Objects.requireNonNull(playingItem.getGenres()))));
            } else {
                cleanChannel(PLAYING_ITEM_GENRES_CHANNEL);
            }
        }
        if (isLinked(PLAYING_ITEM_TYPE_CHANNEL)) {
            if (playingItem != null) {
                updateState(new ChannelUID(this.thing.getUID(), PLAYING_ITEM_TYPE_CHANNEL),
                        new StringType(playingItem.getType().toString()));
            } else {
                cleanChannel(PLAYING_ITEM_TYPE_CHANNEL);
            }
        }
    }

    private void runItemSearch(String terms, @Nullable PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        if (terms.isBlank() || UnDefType.NULL.toFullString().equals(terms)) {
            return;
        }
        // detect series search with season and episode info
        var seriesEpisodeMatcher = seriesSearchPattern.matcher(terms);
        if (seriesEpisodeMatcher.matches()) {
            var season = Integer.parseInt(seriesEpisodeMatcher.group("season"));
            var episode = Integer.parseInt(seriesEpisodeMatcher.group("episode"));
            var cleanTerms = seriesEpisodeMatcher.group("terms");
            runSeriesEpisode(cleanTerms, season, episode, playCommand);
            return;
        }
        // detect search with type info or consider all types are enabled
        var typeMatcher = typeSearchPattern.matcher(terms);
        boolean searchByTypeEnabled = typeMatcher.matches();
        var type = searchByTypeEnabled ? typeMatcher.group("type") : "";
        boolean movieSearchEnabled = !searchByTypeEnabled || "movie".equals(type);
        boolean seriesSearchEnabled = !searchByTypeEnabled || "series".equals(type);
        boolean episodeSearchEnabled = !searchByTypeEnabled || "episode".equals(type);
        var searchTerms = searchByTypeEnabled ? typeMatcher.group("terms") : terms;
        runItemSearchByType(searchTerms, playCommand, movieSearchEnabled, seriesSearchEnabled, episodeSearchEnabled);
    }

    private void runItemSearchByType(String terms, @Nullable PlayCommand playCommand, boolean movieSearchEnabled,
            boolean seriesSearchEnabled, boolean episodeSearchEnabled)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var seriesItem = seriesSearchEnabled ? getServerHandler().searchItem(terms, BaseItemKind.SERIES, null) : null;
        var movieItem = movieSearchEnabled ? getServerHandler().searchItem(terms, BaseItemKind.MOVIE, null) : null;
        var episodeItem = episodeSearchEnabled ? getServerHandler().searchItem(terms, BaseItemKind.EPISODE, null)
                : null;
        if (movieItem != null) {
            logger.debug("Found movie: '{}'", movieItem.getName());
        }
        if (seriesItem != null) {
            logger.debug("Found series: '{}'", seriesItem.getName());
        }
        if (episodeItem != null) {
            logger.debug("Found episode: '{}'", episodeItem.getName());
        }
        if (movieItem != null) {
            runItem(movieItem, playCommand);
        } else if (seriesItem != null) {
            runSeriesItem(seriesItem, playCommand);
        } else if (episodeItem != null) {
            runItem(episodeItem, playCommand);
        } else {
            logger.warn("Nothing to display for: {}", terms);
        }
    }

    private void runSeriesItem(BaseItemDto seriesItem, @Nullable PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        if (playCommand != null) {
            var resumeEpisodeItem = getServerHandler().getSeriesResumeItem(seriesItem.getId());
            var nextUpEpisodeItem = getServerHandler().getSeriesNextUpItem(seriesItem.getId());
            var firstEpisodeItem = getServerHandler().getSeriesEpisodeItem(seriesItem.getId(), 1, 1);
            if (resumeEpisodeItem != null) {
                logger.debug("Resuming series '{}' episode '{}'", seriesItem.getName(), resumeEpisodeItem.getName());
                playItem(resumeEpisodeItem, playCommand,
                        Objects.requireNonNull(resumeEpisodeItem.getUserData()).getPlaybackPositionTicks());
            } else if (nextUpEpisodeItem != null) {
                logger.debug("Playing next series '{}' episode '{}'", seriesItem.getName(),
                        nextUpEpisodeItem.getName());
                playItem(nextUpEpisodeItem, playCommand);
            } else if (firstEpisodeItem != null) {
                logger.debug("Playing series '{}' first episode '{}'", seriesItem.getName(),
                        firstEpisodeItem.getName());
                playItem(firstEpisodeItem, playCommand);
            } else {
                logger.warn("Unable to found episode for series");
            }
        } else {
            logger.debug("Browse series '{}'", seriesItem.getName());
            browseItem(seriesItem);
        }
    }

    private void runSeriesEpisode(String terms, int season, int episode, @Nullable PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        logger.debug("{} series episode mode", playCommand != null ? "Play" : "Browse");
        var seriesItem = getServerHandler().searchItem(terms, BaseItemKind.SERIES, null);
        if (seriesItem != null) {
            logger.debug("Searching series {} episode {}x{}", seriesItem.getName(), season, episode);
            var episodeItem = getServerHandler().getSeriesEpisodeItem(seriesItem.getId(), season, episode);
            if (episodeItem != null) {
                runItem(episodeItem, playCommand);
            } else {
                logger.warn("Series {} episode {}x{} not found", seriesItem.getName(), season, episode);
            }
        } else {
            logger.warn("Series not found");
        }
    }

    private void runItem(BaseItemDto item, @Nullable PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var itemType = Objects.requireNonNull(item.getType());
        logger.debug("{} {} '{}'", playCommand == null ? "Browsing" : "Playing", itemType.toString().toLowerCase(),
                BaseItemKind.EPISODE.equals(itemType) ? item.getSeriesName() + ": " + item.getName() : item.getName());
        if (playCommand == null) {
            browseItem(item);
        } else {
            playItem(item, playCommand);
        }
    }

    private void playItem(BaseItemDto item, PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        playItem(item, playCommand, null);
    }

    private void playItem(BaseItemDto item, PlayCommand playCommand, @Nullable Long startPositionTicks)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        if (playCommand.equals(PlayCommand.PLAY_NOW) && stopCurrentPlayback()) {
            cancelDelayedCommand();
            delayedCommand = scheduler.schedule(() -> {
                try {
                    playItemInternal(item, playCommand, startPositionTicks);
                } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
                    logger.warn("Unexpected error while running channel {}: {}", PLAY_BY_TERMS_CHANNEL, e.getMessage());
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            playItemInternal(item, playCommand, startPositionTicks);
        }
    }

    private void playItemInternal(BaseItemDto item, PlayCommand playCommand, @Nullable Long startPositionTicks)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        getServerHandler().playItem(lastSessionId, playCommand, item.getId().toString(), startPositionTicks);
    }

    private void runItemById(UUID itemId, @Nullable PlayCommand playCommand)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var item = getServerHandler().getItem(itemId, null);
        if (item == null) {
            logger.warn("Unable to find item with id: {}", itemId);
            return;
        }
        if (BaseItemKind.SERIES.equals(item.getType())) {
            runSeriesItem(item, playCommand);
        } else {
            runItem(item, playCommand);
        }
    }

    private void browseItem(BaseItemDto item) throws SyncCallback.SyncCallbackError, ApiClientException {
        if (stopCurrentPlayback()) {
            cancelDelayedCommand();
            delayedCommand = scheduler.schedule(() -> {
                try {
                    browseItemInternal(item);
                } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
                    logger.warn("Unexpected error while running channel {}: {}", BROWSE_ITEM_BY_TERMS_CHANNEL,
                            e.getMessage());
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            browseItemInternal(item);
        }
    }

    private void browseItemInternal(BaseItemDto item) throws SyncCallback.SyncCallbackError, ApiClientException {
        getServerHandler().browseToItem(lastSessionId, Objects.requireNonNull(item.getType()), item.getId().toString(),
                Objects.requireNonNull(item.getName()));
    }

    private boolean stopCurrentPlayback() throws SyncCallback.SyncCallbackError, ApiClientException {
        if (lastPlayingState) {
            sendPlayStateCommand(PlaystateCommand.STOP);
            return true;
        }
        return false;
    }

    private void sendPlayStateCommand(PlaystateCommand command)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        sendPlayStateCommand(command, null);
    }

    private void sendPlayStateCommand(PlaystateCommand command, @Nullable Long seekPositionTick)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        getServerHandler().sendPlayStateCommand(lastSessionId, command, seekPositionTick);
    }

    private void sendDeviceMessage(Command command) throws SyncCallback.SyncCallbackError, ApiClientException {
        getServerHandler().sendDeviceMessage(lastSessionId, "Jellyfin OpenHAB", command.toFullString(), 15000);
    }

    private void handleMediaControlCommand(ChannelUID channelUID, Command command)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        if (command instanceof RefreshType) {
            refreshState();
        } else if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                sendPlayStateCommand(PlaystateCommand.UNPAUSE);
                updateState(channelUID, PlayPauseType.PLAY);
            } else if (command == PlayPauseType.PAUSE) {
                sendPlayStateCommand(PlaystateCommand.PAUSE);
                updateState(channelUID, PlayPauseType.PAUSE);
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                sendPlayStateCommand(PlaystateCommand.NEXT_TRACK);
            } else if (command == NextPreviousType.PREVIOUS) {
                sendPlayStateCommand(PlaystateCommand.PREVIOUS_TRACK);
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                sendPlayStateCommand(PlaystateCommand.FAST_FORWARD);
            } else if (command == RewindFastforwardType.REWIND) {
                sendPlayStateCommand(PlaystateCommand.REWIND);
            }
        } else {
            logger.warn("Unknown media control command: {}", command);
        }
    }

    private void seekToPercentage(Command command)
            throws NumberFormatException, SyncCallback.SyncCallbackError, ApiClientException {
        if (command.toFullString().equals(UnDefType.NULL.toFullString())) {
            return;
        }
        if (lastRunTimeTicks == 0L) {
            logger.warn("Can't seek missing RunTimeTicks info");
            return;
        }
        int percentage = Integer.parseInt(command.toFullString());
        var seekPositionTick = Math.round(((float) lastRunTimeTicks) * ((float) percentage / 100.0));
        logger.debug("Seek to {}%: {} of {}", percentage, seekPositionTick, lastRunTimeTicks);
        seekToTick(seekPositionTick);
    }

    private void seekToSecond(Command command)
            throws NumberFormatException, SyncCallback.SyncCallbackError, ApiClientException {
        if (command.toFullString().equals(UnDefType.NULL.toFullString())) {
            return;
        }
        long second = Long.parseLong(command.toFullString());
        long seekPositionTick = second * 10000000L;
        logger.debug("Seek to second {}: {} of {}", second, seekPositionTick, lastRunTimeTicks);
        seekToTick(seekPositionTick);
    }

    private void seekToTick(long seekPositionTick) throws SyncCallback.SyncCallbackError, ApiClientException {
        sendPlayStateCommand(PlaystateCommand.SEEK, seekPositionTick);
        scheduler.schedule(this::refreshState, 3, TimeUnit.SECONDS);
    }

    private void cleanChannels() {
        List.of(MEDIA_CONTROL_CHANNEL, PLAYING_ITEM_PERCENTAGE_CHANNEL, PLAYING_ITEM_ID_CHANNEL,
                PLAYING_ITEM_NAME_CHANNEL, PLAYING_ITEM_SERIES_NAME_CHANNEL, PLAYING_ITEM_SEASON_NAME_CHANNEL,
                PLAYING_ITEM_SEASON_CHANNEL, PLAYING_ITEM_EPISODE_CHANNEL, PLAYING_ITEM_GENRES_CHANNEL,
                PLAYING_ITEM_TYPE_CHANNEL, PLAYING_ITEM_SECOND_CHANNEL, PLAYING_ITEM_TOTAL_SECOND_CHANNEL)
                .forEach(this::cleanChannel);
    }

    private void cleanChannel(String channelId) {
        updateState(new ChannelUID(this.thing.getUID(), channelId), UnDefType.NULL);
    }

    private JellyfinServerHandler getServerHandler() {
        var bridge = Objects.requireNonNull(getBridge());
        return (JellyfinServerHandler) Objects.requireNonNull(bridge.getHandler());
    }
}
