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
package org.openhab.binding.oceanic.internal;

import java.io.InvalidClassException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Type;

/**
 * The {@link OceanicBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class OceanicBindingConstants {

    public static final String BINDING_ID = "oceanic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID THING_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, "network");

    // List of all Channel ids
    public enum OceanicChannelSelector {

        getSRN("serial", StringType.class, ValueSelectorType.GET, true),
        getMAC("mac", StringType.class, ValueSelectorType.GET, true),
        getDNA("name", StringType.class, ValueSelectorType.GET, true),
        getSCR("type", StringType.class, ValueSelectorType.GET, true) {
            @Override
            public String convertValue(String value) {
                int index = Integer.valueOf(value);
                String convertedValue = value;
                switch (index) {
                    case 0:
                        convertedValue = "Single";
                        break;
                    case 1:
                        convertedValue = "Double Alternative";
                        break;
                    case 2:
                        convertedValue = "Triple Alternative";
                        break;
                    case 3:
                        convertedValue = "Double Parallel";
                        break;
                    case 4:
                        convertedValue = "Triple Parallel";
                        break;
                    case 5:
                        convertedValue = "Single Filter";
                        break;
                    case 6:
                        convertedValue = "Double Filter";
                        break;
                    case 7:
                        convertedValue = "Triple Filter";
                        break;
                    default:
                        break;
                }
                return convertedValue;
            }
        },
        getALM("alarm", StringType.class, ValueSelectorType.GET, false) {
            @Override
            public String convertValue(String value) {
                int index = Integer.valueOf(value);
                String convertedValue = value;
                switch (index) {
                    case 0:
                        convertedValue = "No Alarm";
                        break;
                    case 1:
                        convertedValue = "Lack of salt during regeneration";
                        break;
                    case 2:
                        convertedValue = "Water pressure too low";
                        break;
                    case 3:
                        convertedValue = "Water pressure too high";
                        break;
                    case 4:
                        convertedValue = "Pressure sensor failure";
                        break;
                    case 5:
                        convertedValue = "Camshaft failure";
                        break;
                    default:
                        break;
                }
                return convertedValue;
            }
        },
        getNOT("alert", StringType.class, ValueSelectorType.GET, false) {
            @Override
            public String convertValue(String value) {
                int index = Integer.valueOf(value);
                String convertedValue = value;
                switch (index) {
                    case 0:
                        convertedValue = "No Alert";
                        break;
                    case 1:
                        convertedValue = "Imminent lack of salt";
                        break;
                    default:
                        break;
                }
                return convertedValue;
            }
        },
        getFLO("totalflow", DecimalType.class, ValueSelectorType.GET, false),
        getRES("reserve", DecimalType.class, ValueSelectorType.GET, false),
        getCYN("cycle", StringType.class, ValueSelectorType.GET, false),
        getCYT("endofcycle", StringType.class, ValueSelectorType.GET, false),
        getRTI("endofregeneration", StringType.class, ValueSelectorType.GET, false),
        getWHU("hardnessunit", StringType.class, ValueSelectorType.GET, false) {
            @Override
            public String convertValue(String value) {
                int index = Integer.valueOf(value);
                String convertedValue = value;
                switch (index) {
                    case 0:
                        convertedValue = "dH";
                        break;
                    case 1:
                        convertedValue = "fH";
                        break;
                    case 2:
                        convertedValue = "e";
                        break;
                    case 3:
                        convertedValue = "mg CaCO3/l";
                        break;
                    case 4:
                        convertedValue = "ppm";
                        break;
                    case 5:
                        convertedValue = "mmol/l";
                        break;
                    case 6:
                        convertedValue = "mval/l";
                        break;
                    default:
                        break;
                }
                return convertedValue;
            }
        },
        getIWH("inlethardness", DecimalType.class, ValueSelectorType.GET, false),
        getOWH("outlethardness", DecimalType.class, ValueSelectorType.GET, false),
        getRG1("cylinderstate", StringType.class, ValueSelectorType.GET, false) {
            @Override
            public String convertValue(String value) {
                int index = Integer.valueOf(value);
                String convertedValue = value;
                switch (index) {
                    case 0:
                        convertedValue = "No regeneration";
                        break;
                    case 1:
                        convertedValue = "Paused";
                        break;
                    case 2:
                        convertedValue = "Regeneration";
                        break;
                    default:
                        break;
                }
                return convertedValue;
            }
        },
        setSV1("salt", DecimalType.class, ValueSelectorType.SET, false),
        getSV1("salt", DecimalType.class, ValueSelectorType.GET, false),
        setSIR("regeneratenow", OnOffType.class, ValueSelectorType.SET, false),
        setSDR("regeneratelater", OnOffType.class, ValueSelectorType.SET, false),
        setSMR("multiregenerate", OnOffType.class, ValueSelectorType.SET, false),
        getMOF("consumptionmonday", DecimalType.class, ValueSelectorType.GET, false),
        getTUF("consumptiontuesday", DecimalType.class, ValueSelectorType.GET, false),
        getWEF("consumptionwednesday", DecimalType.class, ValueSelectorType.GET, false),
        getTHF("consumptionthursday", DecimalType.class, ValueSelectorType.GET, false),
        getFRF("consumptionfriday", DecimalType.class, ValueSelectorType.GET, false),
        getSAF("consumptionsaturday", DecimalType.class, ValueSelectorType.GET, false),
        getSUF("consumptionsunday", DecimalType.class, ValueSelectorType.GET, false),
        getTOF("consumptiontoday", DecimalType.class, ValueSelectorType.GET, false),
        getYEF("consumptionyesterday", DecimalType.class, ValueSelectorType.GET, false),
        getCWF("consumptioncurrentweek", DecimalType.class, ValueSelectorType.GET, false),
        getLWF("consumptionlastweek", DecimalType.class, ValueSelectorType.GET, false),
        getCMF("consumptioncurrentmonth", DecimalType.class, ValueSelectorType.GET, false),
        getLMF("consumptionlastmonth", DecimalType.class, ValueSelectorType.GET, false),
        getCOF("consumptioncomplete", DecimalType.class, ValueSelectorType.GET, false),
        getUWF("consumptionuntreated", DecimalType.class, ValueSelectorType.GET, false),
        getTFO("consumptionpeaklevel", DecimalType.class, ValueSelectorType.GET, false),
        getPRS("pressure", DecimalType.class, ValueSelectorType.GET, false),
        getMXP("maxpressure", DecimalType.class, ValueSelectorType.GET, false),
        getMNP("minpressure", DecimalType.class, ValueSelectorType.GET, false),
        getMXF("maxflow", DecimalType.class, ValueSelectorType.GET, false),
        getLAR("lastgeneration", DateTimeType.class, ValueSelectorType.GET, false) {
            @Override
            public String convertValue(String value) {
                final SimpleDateFormat inDateFormatter = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                final SimpleDateFormat outDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                try {
                    Date date = inDateFormatter.parse(value);
                    return outDateFormatter.format(date);
                } catch (ParseException fpe) {
                    throw new IllegalArgumentException(value + " is not in a valid format.", fpe);
                }
            }
        },
        getNOR("normalregenerations", DecimalType.class, ValueSelectorType.GET, false),
        getSRE("serviceregenerations", DecimalType.class, ValueSelectorType.GET, false),
        getINR("incompleteregenerations", DecimalType.class, ValueSelectorType.GET, false),
        getTOR("allregenerations", DecimalType.class, ValueSelectorType.GET, false);

        private final String text;
        private Class<? extends Type> typeClass;
        private ValueSelectorType typeValue;
        private boolean isProperty;

        private OceanicChannelSelector(final String text, Class<? extends Type> typeClass, ValueSelectorType typeValue,
                boolean isProperty) {
            this.text = text;
            this.typeClass = typeClass;
            this.typeValue = typeValue;
            this.isProperty = isProperty;
        }

        @Override
        public String toString() {
            return text;
        }

        public Class<? extends Type> getTypeClass() {
            return typeClass;
        }

        public ValueSelectorType getTypeValue() {
            return typeValue;
        }

        public boolean isProperty() {
            return isProperty;
        }

        /**
         * Procedure to convert selector string to value selector class.
         *
         * @param valueSelectorText selector string e.g. RawData, Command, Temperature
         * @return corresponding selector value.
         * @throws InvalidClassException Not valid class for value selector.
         */
        public static OceanicChannelSelector getValueSelector(String valueSelectorText,
                ValueSelectorType valueSelectorType) throws IllegalArgumentException {
            for (OceanicChannelSelector c : OceanicChannelSelector.values()) {
                if (c.text.equals(valueSelectorText) && c.typeValue == valueSelectorType) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }

        public static ValueSelectorType getValueSelectorType(String valueSelectorText) throws IllegalArgumentException {
            for (OceanicChannelSelector c : OceanicChannelSelector.values()) {
                if (c.text.equals(valueSelectorText)) {
                    return c.typeValue;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }

        public String convertValue(String value) {
            return value;
        }

        public enum ValueSelectorType {
            GET,
            SET
        }
    }
}
