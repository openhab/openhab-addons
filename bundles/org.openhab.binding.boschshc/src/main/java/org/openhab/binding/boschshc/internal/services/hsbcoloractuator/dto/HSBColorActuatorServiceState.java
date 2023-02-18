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
package org.openhab.binding.boschshc.internal.services.hsbcoloractuator.dto;

import java.awt.Color;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.HSBType;

/**
 * State representing colors of light bulbs.
 *
 * @author David Pace - Initial contribution
 *
 */
public class HSBColorActuatorServiceState extends BoschSHCServiceState {

    public HSBColorActuatorServiceState() {
        super("colorState");
    }

    /**
     * RGB value modeled as an sRGB integer value (bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue).
     * Alpha is set to the fixed value <code>255</code>.
     */
    public int rgb;

    public String gamut;

    public ColorTemperatureRange colorTemperatureRange;

    /**
     * Converts the combined {@link #rgb} value to an openHAB-compliant HSB state.
     *
     * @return color as {@link HSBType}
     */
    public HSBType toHSBType() {
        Color color = new Color(rgb);
        return HSBType.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }
}
