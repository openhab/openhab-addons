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
package org.openhab.binding.carnet.internal;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetPosition;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link OpenStreetMapApiDTO} implements some helper functions
 *
 * @author Markus Michels - Initial contribution
 */
public class OpenStreetMapApiDTO {
    private final Logger logger = LoggerFactory.getLogger(OpenStreetMapApiDTO.class);
    protected final Gson gson = new Gson();

    public static class OSMPointResponse {
        public static class OSMAddress {
            public String house_number;
            public String road;
            public String suburb;
            public String village;
            public String town;
            public String county;
            public String state;
            public String postcode;
            public String country;
            @SerializedName("country_cocde")
            public String countryCocde;
        }

        public String licence;
        public String place_id;
        public String osm_type;
        public String osm_id;
        public String lat;
        public String lon;
        public String display_name;
        public OSMAddress address;
        public String[] boundingbox;
    }

    public String getAddressFromPosition(CarNetHttpClient http, CarNetPosition position) throws CarNetException {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?lat=" + position.getLattitude() + "&lon="
                    + position.getLongitude() + "&format=json";
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeader.USER_AGENT.toString(), "openHAB/" + BINDING_ID);
            headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
            String json = http.get(url, headers);
            OSMPointResponse r = fromJson(gson, json, OSMPointResponse.class);
            String address = getString(r.address.road) + ";" + getString(r.address.house_number) + ";"
                    + getString(r.address.postcode) + ";" + getString(r.address.postcode) + ";"
                    + getString(r.address.town) + ";" + getString(r.address.village) + ";"
                    + getString(r.address.country + ";" + getString(r.address.countryCocde));
            return address;
        } catch (CarNetException e) {
            logger.debug("OSM: Unable to lookup address for Geo position: {}", e.toString());
            return "";
        }
    }
}
