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
package org.openhab.binding.bluetooth.govee.internal;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.WarningSettingsDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class GoveeHygrometerConfiguration {
    public int refreshInterval = 300;

    public @Nullable Double temperatureCalibration;
    public boolean temperatureWarningAlarm = false;
    public double temperatureWarningMin;
    public double temperatureWarningMax;

    public @Nullable Double humidityCalibration;
    public boolean humidityWarningAlarm = false;
    public double humidityWarningMin;
    public double humidityWarningMax;

    public @Nullable QuantityType<Temperature> getTemperatureCalibration() {
        var temCali = temperatureCalibration;
        if (temCali != null) {
            return new QuantityType<>(temCali, SIUnits.CELSIUS);
        }
        return null;
    }

    public @Nullable QuantityType<Dimensionless> getHumidityCalibration() {
        var humCali = humidityCalibration;
        if (humCali != null) {
            return new QuantityType<>(humCali, Units.PERCENT);
        }
        return null;
    }

    public WarningSettingsDTO<Temperature> getTemperatureWarningSettings() {
        WarningSettingsDTO<Temperature> temWarnSettings = new WarningSettingsDTO<>();
        temWarnSettings.enableAlarm = OnOffType.from(temperatureWarningAlarm);
        temWarnSettings.min = new QuantityType<>(temperatureWarningMin, SIUnits.CELSIUS);
        temWarnSettings.max = new QuantityType<>(temperatureWarningMax, SIUnits.CELSIUS);
        return temWarnSettings;
    }

    public WarningSettingsDTO<Dimensionless> getHumidityWarningSettings() {
        WarningSettingsDTO<Dimensionless> humWarnSettings = new WarningSettingsDTO<>();
        humWarnSettings.enableAlarm = OnOffType.from(humidityWarningAlarm);
        humWarnSettings.min = new QuantityType<>(humidityWarningMin, Units.PERCENT);
        humWarnSettings.max = new QuantityType<>(humidityWarningMax, Units.PERCENT);
        return humWarnSettings;
    }
}
