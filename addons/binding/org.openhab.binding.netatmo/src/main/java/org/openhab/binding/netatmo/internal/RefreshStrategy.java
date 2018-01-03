/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link RefreshStrategy} is the class used to embed the refreshing
 * needs calculation for devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshStrategy {
    private static int DEFAULT_DELAY = 30;
    private int dataValidityPeriod;
    private long dataTimeStamp;

    // By default we create dataTimeStamp to be outdated
    public RefreshStrategy(int dataValidityPeriod) {
        this.dataValidityPeriod = dataValidityPeriod;
        ZonedDateTime now = ZonedDateTime.now().minus(this.dataValidityPeriod, ChronoUnit.MILLIS);
        dataTimeStamp = now.toInstant().toEpochMilli();
    }

    public void setDataTimeStamp(Integer dataTimestamp) {
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

}
