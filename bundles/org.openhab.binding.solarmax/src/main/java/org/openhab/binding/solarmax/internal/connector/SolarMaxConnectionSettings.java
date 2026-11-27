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
package org.openhab.binding.solarmax.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarMaxConnectionSettings} control how the {@link SolarMaxConnector} talks to the device.
 *
 * @param timeout timeout in milliseconds for connecting and for waiting for a response (MaxComm protocol
 *            description: 3000 ms)
 * @param retries how often a failed request (connection error, I/O error, invalid response) is repeated
 * @param retryDelay pause in milliseconds before a request is repeated
 * @param maxDuration time budget in milliseconds for reading all values once (normally the refresh interval). A
 *            request is only repeated if the repetition still fits into this budget, so a retry never delays the next
 *            poll.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public record SolarMaxConnectionSettings(int timeout, int retries, int retryDelay, long maxDuration) {

    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_RETRIES = 1;
    public static final int DEFAULT_RETRY_DELAY = 500;

    public SolarMaxConnectionSettings {
        timeout = Math.max(1, timeout);
        retries = Math.max(0, retries);
        retryDelay = Math.max(0, retryDelay);
    }

    /**
     * @return the longest time a single attempt can take (connecting plus waiting for the response)
     */
    long maxAttemptDuration() {
        return 2L * timeout;
    }
}
