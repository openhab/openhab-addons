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
package org.openhab.binding.vwweconnect.internal.wrapper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vwweconnect.internal.model.Location.Position;

/**
 * The {@link VehiclePositionWrapper} stores provides utility functions
 * over a {@link Position} provided by the rest API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VehiclePositionWrapper {
    private final Position position;

    public VehiclePositionWrapper(Position position) {
        this.position = position;
    }

    private State getPositionAsState(Position details) {
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
