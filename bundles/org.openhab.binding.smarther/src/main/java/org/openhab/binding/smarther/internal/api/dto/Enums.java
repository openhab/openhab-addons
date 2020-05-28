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
package org.openhab.binding.smarther.internal.api.dto;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.smarther.internal.api.exception.SmartherInvalidPropertyValueException;

/**
 * Container class for enums related to Smarther API
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class Enums {

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

        public static Function fromValue(String value) {
            return lookup(Function.class, value);
        }
    }

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

        public static Mode fromValue(String value) {
            return lookup(Mode.class, value);
        }
    }

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

        public boolean isActive() {
            return ACTIVE.getValue().equals(value);
        }

        public static LoadState fromValue(String value) {
            return lookup(LoadState.class, value);
        }
    }

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

        public static MeasureUnit fromUnit(Unit<?> unit) {
            if (unit == SIUnits.CELSIUS) {
                return CELSIUS;
            } else if (unit == ImperialUnits.FAHRENHEIT) {
                return FAHRENHEIT;
            } else if (unit == SmartHomeUnits.PERCENT) {
                return PERCENTAGE;
            } else {
                return DIMENSIONLESS;
            }
        }

        public static MeasureUnit fromValue(String value) {
            return lookup(MeasureUnit.class, value);
        }
    }

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

        public static BoostTime fromValue(int value) {
            return lookup(BoostTime.class, value);
        }
    }

    // ------------------------------
    // UTILITY INTERFACES AND METHODS
    // ------------------------------

    interface TypeWithIntProperty {
        int getValue();
    }

    public static <E extends Enum<E> & TypeWithIntProperty> E lookup(Class<E> en, int value) {
        for (E constant : en.getEnumConstants()) {
            if (constant.getValue() == value) {
                return constant;
            }
        }
        throw new SmartherInvalidPropertyValueException(en.getSimpleName(), String.valueOf(value));
    }

    interface TypeWithStringProperty {
        String getValue();
    }

    public static <E extends Enum<E> & TypeWithStringProperty> E lookup(Class<E> en, String value) {
        for (E constant : en.getEnumConstants()) {
            if (constant.getValue().equals(value)) {
                return constant;
            }
        }
        throw new SmartherInvalidPropertyValueException(en.getSimpleName(), value);
    }

}
