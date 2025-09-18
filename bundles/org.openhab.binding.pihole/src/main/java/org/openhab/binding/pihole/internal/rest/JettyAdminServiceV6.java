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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.pihole.internal.PiHoleException;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.openhab.binding.pihole.internal.rest.model.v6.Password;
import org.openhab.binding.pihole.internal.rest.model.v6.SessionAnswer;
import org.openhab.binding.pihole.internal.rest.model.v6.SessionAnswer.Session;
import org.openhab.binding.pihole.internal.rest.model.v6.StatAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class JettyAdminServiceV6 extends AdminService {
    private final Logger logger = LoggerFactory.getLogger(JettyAdminServiceV6.class);
    protected final URI apiUrl;
    private @Nullable Session session;

    public JettyAdminServiceV6(String token, URI baseUrl, HttpClient client, Gson gson) {
        super(token, client, gson);
        apiUrl = baseUrl.resolve("/api");
    }

    private void getAuth() throws PiHoleException {
        logger.debug("Check if authentication is required");
        var authUrl = apiUrl.resolve(apiUrl.getPath() + "/auth");
        var request = client.newRequest(authUrl).timeout(TIMEOUT_SECONDS, SECONDS).method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT, "application/json")
                .content(new StringContentProvider(gson.toJson(new Password(token))));
        var response = send(request);
        var content = response.getContentAsString();
        SessionAnswer answer = gson.fromJson(content, SessionAnswer.class);
        logger.debug(answer.session().message());
        session = answer.session();
    }

    @Override
    public Optional<DnsStatistics> summary() throws PiHoleException {
        logger.debug("Getting summary");
        getAuth();
        var url = apiUrl.resolve(apiUrl.getPath() + "/stats/summary");
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS).method(HttpMethod.GET)
                .header(HttpHeader.ACCEPT, "application/json").header("sid", session.sid());
        var response = send(request);
        var content = response.getContentAsString();
        StatAnswer answer = gson.fromJson(content, StatAnswer.class);
        DnsStatistics translated = new DnsStatistics(answer.gravity().domainsBeingBlocked(), null, null, null,
                answer.queries().uniqueDomains(), answer.queries().forwarded(), answer.queries().cached(), null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
        return Optional.of(translated);
    }

    @Override
    public void disableBlocking(long seconds) throws PiHoleException {
        logger.debug("Disabling blocking for {} seconds", seconds);
        // var url = baseUrl.resolve("/admin/api.php?disable=%s&auth=%s".formatted(seconds, token));
        // var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        // send(request);
    }

    @Override
    public void enableBlocking() throws PiHoleException {
        logger.debug("Enabling blocking");
        // var url = baseUrl.resolve("/admin/api.php?enable&auth=%s".formatted(token));
        // var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        // send(request);
    }
}
