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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.ColorUtil.Gamut;

/**
 * DTO for colour gamut of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Gamut2 {
    private @Nullable PairXy red;
    private @Nullable PairXy green;
    private @Nullable PairXy blue;

    public @Nullable Gamut getGamut() {
        PairXy red = this.red;
        PairXy green = this.green;
        PairXy blue = this.blue;
        if (Objects.nonNull(red) && Objects.nonNull(green) && Objects.nonNull(blue)) {
            return new Gamut(red.getXY(), green.getXY(), blue.getXY());
        }
        return null;
    }

    public Gamut2 setGamut(Gamut gamut) {
        red = new PairXy().setXY(gamut.r());
        green = new PairXy().setXY(gamut.g());
        blue = new PairXy().setXY(gamut.b());
        return this;
    }

    public @Nullable String toPropertyValue() {
        PairXy red = this.red;
        PairXy green = this.green;
        PairXy blue = this.blue;
        if (Objects.nonNull(red) && Objects.nonNull(green) && Objects.nonNull(blue)) {
            double[] r = red.getXY();
            double[] g = green.getXY();
            double[] b = blue.getXY();
            return String.format("(%.3f,%.3f) (%.3f,%.3f) (%.3f,%.3f)", r[0], r[1], g[0], g[1], b[0], b[1]);
        }
        return null;
    }
}
