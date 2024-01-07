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
package org.openhab.binding.homematic.internal.common;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;

/**
 * Handles the authentication to Homematic server.
 *
 * @author Christian Kittel
 */
@NonNullByDefault
public class AuthenticationHandler {

    private final HomematicConfig config;
    private final HttpClient httpClient;

    public AuthenticationHandler(HomematicConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    /**
     * Add or remove the basic auth credetials to the AuthenticationStore if needed.
     */
    public synchronized void updateAuthenticationInformation(final URI uri) throws IllegalStateException {
        final AuthenticationStore authStore = httpClient.getAuthenticationStore();

        Authentication findAuthentication = authStore.findAuthentication("Basic", uri, Authentication.ANY_REALM);

        if (config.getUseAuthentication()) {
            if (findAuthentication == null) {
                if (config.getPassword() == null || config.getUserName() == null) {
                    throw new IllegalStateException("Username or password missing");
                }
                authStore.addAuthentication(new BasicAuthentication(uri, Authentication.ANY_REALM, config.getUserName(),
                        config.getPassword()));
            }
        } else if (findAuthentication != null) {
            authStore.removeAuthentication(findAuthentication);
        }
    }
}
