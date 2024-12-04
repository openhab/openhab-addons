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
package org.openhab.binding.wiz.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum represents the possible color modes for WiZ bulbs.
 * The bulbs come in three types - full color with tunable white,
 * tunable white, and dimmable with set white. The full color and
 * tunable white bulbs operate EITHER in color mode OR in tunable
 * white mode.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public enum WizColorMode {
    // Full color mode
    RGBMode("Full Color"),
    // Tunable white (color temperature) mode
    CTMode("Tunable White"),
    // Dimming only
    SingleColorMode("Dimming Only");

    private String colorMode;

    private WizColorMode(final String colorMode) {
        this.colorMode = colorMode;
    }

    /**
     * Gets the colorMode name for request colorMode
     *
     * @return the colorMode name
     */
    public String getColorMode() {
        return colorMode;
    }
}
