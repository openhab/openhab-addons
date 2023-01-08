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

import com.google.gson.annotations.SerializedName;

/**
 * DTO for colour temperature of a light in CLIP 2.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorTemperature2 {
    private @Nullable Integer mirek;
    private @Nullable @SerializedName("mirek_schema") MirekSchema mirekSchema;

    private float getReciprocal(float value) {
        return Math.round(1000000f / value);
    }

    public @Nullable Integer getMirek() {
        return mirek;
    }

    public void setMirek(float mirek) {
        this.mirek = Math.round(mirek);
    }

    /**
     * Convert the mirek value to a percentage value based on the passed MirekSchema argument.
     *
     * @param mirekSchema the reference MirekSchema.
     * @return the percentage of the mirekSchema range.
     */
    public @Nullable Integer getPercent(MirekSchema mirekSchema) {
        Integer mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            float min = mirekSchema.getMirekMinimum();
            float max = mirekSchema.getMirekMaximum();
            float percent = (100f * (mirek.floatValue() - min)) / (max - min);
            return Math.round(Math.max(0, Math.min(100, percent)));
        }
        return null;
    }

    /**
     * Convert the percentage value to a mirek value based on the passed MirekSchema argument.
     *
     * @param mirekSchema the reference MirekSchema.
     */
    public void setPercent(int percent, MirekSchema mirekSchema) {
        float min = mirekSchema.getMirekMinimum();
        float max = mirekSchema.getMirekMaximum();
        float offset = (max - min) * Float.valueOf(percent) / 100f;
        setMirek(min + offset);
    }

    public @Nullable Float getKelvin() {
        Integer mirek = this.mirek;
        if (Objects.nonNull(mirek)) {
            return getReciprocal(mirek.floatValue());
        }
        return null;
    }

    public void setKelvin(float kelvin) {
        setMirek(getReciprocal(kelvin));
    }

    public @Nullable MirekSchema getMirekSchema() {
        return mirekSchema;
    }
}
