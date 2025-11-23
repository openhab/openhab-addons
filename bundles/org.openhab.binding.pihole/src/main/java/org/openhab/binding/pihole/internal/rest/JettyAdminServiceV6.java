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
package org.openhab.binding.pihole.internal.rest;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.pihole.internal.PiHoleException;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.openhab.binding.pihole.internal.rest.model.GravityLastUpdated;
import org.openhab.binding.pihole.internal.rest.model.Relative;
import org.openhab.binding.pihole.internal.rest.model.v6.Blocking;
import org.openhab.binding.pihole.internal.rest.model.v6.ConfigAnswer;
import org.openhab.binding.pihole.internal.rest.model.v6.DnsBlockingAnswer;
import org.openhab.binding.pihole.internal.rest.model.v6.HistoryClients;
import org.openhab.binding.pihole.internal.rest.model.v6.Password;
import org.openhab.binding.pihole.internal.rest.model.v6.SessionAnswer;
import org.openhab.binding.pihole.internal.rest.model.v6.SessionAnswer.Session;
import org.openhab.binding.pihole.internal.rest.model.v6.StatAnswer;
import org.openhab.binding.pihole.internal.rest.model.v6.StatAnswer.Gravity;
import org.openhab.binding.pihole.internal.rest.model.v6.StatAnswer.Queries;
import org.openhab.binding.pihole.internal.rest.model.v6.StatAnswer.Replies;
import org.openhab.binding.pihole.internal.rest.model.v6.StatDatabaseSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class JettyAdminServiceV6 extends AdminService {
    private final Logger logger = LoggerFactory.getLogger(JettyAdminServiceV6.class);
    private final URI authURI;
    private final URI statsSummaryURI;
    private final URI dnsBlockingURI;
    private final URI databaseSummaryURI;
    private final URI historyClientsURI;
    private final URI configURI;
    private final String tokenJson;

    private @Nullable String sid;
    private @Nullable Instant sessionValidity;
    private @Nullable Instant midnightUtc;

    public JettyAdminServiceV6(String token, URI baseUrl, HttpClient client, Gson gson) {
        super(client, gson);
        tokenJson = gson.toJson(new Password(token));

        UriBuilder apiUriBuilder = UriBuilder.fromUri(baseUrl).path("api");
        authURI = apiUriBuilder.clone().path("auth").build();
        configURI = apiUriBuilder.clone().path("config").build();
        dnsBlockingURI = apiUriBuilder.clone().path("dns").path("blocking").build();
        historyClientsURI = apiUriBuilder.clone().path("history").path("clients").build();

        UriBuilder statsUriBuilder = apiUriBuilder.clone().path("stats");
        statsSummaryURI = statsUriBuilder.clone().path("summary").build();
        databaseSummaryURI = statsUriBuilder.clone().path("database").path("summary").build();
    }

    private String updateSid() throws PiHoleException {
        logger.debug("Get or update session ID");
        var request = client.newRequest(authURI).timeout(TIMEOUT_SECONDS, SECONDS).method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT, "application/json").content(new StringContentProvider(tokenJson));
        var content = send(request).getContentAsString();
        logger.debug("Session update request answer: {}", content);

        if (gson.fromJson(content, SessionAnswer.class) instanceof SessionAnswer answer) {
            Session session = answer.session();
            if (session == null) {
                throw new PiHoleException("Received an empty session");
            }
            sid = session.sid();
            sessionValidity = Instant.now().plusSeconds(session.cautiousValidity());
            return session.sid();
        }

        throw new PiHoleException("Error deserializing '%s'".formatted(content));
    }

    private String getSid() throws PiHoleException {
        if (sid instanceof String localSid && sessionValidity instanceof Instant validUntil
                && Instant.now().isBefore(validUntil)) {
            return localSid;
        }
        return updateSid();
    }

    private long getTodayMidnight() {
        Instant now = Instant.now();
        Instant local = midnightUtc;

        if (local == null || now.minus(1, ChronoUnit.DAYS).isAfter(local)) {
            local = now.truncatedTo(ChronoUnit.DAYS);
            midnightUtc = local;
        }

        return local.getEpochSecond();
    }

    @Override
    public Optional<DnsStatistics> summary() throws PiHoleException {
        logger.debug("Building the as if it was a v5 API");
        StatAnswer statAnswer = get(statsSummaryURI, StatAnswer.class);
        Gravity gravity = statAnswer.gravity();
        Queries statQueries = statAnswer.queries();
        Replies replies = statQueries.replies();

        DnsBlockingAnswer blockingAnswer = get(dnsBlockingURI, DnsBlockingAnswer.class);

        long todayMidnight = getTodayMidnight();
        StatDatabaseSummary statDatabase = get(databaseSummaryURI, StatDatabaseSummary.class, "from",
                Long.toString(todayMidnight), "until", Long.toString(todayMidnight + 24 * 60 * 60));

        HistoryClients historyClients = get(historyClientsURI, HistoryClients.class, "N", "0");
        ConfigAnswer configAnswer = get(configURI, ConfigAnswer.class);
        Duration duration = Duration.between(gravity.instant(), Instant.now());
        Relative relative = new Relative((int) duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart());

        DnsStatistics translated = new DnsStatistics(gravity.domainsBeingBlocked(), statDatabase.sumQueries(),
                statDatabase.sumBlocked(), statDatabase.percentBlocked(), statQueries.uniqueDomains(),
                statQueries.forwarded(), statQueries.cached(), historyClients.clients().size(), null,
                statQueries.types().all(), replies.unknown(), replies.nodata(), replies.nxdomain(), replies.cname(),
                replies.ip(), replies.domain(), replies.rrname(), replies.servfail(), replies.refused(),
                replies.notimp(), replies.other(), replies.dnssec(), replies.none(), replies.blob(), replies.all(),
                configAnswer.config().misc().privacylevel(), blockingAnswer.blocking(), new GravityLastUpdated(
                        configAnswer.config().files().gravity() != null, gravity.lastUpdate(), relative));

        return Optional.of(translated);
    }

    @Override
    public void disableBlocking(long seconds) throws PiHoleException {
        logger.debug("Disabling blocking for {} seconds", seconds);
        post(dnsBlockingURI, new Blocking(false, seconds));
    }

    @Override
    public void enableBlocking() throws PiHoleException {
        logger.debug("Enabling blocking");
        post(dnsBlockingURI, Blocking.BLOCK);
    }

    private void post(URI targetURI, Object object) throws PiHoleException {
        var request = client.newRequest(targetURI).timeout(TIMEOUT_SECONDS, SECONDS).method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT, "application/json").header("sid", getSid())
                .content(new StringContentProvider(gson.toJson(object)));
        send(request);
    }

    private <T> T get(URI targetURI, Class<T> clazz, @Nullable Object... params) throws PiHoleException {
        var request = client.newRequest(targetURI).timeout(TIMEOUT_SECONDS, SECONDS)
                .header(HttpHeader.ACCEPT, "application/json").header("sid", getSid());

        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("params count must be even");
        }
        for (int i = 0; i < params.length; i += 2) {
            if (params[i] instanceof String name && params[i + 1] instanceof String param) {
                request = request.param(name, param);
            } else {
                throw new IllegalArgumentException("parameters must be String");
            }
        }

        var content = send(request).getContentAsString();
        T answer = gson.fromJson(content, clazz);
        if (answer != null) {
            return answer;
        }
        throw new PiHoleException("Error deserializing %s".formatted(content));
    }
}
