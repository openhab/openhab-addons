/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Hashtable;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 * Heating radiator valve actuating drive with feed and room temperature measurement, local set point control and
 * display
 *
 * @author Dominik Vorreiter - Initial contribution
 */
public class A5_20_04 extends _4BSMessage {

    public A5_20_04(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_STATUS_REQUEST_EVENT:
                return getStatusRequestEvent();
        }

        return null;
    }

    private String getStatusRequestEvent() {
        return getBit(getDB_0Value(), 6) ? "triggered" : null;
    }

    private byte getPos(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_VALVE_POSITION);

        if ((current != null) && (current instanceof DecimalType)) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return state.byteValue();
            }
        }

        return 50; // 50 %
    }

    private byte getTsp(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_TEMPERATURE_SETPOINT);

        int value = 20; // 20 °C

        if ((current != null) && (current instanceof DecimalType)) {
            QuantityType<Temperature> raw = current.as(QuantityType.class);

            if (raw != null) {
                QuantityType<Temperature> celsius = raw.toUnit(SIUnits.CELSIUS);

                if (celsius != null) {
                    value = celsius.intValue();
                }
            }
        }

        return (byte) ((value - 10) * (255.0 / 20.0));
    }

    private byte getMc(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_MEASUREMENT_CONTROL);

        if ((current != null) && (current instanceof OnOffType)) {
            OnOffType state = current.as(OnOffType.class);

            if (state != null) {
                return (byte) (state.equals(OnOffType.ON) ? 0x40 : 0x00);
            }
        }

        return 0x40; // on
    }

    private byte getWuc(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_WAKEUPCYCLE);

        if ((current != null) && (current instanceof DecimalType)) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (state.byteValue() & 0x3F);
            }
        }

        return 0x13; // 19 = 600 sec = 10 min
    }

    private byte getDso(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_DISPLAY_ORIENTATION);

        if ((current != null) && (current instanceof DecimalType)) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (((state.byteValue() / 90) << 4) & 0x30);
            }
        }

        return 0x00; // 0°
    }

    private byte getBlc(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_BUTTON_LOCK);

        if ((current != null) && (current instanceof OnOffType)) {
            OnOffType state = current.as(OnOffType.class);

            if (state != null) {
                return (byte) (state.equals(OnOffType.ON) ? 0x04 : 0x00);
            }
        }

        return 0x00; // off
    }

    private byte getSer(Hashtable<String, State> currentState) {
        State current = currentState.get(CHANNEL_SERVICECOMMAND);

        if ((current != null) && (current instanceof DecimalType)) {
            DecimalType state = current.as(DecimalType.class);

            if (state != null) {
                return (byte) (state.byteValue() & 0x03);
            }
        }

        return 0x00; // 0 = no change
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Hashtable<String, State> currentState, Configuration config) {

        if (CHANNEL_SEND_COMMAND.equals(channelId) && (command.equals(OnOffType.ON))) {
            byte db3 = getPos(currentState);
            byte db2 = getTsp(currentState);
            byte db1 = (byte) (0x00 | getMc(currentState) | getWuc(currentState));
            byte db0 = (byte) (0x00 | getDso(currentState) | TeachInBit | getBlc(currentState) | getSer(currentState));

            setData(db3, db2, db1, db0);

            return;
        }

        if (command instanceof State) {
            currentState.put(channelId, (State) command);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {

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
        boolean fl = getBit(getDB_0Value(), 0);

        if (fl) {
            return UnDefType.UNDEF;
        }

        double value = getDB_1Value() * (20.0 / 255.0) + 10.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getFailureCode() {
        boolean fl = getBit(getDB_0Value(), 0);

        if (!fl) {
            return new QuantityType<>(-1, SmartHomeUnits.ONE);
        }

        return new QuantityType<>(getDB_1Value(), SmartHomeUnits.ONE);
    }

    private State getMeasurementControl() {
        return getBit(getDB_0Value(), 7) ? OnOffType.OFF : OnOffType.ON;
    }

    private State getFeedTemperature() {
        boolean ts = getBit(getDB_0Value(), 1);

        if (ts) {
            return UnDefType.UNDEF;
        }

        double value = getDB_2Value() * (60.0 / 255.0) + 20.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getTemperatureSetpoint() {
        boolean ts = getBit(getDB_0Value(), 1);

        if (!ts) {
            return UnDefType.UNDEF;
        }

        double value = getDB_2Value() * (20.0 / 255.0) + 10.0;

        return new QuantityType<>(value, SIUnits.CELSIUS);
    }

    private State getButtonLock() {
        return getBit(getDB_0Value(), 2) ? OnOffType.ON : OnOffType.OFF;
    }

    private State getValvePosition() {
        return new QuantityType<>(getDB_3Value(), SmartHomeUnits.PERCENT);
    }
}
