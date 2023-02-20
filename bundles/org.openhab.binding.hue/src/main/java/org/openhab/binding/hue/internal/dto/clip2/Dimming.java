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
 * DTO for dimming brightness of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Dimming {
    private @Nullable Float brightness;

    /**
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public int getBrightness() throws DTOPresentButEmptyException {
        Float brightness = this.brightness;
        if (Objects.nonNull(brightness)) {
            return Math.round(brightness);
        }
        throw new DTOPresentButEmptyException("'dimming' DTO is present but empty");
    }

    public Dimming setBrightness(int brightness) {
        this.brightness = Float.valueOf(brightness);
        return this;
    }
}
