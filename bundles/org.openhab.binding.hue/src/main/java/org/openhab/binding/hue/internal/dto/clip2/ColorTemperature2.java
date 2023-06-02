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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for colour temperature of a light in CLIP 2.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorTemperature2 {
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
     * Get the color temperature as a percentage based on the MirekSchema.
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
            double percent = (100f * (mirek.doubleValue() - min)) / (max - min);
            return Math.max(0, Math.min(100, percent));
        }
        throw new DTOPresentButEmptyException("'color_temperature' DTO is present but empty");
    }

    /**
     * Set the color temperature from a QuantityType. Convert the temperature value to a mirek value based on the passed
     * MirekSchema argument.
     *
     * @param colorTemperature a QuantityType<Temperature> value
     * @param mirekSchema the reference MirekSchema.
     * @return this
     */
    public ColorTemperature2 setAbsolute(QuantityType<?> colorTemperature, MirekSchema mirekSchema) {
        QuantityType<?> kelvin = colorTemperature.toInvertibleUnit(Units.KELVIN);
        if (Objects.nonNull(kelvin)) {
            QuantityType<?> mirek = kelvin.toInvertibleUnit(Units.MIRED);
            if (Objects.nonNull(mirek)) {
                setMirek(Math.max(mirekSchema.getMirekMinimum(),
                        Math.min(mirekSchema.getMirekMaximum(), mirek.doubleValue())));
            }
        }
        return this;
    }

    public ColorTemperature2 setMirek(double mirek) {
        this.mirek = Math.round(mirek);
        return this;
    }

    public ColorTemperature2 setMirekSchema(@Nullable MirekSchema mirekSchema) {
        this.mirekSchema = mirekSchema;
        return this;
    }

    /**
     * Set the color temperature from a PercentType. Convert the percentage value to a mirek value based on the passed
     * MirekSchema argument.
     *
     * @param percent a PercentType value
     * @param mirekSchema the reference MirekSchema.
     * @return this
     */
    public ColorTemperature2 setPercent(double percent, MirekSchema mirekSchema) {
        double min = mirekSchema.getMirekMinimum();
        double max = mirekSchema.getMirekMaximum();
        double offset = (max - min) * percent / 100f;
        setMirek(min + offset);
        return this;
    }
}
