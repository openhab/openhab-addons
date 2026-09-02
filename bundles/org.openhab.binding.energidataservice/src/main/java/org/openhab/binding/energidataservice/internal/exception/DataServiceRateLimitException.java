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
package org.openhab.binding.energidataservice.internal.exception;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;

/**
 * {@link DataServiceRateLimitException} is a specialized Energi Data Service exception
 * thrown in case of exceeding the API rate limit. It contains information about how long to
 * wait before retrying the request.
 *
 * @see <a href="https://www.energidataservice.dk/guides/api-guides">Energi Data Service API documentation on rate
 *      limiting</a>
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DataServiceRateLimitException extends DataServiceException {

    private static final Duration DEFAULT_RETRY_AFTER = Duration.ofMinutes(30);

    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    public DataServiceRateLimitException(String message) {
        this(message, DEFAULT_RETRY_AFTER);
    }

    public DataServiceRateLimitException(String message, Throwable cause) {
        super(message, HttpStatus.TOO_MANY_REQUESTS_429);
        initCause(cause);
        this.retryAfter = DEFAULT_RETRY_AFTER;
    }

    public DataServiceRateLimitException(String message, Duration retryAfter) {
        super(message, HttpStatus.TOO_MANY_REQUESTS_429);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
