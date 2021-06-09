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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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
    private static final int DEFAULT_DELAY_SEC = 30;
    private static final int SEARCH_REFRESH_INTERVAL_SEC = 120;

    private final Logger logger = LoggerFactory.getLogger(RefreshStrategy.class);

    private long dataValidityPeriod;
    private @Nullable ZonedDateTime dataTimeStamp;
    private boolean searchRefreshInterval;
    private @Nullable ZonedDateTime dataTimestamp0;

    // By default we create dataTimeStamp to be outdated
    // A null or negative value for dataValidityPeriod will trigger an automatic search of the validity period
    public RefreshStrategy(int dataValidityPeriod) {
        if (dataValidityPeriod <= 0) {
            this.dataValidityPeriod = 0;
            this.searchRefreshInterval = true;
            logger.debug("Data validity period search...");
        } else {
            this.dataValidityPeriod = dataValidityPeriod;
            this.searchRefreshInterval = false;
            logger.debug("Data validity period set to {} ms", this.dataValidityPeriod);
        }
        expireData();
    }

    public void setDataTimeStamp(ZonedDateTime dataTimestamp) {
        if (searchRefreshInterval) {
            if (dataTimestamp0 == null) {
                dataTimestamp0 = dataTimestamp;
                logger.debug("First data timestamp is {}", dataTimestamp0);
            } else if (dataTimestamp.isAfter(dataTimestamp0)) {
                dataValidityPeriod = ChronoUnit.MILLIS.between(dataTimestamp0, dataTimestamp);
                searchRefreshInterval = false;
                logger.debug("Data validity period found : {} ms", this.dataValidityPeriod);
            } else {
                logger.debug("Data validity period not yet found - data timestamp unchanged");
            }
        }
        this.dataTimeStamp = dataTimestamp;
    }

    public long dataAge() {
        return ChronoUnit.MILLIS.between(dataTimeStamp, ZonedDateTime.now());
    }

    public boolean isDataOutdated() {
        return dataAge() >= dataValidityPeriod;
    }

    public long nextRunDelayInS() {
        return searchRefreshInterval ? SEARCH_REFRESH_INTERVAL_SEC
                : Math.max(0, (dataValidityPeriod - dataAge())) / 1000 + DEFAULT_DELAY_SEC;
    }

    public void expireData() {
        ZonedDateTime now = ZonedDateTime.now().minus(this.dataValidityPeriod, ChronoUnit.MILLIS);
        dataTimeStamp = now;
    }

    public boolean isSearchingRefreshInterval() {
        return searchRefreshInterval && dataTimestamp0 != null;
    }
}
