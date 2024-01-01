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
package org.openhab.binding.miele.internal.handler;

import static java.util.Map.entry;
import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.lang.reflect.Method;
import java.util.Map;

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
import org.openhab.core.library.unit.Units;
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
@NonNullByDefault
public enum TumbleDryerChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getStateTextState(s, dmd, translationProvider);
        }
    },
    STATE("", STATE_CHANNEL_ID, DecimalType.class, false),
    PROGRAM_TEXT(PROGRAM_ID_PROPERTY_NAME, PROGRAM_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getTextState(s, dmd, translationProvider, PROGRAMS, MISSING_PROGRAM_TEXT_PREFIX,
                    MIELE_TUMBLE_DRYER_TEXT_PREFIX);
        }
    },
    PROGRAM("", PROGRAM_CHANNEL_ID, DecimalType.class, false),
    PROGRAMTYPE("programType", "type", StringType.class, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getTextState(s, dmd, translationProvider, PHASES, MISSING_PHASE_TEXT_PREFIX,
                    MIELE_TUMBLE_DRYER_TEXT_PREFIX);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false),
    START_TIME("", START_CHANNEL_ID, DateTimeType.class, false),
    END_TIME("", END_CHANNEL_ID, DateTimeType.class, false),
    DURATION("duration", "duration", QuantityType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            try {
                return new QuantityType<>(Long.valueOf(s), Units.MINUTE);
            } catch (NumberFormatException e) {
                return UnDefType.UNDEF;
            }
        }
    },
    ELAPSED_TIME("elapsedTime", "elapsed", QuantityType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            try {
                return new QuantityType<>(Long.valueOf(s), Units.MINUTE);
            } catch (NumberFormatException e) {
                return UnDefType.UNDEF;
            }
        }
    },
    FINISH_TIME("", FINISH_CHANNEL_ID, QuantityType.class, false),
    DRYING_STEP("dryingStep", "step", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    DOOR("signalDoor", "door", OpenClosedType.class, false) {
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
    SWITCH("", "switch", OnOffType.class, false);

    private final Logger logger = LoggerFactory.getLogger(TumbleDryerChannelSelector.class);

    private static final Map<String, String> PROGRAMS = Map.ofEntries(entry("10", "automatic-plus"),
            entry("20", "cottons"), entry("23", "cottons-hygiene"), entry("30", "minimum-iron"),
            entry("31", "gentle-minimum-iron"), entry("40", "woollens-handcare"), entry("50", "delicates"),
            entry("60", "warm-air"), entry("70", "cool-air"), entry("80", "express"), entry("90", "cottons-eco"),
            entry("100", "gentle-smoothing"), entry("120", "proofing"), entry("130", "denim"),
            entry("131", "gentle-denim"), entry("140", "shirts"), entry("141", "gentle-shirts"),
            entry("150", "sportswear"), entry("160", "outerwear"), entry("170", "silks-handcare"),
            entry("190", "standard-pillows"), entry("220", "basket-programme"), entry("240", "smoothing"),
            entry("65000", "cottons-auto-load-control"), entry("65001", "minimum-iron-auto-load-control"));

    private static final Map<String, String> PHASES = Map.ofEntries(entry("1", "programme-running"),
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

    @Override
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
}
