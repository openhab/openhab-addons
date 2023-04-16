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
package org.openhab.binding.enocean.internal.eep.A5_20;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Heating radiator valve actuating drive with feed and room temperature measurement, local set point control and
 * display
 *
 * @author Dominik Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_20_04 extends A5_20 {

    public A5_20_04() {
        super();
    }

    public A5_20_04(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected @Nullable String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_STATUS_REQUEST_EVENT:
                return getStatusRequestEvent();
        }

        return null;
    }

    private String getStatusRequestEvent() {
        return Boolean.valueOf(getBit(getDB0Value(), 6)).toString();
        // return getBit(getDB_0Value(), 6) ? "triggered" : null;
    }

    private byte getPos(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_VALVE_POSITION);

        if (current instanceof DecimalType) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return state.byteValue();
            }
        }

        return 25; // 25 %
    }

    private byte getTsp(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_TEMPERATURE_SETPOINT);

        double value = 20.0; // 20 °C

        if (current instanceof QuantityType) {
            @SuppressWarnings("unchecked")
            QuantityType<Temperature> raw = current.as(QuantityType.class);

            if (raw != null) {
                QuantityType<Temperature> celsius = raw.toUnit(SIUnits.CELSIUS);

                if (celsius != null) {
                    value = celsius.doubleValue();
                }
            }
        }

        return (byte) ((value - 10.0) * (255.0 / 20.0));
    }

    private byte getMc(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_MEASUREMENT_CONTROL);

        if (current instanceof OnOffType) {
            OnOffType state = current.as(OnOffType.class);

            if (state != null) {
                return (byte) (state.equals(OnOffType.ON) ? 0x00 : 0x40);
            }
        }

        return 0x00; // on
    }

    private byte getWuc(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_WAKEUPCYCLE);

        if (current instanceof DecimalType) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (state.byteValue() & 0x3F);
            }
        }

        return 0x13; // 19 = 600 sec = 10 min
    }

    private byte getDso(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_DISPLAY_ORIENTATION);

        if (current instanceof DecimalType) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (((state.byteValue() / 90) << 4) & 0x30);
            }
        }

        return 0x00; // 0°
    }

    private byte getBlc(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_BUTTON_LOCK);

        if (current instanceof OnOffType) {
            OnOffType state = current.as(OnOffType.class);

            if (state != null) {
                return (byte) (state.equals(OnOffType.ON) ? 0x04 : 0x00);
            }
        }

        return 0x00; // unlocked
    }

    private byte getSer(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_SERVICECOMMAND);

        if (current instanceof DecimalType) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (state.byteValue() & 0x03);
            }
        }

        return 0x00; // 0 = no change
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (VIRTUALCHANNEL_SEND_COMMAND.equals(channelId)) {
            byte db3 = getPos(getCurrentStateFunc);
            byte db2 = getTsp(getCurrentStateFunc);
            byte db1 = (byte) (0x00 | getMc(getCurrentStateFunc) | getWuc(getCurrentStateFunc));
            byte db0 = (byte) (0x00 | getDso(getCurrentStateFunc) | TEACHIN_BIT | getBlc(getCurrentStateFunc)
                    | getSer(getCurrentStateFunc));

            setData(db3, db2, db1, db0);

            return;
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_VALVE_POSITION:
                return getValvePosition();
            case CHANNEL_BUTTON_LOCK:
                return getButtonLock();
            case CHANNEL_TEMPERATURE_SETPOINT:
                return getTemperatureSetpoint();
            case CHANNEL_TEMPERATURE:
                return getTemperature();
            case CHANNEL_FEED_TEMPERATURE:
                return getFeedTemperature();
            case CHANNEL_MEASUREMENT_CONTROL:
                return getMeasurementControl();
            case CHANNEL_FAILURE_CODE:
                return getFailureCode();
        }

        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        boolean fl = getBit(getDB0Value(), 0);
        boolean mst = getBit(getDB0Value(), 7);

        if (fl || mst) {
            return UnDefType.UNDEF;
        }

        double value = getDB1Value() * (20.0 / 255.0) + 10.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getFailureCode() {
        boolean fl = getBit(getDB0Value(), 0);

        if (!fl) {
            return new QuantityType<>(-1, Units.ONE);
        }

        return new QuantityType<>(getDB1Value(), Units.ONE);
    }

    private State getMeasurementControl() {
        return getBit(getDB0Value(), 7) ? OnOffType.OFF : OnOffType.ON;
    }

    private State getFeedTemperature() {
        boolean ts = getBit(getDB0Value(), 1);
        boolean mst = getBit(getDB0Value(), 7);

        if (ts || mst) {
            return UnDefType.UNDEF;
        }

        double value = getDB2Value() * (60.0 / 255.0) + 20.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getTemperatureSetpoint() {
        boolean ts = getBit(getDB0Value(), 1);

        if (!ts) {
            return UnDefType.UNDEF;
        }

        double value = getDB2Value() * (20.0 / 255.0) + 10.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getButtonLock() {
        return getBit(getDB0Value(), 2) ? OnOffType.ON : OnOffType.OFF;
    }

    private State getValvePosition() {
        return new QuantityType<>(getDB3Value(), Units.PERCENT);
    }
}
