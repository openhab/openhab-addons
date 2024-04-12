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

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class JettyAdminService implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(JettyAdminService.class);
    private static final Gson GSON = new Gson();
    private final String token;
    private final URI baseUrl;
    private final HttpClient client;

    public JettyAdminService(String token, URI baseUrl, HttpClient client) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.client = client;
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
    }

    @Override
    public Optional<DnsStatistics> summary() throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Getting summary");
        var url = baseUrl.resolve("/admin/api.php?summaryRaw&auth=" + token);
        var request = client.newRequest(url);
        var response = request.send();
        var content = response.getContentAsString();
        return Optional.ofNullable(GSON.fromJson(content, DnsStatistics.class));
    }

    @Override
    public void disableBlocking(long seconds) throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Disabling blocking for {} seconds", seconds);
        var url = baseUrl.resolve("/admin/api.php?disable=%s&auth=%s".formatted(seconds, token));
        var request = client.newRequest(url);
        request.send();
    }

    @Override
    public void enableBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Enabling blocking");
        var url = baseUrl.resolve("/admin/api.php?disable&auth=%s".formatted(token));
        var request = client.newRequest(url);
        request.send();
    }
}
