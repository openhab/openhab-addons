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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for colour temperature of a light in CLIP 2.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorTemperature {
    private @Nullable Long mirek;
    private @Nullable @SerializedName("mirek_schema") MirekSchema mirekSchema;

    /**
     * Get the color temperature as a QuantityType value.
     *
     * @return a QuantityType value
     */
    public @Nullable QuantityType<?> getAbsolute() {
        Long mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            return QuantityType.valueOf(mirek, Units.MIRED).toInvertibleUnit(Units.KELVIN);
        }
        return null;
    }

    public @Nullable Long getMirek() {
        return mirek;
    }

    public @Nullable MirekSchema getMirekSchema() {
        return mirekSchema;
    }

    public ColorTemperature setMirek(@Nullable Long mirek) {
        this.mirek = mirek;
        return this;
    }

    public ColorTemperature setMirekSchema(@Nullable MirekSchema mirekSchema) {
        this.mirekSchema = mirekSchema;
        return this;
    }
}
