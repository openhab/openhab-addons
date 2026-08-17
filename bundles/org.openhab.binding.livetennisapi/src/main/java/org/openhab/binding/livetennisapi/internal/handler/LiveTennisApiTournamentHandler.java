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

import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_INFO_CATEGORY;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_INFO_NAME;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_INFO_SURFACE;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_BREAK_POINT;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_MATCH_COUNT;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_PLAYERS;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_POINTS;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_SCORE_LINE;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_SERVER;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_SETS;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_STATUS;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.CHANNEL_LIVE_TIEBREAK;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livetennisapi.internal.MatchStateMapper;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiAuthenticationException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiClient;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiNotFoundException;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.MatchPlayers;
import org.openhab.binding.livetennisapi.internal.api.dto.Player;
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
 * Tracks one tournament of the catalogue: its curated metadata and the state of its featured (first listed) live
 * match, pushed by the bridge poll.
 *
 * @author Ben - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiTournamentHandler extends BaseThingHandler implements LiveMatchesListener {

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiTournamentHandler.class);

    private String tournamentId = "";

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
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::refreshInfo);
    }

    @Override
    public void dispose() {
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
            scheduler.execute(this::refreshInfo);
        }
    }

    @Override
    public void onLiveMatches(List<Match> liveMatches) {
        List<Match> matches = liveMatches.stream().filter(match -> tournamentId.equals(match.tournamentId)).toList();
        tournamentMatches = matches;
        updateLiveChannels(matches);
        setOnlineUnlessMisconfigured();
    }

    private void refreshInfo() {
        LiveTennisApiClient client = apiClient();
        if (client == null) {
            return;
        }
        try {
            Tournament refreshedTournament = client.getTournament(tournamentId);
            tournament = refreshedTournament;
            updateInfoChannels(refreshedTournament);
            setOnlineUnlessMisconfigured();
        } catch (LiveTennisApiNotFoundException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-tournament-not-found");
        } catch (LiveTennisApiAuthenticationException e) {
            logger.debug("Authentication failed, the bridge poll will report it", e);
        } catch (LiveTennisApiException e) {
            logger.debug("Tournament info refresh failed", e);
        }
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
            updateState(CHANNEL_LIVE_STATUS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SCORE_LINE, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SETS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_POINTS, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_SERVER, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_BREAK_POINT, UnDefType.UNDEF);
            updateState(CHANNEL_LIVE_TIEBREAK, UnDefType.UNDEF);
            return;
        }
        Integer server = MatchStateMapper.server(featured.score);
        Boolean tiebreak = featured.score == null ? null : featured.score.isTiebreak;

        updateState(CHANNEL_LIVE_PLAYERS, string(playersLine(featured)));
        updateState(CHANNEL_LIVE_STATUS, string(featured.status));
        updateState(CHANNEL_LIVE_SCORE_LINE, string(MatchStateMapper.scoreLine(featured.score, 1)));
        updateState(CHANNEL_LIVE_SETS, string(MatchStateMapper.setsLine(featured.score, 1)));
        updateState(CHANNEL_LIVE_POINTS, string(MatchStateMapper.pointsLine(featured.score, 1)));
        updateState(CHANNEL_LIVE_SERVER, string(server == null ? null : playerName(featured, server)));
        updateState(CHANNEL_LIVE_BREAK_POINT, onOff(MatchStateMapper.isBreakPoint(featured.score)));
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

    private @Nullable LiveTennisApiClient apiClient() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof LiveTennisApiAccountHandler accountHandler
                ? accountHandler.getApiClient()
                : null;
    }

    private static State string(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    private static State onOff(@Nullable Boolean value) {
        return value == null ? UnDefType.UNDEF : OnOffType.from(value.booleanValue());
    }
}
