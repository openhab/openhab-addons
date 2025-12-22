/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.calc.CircadianCalc;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the calculated brightness and color temperature.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record Circadian(int brightness, int temperature) {

    public static final Circadian DEFAULT = new Circadian(0, CircadianCalc.MIN_COLOR_TEMP);

    public Circadian {
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness level out of range");
        }
    }

    public Circadian(double percentage, double colorTemp) {
        this((int) Math.round(percentage), (int) colorTemp);
    }

    public QuantityType<?> getTemperature() {
        return new QuantityType<>(temperature, Units.KELVIN);
    }

    public PercentType getBrightness() {
        return new PercentType(brightness);
    }
}
