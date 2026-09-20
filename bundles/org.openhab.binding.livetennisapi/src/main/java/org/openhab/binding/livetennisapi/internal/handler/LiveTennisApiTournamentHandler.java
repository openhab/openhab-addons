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
import org.openhab.binding.livetennisapi.internal.api.dto.Tournament;
import org.openhab.binding.livetennisapi.internal.config.LiveTennisApiTournamentConfiguration;
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
 * Tracks one tournament of the catalogue: its curated metadata (fetched once per lifecycle) and the state of its
 * featured (first listed) live match, pushed by the bridge poll.
 *
 * @author Ben Abulafia - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiTournamentHandler extends BaseThingHandler implements LiveMatchesListener {

    private static final long TRANSIENT_RETRY_DELAY_S = 60;
    // Lifecycle values start at 1, so 0 marks "no refresh in progress".
    private static final int NO_REFRESH = 0;

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiTournamentHandler.class);

    private String tournamentId = "";

    // The single pending info refresh (initial fetch, bridge ONLINE or transient retry): every trigger replaces it,
    // so at most one is pending per lifecycle and dispose() has exactly one future to cancel.
    private @Nullable ScheduledFuture<?> infoJob;
    private volatile boolean disposed;
    // Bumped on every initialize() and dispose(); an in-flight info refresh captures it and only publishes or re-arms
    // while it still matches, so it cannot publish data or status for a disposed or reconfigured lifecycle.
    private final AtomicInteger lifecycle = new AtomicInteger();
    // Lifecycle of the refresh currently issuing a request, or NO_REFRESH. Keyed by lifecycle so a refresh left in
    // flight by a previous lifecycle can never block the first refresh of the next one.
    private final AtomicInteger refreshingLifecycle = new AtomicInteger(NO_REFRESH);
    private @Nullable Tournament tournament;
    private List<Match> tournamentMatches = List.of();

    public LiveTennisApiTournamentHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        LiveTennisApiTournamentConfiguration config = getConfigAs(LiveTennisApiTournamentConfiguration.class);

        if (config.tournamentId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-tournament-id");
            return;
        }
        tournamentId = config.tournamentId;
        int currentLifecycle = lifecycle.incrementAndGet();
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);

        scheduleInfoRefresh(0, currentLifecycle);
    }

    @Override
    public void dispose() {
        synchronized (this) {
            lifecycle.incrementAndGet();
            disposed = true;
            ScheduledFuture<?> job = infoJob;
            if (job != null) {
                job.cancel(true);
                infoJob = null;
            }
        }
        tournament = null;
        tournamentMatches = List.of();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Re-render from the cached snapshots; the bridge poll is the only API request trigger
            updateInfoChannels(tournament);
            updateLiveChannels(tournamentMatches);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && tournament == null) {
            scheduleInfoRefresh(0, lifecycle.get());
        }
    }

    @Override
    public void onLiveMatches(List<Match> liveMatches) {
        if (disposed) {
            return;
        }
        List<Match> matches = liveMatches.stream().filter(match -> tournamentId.equals(match.tournamentId)).toList();
        tournamentMatches = matches;
        updateLiveChannels(matches);
        // The bridge poll succeeded, so the tournament is reachable; the metadata fetch never drives ONLINE
        setOnlineUnlessMisconfigured();
    }

    /**
     * Schedules the info refresh of the given lifecycle, replacing whatever is pending. Does nothing once that
     * lifecycle is over, so a run that ends while the handler is being disposed or reconfigured cannot re-arm itself.
     */
    private synchronized void scheduleInfoRefresh(long delaySeconds, int lifecycleAtStart) {
        if (isStale(lifecycleAtStart)) {
            return;
        }
        ScheduledFuture<?> pending = infoJob;
        if (pending != null) {
            pending.cancel(false);
        }
        infoJob = scheduler.schedule(() -> refreshInfo(lifecycleAtStart), delaySeconds, TimeUnit.SECONDS);
    }

    private void refreshInfo(int lifecycleAtStart) {
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
            // Another refresh of this lifecycle is issuing the request right now
            return;
        }
        boolean retry = false;
        try {
            Tournament refreshedTournament = bridge.fetchTournament(tournamentId);
            if (isStale(lifecycleAtStart)) {
                return;
            }
            tournament = refreshedTournament;
            updateInfoChannels(refreshedTournament);
        } catch (LiveTennisApiNotFoundException e) {
            if (!isStale(lifecycleAtStart)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-tournament-not-found");
            }
        } catch (LiveTennisApiAuthenticationException e) {
            logger.debug("Authentication failed, the bridge poll will report it", e);
        } catch (LiveTennisApiTransientException e) {
            logger.debug("Tournament info refresh hit a transient error, retrying in {} s", TRANSIENT_RETRY_DELAY_S, e);
            retry = true;
        } catch (LiveTennisApiException e) {
            logger.debug("Tournament info refresh failed", e);
        } finally {
            releaseRefresh(lifecycleAtStart);
            if (retry) {
                scheduleInfoRefresh(TRANSIENT_RETRY_DELAY_S, lifecycleAtStart);
            }
        }
    }

    /** Whether the lifecycle has been disposed or re-initialized since the given value was captured. */
    private boolean isStale(int lifecycleAtStart) {
        return disposed || lifecycle.get() != lifecycleAtStart;
    }

    /**
     * Marks a refresh of the given lifecycle as issuing a request. Refuses only while another refresh of the same
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

    private void updateInfoChannels(@Nullable Tournament tournament) {
        updateState(CHANNEL_INFO_NAME, string(tournament == null ? null : tournament.name));
        updateState(CHANNEL_INFO_SURFACE, string(tournament == null ? null : tournament.surface));
        updateState(CHANNEL_INFO_CATEGORY, string(tournament == null ? null : tournament.category));
    }

    private void updateLiveChannels(List<Match> matches) {
        updateState(CHANNEL_LIVE_MATCH_COUNT, new DecimalType(matches.size()));

        Match featured = matches.isEmpty() ? null : matches.get(0);
        if (featured == null) {
            updateState(CHANNEL_LIVE_PLAYERS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_DISCIPLINE, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_STATUS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SCORE_LINE, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SETS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_POINTS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SERVER, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_BREAK_POINT, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_TIEBREAK, UnDefType.UNDEF);
            return;
        }
        Score score = featured.score;
        Integer server = MatchStateMapper.server(score);
        Boolean tiebreak = score == null ? null : score.isTiebreak;

        updateState(CHANNEL_LIVE_PLAYERS, string(playersLine(featured)));
        updateState(CHANNEL_LIVE_DISCIPLINE, string(MatchStateMapper.discipline(featured)));
        updateState(CHANNEL_LIVE_STATUS, string(featured.status));
        updateState(CHANNEL_LIVE_SCORE_LINE, string(MatchStateMapper.scoreLine(score, 1)));
        updateState(CHANNEL_LIVE_SETS, string(MatchStateMapper.setsLine(score, 1)));
        updateState(CHANNEL_LIVE_POINTS, string(MatchStateMapper.pointsLine(score, 1)));
        updateState(CHANNEL_LIVE_SERVER, string(server == null ? null : playerName(featured, server)));
        updateState(CHANNEL_LIVE_BREAK_POINT, onOff(MatchStateMapper.isBreakPoint(score)));
        updateState(CHANNEL_LIVE_TIEBREAK, onOff(tiebreak));
    }

    private static @Nullable String playersLine(Match match) {
        String p1 = playerName(match, 1);
        String p2 = playerName(match, 2);
        return p1 == null || p2 == null ? null : p1 + " vs " + p2;
    }

    private static @Nullable String playerName(Match match, int side) {
        MatchPlayers players = match.players;
        Player player = players == null ? null : (side == 2 ? players.p2 : players.p1);
        return player == null ? null : player.name;
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
}
