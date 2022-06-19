/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Location} is the common interface for dto holding a location
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface Location {
    double[] getCoordinates();

    double getAltitude();

    default State getLocation() {
        double[] coordinates = getCoordinates();
        return coordinates.length != 2 ? UnDefType.UNDEF
                : new PointType(new DecimalType(coordinates[1]), new DecimalType(coordinates[0]),
                        new DecimalType(getAltitude()));
    }
}
