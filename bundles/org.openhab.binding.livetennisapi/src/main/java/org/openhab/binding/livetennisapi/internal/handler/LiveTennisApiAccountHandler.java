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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiAuthenticationException;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiClient;
import org.openhab.binding.livetennisapi.internal.api.LiveTennisApiException;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.Player;
import org.openhab.binding.livetennisapi.internal.api.dto.Tournament;
import org.openhab.binding.livetennisapi.internal.api.dto.Usage;
import org.openhab.binding.livetennisapi.internal.config.LiveTennisApiAccountConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler for one Live Tennis API key. Polls the current live match snapshot once per cycle and pushes it to
 * all child things, so the request budget does not grow with the number of tracked players or tournaments.
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiAccountHandler extends BaseBridgeHandler {

    private static final long INITIAL_DELAY_S = 1;

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiAccountHandler.class);

    private final HttpClient httpClient;

    private @Nullable LiveTennisApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollingJob;
    private volatile boolean disposed;

    private List<Match> lastLiveMatches = List.of();
    private @Nullable Usage lastUsage;

    public LiveTennisApiAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        LiveTennisApiAccountConfiguration config = getConfigAs(LiveTennisApiAccountConfiguration.class);

        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-api-key");
            return;
        }
        if (config.refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-interval");
            return;
        }

        disposed = false;
        apiClient = new LiveTennisApiClient(httpClient, config.apiKey);
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY_S, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        apiClient = null;
        lastLiveMatches = List.of();
        lastUsage = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Re-render from the cached snapshot; the polling job is the only trigger for API requests
            updateUsageChannels(lastUsage);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof LiveMatchesListener listener && getThing().getStatus() == ThingStatus.ONLINE) {
            listener.onLiveMatches(lastLiveMatches);
        }
    }

    /**
     * Fetches one player's bio and current ranking on behalf of a child thing. The bridge owns the API client and
     * exposes only these data methods, so children never hold implementation details of the transport.
     */
    public Player fetchPlayer(long playerId) throws LiveTennisApiException {
        return requireClient().getPlayer(playerId);
    }

    /** Fetches the given player's next upcoming match on behalf of a child thing, or null when none is scheduled. */
    public @Nullable Match fetchNextMatch(long playerId) throws LiveTennisApiException {
        return requireClient().getNextMatch(playerId);
    }

    /** Fetches one tournament of the catalogue on behalf of a child thing. */
    public Tournament fetchTournament(String tournamentId) throws LiveTennisApiException {
        return requireClient().getTournament(tournamentId);
    }

    private LiveTennisApiClient requireClient() throws LiveTennisApiException {
        LiveTennisApiClient client = apiClient;
        if (client == null) {
            throw new LiveTennisApiException("The account bridge is not initialized");
        }
        return client;
    }

    private void poll() {
        LiveTennisApiClient client = apiClient;
        if (client == null) {
            return;
        }
        List<Match> liveMatches;
        try {
            liveMatches = client.getLiveMatches();
        } catch (LiveTennisApiAuthenticationException e) {
            logger.debug("Authentication failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        } catch (LiveTennisApiException e) {
            logger.debug("Polling failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        if (disposed) {
            return;
        }
        lastLiveMatches = liveMatches;
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        notifyChildHandlers(liveMatches);

        // The usage read is optional telemetry: a failure here must never discard the live data published above.
        try {
            Usage usage = client.getUsage();
            if (disposed) {
                return;
            }
            lastUsage = usage;
            updateUsageChannels(usage);
        } catch (LiveTennisApiException e) {
            logger.debug("Usage read failed; the live match data for this cycle was still published", e);
        }
    }

    private void notifyChildHandlers(List<Match> liveMatches) {
        for (Thing childThing : getThing().getThings()) {
            if (childThing.getHandler() instanceof LiveMatchesListener listener) {
                listener.onLiveMatches(liveMatches);
            }
        }
    }

    private void updateUsageChannels(@Nullable Usage usage) {
        Usage.Today today = usage == null ? null : usage.today;
        String tier = usage == null ? null : usage.tier;
        Integer calls = today == null ? null : today.calls;
        Integer remaining = today == null ? null : today.remainingDay;

        updateState(CHANNEL_TIER, tier == null ? UnDefType.UNDEF : new StringType(tier));
        updateState(CHANNEL_CALLS_TODAY, calls == null ? UnDefType.UNDEF : new DecimalType(calls));
        updateState(CHANNEL_REMAINING_TODAY, remaining == null ? UnDefType.UNDEF : new DecimalType(remaining));
    }
}
