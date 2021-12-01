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
package org.openhab.binding.hdpowerview.internal.api;

import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The position of a single shade, as returned by the HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class ShadePosition {
    /**
     * Primary actuator position.
     */
    private int posKind1;
    private int position1;

    /**
     * Secondary actuator position.
     *
     * Here we have to use Integer objects rather than just int primitives because these are secondary optional position
     * elements in the JSON payload, so the GSON de-serializer might leave them as null.
     */
    private @Nullable Integer posKind2 = null;
    private @Nullable Integer position2 = null;

    public ShadePosition() {
    }

    /**
     * Get the shade's State for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @return the current state.
     */
    public State getState(Capabilities capabilities, CoordinateSystem coordSys) {
        State result = getPosition1(capabilities, coordSys);
        if (UnDefType.UNDEF.equals(result)) {
            result = getPosition2(capabilities, coordSys);
        }
        return result;
    }

    /**
     * Set the shade's position1 value for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @param percent the new position value
     */
    private void setPosition1(Capabilities capabilities, CoordinateSystem coordSys, int percent) {
        switch (coordSys) {
            case PRIMARY_ZERO_IS_CLOSED:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                // on dual rail shades constrain percent to not move the lower rail above the upper
                if (capabilities.supportsPrimary() && capabilities.supportsSecondary()) {
                    State secondary = getState(capabilities, SECONDARY_ZERO_IS_OPEN);
                    if (secondary instanceof PercentType) {
                        percent = Math.max(percent, ((PercentType) secondary).intValue());
                    }
                }
                posKind1 = coordSys.ordinal();
                position1 = MAX_SHADE - (int) Math.round((double) percent / 100 * MAX_SHADE);
                break;

            case SECONDARY_ZERO_IS_OPEN:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                posKind1 = coordSys.ordinal();
                position1 = (int) Math.round((double) percent / 100 * MAX_SHADE);
                break;

            case VANE_COORDS:
                /*
                 * Vane angle of the primary rail of a bottom-up single action shade: => NOT INVERTED
                 */
                posKind1 = coordSys.ordinal();
                int max = capabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                position1 = (int) Math.round((double) percent / 100 * max);
                break;

            default:
                posKind1 = CoordinateSystem.NONE.ordinal();
                position1 = 0;
        }
    }

    /**
     * Get the shade's position1 State for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @return the State (or UNDEF if not available)
     */
    private State getPosition1(Capabilities capabilities, CoordinateSystem coordSys) {
        switch (coordSys) {
            case PRIMARY_ZERO_IS_CLOSED:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                if (coordSys.equals(posKind1)) {
                    return new PercentType(100 - (int) Math.round((double) position1 / MAX_SHADE * 100));
                }
                if (VANE_COORDS.equals(posKind1) && capabilities.supportsTiltOnClosed()) {
                    return PercentType.HUNDRED;
                }
                break;

            case SECONDARY_ZERO_IS_OPEN:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                if (coordSys.equals(posKind1)) {
                    return new PercentType((int) Math.round((double) position1 / MAX_SHADE * 100));
                }
                break;

            case VANE_COORDS:
                /*
                 * Vane angle of the primary rail of a bottom-up single action shade: => NOT INVERTED
                 *
                 * If the shades are not open, the vane position is undefined; if the the shades
                 * are exactly open then the vanes are at zero; otherwise return the actual vane
                 * position itself
                 *
                 * note: sometimes the hub may return a value of position1 > MAX_VANE (seems to
                 * be a bug in the hub) so we avoid an out of range exception via the Math.min()
                 * function below..
                 */
                if (coordSys.equals(posKind1)) {
                    int max = capabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                    return new PercentType((int) Math.round((double) Math.min(position1, max) / max * 100));
                }
                if (PRIMARY_ZERO_IS_CLOSED.equals(posKind1) && capabilities.supportsTiltOnClosed()) {
                    return position1 != 0 ? UnDefType.UNDEF : PercentType.ZERO;
                }

            default:
        }
        return UnDefType.UNDEF;
    }

    /**
     * Set the shade's position2 value for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @param percent the new position value
     */
    private void setPosition2(Capabilities capabilities, CoordinateSystem coordSys, int percent) {
        switch (coordSys) {
            case PRIMARY_ZERO_IS_CLOSED:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                posKind2 = coordSys.ordinal();
                position2 = Integer.valueOf(MAX_SHADE - (int) Math.round((double) percent / 100 * MAX_SHADE));
                break;

            case SECONDARY_ZERO_IS_OPEN:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                // on dual rail shades constrain percent to not move the upper rail below the lower
                if (capabilities.supportsPrimary() && capabilities.supportsSecondary()) {
                    State primary = getState(capabilities, PRIMARY_ZERO_IS_CLOSED);
                    if (primary instanceof PercentType) {
                        percent = Math.min(percent, ((PercentType) primary).intValue());
                    }
                }
                posKind2 = coordSys.ordinal();
                position2 = Integer.valueOf((int) Math.round((double) percent / 100 * MAX_SHADE));
                break;

            case VANE_COORDS:
                posKind2 = coordSys.ordinal();
                int max = capabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                position2 = Integer.valueOf((int) Math.round((double) percent / 100 * max));
                break;

            default:
                posKind2 = null;
                position2 = null;
        }
    }

    /**
     * Get the shade's position2 State for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @return the State (or UNDEF if not available)
     */
    private State getPosition2(Capabilities capabilities, CoordinateSystem coordSys) {
        Integer posKind2 = this.posKind2;
        Integer position2 = this.position2;

        if (position2 == null || posKind2 == null) {
            return UnDefType.UNDEF;
        }

        switch (coordSys) {
            case PRIMARY_ZERO_IS_CLOSED:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                if (coordSys.equals(posKind2)) {
                    return new PercentType(100 - (int) Math.round(position2.doubleValue() / MAX_SHADE * 100));
                }
                break;

            case SECONDARY_ZERO_IS_OPEN:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                if (coordSys.equals(posKind2)) {
                    return new PercentType((int) Math.round(position2.doubleValue() / MAX_SHADE * 100));
                }
                break;

            /*
             * Vane angle of the primary rail of a bottom-up single action shade: => NOT INVERTED
             *
             * note: sometimes the hub may return a value of position1 > MAX_VANE (seems to
             * be a bug in the hub) so we avoid an out of range exception via the Math.min()
             * function below..
             */
            case VANE_COORDS:
                if (coordSys.equals(posKind2)) {
                    int max = capabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                    return new PercentType((int) Math.round((double) Math.min(position2.intValue(), max) / max * 100));
                }

            default:

        }
        return UnDefType.UNDEF;
    }

    /**
     * Detect if the ShadePosition has a posKindN value indicating support for a secondary rail.
     *
     * @return true if the ShadePosition supports a secondary rail.
     */
    public boolean jsonSecondary() {
        return SECONDARY_ZERO_IS_OPEN.equals(posKind1) || SECONDARY_ZERO_IS_OPEN.equals(posKind2);
    }

    /**
     * Detect if the ShadePosition has both a posKindN value indicating support for tilt, AND a posKindN indicating
     * support for a primary rail. i.e. it supports tilt anywhere functionality.
     *
     * @return true if the ShadePosition supports tilt anywhere.
     */
    public boolean jsonTiltAnywhere() {
        return ((PRIMARY_ZERO_IS_CLOSED.equals(posKind1)) && (VANE_COORDS.equals(posKind2))
                || ((PRIMARY_ZERO_IS_CLOSED.equals(posKind2) && (VANE_COORDS.equals(posKind1)))));
    }

    /**
     * Set the shade's position for the given actuator class resp. coordinate system.
     *
     * @param capabilities the shade Thing capabilities.
     * @param coordSys the actuator class resp. coordinate system state to be returned.
     * @param percent the new position value.
     * @return this object.
     */
    public ShadePosition setPosition(Capabilities capabilities, CoordinateSystem coordSys, int percent) {
        // if necessary swap the order of position1 and position2
        if (PRIMARY_ZERO_IS_CLOSED.equals(posKind2) && !PRIMARY_ZERO_IS_CLOSED.equals(posKind1)) {
            final Integer posKind2Temp = posKind2;
            final Integer position2Temp = position2;
            posKind2 = Integer.valueOf(posKind1);
            position2 = Integer.valueOf(position1);
            posKind1 = posKind2Temp != null ? posKind2Temp.intValue() : NONE.ordinal();
            position1 = position2Temp != null ? position2Temp.intValue() : 0;
        }

        // delete position2 if it has an invalid position kind
        if (ERROR_UNKNOWN.equals(posKind2) || NONE.equals(posKind2)) {
            posKind2 = null;
            position2 = null;
        }

        // logic to set wither position1 or position2
        switch (coordSys) {
            case PRIMARY_ZERO_IS_CLOSED:
                if (capabilities.supportsPrimary()) {
                    setPosition1(capabilities, coordSys, percent);
                }
                break;

            case SECONDARY_ZERO_IS_OPEN:
                if (capabilities.supportsSecondary()) {
                    if (capabilities.supportsPrimary()) {
                        setPosition2(capabilities, coordSys, percent);
                    } else {
                        setPosition1(capabilities, coordSys, percent);
                    }
                }
                break;

            case VANE_COORDS:
                if (capabilities.supportsPrimary()) {
                    if (capabilities.supportsTiltOnClosed()) {
                        setPosition1(capabilities, coordSys, percent);
                    } else if (capabilities.supportsTiltAnywhere()) {
                        setPosition2(capabilities, coordSys, percent);
                    }
                } else if (capabilities.supportsTiltAnywhere()) {
                    setPosition1(capabilities, coordSys, percent);
                }
                break;

            default:
        }
        return this;
    }
}
