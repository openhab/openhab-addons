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
package org.openhab.binding.mielecloud.internal.webservice.exception;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RuntimeException} indicating that too many requests have been made against the cloud service.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = 3393292912418862566L;

    @Nullable
    private final String retryAfter;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TooManyRequestsException(String message, @Nullable String retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    /**
     * Gets whether a hint on when to retry the operation is available.
     *
     * @return Whether a hint on when to retry the operation is available.
     */
    public boolean hasRetryAfterHint() {
        return retryAfter != null;
    }

    /**
     * Gets the number of seconds until the operation may be retried.
     *
     * @return The number of seconds until the operation may be retried. This will return -1 if no Retry-After header
     *         was present or parsing the data from the header fails.
     */
    public long getSecondsUntilRetry() {
        String retryAfter = this.retryAfter;
        if (retryAfter == null) {
            logger.debug("Received no Retry-After header.");
            return -1;
        }

        logger.debug("Received Retry-After header: {}", retryAfter);
        try {
            long seconds = Long.parseLong(retryAfter);
            logger.debug("Interpreted Retry-After header value: {} seconds", seconds);
            return seconds;
        } catch (NumberFormatException e) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ccc, d MMM yyyy HH:mm:ss z", Locale.US);

            try {
                LocalDateTime dateTime = LocalDateTime.parse(retryAfter, formatter);
                logger.debug("Interpreted Retry-After header value: {}", dateTime);

                Duration duration = Duration.between(LocalDateTime.now(), dateTime);

                long seconds = Math.max(0, duration.toMillis() / 1000);
                logger.debug("Interpreted Retry-After header value: {} seconds.", seconds);
                return seconds;
            } catch (DateTimeParseException dateTimeParseException) {
                logger.warn("Unable to parse Retry-After header: {}", retryAfter);
                return -1;
            }
        }
    }
}
