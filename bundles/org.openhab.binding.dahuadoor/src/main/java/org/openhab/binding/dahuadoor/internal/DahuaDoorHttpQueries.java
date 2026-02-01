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

    private final Logger logger = LoggerFactory.getLogger(DahuaDoorHttpQueries.class);
    private @Nullable DahuaDoorConfiguration config;
    public @Nullable HttpClient httpClient;

    public DahuaDoorHttpQueries(@Nullable HttpClient httpClient, @Nullable DahuaDoorConfiguration config) {
        this.config = config;
        this.httpClient = httpClient;
    }

    public byte @Nullable [] RequestImage() {

        // HttpClient httpClient = new HttpClient();

        try {
            // httpClient.start();

            URI uri = new URI("http://" + config.hostname + "/cgi-bin/snapshot.cgi");
            AuthenticationStore auth = httpClient.getAuthenticationStore();
            auth.addAuthentication(
                    new DigestAuthentication(uri, Authentication.ANY_REALM, config.username, config.password));
            ContentResponse response = httpClient.newRequest(uri).send();
            if (response.getStatus() == 200) {
                return response.getContent();
            }
        } catch (Exception e) {
            logger.warn("Could not make http connection");
        } /*
           * finally {
           * try {
           * httpClient.stop();
           * } catch (Exception e) {
           * }
           * }
           */
        return null;
    }

    public void OpenDoor(int doorNo) {

        // HttpClient httpClient = new HttpClient();
        try {
            // httpClient.start();
            URI uri = new URI("http://" + config.hostname + "/cgi-bin/accessControl.cgi");
            AuthenticationStore auth = httpClient.getAuthenticationStore();
            auth.addAuthentication(
                    new DigestAuthentication(uri, Authentication.ANY_REALM, config.username, config.password));
            Request request = httpClient.newRequest(uri);
            request.param("action", "openDoor");
            request.param("UserID", "101");
            request.param("Type", "Remote");
            request.param("channel", Integer.toString(doorNo));
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                logger.info("Open Door Success");
            }
        } catch (Exception e) {
            logger.warn("Could not make http connection");
        } /*
           * finally {
           * try {
           * httpClient.stop();
           * } catch (Exception e) {
           * }
           * }
           */
    }
}
