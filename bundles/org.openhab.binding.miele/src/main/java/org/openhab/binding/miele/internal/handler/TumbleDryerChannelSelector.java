/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.TimeZone;

import org.openhab.binding.miele.internal.DeviceMetaData;
import org.openhab.binding.miele.internal.DeviceUtil;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApplianceChannelSelector} for tumble dryers
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 * @author Jacob Laursen - Added raw channels
 */
public enum TumbleDryerChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            State state = DeviceUtil.getStateTextState(s, dmd, translationProvider);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd, translationProvider);
        }
    },
    STATE(null, STATE_CHANNEL_ID, DecimalType.class, false),
    PROGRAM_TEXT(PROGRAM_ID_PROPERTY_NAME, PROGRAM_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            State state = DeviceUtil.getTextState(s, dmd, translationProvider, programs, MISSING_PROGRAM_TEXT_PREFIX,
                    MIELE_TUMBLE_DRYER_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd, translationProvider);
        }
    },
    PROGRAM(null, PROGRAM_CHANNEL_ID, DecimalType.class, false),
    PROGRAMTYPE("programType", "type", StringType.class, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            State state = DeviceUtil.getTextState(s, dmd, translationProvider, phases, MISSING_PHASE_TEXT_PREFIX,
                    MIELE_TUMBLE_DRYER_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd, translationProvider);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false),
    START_TIME("startTime", "start", DateTimeType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
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
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
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
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
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
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
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
    DRYING_STEP("dryingStep", "step", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false) {
        @Override

        public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            if ("true".equals(s)) {
                return getState("OPEN");
            }

            if ("false".equals(s)) {
                return getState("CLOSED");
            }

            return UnDefType.UNDEF;
        }
    },
    SWITCH(null, "switch", OnOffType.class, false);

    private final Logger logger = LoggerFactory.getLogger(TumbleDryerChannelSelector.class);

    private static final Map<String, String> programs = Map.ofEntries(entry("10", "automatic-plus"),
            entry("20", "cottons"), entry("23", "cottons-hygiene"), entry("30", "minimum-iron"),
            entry("31", "gentle-minimum-iron"), entry("40", "woollens-handcare"), entry("50", "delicates"),
            entry("60", "warm-air"), entry("70", "cool-air"), entry("80", "express"), entry("90", "cottons-eco"),
            entry("100", "gentle-smoothing"), entry("120", "proofing"), entry("130", "denim"),
            entry("131", "gentle-denim"), entry("140", "shirts"), entry("141", "gentle-shirts"),
            entry("150", "sportswear"), entry("160", "outerwear"), entry("170", "silks-handcare"),
            entry("190", "standard-pillows"), entry("220", "basket-programme"), entry("240", "smoothing"),
            entry("65000", "cottons-auto-load-control"), entry("65001", "minimum-iron-auto-load-control"));

    private static final Map<String, String> phases = Map.ofEntries(entry("1", "programme-running"),
            entry("2", "drying"), entry("3", "drying-machine-iron"), entry("4", "drying-hand-iron"),
            entry("5", "drying-normal"), entry("6", "drying-normal-plus"), entry("7", "cooling-down"),
            entry("8", "drying-hand-iron"), entry("10", "finished"));

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    TumbleDryerChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
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
    public boolean isProperty() {
        return isProperty;
    }

    @Override
    public boolean isExtendedState() {
        return false;
    }

    @Override
    public State getState(String s, DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
        return this.getState(s, dmd);
    }

    @Override
    public State getState(String s, DeviceMetaData dmd) {
        if (dmd != null) {
            String localizedValue = dmd.getMieleEnum(s);
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
}
