/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link ApplianceChannelSelector} for ovens
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 */
public enum OvenChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    BRAND_ID("brandId", "brandId", StringType.class, true),
    COMPANY_ID("companyId", "companyId", StringType.class, true),
    STATE("state", "state", StringType.class, false),
    PROGRAMID("programId", "program", StringType.class, false),
    PROGRAMPHASE("phase", "phase", StringType.class, false),
    START_TIME("startTime", "start", DateTimeType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(DATE_FORMATTER.format(date));
        }
    },
    DURATION("duration", "duration", DateTimeType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(DATE_FORMATTER.format(date));
        }
    },
    ELAPSED_TIME("elapsedTime", "elapsed", DateTimeType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(DATE_FORMATTER.format(date));
        }
    },
    FINISH_TIME("finishTime", "finish", DateTimeType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            Date date = new Date();
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(DATE_FORMATTER.format(date));
        }
    },
    TARGET_TEMP("targetTemperature", "target", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    MEASURED_TEMP("measuredTemperature", "measured", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    DEVICE_TEMP_ONE("deviceTemperature1", "temp1", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    DEVICE_TEMP_TWO("deviceTemperature2", "temp2", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false) {
        @Override

        public State getState(String s, DeviceMetaData dmd) {
            if (s.equals("true")) {
                return getState("OPEN");
            }

            if (s.equals("false")) {
                return getState("CLOSED");
            }

            return UnDefType.UNDEF;
        }
    },
    STOP(null, "stop", OnOffType.class, false),
    SWITCH(null, "switch", OnOffType.class, false);

    private final Logger logger = LoggerFactory.getLogger(OvenChannelSelector.class);

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    OvenChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
            boolean isProperty) {
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
    public Class<? extends Type> getTypeClass() {
        return typeClass;
    }

    @Override
    public boolean isProperty() {
        return isProperty;
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
