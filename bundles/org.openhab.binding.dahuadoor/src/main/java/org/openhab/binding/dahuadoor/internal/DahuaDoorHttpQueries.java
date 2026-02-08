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
package org.openhab.binding.dahuadoor.internal;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class DahuaDoorHttpQueries {

    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    private final Logger logger = LoggerFactory.getLogger(DahuaDoorHttpQueries.class);
    private @Nullable DahuaDoorConfiguration config;
    private @Nullable HttpClient httpClient;
    private @Nullable DigestAuthentication digestAuth;
    private @Nullable URI authUri;

    public DahuaDoorHttpQueries(@Nullable HttpClient httpClient, @Nullable DahuaDoorConfiguration config) {
        this.config = config;
        this.httpClient = httpClient;

        // Configure digest authentication once during construction to avoid unbounded growth of AuthenticationStore
        if (httpClient != null && config != null) {
            try {
                authUri = new URI("http://" + config.hostname);
                AuthenticationStore auth = httpClient.getAuthenticationStore();
                digestAuth = new DigestAuthentication(authUri, Authentication.ANY_REALM, config.username,
                        config.password);
                auth.addAuthentication(digestAuth);
            } catch (Exception e) {
                logger.warn("Failed to configure digest authentication for {}", config.hostname, e);
            }
        }
    }

    public byte @Nullable [] requestImage() {
        final @Nullable HttpClient localHttpClient = httpClient;
        final @Nullable DahuaDoorConfiguration localConfig = config;

        if (localHttpClient == null || localConfig == null) {
            logger.warn("HTTP client or configuration not initialized");
            return null;
        }

        try {
            URI uri = new URI("http://" + localConfig.hostname + "/cgi-bin/snapshot.cgi");
            ContentResponse response = localHttpClient.newRequest(uri)
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            if (response.getStatus() == 200) {
                return response.getContent();
            } else {
                logger.debug("Snapshot request failed with HTTP status {} from {}", response.getStatus(),
                        localConfig.hostname);
            }
        } catch (Exception e) {
            logger.warn("Could not make http connection to retrieve snapshot from {}", localConfig.hostname, e);
        }
        return null;
    }

    public void openDoor(int doorNo) {
        final @Nullable HttpClient localHttpClient = httpClient;
        final @Nullable DahuaDoorConfiguration localConfig = config;

        if (localHttpClient == null || localConfig == null) {
            logger.warn("HTTP client or configuration not initialized");
            return;
        }

        try {
            URI uri = new URI("http://" + localConfig.hostname + "/cgi-bin/accessControl.cgi");
            Request request = localHttpClient.newRequest(uri).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            request.param("action", "openDoor");
            request.param("UserID", "101");
            request.param("Type", "Remote");
            request.param("channel", Integer.toString(doorNo));
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                logger.debug("Open Door Success");
            } else {
                logger.debug("Open door request failed with HTTP status {} for door {} on {}", response.getStatus(),
                        doorNo, localConfig.hostname);
            }
        } catch (Exception e) {
            logger.warn("Could not make http connection to open door {} on {}", doorNo, localConfig.hostname, e);
        }
    }

    /**
     * Dispose resources and clean up authentication from shared HttpClient
     */
    public void dispose() {
        final HttpClient localHttpClient = httpClient;
        final DigestAuthentication localDigestAuth = digestAuth;
        final URI localAuthUri = authUri;

        if (localHttpClient != null && localDigestAuth != null && localAuthUri != null) {
            AuthenticationStore auth = localHttpClient.getAuthenticationStore();
            auth.removeAuthentication(localDigestAuth);
            logger.debug("Removed digest authentication from shared HttpClient for URI {}", localAuthUri);
        }

        digestAuth = null;
        authUri = null;
        httpClient = null;
        config = null;
    }
}
