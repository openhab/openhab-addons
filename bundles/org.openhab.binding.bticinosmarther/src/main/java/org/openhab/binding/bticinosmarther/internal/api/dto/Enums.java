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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherIllegalPropertyValueException;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@code Enums} class represents a container for enums related to Smarther API.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class Enums {

    /**
     * The {@code Function} enum maps the values of chronothermostat operation function.
     */
    public enum Function implements TypeWithStringProperty {
        HEATING("HEATING"),
        COOLING("COOLING");

        private final String value;

        Function(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        /**
         * Returns a {@code Function} enum value from the given raw value.
         *
         * @param value
         *            the raw value to get an enum value from
         *
         * @return the enum value representing the given raw value
         *
         * @throws SmartherIllegalPropertyValueException if the raw value cannot be mapped to any valid enum value
         */
        public static Function fromValue(String value) throws SmartherIllegalPropertyValueException {
            return lookup(Function.class, value);
        }
    }

    /**
     * The {@code Mode} enum maps the values of chronothermostat operation mode.
     */
    public enum Mode implements TypeWithStringProperty {
        AUTOMATIC("AUTOMATIC"),
        MANUAL("MANUAL"),
        BOOST("BOOST"),
        OFF("OFF"),
        PROTECTION("PROTECTION");

        private final String value;

        Mode(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        /**
         * Returns a {@code Mode} enum value from the given raw value.
         *
         * @param value
         *            the raw value to get an enum value from
         *
         * @return the enum value representing the given raw value
         *
         * @throws SmartherIllegalPropertyValueException if the raw value cannot be mapped to any valid enum value
         */
        public static Mode fromValue(String value) throws SmartherIllegalPropertyValueException {
            return lookup(Mode.class, value);
        }
    }

    /**
     * The {@code LoadState} enum maps the values of chronothermostat operation load state.
     */
    public enum LoadState implements TypeWithStringProperty {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE");

        private final String value;

        LoadState(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        /**
         * Tells whether the load state value is "active".
         *
         * @return {@code true} if the load state value is "active", {@code false} otherwise
         */
        public boolean isActive() {
            return ACTIVE.getValue().equals(value);
        }

        /**
         * Returns a {@code LoadState} enum value from the given raw value.
         *
         * @param value
         *            the raw value to get an enum value from
         *
         * @return the enum value representing the given raw value
         *
         * @throws SmartherIllegalPropertyValueException if the raw value cannot be mapped to any valid enum value
         */
        public static LoadState fromValue(String value) throws SmartherIllegalPropertyValueException {
            return lookup(LoadState.class, value);
        }
    }

    /**
     * The {@code MeasureUnit} enum maps the values of managed measure unit.
     */
    public enum MeasureUnit implements TypeWithStringProperty {
        CELSIUS("C"),
        FAHRENHEIT("F"),
        PERCENTAGE("%"),
        DIMENSIONLESS("");

        private final String value;

        MeasureUnit(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        /**
         * Returns a {@code MeasureUnit} enum value for the given measure {@link Unit}.
         *
         * @param unit
         *            the measure unit to get an enum value for
         *
         * @return the enum value representing the given measure unit
         */
        public static MeasureUnit fromUnit(Unit<?> unit) {
            if (SIUnits.CELSIUS.equals(unit)) {
                return CELSIUS;
            } else if (ImperialUnits.FAHRENHEIT.equals(unit)) {
                return FAHRENHEIT;
            } else if (Units.PERCENT.equals(unit)) {
                return PERCENTAGE;
            } else {
                return DIMENSIONLESS;
            }
        }

        /**
         * Returns a {@code MeasureUnit} enum value from the given raw value.
         *
         * @param value
         *            the raw value to get an enum value from
         *
         * @return the enum value representing the given raw value
         *
         * @throws SmartherIllegalPropertyValueException if the raw value cannot be mapped to any valid enum value
         */
        public static MeasureUnit fromValue(String value) throws SmartherIllegalPropertyValueException {
            return lookup(MeasureUnit.class, value);
        }
    }

    /**
     * The {@code BoostTime} enum maps the time values of chronothermostat boost mode.
     */
    public enum BoostTime implements TypeWithIntProperty {
        MINUTES_30(30),
        MINUTES_60(60),
        MINUTES_90(90);

        private final int value;

        BoostTime(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }

        /**
         * Returns a {@code BoostTime} enum value from the given raw value.
         *
         * @param value
         *            the raw value to get an enum value from
         *
         * @return the enum value representing the given raw value
         *
         * @throws SmartherIllegalPropertyValueException if the raw value cannot be mapped to any valid enum value
         */
        public static BoostTime fromValue(int value) throws SmartherIllegalPropertyValueException {
            return lookup(BoostTime.class, value);
        }
    }

    // ------------------------------
    // UTILITY INTERFACES AND METHODS
    // ------------------------------

    interface TypeWithIntProperty {
        int getValue();
    }

    public static <E extends Enum<E> & TypeWithIntProperty> E lookup(Class<E> en, int value)
            throws SmartherIllegalPropertyValueException {
        E[] constants = en.getEnumConstants();
        if (constants != null) {
            for (E constant : constants) {
                if (constant.getValue() == value) {
                    return constant;
                }
            }
        }
        throw new SmartherIllegalPropertyValueException(en.getSimpleName(), String.valueOf(value));
    }

    interface TypeWithStringProperty {
        String getValue();
    }

    public static <E extends Enum<E> & TypeWithStringProperty> E lookup(Class<E> en, String value)
            throws SmartherIllegalPropertyValueException {
        E[] constants = en.getEnumConstants();
        if (constants != null) {
            for (E constant : constants) {
                if (constant.getValue().equals(value)) {
                    return constant;
                }
            }
        }
        throw new SmartherIllegalPropertyValueException(en.getSimpleName(), value);
    }
}
