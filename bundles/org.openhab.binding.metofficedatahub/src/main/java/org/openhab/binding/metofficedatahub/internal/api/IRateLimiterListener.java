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
package org.openhab.binding.metofficedatahub.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementations of this interface, allow the monitoring of when the rate limiter
 * has updated its operating parameters.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface IRateLimiterListener {

    /**
     * This is invoked to notify implementations of this interface, that the given rate limiter
     * has been updated, with new counts.
     *
     * @param requestLimiter is a reference to the rate limiter that has been updated.
     */
    void processRateLimiterUpdated(final RequestLimiter requestLimiter);
}
