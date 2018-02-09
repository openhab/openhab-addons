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
import java.util.Map.Entry;

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
 * The {@link ApplianceChannelSelector} for fridges with
 * a freezer compartment
 *
 * @author Karel Goderis - Initial contribution
 */
public enum FridgeFreezerChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    BRAND_ID("brandId", "brandId", StringType.class, true),
    COMPANY_ID("companyId", "companyId", StringType.class, true),
    STATE("state", "state", StringType.class, false),
    FREEZERSTATE("freezerState", "freezerstate", StringType.class, false),
    FRIDGESTATE("fridgeState", "fridgestate", StringType.class, false),
    SUPERCOOL(null, "supercool", OnOffType.class, false),
    SUPERFREEZE(null, "superfreeze", OnOffType.class, false),
    FREEZERCURRENTTEMP("freezerCurrentTemperature", "freezercurrent", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    FREEZERTARGETTEMP("freezerTargetTemperature", "freezertarget", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    FRIDGECURRENTTEMP("fridgeCurrentTemperature", "fridgecurrent", DecimalType.class, false) {
        @Override
        public State getState(String s, DeviceMetaData dmd) {
            return getState(s);
        }
    },
    FRIDGETARGETTEMP("fridgeTargetTemperature", "fridgetarget", DecimalType.class, false) {
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
