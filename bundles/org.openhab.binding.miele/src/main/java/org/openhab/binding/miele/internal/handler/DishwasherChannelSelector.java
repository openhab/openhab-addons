/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal.handler;

import static java.util.Map.entry;
import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceMetaData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link ApplianceChannelSelector} for dishwashers
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 * @author Jacob Laursen - Added power/water consumption channels, raw channels
 */
public enum DishwasherChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true, false),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true, false),
    BRAND_ID("brandId", "brandId", StringType.class, true, false),
    COMPANY_ID("companyId", "companyId", StringType.class, true, false),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false, false),
    STATE(null, STATE_CHANNEL_ID, DecimalType.class, false, false),
    PROGRAM_TEXT(PROGRAM_ID_PROPERTY_NAME, PROGRAM_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            State state = getTextState(s, dmd, programs, MISSING_PROGRAM_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd);
        }
    },
    PROGRAM(null, PROGRAM_CHANNEL_ID, DecimalType.class, false, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            State state = getTextState(s, dmd, phases, MISSING_PHASE_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false, false),
    START_TIME("startTime", "start", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(dateFormatter.format(date));
        }
    },
    DURATION("duration", "duration", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(dateFormatter.format(date));
        }
    },
    ELAPSED_TIME("elapsedTime", "elapsed", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(dateFormatter.format(date));
        }
    },
    FINISH_TIME("finishTime", "finish", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(dateFormatter.format(date));
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            if ("true".equals(s)) {
                return getState("OPEN");
            }

            if ("false".equals(s)) {
                return getState("CLOSED");
            }

            return UnDefType.UNDEF;
        }
    },
    SWITCH(null, "switch", OnOffType.class, false, false),
    POWER_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, POWER_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true),
    WATER_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, WATER_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true);

    private final Logger logger = LoggerFactory.getLogger(DishwasherChannelSelector.class);

    private final static Map<String, String> programs = Map.ofEntries(entry("26", "Pots & Pans"),
            entry("27", "Clean Machine"), entry("28", "Economy"), entry("30", "Normal"), entry("32", "Sensor Wash"),
            entry("34", "Energy Saver"), entry("35", "China & Crystal"), entry("36", "Extra Quiet"),
            entry("37", "SaniWash"), entry("38", "QuickPowerWash"), entry("42", "Tall items"));

    private final static Map<String, String> phases = Map.ofEntries(entry("2", "Pre-Wash"), entry("3", "Main Wash"),
            entry("4", "Rinses"), entry("6", "Final rinse"), entry("7", "Drying"), entry("8", "Finished"));

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;
    private final boolean isExtendedState;

    DishwasherChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass, boolean isProperty,
            boolean isExtendedState) {
        this.mieleID = propertyID;
        this.channelID = channelID;
        this.typeClass = typeClass;
        this.isProperty = isProperty;
        this.isExtendedState = isExtendedState;
    }

    @Override
    public String toString() {
        return mieleID;
    }

    @Override
    public String getMieleID() {
        return mieleID;
    }

    @Override
    public String getChannelID() {
        return channelID;
    }

    @Override
    public boolean isProperty() {
        return isProperty;
    }

    @Override
    public boolean isExtendedState() {
        return isExtendedState;
    }

    @Override
    public State getState(String s, DeviceMetaData dmd) {
        if (dmd != null) {
            String localizedValue = getMieleEnum(s, dmd);
            if (localizedValue == null) {
                localizedValue = dmd.LocalizedValue;
            }
            if (localizedValue == null) {
                localizedValue = s;
            }

            return getState(localizedValue);
        } else {
            return getState(s);
        }
    }

    public State getState(String s) {
        try {
            Method valueOf = typeClass.getMethod("valueOf", String.class);
            State state = (State) valueOf.invoke(typeClass, s);
            if (state != null) {
                return state;
            }
        } catch (Exception e) {
            logger.error("An exception occurred while converting '{}' into a State", s);
        }

        return null;
    }

    public State getTextState(String s, DeviceMetaData dmd, Map<String, String> valueMap, String prefix) {
        if ("0".equals(s)) {
            return UnDefType.UNDEF;
        }

        if (dmd == null || dmd.LocalizedValue == null || dmd.LocalizedValue.startsWith(prefix)) {
            String text = valueMap.get(s);
            if (text != null) {
                return getState(text);
            }
            if (dmd == null || dmd.LocalizedValue == null) {
                return getState(prefix + s);
            }
        }

        return null;
    }

    public String getMieleEnum(String s, DeviceMetaData dmd) {
        if (dmd.MieleEnum != null) {
            for (Entry<String, JsonElement> enumEntry : dmd.MieleEnum.entrySet()) {
                if (enumEntry.getValue().getAsString().trim().equals(s.trim())) {
                    return enumEntry.getKey();
                }
            }
        }

        return null;
    }
}
