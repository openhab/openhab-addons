/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_11;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.eep.EEPHelper;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vincent Bakker - Initial contribution
 */

public class A5_11_04 extends _4BSMessage {

    private enum Error {
        NO_ERROR_PRESENT,
        LAMP_FAILURE,
        INTERNAL_FAILURE,
        FAILURE_ON_THE_EXTERNAL_PERIPHERY
    }

    private enum ParameterMode {
        EIGHT_BIT_DIMMER_VALUE_AND_LAMP_OPERATING_HOURS,
        RGB_VALUE,
        ENERGY_METERING_VALUE,
        NOT_USED
    }

    public enum EnergyUnit {
        MILLIWATT,
        WATT,
        KILOWATT,
        MEGAWATT,
        WATTHOUR,
        KILOWATTHOUR,
        MEGAWATTHOUR,
        GIGAWATTHOUR,
        NOT_SUPPORTED
    }

    private static Logger logger = LoggerFactory.getLogger(A5_11_04.class);

    public A5_11_04(ERP1Message packet) {
        super(packet);
    }

    protected boolean isErrorState() {
        byte db0 = getDB_0();

        int state = (db0 >> 4) & 0x03;

        if (state != 0) {
            // TODO: display error state on thing
            logger.warn("Received error {}: {}", state, Error.values()[state]);
            return true;
        } else {
            return false;
        }
    }

    protected ParameterMode getParameterMode() {
        int pm = (getDB_0() >> 1) & 0x03;
        return ParameterMode.values()[pm];
    }

    protected EnergyUnit getEnergyUnit() {
        int unit = getDB_1();
        if (unit < 8) {
            return EnergyUnit.values()[unit];
        }

        return EnergyUnit.NOT_SUPPORTED;
    }

    protected State getLightingStatus() {
        byte db0 = getDB_0();
        boolean lightOn = getBit(db0, 0);

        return lightOn ? OnOffType.ON : OnOffType.OFF;
    }

    protected State getDimmerStatus() {
        if (getParameterMode() == ParameterMode.EIGHT_BIT_DIMMER_VALUE_AND_LAMP_OPERATING_HOURS) {
            return new PercentType(getDB_3Value() * 100 / 255);
        }
        return UnDefType.UNDEF;
    }

    protected State getEnergyMeasurementData() {
        if (getParameterMode() == ParameterMode.ENERGY_METERING_VALUE) {
            EnergyUnit unit = getEnergyUnit();

            float factor = 1;
            switch (unit) {
                case WATTHOUR:
                    factor /= 1000;
                    break;
                case KILOWATTHOUR:
                    factor = 1;
                    break;
                case GIGAWATTHOUR:
                    factor *= 1000;
                case MEGAWATTHOUR:
                    factor *= 1000;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            return new QuantityType<>(
                    Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2() }), 16) * factor,
                    Units.KILOWATT_HOUR);
        }

        return UnDefType.UNDEF;
    }

    protected State getPowerMeasurementData() {
        if (getParameterMode() == ParameterMode.ENERGY_METERING_VALUE) {
            EnergyUnit unit = getEnergyUnit();

            float factor = 1;
            switch (unit) {
                case MILLIWATT:
                    factor /= 1000;
                    break;
                case WATT:
                    factor = 1;
                    break;
                case MEGAWATT:
                    factor *= 1000;
                case KILOWATT:
                    factor *= 1000;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            return new QuantityType<>(
                    Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2() }), 16) * factor, Units.WATT);
        }

        return UnDefType.UNDEF;
    }

    protected State getOperatingHours() {
        if (getParameterMode() == ParameterMode.EIGHT_BIT_DIMMER_VALUE_AND_LAMP_OPERATING_HOURS) {
            return new DecimalType(getDB_2Value() << 8 + getDB_1Value());
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (isErrorState()) {
            return UnDefType.UNDEF;
        }

        switch (channelId) {
            case CHANNEL_GENERAL_SWITCHING:
                return getLightingStatus();
            case CHANNEL_DIMMER:
                return getDimmerStatus();
            case CHANNEL_INSTANTPOWER:
                return getPowerMeasurementData();
            case CHANNEL_TOTALUSAGE:
                State value = getEnergyMeasurementData();
                State currentState = getCurrentStateFunc.apply(channelId);
                return EEPHelper.validateTotalUsage(value, currentState, config);
            case CHANNEL_COUNTER:
                return getOperatingHours();
        }

        return UnDefType.UNDEF;
    }
}
