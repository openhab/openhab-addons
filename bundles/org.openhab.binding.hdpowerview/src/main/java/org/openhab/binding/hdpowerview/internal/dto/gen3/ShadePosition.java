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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * DTO for the position of a shade as returned by an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadePosition {
    private static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);

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
        return value != null ? new PercentType(new BigDecimal(value * 100f, MATH_CONTEXT)) : UnDefType.UNDEF;
    }

    /**
     * Set a new position value for this object based on the given coordinates, and the given new value.
     *
     * @param coordinates which of the position fields shall be set.
     * @param percent the new value in percent.
     * @return this object.
     */
    public ShadePosition setPosition(CoordinateSystem coordinates, PercentType percent) {
        Double value = percent.doubleValue() / 100f;
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
}
