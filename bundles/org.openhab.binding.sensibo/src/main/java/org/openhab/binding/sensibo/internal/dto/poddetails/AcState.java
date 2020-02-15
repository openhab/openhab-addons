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
package org.openhab.binding.sensibo.internal.dto.poddetails;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;

/**
 * @author Arne Seime - Initial contribution
 */
public class AcState {
    public boolean on;
    public String fanLevel;
    public String temperatureUnit;
    public Integer targetTemperature;
    public String mode;
    public String swing;

    public AcState(boolean on, String fanLevel, String temperatureUnit, Integer targetTemperature, String mode,
            String swing) {
        this.on = on;
        this.fanLevel = fanLevel;
        this.temperatureUnit = temperatureUnit;
        this.targetTemperature = targetTemperature;
        this.mode = mode;
        this.swing = swing;
    }

    public AcState() {
    }

    public AcState(org.openhab.binding.sensibo.internal.model.AcState acState) {
        this.on = acState.isOn();
        this.fanLevel = acState.getFanLevel();
        this.targetTemperature = acState.getTargetTemperature();
        this.mode = acState.getMode();
        this.swing = acState.getSwing();

        Unit<Temperature> unit = acState.getTemperatureUnit();

        if (unit.equals(SIUnits.CELSIUS)) {
            this.temperatureUnit = "C";
        } else if (unit.equals(ImperialUnits.FAHRENHEIT)) {
            this.temperatureUnit = "F";
        } else {
            throw new IllegalArgumentException("Unexpected temperature unit " + unit);
        }
    }
}
