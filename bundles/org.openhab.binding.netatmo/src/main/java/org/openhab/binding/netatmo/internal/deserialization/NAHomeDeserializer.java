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
package org.openhab.binding.netatmo.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEnergy;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeSecurity;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeWeather;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Specialized deserializer NAHome
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAHomeDeserializer implements JsonDeserializer<NAHome> {

    @Override
    public @Nullable NAHome deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        NAHomeWeather result = context.deserialize(json, NAHomeWeather.class);
        if (result.containsModuleType(ModuleType.NAPlug)) {
            NAHomeEnergy homeEnergy = context.deserialize(json, NAHomeEnergy.class);
            homeEnergy.setType(ModuleType.NAHomeEnergy);
            return homeEnergy;
        }
        if (result.containsModuleType(ModuleType.NACamera) || result.containsModuleType(ModuleType.NOC)) {
            // This way of detecting home kinds presents a problem if no module is installed then everything will fall
            // as a HomeWeather
            NAHomeSecurity homeSec = context.deserialize(json, NAHomeSecurity.class);
            homeSec.getModules().putAll(homeSec.getPersons());
            homeSec.getModules().putAll(homeSec.getCameras());
            homeSec.setType(ModuleType.NAHomeSecurity);
            return homeSec;
        }
        result.setType(ModuleType.NAHomeWeather);
        return result;
    }
}
