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
package org.openhab.binding.vwweconnect.internal.wrapper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vwweconnect.internal.dto.LocationDTO.PositionDTO;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;

/**
 * The {@link VehiclePositionWrapper} stores provides utility functions
 * over a {@link PositionDTO} provided by the rest API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VehiclePositionWrapper {
    private final PositionDTO position;

    public VehiclePositionWrapper(PositionDTO position) {
        this.position = position;
    }

    private State getPositionAsState(PositionDTO details) {
        return new PointType(details.getLat() + "," + details.getLng());
    }

    public State getPosition() {
        return getPositionAsState(position);
    }

    public @Nullable String getPositionAsJSon() {
        StringBuilder json = new StringBuilder();

        json.append("{\"clientLatitude\":");
        json.append(position.getLat());
        json.append(",\"clientLongitude\":");
        json.append(position.getLng());
        json.append(",\"clientAccuracy\":0}");

        return json.toString();
    }
}
