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
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public @Nullable QuantityType<?> getAbsolute() throws DTOPresentButEmptyException {
        Long mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            return QuantityType.valueOf(mirek, Units.MIRED).toInvertibleUnit(Units.KELVIN);
        }
        throw new DTOPresentButEmptyException("'color_temperature' DTO is present but empty");
    }

    public @Nullable Long getMirek() {
        return mirek;
    }

    public @Nullable MirekSchema getMirekSchema() {
        return mirekSchema;
    }

    /**
     * Get the color temperature as a percentage based on the MirekSchema. Note: this method is only to be used on
     * cached state DTOs which already have a defined mirek schema.
     *
     * @return the percentage of the mirekSchema range.
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public @Nullable Double getPercent() throws DTOPresentButEmptyException {
        Long mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            MirekSchema mirekSchema = this.mirekSchema;
            mirekSchema = Objects.nonNull(mirekSchema) ? mirekSchema : MirekSchema.DEFAULT_SCHEMA;
            double min = mirekSchema.getMirekMinimum();
            double max = mirekSchema.getMirekMaximum();
            double percent = 100f * (mirek.doubleValue() - min) / (max - min);
            return Math.max(0, Math.min(100, percent));
        }
        throw new DTOPresentButEmptyException("'mirek_schema' DTO is present but empty");
    }

    public ColorTemperature setMirek(double mirek) {
        this.mirek = Math.round(mirek);
        return this;
    }

    public ColorTemperature setMirekSchema(@Nullable MirekSchema mirekSchema) {
        this.mirekSchema = mirekSchema;
        return this;
    }
}
