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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHome extends NADevice {
    private double[] location = {};
    private double altitude;
    private List<NARoom> rooms = List.of();

    public @Nullable PointType getLocation() {
        return location.length != 2 ? null
                : new PointType(new DecimalType(location[1]), new DecimalType(location[0]), new DecimalType(altitude));
    }

    public List<NARoom> getRooms() {
        return rooms;
    }

    // TODO : dégager setters ?
    public void setRooms(List<NARoom> rooms) {
        this.rooms = rooms;
    }

    public @Nullable NARoom getRoom(String id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }
}
