/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.List;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.FanLincFanSpeed;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatFanMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.ThermostatSystemMode;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.VenstarSystemMode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link OnLevel} represents on level format functions for Insteon products
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class OnLevel {
    /**
     * Returns an on level string as a hex value based on a feature type
     *
     * @param string the on level string to use
     * @param featureType the feature type
     * @return the on level hex value if valid, otherwise -1
     */
    public static int getHexValue(String string, String featureType) {
        try {
            switch (featureType) {
                case FEATURE_TYPE_GENERIC_DIMMER:
                    int level = Integer.parseInt(string);
                    return level >= 0 && level <= 100 ? (int) Math.round(level * 255 / 100.0) : -1;
                case FEATURE_TYPE_GENERIC_SWITCH:
                case FEATURE_TYPE_OUTLET_SWITCH:
                case FEATURE_TYPE_KEYPAD_BUTTON:
                    return "OFF".equals(string) ? 0x00 : "ON".equals(string) ? 0xFF : -1;
                case FEATURE_TYPE_FANLINC_FAN:
                    return FanLincFanSpeed.valueOf(string).getValue();
                case FEATURE_TYPE_THERMOSTAT_COOL_SETPOINT:
                case FEATURE_TYPE_THERMOSTAT_HEAT_SETPOINT:
                case FEATURE_TYPE_VENSTAR_COOL_SETPOINT:
                case FEATURE_TYPE_VENSTAR_HEAT_SETPOINT:
                    double temperature = Double.parseDouble(string);
                    return temperature >= 0 && temperature <= 127.5 ? (int) Math.round(temperature * 2) : -1;
                case FEATURE_TYPE_THERMOSTAT_FAN_MODE:
                case FEATURE_TYPE_VENSTAR_FAN_MODE:
                    return ThermostatFanMode.valueOf(string).getValue();
                case FEATURE_TYPE_THERMOSTAT_SYSTEM_MODE:
                    return ThermostatSystemMode.valueOf(string).getValue();
                case FEATURE_TYPE_VENSTAR_SYSTEM_MODE:
                    return VenstarSystemMode.valueOf(string).getValue();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return -1;
    }

    /**
     * Returns an on level value as a state based on a feature type
     *
     * @param value the on level value to use
     * @param featureType the feature type
     * @return the on level state
     */
    public static State getState(int value, String featureType) {
        try {
            switch (featureType) {
                case FEATURE_TYPE_GENERIC_DIMMER:
                    return new PercentType((int) Math.round(value * 100 / 255.0));
                case FEATURE_TYPE_GENERIC_SWITCH:
                case FEATURE_TYPE_OUTLET_SWITCH:
                case FEATURE_TYPE_KEYPAD_BUTTON:
                    return OnOffType.from(value != 0x00);
                case FEATURE_TYPE_FANLINC_FAN:
                    return new StringType(FanLincFanSpeed.valueOf(value).toString());
                case FEATURE_TYPE_THERMOSTAT_COOL_SETPOINT:
                case FEATURE_TYPE_THERMOSTAT_HEAT_SETPOINT:
                case FEATURE_TYPE_VENSTAR_COOL_SETPOINT:
                case FEATURE_TYPE_VENSTAR_HEAT_SETPOINT:
                    return new QuantityType<Temperature>(Math.round(value * 0.5), ImperialUnits.FAHRENHEIT);
                case FEATURE_TYPE_THERMOSTAT_FAN_MODE:
                case FEATURE_TYPE_VENSTAR_FAN_MODE:
                    return new StringType(ThermostatFanMode.valueOf(value).toString());
                case FEATURE_TYPE_THERMOSTAT_SYSTEM_MODE:
                    return new StringType(ThermostatSystemMode.valueOf(value).toString());
                case FEATURE_TYPE_VENSTAR_SYSTEM_MODE:
                    return new StringType(VenstarSystemMode.valueOf(value).toString());
            }
        } catch (IllegalArgumentException ignored) {
        }
        return UnDefType.NULL;
    }

    /**
     * Returns a list of supported on level values based on a feature type
     *
     * @param featureType the feature type
     * @return the list of on level values
     */
    public static List<String> getSupportedValues(String featureType) {
        switch (featureType) {
            case FEATURE_TYPE_GENERIC_DIMMER:
                return List.of("0", "25", "50", "75", "100");
            case FEATURE_TYPE_GENERIC_SWITCH:
            case FEATURE_TYPE_OUTLET_SWITCH:
            case FEATURE_TYPE_KEYPAD_BUTTON:
                return List.of("ON", "OFF");
            case FEATURE_TYPE_FANLINC_FAN:
                return FanLincFanSpeed.names();
            case FEATURE_TYPE_THERMOSTAT_FAN_MODE:
            case FEATURE_TYPE_VENSTAR_FAN_MODE:
                return ThermostatFanMode.names();
            case FEATURE_TYPE_THERMOSTAT_SYSTEM_MODE:
                return ThermostatSystemMode.names();
            case FEATURE_TYPE_VENSTAR_SYSTEM_MODE:
                return VenstarSystemMode.names();
        }
        return List.of();
    }
}
