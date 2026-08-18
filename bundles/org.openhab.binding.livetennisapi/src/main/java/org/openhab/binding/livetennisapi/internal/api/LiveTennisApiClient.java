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
package org.openhab.binding.livetennisapi.internal.api;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.URIUtil;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.MatchListResponse;
import org.openhab.binding.livetennisapi.internal.api.dto.Player;
import org.openhab.binding.livetennisapi.internal.api.dto.Tournament;
import org.openhab.binding.livetennisapi.internal.api.dto.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Client for the free-tier endpoints of the Live Tennis API
 * (see <a href="https://docs.livetennisapi.com">the API documentation</a>).
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiClient {

    private static final String API_BASE = "https://api.livetennisapi.com/api/public/v1";
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final int TIMEOUT_S = 20;
    private static final int LIVE_MATCH_LIMIT = 200;
    private static final int UPCOMING_MATCH_LIMIT = 10;

    private final Logger logger = LoggerFactory.getLogger(LiveTennisApiClient.class);

    private final HttpClient httpClient;
    private final String apiKey;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public LiveTennisApiClient(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }

    /**
     * Returns the calling key's own usage and quota. Quota-exempt on the API side, so it is also suited as a
     * connection check.
     */
    public Usage getUsage() throws LiveTennisApiException {
        return get("/usage", Usage.class);
    }

    /** Returns all matches currently in progress, with their latest score. */
    public List<Match> getLiveMatches() throws LiveTennisApiException {
        MatchListResponse response = get("/matches?status=live&limit=" + LIVE_MATCH_LIMIT, MatchListResponse.class);
        List<Match> matches = response.data;
        if (matches == null) {
            return List.of();
        }
        if (matches.size() >= LIVE_MATCH_LIMIT) {
            logger.warn("The live match snapshot hit the page limit of {}; any matches beyond it are not tracked",
                    LIVE_MATCH_LIMIT);
        }
        return matches;
    }

    /** Returns the given player's next upcoming match, or null if none is scheduled. */
    public @Nullable Match getNextMatch(long playerId) throws LiveTennisApiException {
        MatchListResponse response = get(
                "/matches?status=upcoming&player=" + playerId + "&limit=" + UPCOMING_MATCH_LIMIT,
                MatchListResponse.class);
        List<Match> matches = response.data;
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        // Order by scheduled time; matches without one yet (a real state in the API) sort last
        return matches.stream()
                .sorted(Comparator.comparing(match -> match.scheduledTime == null ? "9999" : match.scheduledTime))
                .findFirst().orElse(null);
    }

    /** Returns one player's bio and current ranking. */
    public Player getPlayer(long playerId) throws LiveTennisApiException {
        return get("/players/" + playerId, Player.class);
    }

    /** Returns one tournament of the catalogue by its stable id. */
    public Tournament getTournament(String tournamentId) throws LiveTennisApiException {
        return get("/tournaments/" + URIUtil.encodePath(tournamentId), Tournament.class);
    }

    private <T> T get(String path, Class<T> type) throws LiveTennisApiException {
        final String url = API_BASE + path;

        logger.debug("GET {}", path);
        try {
            ContentResponse response = httpClient.newRequest(url).header(API_KEY_HEADER, apiKey)
                    .header(HttpHeader.ACCEPT, "application/json").timeout(TIMEOUT_S, TimeUnit.SECONDS).send();
            checkStatus(response.getStatus(), url);
            @Nullable
            T result = gson.fromJson(response.getContentAsString(), type);
            if (result == null) {
                throw new LiveTennisApiException("Empty response for " + url);
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new LiveTennisApiException("Malformed JSON response for " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveTennisApiException("Request interrupted: " + url, e);
        } catch (TimeoutException e) {
            throw new LiveTennisApiTransientException("Request timed out: " + url, e);
        } catch (ExecutionException e) {
            throw new LiveTennisApiException("Request failed: " + url, e);
        }
    }

    private void checkStatus(int status, String url) throws LiveTennisApiException {
        if (status == HttpStatus.UNAUTHORIZED_401 || status == HttpStatus.FORBIDDEN_403) {
            throw new LiveTennisApiAuthenticationException("@text/offline.conf-error-invalid-api-key");
        }
        if (status == HttpStatus.NOT_FOUND_404) {
            throw new LiveTennisApiNotFoundException("HTTP 404 for " + url);
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS_429) {
            throw new LiveTennisApiTransientException("@text/offline.comm-error-rate-limited");
        }
        if (status < HttpStatus.OK_200 || status >= HttpStatus.MULTIPLE_CHOICES_300) {
            throw new LiveTennisApiException("HTTP " + status + " for " + url);
        }
    }
}
