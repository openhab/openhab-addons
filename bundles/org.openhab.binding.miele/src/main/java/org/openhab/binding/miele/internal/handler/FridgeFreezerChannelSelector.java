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

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import org.openhab.binding.miele.internal.ExtendedDeviceStateUtil;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceMetaData;
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
 * The {@link ApplianceChannelSelector} for fridges with
 * a freezer compartment
 *
 * @author Karel Goderis - Initial contribution
 * @author Jacob Laursen - Added UoM for temperatures, raw channels
 */
public enum FridgeFreezerChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    BRAND_ID("brandId", "brandId", StringType.class, true),
    COMPANY_ID("companyId", "companyId", StringType.class, true),
    STATE_TEXT(STATE_PROPERTY_NAME, STATE_TEXT_CHANNEL_ID, StringType.class, false),
    STATE(null, STATE_CHANNEL_ID, DecimalType.class, false),
    FREEZERSTATE("freezerState", "freezerstate", StringType.class, false),
    FRIDGESTATE("fridgeState", "fridgestate", StringType.class, false),
    SUPERCOOL(null, SUPERCOOL_CHANNEL_ID, OnOffType.class, false),
    SUPERFREEZE(null, SUPERFREEZE_CHANNEL_ID, OnOffType.class, false),
    FREEZERCURRENTTEMP("freezerCurrentTemperature", "freezercurrent", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    FREEZERTARGETTEMP("freezerTargetTemperature", "freezertarget", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    FRIDGECURRENTTEMP("fridgeCurrentTemperature", "fridgecurrent", QuantityType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getTemperatureState(s);
        }
    },
    FRIDGETARGETTEMP("fridgeTargetTemperature", "fridgetarget", QuantityType.class, false) {
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
    START(null, "start", OnOffType.class, false);

    private final Logger logger = LoggerFactory.getLogger(FridgeFreezerChannelSelector.class);

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    FridgeFreezerChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
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
