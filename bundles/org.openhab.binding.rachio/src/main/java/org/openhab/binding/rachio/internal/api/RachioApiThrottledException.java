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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;

/**
 * Signals that the binding intentionally deferred a Rachio API call due to local client-side rate protection.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioApiThrottledException extends RachioApiException {
    private static final long serialVersionUID = 1L;

    private final Priority priority;
    private final RequestPurpose requestPurpose;
    private final double budgetRate;
    private final double currentRate;
    private final Duration suggestedRetryDelay;

    public RachioApiThrottledException(RateLimitThrottleException throttle, RachioApiResult result) {
        super("RachioApi: " + throttle.toString(), result);
        this.priority = throttle.priority;
        this.requestPurpose = throttle.requestPurpose;
        this.budgetRate = throttle.budgetRate;
        this.currentRate = throttle.currentRate;
        this.suggestedRetryDelay = throttle.suggestedRetryDelay;
    }

    public Priority getPriority() {
        return priority;
    }

    public RequestPurpose getRequestPurpose() {
        return requestPurpose;
    }

    public double getBudgetRate() {
        return budgetRate;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public Duration getSuggestedRetryDelay() {
        return suggestedRetryDelay;
    }

    @Override
    public String toString() {
        String message = getMessage();
        return message != null ? message : super.toString();
    }
}
