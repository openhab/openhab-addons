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
package org.openhab.binding.sensibo.internal.model;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensibo.internal.SensiboTemperatureUnitConverter;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcStateDTO;

/**
 * Represents the state of the AC unit.
 * 
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class AcState {
    private final boolean on;
    private final @Nullable String fanLevel;
    private final @Nullable Unit<Temperature> temperatureUnit;
    private final @Nullable Integer targetTemperature;
    private final @Nullable String mode;
    private final @Nullable String swing;

    public AcState(final AcStateDTO dto) {
        this.on = dto.on;
        this.fanLevel = dto.fanLevel;
        this.targetTemperature = dto.targetTemperature;
        this.mode = dto.mode;
        this.swing = dto.swing;
        this.temperatureUnit = SensiboTemperatureUnitConverter.parseFromSensiboFormat(dto.temperatureUnit);
    }

    public boolean isOn() {
        return on;
    }

    @Nullable
    public String getFanLevel() {
        return fanLevel;
    }

    @Nullable
    public Unit<Temperature> getTemperatureUnit() {
        return temperatureUnit;
    }

    @Nullable
    public Integer getTargetTemperature() {
        return targetTemperature;
    }

    @Nullable
    public String getMode() {
        return mode;
    }

    @Nullable
    public String getSwing() {
        return swing;
    }
}
