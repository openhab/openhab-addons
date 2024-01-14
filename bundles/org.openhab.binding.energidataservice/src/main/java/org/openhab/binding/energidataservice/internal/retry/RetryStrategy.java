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
package org.openhab.binding.energidataservice.internal.retry;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface defines a retry strategy for failed network
 * requests.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface RetryStrategy {
    /**
     * Get {@link Duration} until next attempt. This will auto-increment number of
     * attempts, so should only be called once after each failed request.
     *
     * @return duration until next attempt according to strategy
     */
    Duration getDuration();
}
