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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioApiResult} helps to supervise the API rate limits. Rachio enforces a daily public API request limit.
 * See the official Rachio rate-limit documentation for the current quota and reset behavior.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioApiResult {
    private final Logger logger = LoggerFactory.getLogger(RachioApiResult.class);

    public String requestMethod = "";
    public String url = "";
    public Integer responseCode = 0;
    public String resultString = "";

    public Integer apiCalls = 0;
    public Integer rateLimit = 0;
    public Integer rateRemaining = 0;
    public String rateReset = "";
    private transient boolean rateRemainingKnown = false;

    public void setRateLimit(int rateLimit, int rateRemaining, String rateReset) {
        this.rateLimit = rateLimit;
        this.rateRemaining = rateRemaining;
        this.rateReset = rateReset;
        this.rateRemainingKnown = true;
    }

    public void setRateLimit(@Nullable String rateLimit, @Nullable String rateRemaining, @Nullable String rateReset) {
        if (rateLimit != null) {
            this.rateLimit = Integer.parseInt(rateLimit);
        }
        if (rateRemaining != null) {
            this.rateRemaining = Integer.parseInt(rateRemaining);
            this.rateRemainingKnown = true;
        } else {
            this.rateRemainingKnown = false;
        }
        if (rateReset != null) {
            this.rateReset = rateReset;
        }

        if (this.rateLimit == 0 || !rateRemainingKnown || this.rateRemaining == 0) {
            return;
        }

        if (isRateLimitCritical()) {
            logger.warn("Remaining number of API calls is getting critical: limit={}, remaining={}, reset at {}",
                    rateLimit, rateRemaining, rateReset);
            return;
        }
        if (isRateLimitWarning()) {
            logger.warn("Remaining number of API calls is low: limit={}, remaining={}, reset at {}", rateLimit,
                    rateRemaining, rateReset);
            return;
        }

        logger.trace("API rate limit: remaining={}, limit={}, reset at {}", this.rateRemaining, rateLimit,
                this.rateReset);
    }

    boolean hasKnownRateRemaining() {
        return rateRemainingKnown;
    }

    public boolean isResponseRateLimit() {
        return responseCode == HttpStatus.TOO_MANY_REQUESTS_429;
    }

    public boolean isRateLimitWarning() {
        return rateRemaining > 0 && rateRemaining < RACHIO_RATE_LIMIT_WARNING;
    }

    public boolean isRateLimitCritical() {
        return rateRemaining > 0 && rateRemaining <= RACHIO_RATE_LIMIT_CRITICAL;
    }

    public boolean isRateLimitBlocked() {
        return rateRemainingKnown && rateRemaining >= 0 && rateRemaining <= RACHIO_RATE_LIMIT_BLOCK;
    }
}
