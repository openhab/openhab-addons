/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.geo.internal.profiles;

import static org.openhab.transform.geo.internal.GeoConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;

/**
 * Reverse Geocoding of JSON String
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ReverseGeocoding {
    private boolean resolved = false;
    private PointType location;

    public boolean isResolved() {
        return resolved;
    }

    public ReverseGeocoding(PointType location) {
        this.location = location;
    }

    public static String decode(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        if (jsonObject.has("address")) {
            JSONObject address = jsonObject.getJSONObject("address");
            String street = (get(address, ROAD_KEYS) + " " + get(address, HOUSE_NUMBER_KEYS)).strip();
            String city = (get(address, ZIP_CODE_KEYS) + " " + get(address, CITY_KEYS) + " "
                    + get(address, DISTRICT_KEYS)).strip();
            if (!street.isBlank()) {
                street += ", ";
            }
            return street + city;
        }
        return "";
    }

    private static String get(JSONObject jsonObject, List<String> keys) {
        for (String key : keys) {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            }
        }
        return "";
    }
}
