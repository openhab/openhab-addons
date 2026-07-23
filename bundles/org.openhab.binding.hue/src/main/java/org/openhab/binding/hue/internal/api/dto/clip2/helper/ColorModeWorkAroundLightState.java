/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip2.helper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.PairXy;

/**
 * State machine for work-arounds for 3rd party legacy lights that still operate bi-stable in
 * either FULL_COLOR or COLOR_TEMP mode.
 * 
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class ColorModeWorkAroundLightState {
    public enum Mode {
        FULL_COLOR,
        COLOR_TEMP
    }

    private Mode mode = Mode.FULL_COLOR;
    private @Nullable Long mirek;
    private @Nullable PairXy colorTempXY;
    private @Nullable PairXy fullColorXY;

    private boolean equal(@Nullable PairXy a, @Nullable PairXy b) {
        return a != null && a.equals(b);
    }

    public void setValues(@Nullable Long mirekArg, @Nullable PairXy colorXY) throws IllegalArgumentException {
        // if there is a mirek value, definitely switch to COLOR_TEMP mode
        if (mirekArg != null) {
            mode = Mode.COLOR_TEMP;
            mirek = mirekArg;
            colorTempXY = colorXY;
            fullColorXY = null;
            return;
        }

        if (colorXY == null) {
            throw new IllegalArgumentException("if mirek is null then colorXY must be non null");
        }

        switch (mode) {
            case COLOR_TEMP:
                // if colorXY update is the same as colorTempXY, then do nothing
                if (equal(colorXY, colorTempXY)) {
                    return;
                }
                // if fullColorXY is null, then update it
                if (fullColorXY == null) {
                    fullColorXY = colorXY;
                    return;
                }
                // if colorXY is not the same as fullColorXY, definitely switch to FULL_COLOR mode
                if (!equal(colorXY, fullColorXY)) {
                    mode = Mode.FULL_COLOR;
                    mirek = null;
                    colorTempXY = null;
                    fullColorXY = colorXY;
                    return;
                }
                break;

            case FULL_COLOR:
                // stay in FULL_COLOR mode and update fullColorXY
                mirek = null;
                colorTempXY = null;
                fullColorXY = colorXY;
                return;
        }
    }

    public @Nullable Long getMirek() {
        return mode == Mode.COLOR_TEMP ? mirek : null;
    }

    public Mode getMode() {
        return mode;
    }

    public @Nullable PairXy getXY() {
        return mode == Mode.COLOR_TEMP ? colorTempXY : fullColorXY;
    }

    @Override
    public String toString() {
        return "mode=%s, mirek=%d, colorTempXY=%s, fullColorXY=%s".formatted(mode, mirek, toString(colorTempXY),
                toString(fullColorXY));
    }

    private static String toString(@Nullable PairXy xy) {
        return xy == null ? "null" : "(%.4f, %.4f)".formatted(xy.getXY()[0], xy.getXY()[1]);
    }
}
