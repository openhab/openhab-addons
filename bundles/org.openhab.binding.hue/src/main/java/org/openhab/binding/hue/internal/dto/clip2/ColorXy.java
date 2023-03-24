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
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;

/**
 * DTO for colour X/Y of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorXy {
    private @Nullable PairXy xy;

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

    public ColorXy setXY(double[] xyValues) {
        PairXy pairXy = this.xy;
        pairXy = Objects.nonNull(pairXy) ? pairXy : new PairXy();
        pairXy.setXY(xyValues);
        this.xy = pairXy;
        return this;
    }
}
