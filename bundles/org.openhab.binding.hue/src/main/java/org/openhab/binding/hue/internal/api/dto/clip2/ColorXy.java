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
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;
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

    /**
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public double[] getXY() throws DTOPresentButEmptyException {
        PairXy pairXy = this.xy;
        if (Objects.nonNull(pairXy)) {
            return pairXy.getXY();
        }
        throw new DTOPresentButEmptyException("'color' DTO is present but empty");
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
}
