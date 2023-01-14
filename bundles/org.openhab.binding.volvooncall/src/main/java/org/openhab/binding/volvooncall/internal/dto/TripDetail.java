/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.UNDEFINED;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TripDetail} is responsible for storing
 * trip details returned by trip rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TripDetail {
    private @Nullable Integer fuelConsumption;
    private @Nullable Integer electricalConsumption;
    private @Nullable Integer electricalRegeneration;
    public int distance = UNDEFINED;
    public int startOdometer = UNDEFINED;
    public int endOdometer = UNDEFINED;
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
        }
        return UnDefType.NULL;
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

    public Optional<Long> getDurationInMinutes() {
        Temporal start = startTime;
        Temporal end = endTime;
        if (start == null || end == null) {
            return Optional.empty();
        } else {
            return Optional.of(Duration.between(start, end).toMinutes());
        }
    }

    public Optional<Integer> getFuelConsumption() {
        Integer fuelConsumption = this.fuelConsumption;
        if (fuelConsumption != null) {
            return Optional.of(fuelConsumption);
        }
        return Optional.empty();
    }

    public Optional<Integer> getElectricalConsumption() {
        Integer electricalConsumption = this.electricalConsumption;
        if (electricalConsumption != null) {
            return Optional.of(electricalConsumption);
        }
        return Optional.empty();
    }

    public Optional<Integer> getElectricalRegeneration() {
        Integer electricalRegeneration = this.electricalRegeneration;
        if (electricalRegeneration != null) {
            return Optional.of(electricalRegeneration);
        }
        return Optional.empty();
    }
}
