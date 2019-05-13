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
package org.openhab.binding.volvooncall.internal.wrapper;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.volvooncall.internal.dto.Position;
import org.openhab.binding.volvooncall.internal.dto.PositionData;

/**
 * The {@link VehiclePositionWrapper} stores provides utility functions
 * over a {@link Position} provided by the rest API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehiclePositionWrapper {
    private final Position vehicle;

    public VehiclePositionWrapper(Position vehicle) {
        this.vehicle = vehicle;
    }

    private State getPositionAsState(PositionData details) {
        if (details.latitude != null && details.longitude != null) {
            return new PointType(details.latitude + "," + details.longitude);
        } else {
            return UnDefType.NULL;
        }
    }

    public State getPosition() {
        if (vehicle.position.latitude != null) {
            return getPositionAsState(vehicle.position);
        } else if (vehicle.calculatedPosition.latitude != null) {
            return getPositionAsState(vehicle.calculatedPosition);
        } else {
            return UnDefType.NULL;
        }
    }

    public @Nullable String getPositionAsJSon() {
        PositionData details = vehicle.position;
        if (details != null && details.latitude != null && details.longitude != null) {
            StringBuilder json = new StringBuilder();

            json.append("{\"clientLatitude\":");
            json.append(details.latitude);
            json.append(",\"clientLongitude\":");
            json.append(details.longitude);
            json.append(",\"clientAccuracy\":0}");

            return json.toString();
        }
        return null;
    }

    public State isCalculated() {
        return vehicle.calculatedPosition.latitude != null ? OnOffType.ON : OnOffType.OFF;
    }

    public State isHeading() {
        return (vehicle.position.isHeading() || vehicle.calculatedPosition.isHeading()) ? OnOffType.ON : OnOffType.OFF;
    }

    public State getTimestamp() {
        return vehicle.position.timestamp != null
                ? new DateTimeType(vehicle.position.timestamp.withZoneSameInstant(ZoneId.systemDefault()))
                : vehicle.calculatedPosition.timestamp != null
                        ? new DateTimeType(
                                vehicle.calculatedPosition.timestamp.withZoneSameInstant(ZoneId.systemDefault()))
                        : UnDefType.NULL;
    }
}
