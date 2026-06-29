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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_QUERY_BASE_STATION_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_QUERY_LIGHTING_AREA_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_QUERY_LOCATION_ID;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioProperty;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioPropertyEntityLookupResponse;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioPropertyListResponse;

/**
 * Tests Property Service DTO parsing and request helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioPropertyApiTest {
    @Test
    void propertyListResponseParsesWrappedProperties() {
        String json = """
                {
                  "properties": [
                    {"id":"property-1","name":"Home"},
                    {"id":"property-2","name":"Cabin"}
                  ]
                }
                """;

        RachioPropertyListResponse response = RachioPropertyListResponse.fromJson(json);

        assertThat(response.properties.size(), is(2));
        assertThat(response.properties.get(0).getId(), is("property-1"));
        assertThat(response.properties.get(1).getName(), is("Cabin"));
    }

    @Test
    void singlePropertyResponseParsesWrappedProperty() {
        String json = """
                {
                  "property": {
                    "propertyId": "property-1",
                    "nickname": "Home"
                  }
                }
                """;

        RachioProperty property = Objects.requireNonNull(RachioPropertyGsonDTO.parseProperty(json));

        assertThat(property.getId(), is("property-1"));
        assertThat(property.getName(), is("Home"));
    }

    @Test
    void propertyEntityLookupReadsFirstProperty() {
        String json = """
                {
                  "data": [
                    {"id":"property-1","name":"Home"}
                  ]
                }
                """;

        RachioPropertyEntityLookupResponse response = RachioPropertyEntityLookupResponse.fromJson(json);

        assertThat(response.getProperty(), is(notNullValue()));
        assertThat(Objects.requireNonNull(response.getProperty()).getId(), is("property-1"));
    }

    @Test
    void propertyEntityQueryUsesDocumentedResourceParameters() throws RachioApiException {
        assertThat(RachioApi.buildPropertyEntityQuery("location id", "locationId"),
                is(PROPERTY_QUERY_LOCATION_ID + "=location+id"));
        assertThat(RachioApi.buildPropertyEntityQuery("base-station-id", "baseStationId"),
                is(PROPERTY_QUERY_BASE_STATION_ID + "=base-station-id"));
        assertThat(RachioApi.buildPropertyEntityQuery("lighting-area-id", "resource_id.lighting_area_id"),
                is(PROPERTY_QUERY_LIGHTING_AREA_ID + "=lighting-area-id"));
    }
}
