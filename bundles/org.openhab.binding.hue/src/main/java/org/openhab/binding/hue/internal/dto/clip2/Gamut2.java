/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

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
        PairXy r = red;
        PairXy g = green;
        PairXy b = blue;
        if (Objects.nonNull(r) && Objects.nonNull(g) && Objects.nonNull(b)) {
            return new Gamut(r.getXY(), g.getXY(), b.getXY());
        }
        return null;
    }

    public Gamut2 setGamut(Gamut gamut) {
        red = new PairXy().setXY(gamut.r());
        green = new PairXy().setXY(gamut.g());
        blue = new PairXy().setXY(gamut.b());
        return this;
    }
}
