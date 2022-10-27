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
package org.openhab.binding.hdpowerview.internal.gen3.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The position of a shade as returned by an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadePosition3 {
    private @NonNullByDefault({}) Double primary;
    private @NonNullByDefault({}) Double secondary;
    private @NonNullByDefault({}) Double tilt;

    public State getState(CoordinateSystem posKindCoords) {
        Double value;
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                value = primary;
                break;
            case SECONDARY_POSITION:
                value = secondary;
                break;
            case VANE_TILT_POSITION:
                value = tilt;
                break;
            default:
                value = null;
        }
        return value != null ? new PercentType((int) (value.doubleValue() * 100)) : UnDefType.UNDEF;
    }

    /**
     * Set a new position value for this object based on the given coordinates, and the given new value.
     *
     * @param coordinates which of the position fields shall be set.
     * @param percent the new value in percent.
     * @return this object.
     */
    public ShadePosition3 setPosition(CoordinateSystem coordinates, int percent) {
        Double value = Double.valueOf(percent) / 100.0;
        switch (coordinates) {
            case PRIMARY_POSITION:
                primary = value;
                break;
            case SECONDARY_POSITION:
                secondary = value;
                break;
            case VANE_TILT_POSITION:
                tilt = value;
                break;
            default:
        }
        return this;
    }

    public boolean supportsPrimary() {
        return primary != null;
    }

    public boolean supportsSecondary() {
        return secondary != null;
    }

    public boolean supportsTilt() {
        return tilt != null;
    }
}
