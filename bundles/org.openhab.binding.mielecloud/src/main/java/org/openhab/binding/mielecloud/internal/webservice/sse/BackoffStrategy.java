/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A strategy computing the wait time between multiple connection attempts.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
interface BackoffStrategy {
    /**
     * Gets the number of seconds until a retryable operation is performed.
     *
     * @param failedConnectionAttempts The number of failed attempts.
     * @return The number of seconds to wait before making the next attempt.
     */
    long getSecondsUntilRetry(int failedAttempts);
}
