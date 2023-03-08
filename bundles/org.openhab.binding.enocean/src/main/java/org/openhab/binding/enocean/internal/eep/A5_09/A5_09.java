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
package org.openhab.binding.enocean.internal.eep.A5_09;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.eep.EEPHelper;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Zhivka Dimova - Initial contribution
 */
@NonNullByDefault
public abstract class A5_09 extends _4BSMessage {

    public A5_09(ERP1Message packet) {
        super(packet);
    }

    // CO
    protected double getUnscaledCOMin() {
        return 0;
    }

    protected double getUnscaledCOMax() {
        return 255;
    }

    protected double getScaledCOMin() {
        return 0;
    }

    protected double getScaledCOMax() {
        return 1020;
    }

    protected int getUnscaledCOValue() {
        return getDB_2Value();
    }

    // CO2
    protected double getUnscaledCO2Min() {
        return 0;
    }

    protected double getUnscaledCO2Max() {
        return 255;
    }

    protected double getScaledCO2Min() {
        return 0;
    }

    protected double getScaledCO2Max() {
        return 2550;
    }

    protected int getUnscaledCO2Value() {
        return getDB_2Value();
    }

    // Temperature
    protected double getUnscaledTemperatureMin() {
        return 0;
    }

    protected double getUnscaledTemperatureMax() {
        return 255;
    }

    protected double getScaledTemperatureMin() {
        return 0;
    }

    protected double getScaledTemperatureMax() {
        return 51;
    }

    protected int getUnscaledTemperatureValue() {
        return getDB_1Value();
    }

    // Humidity
    protected double getUnscaledHumidityMin() {
        return 0;
    }

    protected double getUnscaledHumidityMax() {
        return 200;
    }

    protected double getScaledHumidityMin() {
        return 0;
    }

    protected double getScaledHumidityMax() {
        return 100;
    }

    protected int getUnscaledHumidityValue() {
        return getDB_3Value();
    }

    // Battery
    protected double getUnscaledBatteryVoltageMin() {
        return 0;
    }

    protected double getUnscaledBatteryVoltageMax() {
        return 255;
    }

    protected double getScaledBatteryVoltageMin() {
        return 0;
    }

    protected double getScaledBatteryVoltageMax() {
        return 5.1;
    }

    private int getUnscaledBatteryVoltageValue() {
        return getDB_3Value();
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        if (CHANNEL_CO.equals(channelId)) {
            double scaledCO = EEPHelper.calculateScaledValue(getUnscaledCOValue(), getScaledCOMin(), getScaledCOMax(),
                    getUnscaledCOMin(), getUnscaledCOMax());
            return new QuantityType<>(scaledCO, Units.PARTS_PER_MILLION);
        } else if (CHANNEL_CO2.equals(channelId)) {
            double scaledCO2 = EEPHelper.calculateScaledValue(getUnscaledCO2Value(), getScaledCO2Min(),
                    getScaledCO2Max(), getUnscaledCO2Min(), getUnscaledCO2Max());
            return new QuantityType<>(scaledCO2, Units.PARTS_PER_MILLION);
        } else if (CHANNEL_TEMPERATURE.equals(channelId)) {
            double scaledTemp = EEPHelper.calculateScaledValue(getUnscaledTemperatureValue(), getScaledTemperatureMin(),
                    getScaledTemperatureMax(), getUnscaledTemperatureMin(), getUnscaledTemperatureMax());
            return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
        } else if (CHANNEL_HUMIDITY.equals(channelId)) {
            return new DecimalType((getUnscaledHumidityValue() * 100.0) / getUnscaledHumidityMax());
        } else if (CHANNEL_BATTERY_VOLTAGE.equals(channelId)) {
            double scaledBatteryVoltage = EEPHelper.calculateScaledValue(getUnscaledBatteryVoltageValue(),
                    getScaledBatteryVoltageMin(), getScaledBatteryVoltageMax(), getUnscaledBatteryVoltageMin(),
                    getUnscaledBatteryVoltageMax());
            return new QuantityType<>(scaledBatteryVoltage, Units.VOLT);
        }

        return UnDefType.UNDEF;
    }
}
