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
package org.openhab.binding.mielecloud.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.config.exception.OngoingAuthorizationException;
import org.openhab.core.thing.ThingUID;

/**
 * Handles OAuth 2 authorization processes.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public interface OAuthAuthorizationHandler {
    /**
     * Begins the authorization process after the user provided client ID, client secret and a bridge ID.
     *
     * @param clientId Client ID.
     * @param clientSecret Client secret.
     * @param bridgeUid The UID of the bridge to authorize.
     * @param email E-mail address identifying the account to authorize.
     * @throws OngoingAuthorizationException if there already is an ongoing authorization.
     */
    void beginAuthorization(String clientId, String clientSecret, ThingUID bridgeUid, String email);

    /**
     * Creates the authorization URL for the ongoing authorization.
     *
     * @param redirectUri The URI to which the user is redirected after a successful login. This should point to our own
     *            service.
     * @return The authorization URL to which the user is redirected for the log in.
     * @throws NoOngoingAuthorizationException if there is no ongoing authorization.
     * @throws org.openhab.core.auth.client.oauth2.OAuthException if the authorization URL cannot be determined. In this
     *             case the ongoing authorization is
     *             cancelled.
     */
    String getAuthorizationUrl(String redirectUri);

    /**
     * Gets the UID of the bridge that is currently being authorized.
     */
    ThingUID getBridgeUid();

    /**
     * Gets the e-mail address associated with the account that is currently being authorized.
     */
    String getEmail();

    /**
     * Completes the authorization by extracting the authorization code from the given redirection URL, fetching the
     * access token response and persisting it. After this method succeeded the access token can be read from the
     * persistent storage.
     *
     * @param redirectUrlWithParameters The URL the remote service redirected the user to. This is the URL our servlet
     *            was called with.
     * @throws NoOngoingAuthorizationException if there is no ongoing authorization.
     * @throws org.openhab.core.auth.client.oauth2.OAuthException if the authorization failed. In this case the ongoing
     *             authorization is cancelled.
     */
    void completeAuthorization(String redirectUrlWithParameters);

    /**
     * Gets the access token from persistent storage.
     *
     * @param email E-mail address for which the access token is requested.
     * @return The access token.
     * @throws org.openhab.core.auth.client.oauth2.OAuthException if the access token cannot be obtained.
     */
    String getAccessToken(String email);
}
