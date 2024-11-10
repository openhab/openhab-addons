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

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.i18n.ConfigurationException;

/**
 * Handles the authentication to Homematic server.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class AuthenticationHandler {

    private Boolean useAuthentication;
    private @Nullable String authValue;

    public AuthenticationHandler(HomematicConfig config) throws ConfigurationException {
        this.useAuthentication = config.getUseAuthentication();
        if (!useAuthentication) {
            return;
        }

        if (config.getPassword() == null || config.getUserName() == null) {
            throw new ConfigurationException("Username or password missing");
        }
        this.authValue = "Basic "
                + Base64.getEncoder().encodeToString((config.getUserName() + ":" + config.getPassword()).getBytes());
    }

    /**
     * Add or remove the basic auth credentials th the request if needed.
     */
    public Request updateAuthenticationInformation(final Request request) {
        return useAuthentication ? request.header(HttpHeader.AUTHORIZATION, authValue) : request;
    }
}
