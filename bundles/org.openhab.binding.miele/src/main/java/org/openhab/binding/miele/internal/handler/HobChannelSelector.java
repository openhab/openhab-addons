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
package org.openhab.binding.miele.internal.handler;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.lang.reflect.Method;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.DeviceUtil;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
import org.openhab.binding.miele.internal.api.dto.DeviceMetaData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApplianceChannelSelector} for hobs
 *
 * @author Karel Goderis - Initial contribution
 * @author Jacob Laursen - Added raw channels
 */
@NonNullByDefault
public enum HobChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return DeviceUtil.getStateTextState(s, dmd, translationProvider);
        }
    },
    STATE("", STATE_CHANNEL_ID, DecimalType.class, false),
    PLATES("plateNumbers", "plates", DecimalType.class, true),
    PLATE1_POWER("plate1PowerStep", "plate1power", DecimalType.class, false),
    PLATE1_HEAT("plate1RemainingHeat", "plate1heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            // If there is remaining heat, the device metadata contains some informative string which can not be
            // converted into a DecimalType. We therefore ignore the metadata and return the device property value as a
            // State
            return getState(s);
        }
    },
    PLATE1_TIME("plate1RemainingTime", "plate1time", StringType.class, false),
    PLATE2_POWER("plate2PowerStep", "plate2power", DecimalType.class, false),
    PLATE2_HEAT("plate2RemainingHeat", "plate2heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    PLATE2_TIME("plate2RemainingTime", "plate2time", StringType.class, false),
    PLATE3_POWER("plate3PowerStep", "plate3power", DecimalType.class, false),
    PLATE3_HEAT("plate3RemainingHeat", "plate3heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    PLATE3_TIME("plate3RemainingTime", "plate3time", StringType.class, false),
    PLATE4_POWER("plate4PowerStep", "plate4power", DecimalType.class, false),
    PLATE4_HEAT("plate4RemainingHeat", "plate4heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    PLATE4_TIME("plate4RemainingTime", "plate4time", StringType.class, false),
    PLATE5_POWER("plate5PowerStep", "plate5power", DecimalType.class, false),
    PLATE5_HEAT("plate5RemainingHeat", "plate5heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    PLATE5_TIME("plate5RemainingTime", "plate5time", StringType.class, false),
    PLATE6_POWER("plate6PowerStep", "plate6power", DecimalType.class, false),
    PLATE6_HEAT("plate6RemainingHeat", "plate6heat", DecimalType.class, false) {
        @Override
        public State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider) {
            return getState(s);
        }
    },
    PLATE6_TIME("plate6RemainingTime", "plate6time", StringType.class, false);

    private final Logger logger = LoggerFactory.getLogger(HobChannelSelector.class);

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    HobChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass, boolean isProperty) {
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
