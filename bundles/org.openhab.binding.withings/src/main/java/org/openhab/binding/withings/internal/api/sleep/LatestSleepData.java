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
package org.openhab.binding.withings.internal.api.sleep;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LatestSleepData {

    private final long sleepStartTimestamp;
    private final long sleepEndTimestamp;
    private final SleepResponseDTO.SleepData sleepData;

    public LatestSleepData(long sleepStartTimestamp, long sleepEndTimestamp, SleepResponseDTO.SleepData sleepData) {
        this.sleepStartTimestamp = sleepStartTimestamp;
        this.sleepEndTimestamp = sleepEndTimestamp;
        this.sleepData = sleepData;
    }

    public Date getSleepStart() {
        return new Date(sleepStartTimestamp * 1000L);
    }

    public Date getSleepEnd() {
        return new Date(sleepEndTimestamp * 1000L);
    }

    public Integer getSleepScore() {
        return sleepData.getSleepScore();
    }
}
