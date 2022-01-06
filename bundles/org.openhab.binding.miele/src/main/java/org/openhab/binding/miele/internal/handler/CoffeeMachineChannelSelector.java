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

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.openhab.binding.miele.internal.DeviceMetaData;
import org.openhab.binding.miele.internal.DeviceUtil;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
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
 * The {@link ApplianceChannelSelector} for coffee machines
 *
 * @author Stephan Esch - Initial contribution
 * @author Jacob Laursen - Added raw channels
 */
public enum CoffeeMachineChannelSelector implements ApplianceChannelSelector {

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
                    MIELE_COFFEE_MACHINE_TEXT_PREFIX);
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
                    MIELE_COFFEE_MACHINE_TEXT_PREFIX);
            if (state != null) {
                return state;
            }
            return super.getState(s, dmd, translationProvider);
        }
    },
    PROGRAM_PHASE(RAW_PHASE_PROPERTY_NAME, PHASE_CHANNEL_ID, DecimalType.class, false),
    // lightingStatus signalFailure signalInfo
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

    private final Logger logger = LoggerFactory.getLogger(CoffeeMachineChannelSelector.class);

    private static final Map<String, String> programs = Collections.<String, String> emptyMap();

    private static final Map<String, String> phases = Collections.<String, String> emptyMap();

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    CoffeeMachineChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
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
