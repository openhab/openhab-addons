/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.retry;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthorizationFailedRetryStrategy} retries an operation after refreshing the access token in case of an
 * authorization failure.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class AuthorizationFailedRetryStrategy implements RetryStrategy {
    /**
     * Message of exception thrown by the Jetty client in case of unmatching header fields and body content. E.g.
     * application/json header with HTML body content. Mostly thrown when an invalid 401 response is received.
     */
    public static final String JETTY_401_HEADER_BODY_MISMATCH_EXCEPTION_MESSAGE = "org.eclipse.jetty.client.HttpResponseException: HTTP protocol violation: Authentication challenge without WWW-Authenticate header";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OAuthTokenRefresher tokenRefresher;
    private final String serviceHandle;

    public AuthorizationFailedRetryStrategy(OAuthTokenRefresher tokenRefresher, String serviceHandle) {
        this.tokenRefresher = tokenRefresher;
        this.serviceHandle = serviceHandle;
    }

    private void refreshToken() {
        try {
            logger.debug("Refreshing Miele OAuth access token.");
            tokenRefresher.refreshToken(serviceHandle);
            logger.debug("Miele OAuth access token has successfully been refreshed.");
        } catch (OAuthException e) {
            throw new MieleWebserviceException("Failed to refresh access token.", e,
                    ConnectionError.AUTHORIZATION_FAILED);
        }
    }

    @Override
    public <@Nullable T> T performRetryableOperation(Supplier<T> operation, Consumer<Exception> onException) {
        try {
            return operation.get();
        } catch (AuthorizationFailedException e) {
            onException.accept(e);
            refreshToken();
        } catch (MieleWebserviceException e) {
            // Workaround for HTML response from cloud in case of a 401 HTTP error.
            var cause = e.getCause();
            if (!(cause instanceof ExecutionException)) {
                throw e;
            }

            if (!JETTY_401_HEADER_BODY_MISMATCH_EXCEPTION_MESSAGE.equals(cause.getMessage())) {
                throw e;
            }

            onException.accept(e);
            refreshToken();
        }

        try {
            return operation.get();
        } catch (AuthorizationFailedException e) {
            throw new MieleWebserviceException("Request failed after access token renewal.", e,
                    ConnectionError.AUTHORIZATION_FAILED);
        }
    }
}
