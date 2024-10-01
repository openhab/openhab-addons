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
package org.openhab.binding.pihole.internal.rest;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.pihole.internal.PiHoleException;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class JettyAdminService implements AdminService {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private static final long TIMEOUT_SECONDS = 10L;
    private final Logger logger = LoggerFactory.getLogger(JettyAdminService.class);
    private final String token;
    private final URI baseUrl;
    private final HttpClient client;

    public JettyAdminService(String token, URI baseUrl, HttpClient client) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.client = client;
    }

    @Override
    public Optional<DnsStatistics> summary() throws PiHoleException {
        logger.debug("Getting summary");
        var url = baseUrl.resolve("/admin/api.php?summaryRaw&auth=" + token);
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        var response = send(request);
        var content = response.getContentAsString();
        return Optional.ofNullable(GSON.fromJson(content, DnsStatistics.class));
    }

    private static ContentResponse send(Request request) throws PiHoleException {
        try {
            return request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new PiHoleException(
                    "Exception while sending request to Pi-hole. %s".formatted(e.getLocalizedMessage()), e);
        }
    }

    @Override
    public void disableBlocking(long seconds) throws PiHoleException {
        logger.debug("Disabling blocking for {} seconds", seconds);
        var url = baseUrl.resolve("/admin/api.php?disable=%s&auth=%s".formatted(seconds, token));
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        send(request);
    }

    @Override
    public void enableBlocking() throws PiHoleException {
        logger.debug("Enabling blocking");
        var url = baseUrl.resolve("/admin/api.php?disable&auth=%s".formatted(token));
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        send(request);
    }
}
