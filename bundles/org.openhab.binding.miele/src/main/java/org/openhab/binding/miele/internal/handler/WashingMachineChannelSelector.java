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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.DeviceUtil;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
import org.openhab.binding.miele.internal.api.dto.DeviceMetaData;
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

/**
 * The {@link ApplianceChannelSelector} for washing machines
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 * @author Jacob Laursen - Added power/water consumption channels, UoM for temperatures, raw channels
 */
@NonNullByDefault
public enum WashingMachineChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true, false),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true, false),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getStateTextState(s, dmd, translationProvider);
        }
    },
    STATE("", STATE_CHANNEL_ID, DecimalType.class, false, false),
    PROGRAM_TEXT(PROGRAM_ID_PROPERTY_NAME, PROGRAM_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getTextState(s, dmd, translationProvider, PROGRAMS, MISSING_PROGRAM_TEXT_PREFIX,
                    MIELE_WASHING_MACHINE_TEXT_PREFIX);
        }
    },
    PROGRAM("", PROGRAM_CHANNEL_ID, DecimalType.class, false, false),
    PROGRAMTYPE("programType", "type", StringType.class, false, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getTextState(s, dmd, translationProvider, PHASES, MISSING_PHASE_TEXT_PREFIX,
                    MIELE_WASHING_MACHINE_TEXT_PREFIX);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false, false),
    START_TIME("", START_CHANNEL_ID, DateTimeType.class, false, false),
    END_TIME("", END_CHANNEL_ID, DateTimeType.class, false, false),
    DURATION("duration", "duration", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            try {
                date.setTime(Long.valueOf(s.trim()) * 60000);
            } catch (Exception e) {
                date.setTime(0);
            }
            return getState(dateFormatter.format(date));
        }
    },
    ELAPSED_TIME("elapsedTime", "elapsed", DateTimeType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
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
    FINISH_TIME("", FINISH_CHANNEL_ID, DateTimeType.class, false, false),
    TARGET_TEMP("targetTemperature", "target", QuantityType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getTemperatureState(s);
        }
    },
    SPINNING_SPEED("spinningSpeed", "spinningspeed", StringType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            if ("0".equals(s)) {
                return getState("Without spinning");
            }
            if ("256".equals(s)) {
                return getState("Rinsing");
            }
            return getState(Integer.toString((Integer.valueOf(s))));
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            if ("true".equals(s)) {
                return getState("OPEN");
            }

            if ("false".equals(s)) {
                return getState("CLOSED");
            }

            return UnDefType.UNDEF;
        }
    },
    SWITCH("", "switch", OnOffType.class, false, false),
    POWER_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, POWER_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true),
    WATER_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, WATER_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true);

    private final Logger logger = LoggerFactory.getLogger(WashingMachineChannelSelector.class);

    private static final Map<String, String> PROGRAMS = Map.ofEntries(entry("1", "cottons"), entry("3", "minimum-iron"),
            entry("4", "delicates"), entry("8", "woollens"), entry("9", "silks"), entry("17", "starch"),
            entry("18", "rinse"), entry("21", "drain-spin"), entry("22", "curtains"), entry("23", "shirts"),
            entry("24", "denim"), entry("27", "proofing"), entry("29", "sportswear"), entry("31", "automatic-plus"),
            entry("37", "outerwear"), entry("39", "pillows"), entry("50", "dark-garments"), entry("53", "first-wash"),
            entry("75", "steam-care"), entry("76", "freshen-up"), entry("91", "maintenance-wash"),
            entry("95", "down-duvets"), entry("122", "express-20"), entry("129", "down-filled-items"),
            entry("133", "cottons-eco"), entry("146", "quickpowerwash"), entry("65532", "mix"));

    private static final Map<String, String> PHASES = Map.ofEntries(entry("1", "pre-wash"), entry("4", "washing"),
            entry("5", "rinses"), entry("7", "clean"), entry("9", "drain"), entry("10", "spin"),
            entry("11", "anti-crease"), entry("12", "finished"));

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;
    private final boolean isExtendedState;

    WashingMachineChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
            boolean isProperty, boolean isExtendedState) {
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
    public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
        return this.getState(s, dmd);
    }

    @Override
    public State getState(String s, @Nullable DeviceMetaData dmd) {
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
            logger.warn("An exception occurred while converting '{}' into a State", s);
        }

        return UnDefType.UNDEF;
    }

    public State getTemperatureState(String s) {
        try {
            return DeviceUtil.getTemperatureState(s);
        } catch (NumberFormatException e) {
            logger.warn("An exception occurred while converting '{}' into a State", s);
            return UnDefType.UNDEF;
        }
    }
}
