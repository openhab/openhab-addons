/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshStrategy} is the class used to embed the refreshing
 * needs calculation for devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshStrategy {
    private static final Duration DEFAULT_DELAY = Duration.of(20, SECONDS);
    private static final Duration PROBING_INTERVAL = Duration.of(120, SECONDS);

    private final Logger logger = LoggerFactory.getLogger(RefreshStrategy.class);

    private Duration dataValidity = Duration.ZERO;
    private boolean searchRefreshInterval;
    private ZonedDateTime dataTimeStamp = ZonedDateTime.now();
    private @Nullable ZonedDateTime dataTimeStamp0;

    public RefreshStrategy(int validityPeriod) {
        setDataValidityPeriod(Duration.ofMillis(Math.max(0, validityPeriod)));
        searchRefreshInterval = (validityPeriod <= 0);
        expireData();
    }

    private void setDataValidityPeriod(Duration duration) {
        dataValidity = duration;
        logger.debug("Data validity period set to {}", duration);
    }

    public void expireData() {
        dataTimeStamp = ZonedDateTime.now().minus(dataValidity);
    }

    public void setDataTimeStamp(ZonedDateTime timeStamp) {
        if (searchRefreshInterval) {
            ZonedDateTime firstTimeStamp = dataTimeStamp0;
            if (firstTimeStamp == null) {
                dataTimeStamp0 = timeStamp;
                logger.debug("First data timestamp is {}", dataTimeStamp0);
            } else if (timeStamp.isAfter(firstTimeStamp)) {
                Duration a = Duration.between(firstTimeStamp, timeStamp);
                setDataValidityPeriod(a);
                searchRefreshInterval = false;
            } else {
                logger.debug("Data validity period not yet found - data timestamp unchanged");
            }
        }
        dataTimeStamp = timeStamp;
    }

    private Duration dataAge() {
        return Duration.between(dataTimeStamp, ZonedDateTime.now());
    }

    public boolean isDataOutdated() {
        return dataAge().compareTo(dataValidity) > 0;
    }

    public Duration nextRunDelay() {
        Duration result = searchRefreshInterval ? PROBING_INTERVAL : dataValidity.minus(dataAge()).plus(DEFAULT_DELAY);
        return result.isNegative() || result.isZero() ? PROBING_INTERVAL : result;
    }

    public boolean isSearchingRefreshInterval() {
        return searchRefreshInterval;
    }
}
