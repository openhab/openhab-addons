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
import java.util.concurrent.atomic.AtomicInteger;

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
 * Tracks one player: the state of their live match (pushed by the bridge poll), their next scheduled match (fetched
 * every detail refresh interval) and their current ranking (fetched on a slower, fixed internal schedule).
 *
 * @author Ben Abulafia - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiPlayerHandler extends BaseThingHandler implements LiveMatchesListener {

    private static final long DETAIL_INITIAL_DELAY_S = 5;
    private static final long TRANSIENT_RETRY_DELAY_S = 60;
    // Collapse detail refreshes that fire within this window of the previous one into a single request: the
    // near-simultaneous startup/reconnect triggers (initial-delay job + bridge-ONLINE refresh) add no fresh data.
    // The periodic refresh (interval >= 300 s) and the 60 s transient retry are never suppressed by it.
    private static final long MIN_DETAIL_REFRESH_SPACING_NANOS = TimeUnit.SECONDS.toNanos(30);
    // Rankings change at most weekly, so the profile is refreshed on this fixed schedule while the configured detail
    // refresh interval drives only the next-match request.
    private static final long PROFILE_REFRESH_INTERVAL_NANOS = TimeUnit.HOURS.toNanos(24);
    // Lifecycle values start at 1, so 0 marks "no refresh in progress".
    private static final int NO_REFRESH = 0;

    /**
     * When a detail refresh issued requests and for which lifecycle, so a mark of an earlier lifecycle never applies.
     */
    private record RefreshMark(int lifecycle, long nanos) {
    }

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiPlayerHandler.class);

    private long playerId = -1;
    private long detailRefreshInterval = 7200;
    private boolean detailRefreshEnabled = true;
    // The single pending detail refresh. Every trigger (initial delay, bridge ONLINE, periodic re-arm, transient retry)
    // replaces it and every run re-arms it, so at most one refresh is pending and a refresh that just ran pushes the
    // periodic one out by a full interval instead of letting both run within seconds.
    private @Nullable ScheduledFuture<?> detailJob;
    private volatile boolean disposed;
    // Bumped on every initialize() and dispose(); a detail refresh captures it and only publishes or re-arms while it
    // still matches, so an in-flight request cannot publish data or status for a disposed or reconfigured lifecycle.
    private final AtomicInteger lifecycle = new AtomicInteger();
    // Lifecycle of the refresh currently issuing requests, or NO_REFRESH. Keyed by lifecycle so a refresh left in
    // flight by a previous lifecycle can never block the first refresh of the next one.
    private final AtomicInteger refreshingLifecycle = new AtomicInteger(NO_REFRESH);
    private volatile @Nullable RefreshMark lastDetailRefresh;
    private volatile @Nullable RefreshMark lastProfileRefresh;

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
        detailRefreshInterval = config.detailRefreshInterval;
        detailRefreshEnabled = config.detailRefreshEnabled;
        int currentLifecycle = lifecycle.incrementAndGet();
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);

        // The ranking and next-match refresh is the only quota this thing spends on its own; when it is switched off
        // the thing still tracks live match state pushed by the bridge poll at no extra cost.
        if (detailRefreshEnabled) {
            scheduleDetailRefresh(DETAIL_INITIAL_DELAY_S, currentLifecycle);
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            lifecycle.incrementAndGet();
            disposed = true;
            ScheduledFuture<?> job = detailJob;
            if (job != null) {
                job.cancel(true);
                detailJob = null;
            }
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
            scheduleDetailRefresh(0, lifecycle.get());
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

    /**
     * Schedules the next detail refresh of the given lifecycle, replacing whatever is pending. Does nothing once that
     * lifecycle is over, so a run that ends while the handler is being disposed or reconfigured cannot re-arm itself.
     */
    private synchronized void scheduleDetailRefresh(long delaySeconds, int lifecycleAtStart) {
        if (isStale(lifecycleAtStart)) {
            return;
        }
        ScheduledFuture<?> pending = detailJob;
        if (pending != null) {
            pending.cancel(false);
        }
        detailJob = scheduler.schedule(() -> refreshDetails(lifecycleAtStart), delaySeconds, TimeUnit.SECONDS);
    }

    private void refreshDetails(int lifecycleAtStart) {
        if (isStale(lifecycleAtStart)) {
            return;
        }
        LiveTennisApiAccountHandler bridge = accountHandler();
        Bridge bridgeThing = getBridge();
        if (bridge == null || bridgeThing == null || bridgeThing.getStatus() != ThingStatus.ONLINE) {
            // Do not spend quota while the bridge is not known to be up; bridgeStatusChanged re-arms on recovery
            return;
        }
        if (!acquireRefresh(lifecycleAtStart)) {
            // Another refresh of this lifecycle is issuing requests right now and re-arms when it finishes
            return;
        }
        long nextDelaySeconds = detailRefreshInterval;
        try {
            long now = System.nanoTime();
            RefreshMark last = lastDetailRefresh;
            if (last != null && last.lifecycle() == lifecycleAtStart
                    && now - last.nanos() < MIN_DETAIL_REFRESH_SPACING_NANOS) {
                return;
            }
            lastDetailRefresh = new RefreshMark(lifecycleAtStart, now);

            if (isProfileRefreshDue(lifecycleAtStart, now)) {
                Player refreshedPlayer = bridge.fetchPlayer(playerId);
                if (isStale(lifecycleAtStart)) {
                    return;
                }
                player = refreshedPlayer;
                updateProfileChannels(refreshedPlayer);
                lastProfileRefresh = new RefreshMark(lifecycleAtStart, now);
            }
            Match refreshedNextMatch = bridge.fetchNextMatch(playerId);
            if (isStale(lifecycleAtStart)) {
                return;
            }
            nextMatch = refreshedNextMatch;
            updateNextMatchChannels(refreshedNextMatch);
        } catch (LiveTennisApiNotFoundException e) {
            // A wrong player id will not fix itself; the next configuration change starts a new lifecycle
            nextDelaySeconds = -1;
            if (!isStale(lifecycleAtStart)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-player-not-found");
            }
        } catch (LiveTennisApiAuthenticationException e) {
            logger.debug("Authentication failed, the bridge poll will report it", e);
        } catch (LiveTennisApiTransientException e) {
            logger.debug("Detail refresh hit a transient error, retrying in {} s", TRANSIENT_RETRY_DELAY_S, e);
            nextDelaySeconds = TRANSIENT_RETRY_DELAY_S;
        } catch (LiveTennisApiException e) {
            logger.debug("Detail refresh failed", e);
        } finally {
            releaseRefresh(lifecycleAtStart);
            if (nextDelaySeconds >= 0) {
                scheduleDetailRefresh(nextDelaySeconds, lifecycleAtStart);
            }
        }
    }

    /** Whether the lifecycle has been disposed or re-initialized since the given value was captured. */
    private boolean isStale(int lifecycleAtStart) {
        return disposed || lifecycle.get() != lifecycleAtStart;
    }

    /**
     * Marks a refresh of the given lifecycle as issuing requests. Refuses only while another refresh of the same
     * lifecycle is in progress; a refresh that a previous lifecycle left in flight can no longer publish anything and
     * must not block the first refresh of the new lifecycle.
     */
    private boolean acquireRefresh(int lifecycleAtStart) {
        while (true) {
            int current = refreshingLifecycle.get();
            if (current == lifecycleAtStart) {
                return false;
            }
            if (refreshingLifecycle.compareAndSet(current, lifecycleAtStart)) {
                return true;
            }
        }
    }

    private void releaseRefresh(int lifecycleAtStart) {
        refreshingLifecycle.compareAndSet(lifecycleAtStart, NO_REFRESH);
    }

    private boolean isProfileRefreshDue(int lifecycleAtStart, long now) {
        RefreshMark last = lastProfileRefresh;
        return last == null || last.lifecycle() != lifecycleAtStart
                || now - last.nanos() >= PROFILE_REFRESH_INTERVAL_NANOS;
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
