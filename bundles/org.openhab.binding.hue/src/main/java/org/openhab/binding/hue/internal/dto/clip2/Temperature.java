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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 temperature sensor.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Temperature {
    private float temperature;
    private @SerializedName("temperature_valid") boolean temperatureValid;
    private @Nullable @SerializedName("temperature_report") TemperatureReport temperatureReport;

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Moved to temperature_report/temperature.
     * Should be used only as fallback for older firmwares.
     *
     * @return temperature in 1.00 degrees Celsius
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Indication whether the value presented in temperature is valid
     * Should be used only as fallback for older firmwares.
     */
    public boolean isTemperatureValid() {
        return temperatureValid;
    }

    public State getTemperatureState() {
        return temperatureValid ? new QuantityType<>(temperature, SIUnits.CELSIUS) : UnDefType.UNDEF;
    }

    public State getTemperatureValidState() {
        return OnOffType.from(temperatureValid);
    }

    public @Nullable TemperatureReport getTemperatureReport() {
        return temperatureReport;
    }
}
