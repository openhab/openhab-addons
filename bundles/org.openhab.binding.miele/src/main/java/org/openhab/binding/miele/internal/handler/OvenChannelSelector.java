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

import org.openhab.binding.miele.internal.ExtendedDeviceStateUtil;
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
 * The {@link ApplianceChannelSelector} for ovens
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 * @author Jacob Laursen - Added UoM for temperatures, raw channels
 */
public enum OvenChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    BRAND_ID("brandId", "brandId", StringType.class, true),
    COMPANY_ID("companyId", "companyId", StringType.class, true),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false),
    STATE(null, STATE_CHANNEL_ID, DecimalType.class, false),
    PROGRAM_TEXT(PROGRAM_ID_PROPERTY_NAME, PROGRAM_TEXT_CHANNEL_ID, StringType.class, false),
    PROGRAM(null, PROGRAM_CHANNEL_ID, DecimalType.class, false),
    PROGRAMTYPE("programType", "type", StringType.class, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            State state = getTextState(s, dmd, phases, MISSING_PHASE_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false),
    START_TIME("startTime", "start", DateTimeType.class, false) {
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
    DURATION("duration", "duration", DateTimeType.class, false) {
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
    ELAPSED_TIME("elapsedTime", "elapsed", DateTimeType.class, false) {
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
    FINISH_TIME("finishTime", "finish", DateTimeType.class, false) {
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
    TARGET_TEMP("targetTemperature", "target", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    MEASURED_TEMP("measuredTemperature", "measured", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    DEVICE_TEMP_ONE("deviceTemperature1", "temp1", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    DEVICE_TEMP_TWO("deviceTemperature2", "temp2", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false) {
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
    STOP(null, "stop", OnOffType.class, false),
    SWITCH(null, "switch", OnOffType.class, false);

    private final Logger logger = LoggerFactory.getLogger(OvenChannelSelector.class);

    private final static Map<String, String> phases = Map.ofEntries(entry("1", "Heating"), entry("2", "Temp. hold"),
            entry("3", "Door Open"), entry("4", "Pyrolysis"), entry("7", "Lighting"), entry("8", "Searing phase"),
            entry("10", "Defrost"), entry("11", "Cooling down"), entry("12", "Energy save phase"));

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    OvenChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass, boolean isProperty) {
        this.mieleID = propertyID;
        this.channelID = channelID;
        this.typeClass = typeClass;
        this.isProperty = isProperty;
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
        return false;
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

    public State getTemperatureState(String s) {
        try {
            return ExtendedDeviceStateUtil.getTemperatureState(s);
        } catch (NumberFormatException e) {
            logger.warn("An exception occurred while converting '{}' into a State", s);
            return UnDefType.UNDEF;
        }
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
