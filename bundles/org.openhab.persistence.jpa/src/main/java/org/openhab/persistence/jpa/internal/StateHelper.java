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
package org.openhab.persistence.jpa.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;

/**
 * Helper class for dealing with State
 *
 * @author Manfred Bergmann - Initial contribution
 *
 */
@NonNullByDefault
public class StateHelper {

    /**
     * Converts the given State to a string that can be persisted in the database.
     *
     * @param state the state of the item to be persisted
     * @return state converted as string
     */
    public static String toString(State state) {
        if (state instanceof DateTimeType type) {
            return String.valueOf(type.getZonedDateTime().toInstant().toEpochMilli());
        }
        if (state instanceof DecimalType type) {
            return String.valueOf(type.doubleValue());
        }
        if (state instanceof PointType pType) {
            return String.format(Locale.ENGLISH, "%f;%f;%f", pType.getLatitude().doubleValue(),
                    pType.getLongitude().doubleValue(), pType.getAltitude().doubleValue());
        }

        return state.toString();
    }
}
