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

import org.eclipse.smarthome.config.core.Configuration;
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

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Hashtable<String, State> currentState, Configuration config) {

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
