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

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.retry.strategy.ExponentialBackoff;
import org.openhab.binding.energidataservice.internal.retry.strategy.FixedTime;
import org.openhab.binding.energidataservice.internal.retry.strategy.Linear;

/**
 * This factory defines policies for determining appropriate {@link RetryStrategy} based
 * on scenario.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class RetryPolicyFactory {

    /**
     * Determine {@link RetryStrategy} from {@link Throwable}.
     *
     * @param e thrown exception
     * @return retry strategy
     */
    public static RetryStrategy fromThrowable(Throwable e) {
        if (e instanceof DataServiceException dse) {
            switch (dse.getHttpStatus()) {
                case HttpStatus.TOO_MANY_REQUESTS_429:
                    return new ExponentialBackoff().withMinimum(Duration.ofMinutes(30));
                default:
                    return new ExponentialBackoff().withMinimum(Duration.ofMinutes(1)).withJitter(0.2);
            }
        }

        return new ExponentialBackoff().withMinimum(Duration.ofMinutes(1)).withJitter(0.2);
    }

    /**
     * Default {@link RetryStrategy} with one retry per day.
     * This is intended as a dummy strategy until replaced by a concrete one.
     *
     * @return retry strategy
     */
    public static RetryStrategy initial() {
        return new Linear().withMinimum(Duration.ofDays(1));
    }

    /**
     * Determine {@link RetryStrategy} for next expected data publishing.
     *
     * @param localTime the time of daily data request in local time-zone
     * @param zoneId the local time-zone
     * @return retry strategy
     */
    public static RetryStrategy atFixedTime(LocalTime localTime, ZoneId zoneId) {
        return new FixedTime(localTime, Clock.system(zoneId)).withJitter(1);
    }

    /**
     * Determine {@link RetryStrategy} when expected spot price data is missing.
     *
     * @param localTime the time of daily data request
     * @param zoneId time-zone
     * @return retry strategy
     */
    public static RetryStrategy whenExpectedSpotPriceDataMissing(LocalTime localTime, ZoneId zoneId) {
        LocalTime now = LocalTime.now(zoneId);
        if (now.isAfter(localTime)) {
            return new ExponentialBackoff().withMinimum(Duration.ofMinutes(10)).withJitter(0.2);
        }
        return atFixedTime(localTime, zoneId);
    }
}
