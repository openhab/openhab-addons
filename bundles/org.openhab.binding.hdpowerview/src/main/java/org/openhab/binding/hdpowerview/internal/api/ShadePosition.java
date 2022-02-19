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
package org.openhab.binding.hdpowerview.internal.api;

import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The position of a single shade, as returned by the HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class ShadePosition {

    private final transient Logger logger = LoggerFactory.getLogger(ShadePosition.class);

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
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be returned.
     * @return the current state.
     */
    public State getState(Capabilities shadeCapabilities, CoordinateSystem posKindCoords) {
        State result = getPosition1(shadeCapabilities, posKindCoords);
        if (result == UnDefType.UNDEF) {
            result = getPosition2(shadeCapabilities, posKindCoords);
        }
        logger.trace("getState(): capabilities={}, coords={} => result={}", shadeCapabilities, posKindCoords, result);
        return result;
    }

    /**
     * Set the shade's position1 value for the given actuator class resp. coordinate system.
     *
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be changed.
     * @param percent the new position value.
     */
    private void setPosition1(Capabilities shadeCapabilities, CoordinateSystem posKindCoords, int percent) {
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                if (shadeCapabilities.supportsPrimary() && shadeCapabilities.supportsSecondary()) {
                    // on dual rail shades constrain percent to not move the lower rail above the upper
                    State secondary = getState(shadeCapabilities, SECONDARY_POSITION);
                    if (secondary instanceof PercentType) {
                        int secPercent = ((PercentType) secondary).intValue();
                        if (percent < secPercent) {
                            percent = secPercent;
                        }
                    }
                }
                posKind1 = posKindCoords.ordinal();
                position1 = MAX_SHADE - (int) Math.round((double) percent / 100 * MAX_SHADE);
                break;

            case SECONDARY_POSITION:
                /*
                 * Secondary, blackout shade a 'Duolite' shade: => INVERTED
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                posKind1 = posKindCoords.ordinal();
                if (shadeCapabilities.supportsBlackoutShade()) {
                    position1 = MAX_SHADE - (int) Math.round((double) percent / 100 * MAX_SHADE);
                } else {
                    position1 = (int) Math.round((double) percent / 100 * MAX_SHADE);
                }
                break;

            case VANE_TILT_POSITION:
                /*
                 * Vane angle of the primary rail of a bottom-up single action shade: => NOT INVERTED
                 */
                posKind1 = posKindCoords.ordinal();
                int max = shadeCapabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
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
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be returned.
     * @return the State (or UNDEF if not available).
     */
    private State getPosition1(Capabilities shadeCapabilities, CoordinateSystem posKindCoords) {
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                if (posKindCoords.equals(posKind1)) {
                    return new PercentType(100 - (int) Math.round((double) position1 / MAX_SHADE * 100));
                }
                if (VANE_TILT_POSITION.equals(posKind1) && shadeCapabilities.supportsTiltOnClosed()) {
                    return PercentType.HUNDRED;
                }
                if (SECONDARY_POSITION.equals(posKind1) && shadeCapabilities.supportsBlackoutShade()) {
                    return PercentType.HUNDRED;
                }
                break;

            case SECONDARY_POSITION:
                /*
                 * Secondary, blackout shade a 'Duolite' shade: => INVERTED
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                if (posKindCoords.equals(posKind1)) {
                    if (shadeCapabilities.supportsBlackoutShade()) {
                        return new PercentType(100 - (int) Math.round((double) position1 / MAX_SHADE * 100));
                    }
                    return new PercentType((int) Math.round((double) position1 / MAX_SHADE * 100));
                }
                if (PRIMARY_POSITION.equals(posKind1) && shadeCapabilities.supportsBlackoutShade()) {
                    return PercentType.ZERO;
                }
                break;

            case VANE_TILT_POSITION:
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
                if (posKindCoords.equals(posKind1)) {
                    int max = shadeCapabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                    return new PercentType((int) Math.round((double) Math.min(position1, max) / max * 100));
                }
                if (PRIMARY_POSITION.equals(posKind1) && shadeCapabilities.supportsTiltOnClosed()) {
                    return position1 != 0 ? UnDefType.UNDEF : PercentType.ZERO;
                }
                break;

            case ERROR_UNKNOWN:
            case NONE:
                // fall through, return UNDEF
        }
        return UnDefType.UNDEF;
    }

    /**
     * Set the shade's position2 value for the given actuator class resp. coordinate system.
     *
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be changed.
     * @param percent the new position value.
     */
    private void setPosition2(Capabilities shadeCapabilities, CoordinateSystem posKindCoords, int percent) {
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                posKind2 = posKindCoords.ordinal();
                position2 = Integer.valueOf(MAX_SHADE - (int) Math.round((double) percent / 100 * MAX_SHADE));
                break;

            case SECONDARY_POSITION:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                if (shadeCapabilities.supportsPrimary() && shadeCapabilities.supportsSecondary()) {
                    // on dual rail shades constrain percent to not move the upper rail below the lower
                    State primary = getState(shadeCapabilities, PRIMARY_POSITION);
                    if (primary instanceof PercentType) {
                        int primaryPercent = ((PercentType) primary).intValue();
                        if (percent > primaryPercent) {
                            percent = primaryPercent;
                        }
                    }
                }
                posKind2 = posKindCoords.ordinal();
                position2 = Integer.valueOf((int) Math.round((double) percent / 100 * MAX_SHADE));
                break;

            case VANE_TILT_POSITION:
                posKind2 = posKindCoords.ordinal();
                int max = shadeCapabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
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
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be returned.
     * @return the State (or UNDEF if not available).
     */
    private State getPosition2(Capabilities shadeCapabilities, CoordinateSystem posKindCoords) {
        Integer posKind2 = this.posKind2;
        Integer position2 = this.position2;

        if (position2 == null || posKind2 == null) {
            return UnDefType.UNDEF;
        }

        switch (posKindCoords) {
            case PRIMARY_POSITION:
                /*
                 * Primary rail of a bottom-up shade, or lower rail of a dual action shade: => INVERTED
                 */
                if (posKindCoords.equals(posKind2)) {
                    return new PercentType(100 - (int) Math.round(position2.doubleValue() / MAX_SHADE * 100));
                }
                break;

            case SECONDARY_POSITION:
                /*
                 * Secondary, upper rail of a dual action shade: => NOT INVERTED
                 */
                if (posKindCoords.equals(posKind2)) {
                    return new PercentType((int) Math.round(position2.doubleValue() / MAX_SHADE * 100));
                }
                break;

            /*
             * Vane angle of the primary rail of a bottom-up single action shade: => NOT INVERTED
             */
            case VANE_TILT_POSITION:
                if (posKindCoords.equals(posKind2)) {
                    int max = shadeCapabilities.supportsTilt180() ? MAX_SHADE : MAX_VANE;
                    return new PercentType((int) Math.round((double) Math.min(position2.intValue(), max) / max * 100));
                }
                break;

            case ERROR_UNKNOWN:
            case NONE:
                // fall through, return UNDEF
        }
        return UnDefType.UNDEF;
    }

    /**
     * Detect if the ShadePosition has a posKindN value indicating potential support for a secondary rail.
     *
     * @return true if the ShadePosition supports a secondary rail.
     */
    public boolean secondaryRailDetected() {
        return SECONDARY_POSITION.equals(posKind1) || SECONDARY_POSITION.equals(posKind2);
    }

    /**
     * Detect if the ShadePosition has both a posKindN value indicating potential support for tilt, AND a posKindN
     * indicating support for a primary rail. i.e. it potentially supports tilt anywhere functionality.
     *
     * @return true if potential support for tilt anywhere functionality was detected.
     */
    public boolean tiltAnywhereDetected() {
        return ((PRIMARY_POSITION.equals(posKind1)) && (VANE_TILT_POSITION.equals(posKind2))
                || ((PRIMARY_POSITION.equals(posKind2) && (VANE_TILT_POSITION.equals(posKind1)))));
    }

    /**
     * Set the shade's position for the given actuator class resp. coordinate system.
     *
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be changed.
     * @param percent the new position value.
     * @return this object.
     */
    public ShadePosition setPosition(Capabilities shadeCapabilities, CoordinateSystem posKindCoords, int percent) {
        logger.trace("setPosition(): capabilities={}, coords={}, percent={}", shadeCapabilities, posKindCoords,
                percent);
        // if necessary swap the order of position1 and position2
        if (PRIMARY_POSITION.equals(posKind2) && !PRIMARY_POSITION.equals(posKind1)) {
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

        // logic to set either position1 or position2
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                if (shadeCapabilities.supportsPrimary()) {
                    setPosition1(shadeCapabilities, posKindCoords, percent);
                }
                break;

            case SECONDARY_POSITION:
                if (shadeCapabilities.supportsSecondary()) {
                    if (shadeCapabilities.supportsPrimary()) {
                        setPosition2(shadeCapabilities, posKindCoords, percent);
                    } else {
                        setPosition1(shadeCapabilities, posKindCoords, percent);
                    }
                } else if (shadeCapabilities.supportsBlackoutShade()) {
                    setPosition1(shadeCapabilities, posKindCoords, percent);
                }
                break;

            case VANE_TILT_POSITION:
                if (shadeCapabilities.supportsPrimary()) {
                    if (shadeCapabilities.supportsTiltOnClosed()) {
                        setPosition1(shadeCapabilities, posKindCoords, percent);
                    } else if (shadeCapabilities.supportsTiltAnywhere()) {
                        setPosition2(shadeCapabilities, posKindCoords, percent);
                    }
                } else if (shadeCapabilities.supportsTiltAnywhere()) {
                    setPosition1(shadeCapabilities, posKindCoords, percent);
                }
                break;

            case ERROR_UNKNOWN:
            case NONE:
                // fall through, do nothing
        }
        return this;
    }
}
