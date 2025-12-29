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
package org.openhab.transform.geo.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;

/**
 * The {@link GeoConstants} class to define transform constants
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeoConstants {

    // Profile Type UID
    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "geo-coding");

    // URLs
    public static final String BASE_URL = "https://nominatim.openstreetmap.org/";
    public static final String SEARCH_URL = BASE_URL + "search?q=%s&format=jsonv2";
    public static final String REVERSE_URL = BASE_URL + "reverse?lat=%.7f&lon=%.7f&format=jsonv2";

    public static final String LATITUDE_KEY = "lat";
    public static final String LONGITUDE_KEY = "lon";

    public static final String ADDRESS_FORMAT = "address";
    public static final String JSON_FORMAT = "json";

    // see https://nominatim.org/release-docs/latest/api/Output/#addressdetails
    public static final List<String> ROAD_KEYS = List.of("road");
    public static final List<String> HOUSE_NUMBER_KEYS = List.of("house_number", "house_name");
    public static final List<String> ZIP_CODE_KEYS = List.of("postcode");
    public static final List<String> CITY_KEYS = List.of("municipality", "city", "town", "village");
    public static final List<String> DISTRICT_KEYS = List.of("city_district", "district", "borough", "suburb",
            "subdivision");
}
