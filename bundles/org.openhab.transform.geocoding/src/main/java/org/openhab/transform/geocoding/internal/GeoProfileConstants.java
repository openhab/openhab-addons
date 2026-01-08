/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.transform.geocoding.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;

/**
 * The {@link GeoProfileConstants} defines fields used for the profile and across providers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeoProfileConstants {

    // Profile Type UID
    public static final ProfileTypeUID GEOCODING_PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "geocoding");

    // Constants for Provider
    public static final String PROVIDER_NOMINATIM_OPENSTREETMAP = "nominatim-osm";

    // JSON Keys
    public static final String ADDRESS_KEY = "address";
    public static final String LATITUDE_KEY = "lat";
    public static final String LONGITUDE_KEY = "lon";

    // Address options constants for US/UK and ROW (Rest of World)
    public static final String FORMAT_ADDRESS_ROW = "address_row";
    public static final String FORMAT_ADDRESS_US = "address_us";
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_RAW = "raw";

    // see https://nominatim.org/release-docs/latest/api/Output/#addressdetails
    public static final List<String> ROAD_KEYS = List.of("road");
    public static final List<String> HOUSE_NUMBER_KEYS = List.of("house_number", "house_name");
    public static final List<String> ZIP_CODE_KEYS = List.of("postcode");
    public static final List<String> CITY_KEYS = List.of("municipality", "city", "town", "village");
    public static final List<String> DISTRICT_KEYS = List.of("city_district", "district", "borough", "suburb",
            "subdivision");

    // Nominatim / OpenStreetMap URLs
    public static final String BASE_URL = "https://nominatim.openstreetmap.org/";
    public static final String SEARCH_URL = BASE_URL + "search?q=%s&format=jsonv2";
    public static final String REVERSE_URL = BASE_URL + "reverse?lat=%.7f&lon=%.7f&format=jsonv2";
}
