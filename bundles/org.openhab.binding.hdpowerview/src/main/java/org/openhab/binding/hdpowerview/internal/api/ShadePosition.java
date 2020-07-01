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
package org.openhab.binding.hdpowerview.internal.api;

import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.*;

/**
 * The position of a shade, as returned by the HD Power View HUB
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class ShadePosition {
    /*
     * Primary actuator position
     */
    private int posKind1;
    private int position1;
    /*
     * Secondary actuator position
     * 
     * here we have to use Integer objects rather than just int primitives because
     * these are secondary optional position elements in the JSON payload, so the
     * GSON de-serializer might leave them as null
     */
    private @Nullable Integer posKind2 = null;
    private @Nullable Integer position2 = null;

    public static ShadePosition create(CoordinateSystem coordSys, int percent) {
        return new ShadePosition(coordSys, percent, null, null);
    }

    public static ShadePosition create(CoordinateSystem primaryCoordSys, int primaryPercent,
            @Nullable CoordinateSystem secondaryCoordSys, @Nullable Integer secondaryPercent) {
        return new ShadePosition(primaryCoordSys, primaryPercent, secondaryCoordSys, secondaryPercent);
    }

    ShadePosition(CoordinateSystem primaryCoordSys, int primaryPercent, @Nullable CoordinateSystem secondaryCoordSys,
            @Nullable Integer secondaryPercent) {
        setPosition1(primaryCoordSys, primaryPercent);
        setPosition2(secondaryCoordSys, secondaryPercent);
    }

    public State getState(ActuatorClass actuatorClass, CoordinateSystem coordSys) {
        switch (actuatorClass) {
            case PRIMARY_ACTUATOR:
                return getPosition1(coordSys);
            case SECONDARY_ACTUATOR:
                return getPosition2(coordSys);
            default:
                return UnDefType.UNDEF;
        }
    }

    public CoordinateSystem getCoordinateSystem(ActuatorClass actuatorClass) {
        switch (actuatorClass) {
            case PRIMARY_ACTUATOR:
                return fromPosKind(posKind1);
            case SECONDARY_ACTUATOR:
                Integer posKind2 = this.posKind2;
                if (posKind2 != null) {
                    return fromPosKind(posKind2.intValue());
                }
            default:
                return ERROR_UNKNOWN;
        }
    }

    private void setPosition1(CoordinateSystem coordSys, int percent) {
        posKind1 = coordSys.toPosKind();
        switch (coordSys) {
            case ZERO_IS_CLOSED:
                /*-
                 * Primary rail of a single action bottom-up shade, or
                 * Primary, lower, bottom-up, rail of a dual action shade
                 */
            case ZERO_IS_OPEN:
                /*-
                 * Primary rail of a single action top-down shade
                 *
                 * All these types use the same coordinate system; which is inverted in relation
                 * to that of OpenHAB
                 */
                position1 = MAX_SHADE - (int) Math.round(percent / 100d * MAX_SHADE);
                break;
            case VANE_COORDS:
                /*
                 * Vane angle of the primary rail of a bottom-up single action shade
                 */
                position1 = (int) Math.round(percent / 100d * MAX_VANE);
                break;
            default:
                position1 = 0;
        }
    }

    private State getPosition1(CoordinateSystem coordSys) {
        switch (coordSys) {
            case ZERO_IS_CLOSED:
                /*-
                 * Primary rail of a single action bottom-up shade, or
                 * Primary, lower, bottom-up, rail of a dual action shade
                 */
            case ZERO_IS_OPEN:
                /*
                 * Primary rail of a single action top-down shade
                 *
                 * All these types use the same coordinate system; which is inverted in relation
                 * to that of OpenHAB
                 * 
                 * If the slats have a defined position then the shade position must by
                 * definition be 100%
                 */
                return posKind1 == 3 ? PercentType.HUNDRED
                        : new PercentType(100 - (int) Math.round((double) position1 / MAX_SHADE * 100));

            case VANE_COORDS:
                /*
                 * Vane angle of the primary rail of a bottom-up single action shade
                 * 
                 * If the shades are not open, the vane position is undefined; if the the shades
                 * are exactly open then the vanes are at zero; otherwise return the actual vane
                 * position itself
                 * 
                 * note: sometimes the hub may return a value of position1 > MAX_VANE (seems to
                 * be a bug in the hub) so we avoid an out of range exception via the Math.min()
                 * function below..
                 */
                return posKind1 != 3 ? (position1 != 0 ? UnDefType.UNDEF : PercentType.ZERO)
                        : new PercentType((int) Math.round((double) Math.min(position1, MAX_VANE) / MAX_VANE * 100));

            default:
                return UnDefType.UNDEF;
        }
    }

    private void setPosition2(@Nullable CoordinateSystem coordSys, @Nullable Integer percent) {
        if (coordSys == null || percent == null) {
            return;
        }
        posKind2 = Integer.valueOf(coordSys.toPosKind());
        switch (coordSys) {
            case ZERO_IS_CLOSED:
            case ZERO_IS_OPEN:
                /*
                 * Secondary, upper, top-down rail of a dual action shade
                 * 
                 * Uses a coordinate system that is NOT inverted in relation to OpenHAB
                 */
                position2 = Integer.valueOf((int) Math.round(percent.doubleValue() / 100 * MAX_SHADE));
                break;
            default:
                position2 = Integer.valueOf(0);
        }
    }

    private State getPosition2(CoordinateSystem coordSys) {
        Integer posKind2 = this.posKind2;
        Integer position2 = this.position2;
        if (position2 == null || posKind2 == null) {
            return UnDefType.UNDEF;
        }
        switch (coordSys) {
            case ZERO_IS_CLOSED:
                /*
                 * This case should never occur; but return a value anyway just in case
                 */
            case ZERO_IS_OPEN:
                /*
                 * Secondary, upper, top-down rail of a dual action shade
                 * 
                 * Uses a coordinate system that is NOT inverted in relation to OpenHAB
                 */
                if (posKind2.intValue() != 3) {
                    return new PercentType(100 - (int) Math.round(position2.doubleValue() / MAX_SHADE * 100));
                }
            default:
                return UnDefType.UNDEF;
        }
    }
}
