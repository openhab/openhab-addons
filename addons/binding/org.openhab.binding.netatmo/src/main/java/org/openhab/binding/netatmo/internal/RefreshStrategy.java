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
package org.openhab.binding.netatmo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

/**
 * {@link RefreshStrategy} is the class used to embed the refreshing
 * needs calculation for devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshStrategy {
    private static final int DEFAULT_DELAY = 30;
    private int dataValidityPeriod;
    private long dataTimeStamp;

    // By default we create dataTimeStamp to be outdated
    public RefreshStrategy(int dataValidityPeriod) {
        this.dataValidityPeriod = dataValidityPeriod;
        expireData();
    }

    public void setDataTimeStamp(Long dataTimestamp) {
        this.dataTimeStamp = ChannelTypeUtils.toZonedDateTime(dataTimestamp).toInstant().toEpochMilli();
    }

    public long dataAge() {
        long now = Calendar.getInstance().getTimeInMillis();
        return now - dataTimeStamp;
    }

    public boolean isDataOutdated() {
        return dataAge() >= dataValidityPeriod;
    }

    public long nextRunDelayInS() {
        return Math.max(0, (dataValidityPeriod - dataAge())) / 1000 + DEFAULT_DELAY;
    }

    public void expireData() {
        ZonedDateTime now = ZonedDateTime.now().minus(this.dataValidityPeriod, ChronoUnit.MILLIS);
        dataTimeStamp = now.toInstant().toEpochMilli();
    }

}
