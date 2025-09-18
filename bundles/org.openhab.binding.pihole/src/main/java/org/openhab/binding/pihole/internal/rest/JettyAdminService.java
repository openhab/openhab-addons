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
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.PiHoleException;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class JettyAdminService extends AdminService {
    private final Logger logger = LoggerFactory.getLogger(JettyAdminService.class);

    private final URI baseUrl;

    public JettyAdminService(String token, URI baseUrl, HttpClient client, Gson gson) {
        super(token, client, gson);
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<DnsStatistics> summary() throws PiHoleException {
        logger.debug("Getting summary");
        var url = baseUrl.resolve("/admin/api.php?summaryRaw&auth=" + token);
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        var response = send(request);
        var content = response.getContentAsString();
        return Optional.ofNullable(gson.fromJson(content, DnsStatistics.class));
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
        var url = baseUrl.resolve("/admin/api.php?enable&auth=%s".formatted(token));
        var request = client.newRequest(url).timeout(TIMEOUT_SECONDS, SECONDS);
        send(request);
    }
}
