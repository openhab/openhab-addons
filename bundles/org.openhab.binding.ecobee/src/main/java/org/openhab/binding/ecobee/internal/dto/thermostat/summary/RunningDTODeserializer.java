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
package org.openhab.binding.ecobee.internal.dto.thermostat.summary;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link RunningDTODeserializer} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class RunningDTODeserializer implements JsonDeserializer<@Nullable RunningDTO> {

    @Override
    public @Nullable RunningDTO deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null || typeOfT == null || context == null) {
            return null;
        }
        String[] fields = json.getAsString().split(":");
        if (fields.length < 1) {
            throw new JsonParseException("unable to parse StatusList");
        }
        RunningDTO statusList = new RunningDTO();
        statusList.identifier = fields[0];
        if (fields.length >= 2) {
            for (String equip : fields[1].split(",")) {
                if (equip.length() == 0) {
                    continue;
                }
                statusList.runningEquipment.add(equip);
            }
        }
        return statusList;
    }
}
