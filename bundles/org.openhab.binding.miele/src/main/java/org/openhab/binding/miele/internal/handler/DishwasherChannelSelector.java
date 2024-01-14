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
 * The {@link ApplianceChannelSelector} for dishwashers
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed START_TIME to DateTimeType
 * @author Jacob Laursen - Added power/water consumption channels, raw channels
 */
@NonNullByDefault
public enum DishwasherChannelSelector implements ApplianceChannelSelector {

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
                    MIELE_DISHWASHER_TEXT_PREFIX);
        }
    },
    PROGRAM("", PROGRAM_CHANNEL_ID, DecimalType.class, false, false),
    PROGRAM_PHASE_TEXT(PHASE_PROPERTY_NAME, PHASE_TEXT_CHANNEL_ID, StringType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getTextState(s, dmd, translationProvider, PHASES, MISSING_PHASE_TEXT_PREFIX,
                    MIELE_DISHWASHER_TEXT_PREFIX);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false, false),
    START_TIME("", START_CHANNEL_ID, DateTimeType.class, false, false),
    END_TIME("", END_CHANNEL_ID, DateTimeType.class, false, false),
    DURATION("duration", "duration", QuantityType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            try {
                return new QuantityType<>(Long.valueOf(s), Units.MINUTE);
            } catch (NumberFormatException e) {
                return UnDefType.UNDEF;
            }
        }
    },
    ELAPSED_TIME("elapsedTime", "elapsed", QuantityType.class, false, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            try {
                return new QuantityType<>(Long.valueOf(s), Units.MINUTE);
            } catch (NumberFormatException e) {
                return UnDefType.UNDEF;
            }
        }
    },
    FINISH_TIME("", FINISH_CHANNEL_ID, QuantityType.class, false, false),
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
    ENERGY_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, ENERGY_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true),
    WATER_CONSUMPTION(EXTENDED_DEVICE_STATE_PROPERTY_NAME, WATER_CONSUMPTION_CHANNEL_ID, QuantityType.class, false,
            true);

    private final Logger logger = LoggerFactory.getLogger(DishwasherChannelSelector.class);

    private static final Map<String, String> PROGRAMS = Map.ofEntries(entry("26", "intensive"),
            entry("27", "maintenance-programme"), entry("28", "eco"), entry("30", "normal"), entry("32", "automatic"),
            entry("34", "solarsave"), entry("35", "gentle"), entry("36", "extra-quiet"), entry("37", "hygiene"),
            entry("38", "quickpowerwash"), entry("42", "tall-items"));

    private static final Map<String, String> PHASES = Map.ofEntries(entry("2", "pre-wash"), entry("3", "main-wash"),
            entry("4", "rinses"), entry("6", "final-rinse"), entry("7", "drying"), entry("8", "finished"));

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
