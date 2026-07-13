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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.ColorUtil.Gamut;

/**
 * DTO for colour X/Y of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorXy {
    private @Nullable PairXy xy;
    private @Nullable Gamut2 gamut;

    public @Nullable Gamut getGamut() {
        Gamut2 gamut = this.gamut;
        return Objects.nonNull(gamut) ? gamut.getGamut() : null;
    }

    public @Nullable Gamut2 getGamut2() {
        return this.gamut;
    }

    public @Nullable PairXy getXY() {
        return xy;
    }

    public ColorXy setGamut(@Nullable Gamut gamut) {
        this.gamut = Objects.nonNull(gamut) ? new Gamut2().setGamut(gamut) : null;
        return this;
    }

    public ColorXy setXY(double[] xyValues) {
        PairXy pairXy = this.xy;
        pairXy = Objects.nonNull(pairXy) ? pairXy : new PairXy();
        pairXy.setXY(xyValues);
        this.xy = pairXy;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (this == obj) || ((xy instanceof PairXy p && obj instanceof ColorXy c) ? p.equals(c.getXY()) : false);
    }
}
