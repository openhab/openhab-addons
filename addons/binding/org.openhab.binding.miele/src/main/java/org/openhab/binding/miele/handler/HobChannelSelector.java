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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link ApplianceChannelSelector} for hobs
 *
 * @author Karel Goderis - Initial contribution
 */
public enum HobChannelSelector implements ApplianceChannelSelector {

    PRODUCT_TYPE("productTypeId", "productType", StringType.class, true),
    DEVICE_TYPE("mieleDeviceType", "deviceType", StringType.class, true),
    BRAND_ID("brandId", "brandId", StringType.class, true),
    COMPANY_ID("companyId", "companyId", StringType.class, true),
    STATE("state", "state", StringType.class, false),
    PLATES("plateNumbers", "plates", DecimalType.class, true),
    PLATE1_POWER("plate1PowerStep", "plate1power", DecimalType.class, false),
    PLATE1_HEAT("plate1RemainingHeat", "plate1heat", DecimalType.class, false),
    PLATE1_TIME("plate1RemainingTime", "plate1time", StringType.class, false),
    PLATE2_POWER("plate2PowerStep", "plate2power", DecimalType.class, false),
    PLATE2_HEAT("plate2RemainingHeat", "plate2heat", DecimalType.class, false),
    PLATE2_TIME("plate2RemainingTime", "plate2time", StringType.class, false),
    PLATE3_POWER("plate3PowerStep", "plate3power", DecimalType.class, false),
    PLATE3_HEAT("plate3RemainingHeat", "plate3heat", DecimalType.class, false),
    PLATE3_TIME("plate3RemainingTime", "plate3time", StringType.class, false),
    PLATE4_POWER("plate4PowerStep", "plate4power", DecimalType.class, false),
    PLATE4_HEAT("plate4RemainingHeat", "plate4heat", DecimalType.class, false),
    PLATE4_TIME("plate4RemainingTime", "plate4time", StringType.class, false),
    PLATE5_POWER("plate5PowerStep", "plate5power", DecimalType.class, false),
    PLATE5_HEAT("plate5RemainingHeat", "plate5heat", DecimalType.class, false),
    PLATE5_TIME("plate5RemainingTime", "plate5time", StringType.class, false),
    PLATE6_POWER("plate6PowerStep", "plate6power", DecimalType.class, false),
    PLATE6_HEAT("plate6RemainingHeat", "plate6heat", DecimalType.class, false),
    PLATE6_TIME("plate6RemainingTime", "plate6time", StringType.class, false);

    private final Logger logger = LoggerFactory.getLogger(HobChannelSelector.class);

    private final String mieleID;
    private final String channelID;
    private final Class<? extends Type> typeClass;
    private final boolean isProperty;

    private HobChannelSelector(String propertyID, String channelID, Class<? extends Type> typeClass,
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
