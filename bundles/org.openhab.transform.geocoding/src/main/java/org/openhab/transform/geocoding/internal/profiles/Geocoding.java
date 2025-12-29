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
package org.openhab.transform.geocoding.internal.profiles;

import static org.openhab.transform.geocoding.internal.GeoConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;

/**
 * Get coordinates from a geocoding JSON response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Geocoding {

    public static @Nullable PointType parse(String jsonResponse) {
        JSONArray searchResults = new JSONArray(jsonResponse);
        if (searchResults.length() > 0) {
            JSONObject firstResult = searchResults.getJSONObject(0);
            if (firstResult.has(LATITUDE_KEY) && firstResult.has(LONGITUDE_KEY)) {
                return PointType
                        .valueOf(firstResult.getString(LATITUDE_KEY) + "," + firstResult.getString(LONGITUDE_KEY));
            }
        }
        return null;
    }
}
