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
package org.openhab.binding.volvooncall.internal.dto;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link TripDetail} is responsible for storing
 * trip details returned by trip rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TripDetail {
    public @Nullable Integer fuelConsumption;
    public @Nullable Integer electricalConsumption;
    public @Nullable Integer electricalRegeneration;
    public int distance;
    public int startOdometer;
    public int endOdometer;
    private @Nullable ZonedDateTime endTime;
    private @Nullable ZonedDateTime startTime;
    private @NonNullByDefault({}) PositionData startPosition;
    private @NonNullByDefault({}) PositionData endPosition;

    private State ZonedDateTimeToState(@Nullable ZonedDateTime datetime) {
        return datetime != null ? new DateTimeType(datetime.withZoneSameInstant(ZoneId.systemDefault()))
                : UnDefType.NULL;
    }

    private State getPositionAsState(PositionData details) {
        if (details.latitude != null && details.longitude != null) {
            return new PointType(details.latitude + "," + details.longitude);
        } else {
            return UnDefType.NULL;
        }
    }

    public State getStartTime() {
        return ZonedDateTimeToState(startTime);
    }

    public State getStartPosition() {
        return getPositionAsState(startPosition);
    }

    public State getEndTime() {
        return ZonedDateTimeToState(endTime);
    }

    public State getEndPosition() {
        return getPositionAsState(endPosition);
    }

    public long getDurationInMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

}
