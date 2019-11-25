/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioApiRateLimit} help to supervise the API rate limits. Rachio blocks API access for accounts exceeding
 * the limit of 1700 calls per day.
 *
 * @author Markus Michels - Initial contribution
 */
public class RachioApiRateLimit {
    private final Logger logger        = LoggerFactory.getLogger(RachioApiRateLimit.class);

    public String        requestMethod = "";
    public String        url           = "";
    @Nullable
    public String        apikey        = "";
    public Integer       responseCode  = 0;
    public String        resultString  = "";

    public Integer       apiCalls      = 0;
    public Integer       rateLimit     = 0;
    public Integer       rateRemaining = 0;
    public String        rateReset     = "";

    public void setRateLimit(int rateLimit, int rateRemaining, String rateReset) {
        this.rateLimit = rateLimit;
        this.rateRemaining = rateRemaining;
        this.rateReset = rateReset;
    }

    public void setRateLimit(@Nullable String rateLimit, @Nullable String rateRemaining, @Nullable String rateReset) {
        if (rateLimit != null) {
            this.rateLimit = Integer.parseInt(rateLimit);
        }
        if (rateRemaining != null) {
            this.rateRemaining = Integer.parseInt(rateRemaining);
        }
        if (rateReset != null) {
            this.rateReset = rateReset;
        }

        if ((this.rateLimit == 0) || (this.rateRemaining == 0)) {
            return;
        }

        if (isRateLimitCritical()) {
            logger.warn("Remaing number of API calls is getting critical: limit={}, remaining={}, reset at {}",
                    rateLimit, rateRemaining, rateReset);
            return;
        }
        if (isRateLimitWarning()) {
            logger.warn("Remaing number of  API calls is low: limit={}, remaining={}, reset at {}", rateLimit,
                    rateRemaining, rateReset);
            return;
        }

        logger.trace("API rate limit: remaining={}, limit={}, reset at {}", this.rateRemaining, rateLimit,
                this.rateReset);
    }

    public boolean isRateLimitWarning() {
        return (rateRemaining > 0) && (rateRemaining < RACHIO_RATE_LIMIT_WARNING);
    }

    public boolean isRateLimitCritical() {
        return (rateRemaining > 0) && (rateRemaining <= RACHIO_RATE_LIMIT_CRITICAL);
    }

    public boolean isRateLimitBlocked() {
        return (rateRemaining > 0) && (rateRemaining <= RACHIO_RATE_LIMIT_BLOCK);
    }
}
