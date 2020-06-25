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

import static org.openhab.binding.hdpowerview.internal.api.PosKind.*;

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

    /*-
     * Specifies the position of the shade. Top-down shades are in the same
     * coordinate space as bottom-up shades. Shade position values for top-down
     * shades would be reversed for bottom-up shades. For example, since 65535 is
     * the open value for a bottom-up shade, it is the closed value for a top-down
     * shade. The top-down/bottom-up shade is different in that instead of the top
     * and bottom rail operating in one coordinate space like the top-down and the
     * bottom-up, it operates in two where the top (middle) rail closed value is 0
     * and the bottom (primary) rail closed position is also 0 and fully open for
     * both is 65535
     * 
     * The position element can take on multiple states depending on the family of
     * shade under control.
     *
     * The position1 element will only ever show one type of posKind1: either 1 or
     * 3; this is because the shade cannot physically exist with both shade and vane
     * open (to any degree).
     *
     * Therefore one can make the assumption that if there is a non-zero position1
     * while posKind1 == 1, then position1 == 0 for the implied posKind1 == 3.
     * 
     * The range of integer values for position1 is: 0..65535  
     * 
     * Shade fully up: (top-down: open, bottom-up: closed)
     *    posKind1: 1
     *    position1: 65535
     *
     * Shade and vane fully down: (top-down: closed, bottom-up: open) 
     *    posKind1: 1
     *    position1: 0
     *    
     * ALTERNATE: Shade and vane fully down: (top-down: closed, bottom-up: open)
     *    posKind1: 3
     *    position1: 0
     *
     * Shade fully down (closed) and vane fully up (open):
     *    posKind1: 3
     *    position1: 65535
     */

    private static final int MAX_POS = 65535;

    private int posKind1;
    private int position1;

    /*
     * here we have to use Integer objects rather than just int primitives because
     * these are secondary optional position elements in the JSON payload, so the
     * GSON de-serializer might leave them as null
     */
    private @Nullable Integer posKind2 = null;
    private @Nullable Integer position2 = null;

    public static ShadePosition create(PosKind kind, int percent) {
        return new ShadePosition(kind, percent, null, null);
    }

    public static ShadePosition create(PosKind kind, int primaryPercent, @Nullable PosKind secondaryKind,
            @Nullable Integer secondaryPercent) {
        return new ShadePosition(kind, primaryPercent, secondaryKind, secondaryPercent);
    }

    public static ShadePosition create(ShadePosition primary, @Nullable PosKind secondaryKind,
            @Nullable Integer secondaryPercent) {
        ShadePosition clone = new ShadePosition(REGULAR, 0, secondaryKind, secondaryPercent);
        clone.position1 = primary.position1;
        clone.posKind1 = primary.posKind1;
        return clone;
    }

    ShadePosition(PosKind primaryKind, int primaryPercent, @Nullable PosKind secondaryKind,
            @Nullable Integer secondaryPercent) {
        posKind1 = primaryKind.ordinal() + 1;
        switch (primaryKind) {
            case REGULAR:
                /*-
                 * Primary rail of a single action top-down shade, or 
                 * Primary, lower, top-down, rail of a dual action shade
                 */
            case INVERTED:
                /*
                 * Primary rail of a single action bottom-up shade
                 *
                 * All these types use the same coordinate system; which is inverted in relation
                 * to that of OpenHAB
                 */
                position1 = MAX_POS - (int) Math.round(primaryPercent / 100d * MAX_POS);
                break;
            case VANE:
                /*
                 * Slat angle of the primary rail of a top-down single action shade
                 */
                position1 = (int) Math.round(primaryPercent / 100d * MAX_POS);
                break;
            default:
                position1 = 0;
        }

        if (secondaryKind == null || secondaryPercent == null) {
            return;
        }

        posKind2 = Integer.valueOf(secondaryKind.ordinal() + 1);
        switch (secondaryKind) {
            case INVERTED:
                /*-
                 * Secondary, upper, bottom-up rail of a dual action shade
                 * 
                 * Uses a coordinate system that is NOT inverted in relation to
                 * that of OpenHAB
                 */
                position2 = Integer.valueOf((int) Math.round(secondaryPercent.doubleValue() / 100 * MAX_POS));
                break;
            default:
                position2 = Integer.valueOf(0);
        }
    }

    public State getState(PosSeq seq, PosKind kind) {
        switch (seq) {
            case PRIMARY:
                switch (kind) {
                    case REGULAR:
                        /*-
                         * Primary rail of a single action top-down shade, or 
                         * Primary, lower, top-down, rail of a dual action shade
                         */
                    case INVERTED:
                        /*
                         * Primary rail of a single action bottom-up shade
                         *
                         * All these types use the same coordinate system; which is inverted in
                         * relation to that of OpenHAB
                         * 
                         * If the slats have a defined position then the shade position must by
                         * definition be 100%
                         */
                        return posKind1 == 3 ? PercentType.HUNDRED
                                : new PercentType(100 - (int) Math.round((double) position1 / MAX_POS * 100));

                    case VANE:
                        /*
                         * Slat angle of the primary rail of a top-down single action shade
                         * 
                         * If the shades are not open, the slat position is undefined; if the the shades
                         * are exactly open then the slats are at zero; otherwise return the actual slat
                         * position itself
                         */
                        return posKind1 != 3 ? (position1 != 0 ? UnDefType.UNDEF : PercentType.ZERO)
                                : new PercentType((int) Math.round((double) position1 / MAX_POS * 100));

                    default:
                        break;
                }

            case SECONDARY:
                Integer posKind2 = this.posKind2;
                Integer position2 = this.position2;
                if (position2 != null && posKind2 != null) {
                    switch (kind) {
                        case REGULAR:
                        case INVERTED:
                            /*-
                             * Secondary, upper, bottom-up rail of a dual action shade
                             * 
                             * Uses a coordinate system that is NOT inverted in relation to
                             * that of OpenHAB
                             */
                            return new PercentType(100 - (int) Math.round(position2.doubleValue() / MAX_POS * 100));
                        default:
                            break;
                    }
                }
        }
        return UnDefType.UNDEF;
    }

    public PosKind getPosKind() {
        return PosKind.get(posKind1);
    }
}
