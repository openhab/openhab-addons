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
package org.openhab.binding.livetennisapi.internal.handler;

import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livetennisapi.internal.MatchStateMapper;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiAuthenticationException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiNotFoundException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiTransientException;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.MatchPlayers;
import org.openhab.binding.livetennisapi.internal.api.dto.Player;
import org.openhab.binding.livetennisapi.internal.api.dto.Score;
import org.openhab.binding.livetennisapi.internal.config.LiveTennisApiPlayerConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks one player: the state of their live match (pushed by the bridge poll), their next scheduled match and their
 * current ranking (fetched on a slower cycle).
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiPlayerHandler extends BaseThingHandler implements LiveMatchesListener {

    private static final long DETAIL_INITIAL_DELAY_S = 5;
    private static final long TRANSIENT_RETRY_DELAY_S = 60;

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiPlayerHandler.class);

    private long playerId = -1;
    private boolean detailRefreshEnabled = true;
    private @Nullable ScheduledFuture<?> detailJob;
    private @Nullable ScheduledFuture<?> retryJob;
    private volatile boolean disposed;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();

    private @Nullable Match liveMatch;
    private @Nullable Match nextMatch;
    private @Nullable Player player;

    public LiveTennisApiPlayerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        LiveTennisApiPlayerConfiguration config = getConfigAs(LiveTennisApiPlayerConfiguration.class);

        if (config.playerId < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-player-id");
            return;
        }
        if (config.detailRefreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-interval");
            return;
        }
        playerId = config.playerId;
        detailRefreshEnabled = config.detailRefreshEnabled;
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);

        // The ranking and next-match refresh is the only quota this thing spends on its own; when it is switched off
        // the thing still tracks live match state pushed by the bridge poll at no extra cost.
        if (detailRefreshEnabled) {
            detailJob = scheduler.scheduleWithFixedDelay(this::refreshDetails, DETAIL_INITIAL_DELAY_S,
                    config.detailRefreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> job = detailJob;
        if (job != null) {
            job.cancel(true);
            detailJob = null;
        }
        ScheduledFuture<?> retry = retryJob;
        if (retry != null) {
            retry.cancel(true);
            retryJob = null;
        }
        liveMatch = null;
        nextMatch = null;
        player = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Re-render from the cached snapshots; the bridge and detail jobs are the only API request triggers
            updateLiveChannels(liveMatch);
            updateNextMatchChannels(nextMatch);
            updateProfileChannels(player);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (detailRefreshEnabled && bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            scheduler.execute(this::refreshDetails);
        }
    }

    @Override
    public void onLiveMatches(List<Match> liveMatches) {
        if (disposed) {
            return;
        }
        Match match = liveMatches.stream().filter(candidate -> sideOf(candidate) > 0).findFirst().orElse(null);
        liveMatch = match;
        updateLiveChannels(match);
        // The bridge poll succeeded, so the player is reachable; the detail fetch never drives the ONLINE transition
        setOnlineUnlessMisconfigured();
    }

    private void refreshDetails() {
        LiveTennisApiAccountHandler bridge = accountHandler();
        Bridge bridgeThing = getBridge();
        if (bridge == null || bridgeThing == null || bridgeThing.getStatus() != ThingStatus.ONLINE) {
            // Do not spend quota while the bridge is not known to be up; bridgeStatusChanged retriggers on recovery
            return;
        }
        // The periodic job, the ONLINE-triggered refresh and the transient retry can all fire close together;
        // let only one detail refresh run at a time so a startup or reconnect burst does not waste quota.
        if (!refreshInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            Player refreshedPlayer = bridge.fetchPlayer(playerId);
            Match refreshedNextMatch = bridge.fetchNextMatch(playerId);
            if (disposed) {
                return;
            }
            player = refreshedPlayer;
            updateProfileChannels(refreshedPlayer);
            nextMatch = refreshedNextMatch;
            updateNextMatchChannels(refreshedNextMatch);
        } catch (LiveTennisApiNotFoundException e) {
            if (!disposed) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-player-not-found");
            }
        } catch (LiveTennisApiAuthenticationException e) {
            logger.debug("Authentication failed, the bridge poll will report it", e);
        } catch (LiveTennisApiTransientException e) {
            if (!disposed) {
                logger.debug("Detail refresh hit a transient error, scheduling a retry", e);
                scheduleDetailRetry();
            }
        } catch (LiveTennisApiException e) {
            logger.debug("Detail refresh failed", e);
        } finally {
            refreshInProgress.set(false);
        }
    }

    private void scheduleDetailRetry() {
        ScheduledFuture<?> retry = retryJob;
        if (retry != null && !retry.isDone()) {
            return;
        }
        retryJob = scheduler.schedule(this::refreshDetails, TRANSIENT_RETRY_DELAY_S, TimeUnit.SECONDS);
    }

    private void updateLiveChannels(@Nullable Match match) {
        int side = match == null ? -1 : sideOf(match);
        if (match == null || side < 1) {
            updateState(CHANNEL_LIVE_STATUS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_DISCIPLINE, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_TOURNAMENT, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_ROUND, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_OPPONENT, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SCORE_LINE, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SETS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_POINTS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SERVING, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_BREAK_POINT, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_TIEBREAK, UnDefType.UNDEF);
            return;
        }
        Score score = match.score;
        Integer server = MatchStateMapper.server(score);
        Boolean tiebreak = score == null ? null : score.isTiebreak;

        updateState(CHANNEL_LIVE_STATUS, string(match.status));
        updateState(CHANNEL_LIVE_DISCIPLINE, string(MatchStateMapper.discipline(match)));
        updateState(CHANNEL_LIVE_TOURNAMENT, string(match.tournament));
        updateState(CHANNEL_LIVE_ROUND, string(match.round));
        updateState(CHANNEL_LIVE_OPPONENT, string(opponentName(match, side)));
        updateState(CHANNEL_LIVE_SCORE_LINE, string(MatchStateMapper.scoreLine(score, side)));
        updateState(CHANNEL_LIVE_SETS, string(MatchStateMapper.setsLine(score, side)));
        updateState(CHANNEL_LIVE_POINTS, string(MatchStateMapper.pointsLine(score, side)));
        updateState(CHANNEL_LIVE_SERVING, server == null ? UnDefType.UNDEF : OnOffType.from(server == side));
        updateState(CHANNEL_LIVE_BREAK_POINT, onOff(MatchStateMapper.isBreakPoint(score)));
        updateState(CHANNEL_LIVE_TIEBREAK, onOff(tiebreak));
    }

    private void updateNextMatchChannels(@Nullable Match match) {
        int side = match == null ? -1 : sideOf(match);
        String startTime = match == null ? null : match.scheduledTime;

        updateState(CHANNEL_NEXT_OPPONENT, match == null ? UnDefType.UNDEF : string(opponentName(match, side)));
        updateState(CHANNEL_NEXT_START_TIME, dateTime(startTime));
        updateState(CHANNEL_NEXT_TOURNAMENT, match == null ? UnDefType.UNDEF : string(match.tournament));
        updateState(CHANNEL_NEXT_ROUND, match == null ? UnDefType.UNDEF : string(match.round));
    }

    private void updateProfileChannels(@Nullable Player player) {
        Integer ranking = player == null ? null : player.ranking;
        Integer rankingPoints = player == null ? null : player.rankingPoints;

        updateState(CHANNEL_PROFILE_RANKING, ranking == null ? UnDefType.UNDEF : new DecimalType(ranking));
        updateState(CHANNEL_PROFILE_RANKING_POINTS,
                rankingPoints == null ? UnDefType.UNDEF : new DecimalType(rankingPoints));
    }

    /** Returns which side (1 or 2) of the given match is the tracked player, or -1 when the player is not in it. */
    private int sideOf(@Nullable Match match) {
        MatchPlayers players = match == null ? null : match.players;
        if (players == null) {
            return -1;
        }
        Player p1 = players.p1;
        Player p2 = players.p2;
        if (p1 != null && p1.id != null && p1.id == playerId) {
            return 1;
        }
        if (p2 != null && p2.id != null && p2.id == playerId) {
            return 2;
        }
        return -1;
    }

    private static @Nullable String opponentName(Match match, int side) {
        MatchPlayers players = match.players;
        Player opponent = players == null ? null : (side == 2 ? players.p1 : players.p2);
        return opponent == null ? null : opponent.name;
    }

    private void setOnlineUnlessMisconfigured() {
        if (getThing().getStatus() != ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable LiveTennisApiAccountHandler accountHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof LiveTennisApiAccountHandler accountHandler
                ? accountHandler
                : null;
    }

    private static State string(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    private static State onOff(@Nullable Boolean value) {
        return value == null ? UnDefType.UNDEF : OnOffType.from(value.booleanValue());
    }

    private static State dateTime(@Nullable String value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        try {
            return new DateTimeType(OffsetDateTime.parse(value).toInstant());
        } catch (DateTimeParseException e) {
            return UnDefType.UNDEF;
        }
    }
}
