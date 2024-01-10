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
package org.openhab.binding.volvooncall.internal.wrapper;

import java.time.ZoneId;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.dto.Position;
import org.openhab.binding.volvooncall.internal.dto.PositionData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link VehiclePositionWrapper} stores provides utility functions
 * over a {@link Position} provided by the rest API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehiclePositionWrapper {
    private final Optional<PositionData> position;
    private boolean isCalculated;

    public VehiclePositionWrapper(Position vehicle) {
        if (vehicle.calculatedPosition != null && vehicle.position.latitude != null) {
            position = Optional.of(vehicle.position);
            isCalculated = false;
        } else if (vehicle.calculatedPosition != null && vehicle.calculatedPosition.latitude != null) {
            position = Optional.of(vehicle.calculatedPosition);
            isCalculated = true;
        } else {
            position = Optional.empty();
        }
    }

    private State getPositionAsState(PositionData details) {
        if (details.latitude != null && details.longitude != null) {
            return new PointType(details.latitude + "," + details.longitude);
        }
        return UnDefType.NULL;
    }

    public State getPosition() {
        return position.map(pos -> getPositionAsState(pos)).orElse(UnDefType.UNDEF);
    }

    public @Nullable String getPositionAsJSon() {
        if (getPosition() != UnDefType.UNDEF) {
            StringBuilder json = new StringBuilder("{\"clientLatitude\":");
            json.append(position.get().latitude);
            json.append(",\"clientLongitude\":");
            json.append(position.get().longitude);
            json.append(",\"clientAccuracy\":0}");

            return json.toString();
        }
        return null;
    }

    public State isCalculated() {
        return position.map(pos -> (State) OnOffType.from(isCalculated)).orElse(UnDefType.UNDEF);
    }

    public State isHeading() {
        return position.map(pos -> (State) OnOffType.from(pos.isHeading())).orElse(UnDefType.UNDEF);
    }

    public State getTimestamp() {
        return position.flatMap(pos -> pos.getTimestamp())
                .map(dt -> (State) new DateTimeType(dt.withZoneSameInstant(ZoneId.systemDefault())))
                .orElse(UnDefType.NULL);
    }
}
