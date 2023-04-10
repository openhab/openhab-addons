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
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public @Nullable Double getKelvin() throws DTOPresentButEmptyException {
        Long mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            return getReciprocal(mirek.doubleValue());
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
     * Convert the mirek value to a percentage value based on the MirekSchema.
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

    private double getReciprocal(double value) {
        return 1000000f / value;
    }

    public void setKelvin(double kelvin) {
        setMirek(getReciprocal(kelvin));
    }

    public void setMirek(double mirek) {
        this.mirek = Math.round(mirek);
    }

    public ColorTemperature2 setMirekSchema(@Nullable MirekSchema mirekSchema) {
        this.mirekSchema = mirekSchema;
        return this;
    }

    /**
     * Convert the percentage value to a mirek value based on the passed MirekSchema argument.
     *
     * @param mirekSchema the reference MirekSchema.
     */
    public void setPercent(double percent, MirekSchema mirekSchema) {
        double min = mirekSchema.getMirekMinimum();
        double max = mirekSchema.getMirekMaximum();
        double offset = (max - min) * percent / 100f;
        setMirek(min + offset);
    }
}
