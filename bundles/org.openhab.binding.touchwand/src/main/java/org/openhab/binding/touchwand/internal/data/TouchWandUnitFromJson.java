/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.touchwand.internal.data;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.SUPPORTED_TOCUHWAND_TYPES;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TouchWandUnitFromJson} parse Json unit data
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitFromJson {

    private final Logger logger = LoggerFactory.getLogger(TouchWandUnitFromJson.class);

    TouchWandUnitFromJson(String JsonUnit) {
        JsonParser jsonParser = new JsonParser();
        Gson gson = new Gson();
        JsonObject unitObj = jsonParser.parse(JsonUnit).getAsJsonObject();
        TouchWandUnitData touchWandUnit;
        String type = unitObj.get("type").getAsString();
        if (!Arrays.asList(SUPPORTED_TOCUHWAND_TYPES).contains(type)) {
            logger.debug("Unit parse skipping unsupported unit type : {} ", type);
            continue;
        }

        if (!unitObj.has("currStatus") || (unitObj.get("currStatus") == null)) {
            logger.warn("Unit discovery unit currStatus is null : {}", JsonUnit);
            continue;
        }

        if (type.equals("WallController")) {
            touchWandUnit = gson.fromJson(unitObj, TouchWandUnitDataWallController.class);
        } else {
            touchWandUnit = gson.fromJson(unitObj, TouchWandShutterSwitchUnitData.class);
        }

        return null;
    }

}
