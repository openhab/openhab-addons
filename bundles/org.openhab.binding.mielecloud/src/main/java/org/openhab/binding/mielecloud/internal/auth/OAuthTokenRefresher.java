/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.auth;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link OAuthTokenRefresher} offers convenient access to OAuth 2 authentication related functionality,
 * especially refreshing the access token.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Allow removing tokens from the storage
 */
@NonNullByDefault
public interface OAuthTokenRefresher {
    /**
     * Sets the listener that is called when the access token was refreshed.
     *
     * @param listener The listener to register.
     * @param serviceHandle The service handle identifying the internal OAuth configuration.
     * @throws OAuthException if the listener needs to be registered at an underlying service which is not available
     *             because the account has not yet been authorized
     */
    public void setRefreshListener(OAuthTokenRefreshListener listener, String serviceHandle);

    /**
     * Unsets a listener.
     *
     * @param serviceHandle The service handle identifying the internal OAuth configuration.
     */
    public void unsetRefreshListener(String serviceHandle);

    /**
     * Refreshes the access and refresh tokens for the given service handle. If an {@link OAuthTokenRefreshListener} is
     * registered for the service handle then it is notified after the refresh has completed.
     *
     * This call will succeed if the access token is still valid or a valid refresh token exists, which can be used to
     * refresh the expired access token. If refreshing fails, an {@link OAuthException} is thrown.
     *
     * @param serviceHandle The service handle identifying the internal OAuth configuration.
     * @throws OAuthException if the token cannot be obtained or refreshed
     */
    public void refreshToken(String serviceHandle);

    /**
     * Gets the currently stored access token from persistent storage.
     *
     * @param serviceHandle The service handle identifying the internal OAuth configuration.
     * @return The currently stored access token or an empty {@link Optional} if there is no stored token.
     */
    public Optional<String> getAccessTokenFromStorage(String serviceHandle);

    /**
     * Removes the tokens from persistent storage.
     *
     * Note: Calling this method will force the user to run through the pairing process again in order to obtain a
     * working bridge.
     *
     * @param serviceHandle The service handle identifying the internal OAuth configuration.
     */
    public void removeTokensFromStorage(String serviceHandle);
}
