/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.sensibo.internal.SensiboTemperatureUnitParser;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcStateDTO;

/**
 * Represents the state of the AC unit.
 * 
 * @author Arne Seime - Initial contribution
 */
public class AcState {
    private final boolean on;
    private final String fanLevel;
    private final Unit<Temperature> temperatureUnit;
    private final Integer targetTemperature;
    private final String mode;
    private final String swing;

    public AcState(final AcStateDTO dto) {
        this.on = dto.on;
        this.fanLevel = dto.fanLevel;
        this.targetTemperature = dto.targetTemperature;
        this.mode = dto.mode;
        this.swing = dto.swing;
        this.temperatureUnit = SensiboTemperatureUnitParser.parse(dto.temperatureUnit);
    }

    public boolean isOn() {
        return on;
    }

    public String getFanLevel() {
        return fanLevel;
    }

    public Unit<Temperature> getTemperatureUnit() {
        return temperatureUnit;
    }

    public Integer getTargetTemperature() {
        return targetTemperature;
    }

    public String getMode() {
        return mode;
    }

    public String getSwing() {
        return swing;
    }
}
