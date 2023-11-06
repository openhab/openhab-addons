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
package org.openhab.binding.enocean.internal.eep.A5_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * From A5_10_01 up to A5_10_0D temperature is given as a 8Bit value (range: 255..0).
 * Therefore higher values mean lower temperatures.
 * Temperature range 0..40.
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class A5_10 extends _4BSMessage {

    public A5_10(ERP1Message packet) {
        super(packet);
    }

    protected int getSetPointValue() {
        // this is the default one
        return getDB2Value();
    }

    protected double getMinTemperatureValue() {
        return 0.0;
    }

    protected double getMinUnscaledTemperatureValue() {
        return 255.0;
    }

    protected double getMaxTemperatureValue() {
        return 40.0;
    }

    protected double getMaxUnscaledTemperatureValue() {
        return 0.0;
    }

    protected double getTemperatureValue() {
        return getDB1Value();
    }

    protected State getTemperature() {
        return new QuantityType<>(
                Helper.scaleValue(getTemperatureValue(), getMinUnscaledTemperatureValue(),
                        getMaxUnscaledTemperatureValue(), getMinTemperatureValue(), getMaxTemperatureValue()),
                SIUnits.CELSIUS);
    }

    protected State getFanSpeedStage() {
        if (getDB3Value() > 209) {
            return new DecimalType(-1);
        } else if (getDB3Value() > 189) {
            return new DecimalType(0);
        } else if (getDB3Value() > 164) {
            return new DecimalType(1);
        } else if (getDB3Value() > 144) {
            return new DecimalType(2);
        } else {
            return new DecimalType(3);
        }
    }

    protected int getIlluminationValue() {
        return getDB3Value();
    }

    protected State getIllumination() {
        return new QuantityType<>(getIlluminationValue() * 4, Units.LUX);
    }

    protected double getHumidityValue() {
        return getDB2Value();
    }

    protected State getSupplyVoltage() {
        double voltage = ((double) getDB3Value()) / 50.0;
        return new QuantityType<>(voltage, Units.VOLT);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_BATTERY_VOLTAGE:
                return getSupplyVoltage();

            case CHANNEL_ILLUMINATION:
                return getIllumination();

            case CHANNEL_FANSPEEDSTAGE:
                return getFanSpeedStage();

            case CHANNEL_SETPOINT:
                return new DecimalType(getSetPointValue());

            case CHANNEL_HUMIDITY:
                return new DecimalType(getHumidityValue() / 2.5);

            case CHANNEL_TEMPERATURE:
                return getTemperature();

            case CHANNEL_BATTERYLOW:
                return getBit(getDB0(), 4) ? OnOffType.ON : OnOffType.OFF;

            case CHANNEL_OCCUPANCY:
                return getBit(getDB0(), 0) ? OnOffType.OFF : OnOffType.ON;

            case CHANNEL_DAYNIGHTMODESTATE:
                return new DecimalType(getDB0Value() & 0x01);

            case CHANNEL_CONTACT:
                return getBit(getDB0(), 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;

        }

        return UnDefType.UNDEF;
    }
}
