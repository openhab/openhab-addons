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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 mirek schema.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class MirekSchema {
    private static final int MIN = 153;
    private static final int MAX = 500;

    public static final MirekSchema DEFAULT_SCHEMA = new MirekSchema();

    private @SerializedName("mirek_minimum") int mirekMinimum = MIN;
    private @SerializedName("mirek_maximum") int mirekMaximum = MAX;

    public int getMirekMaximum() {
        return mirekMaximum;
    }

    public int getMirekMinimum() {
        return mirekMinimum;
    }

    private String toKelvin(int mirek) {
        QuantityType<?> kelvin = QuantityType.valueOf(mirek, Units.MIRED).toInvertibleUnit(Units.KELVIN);
        return Objects.nonNull(kelvin) ? String.format("%.0f K", kelvin.doubleValue()) : "";
    }

    public String toPropertyValue() {
        return String.format("%s .. %s", toKelvin(mirekMinimum), toKelvin(mirekMaximum));
    }
}
