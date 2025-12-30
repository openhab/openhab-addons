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
package org.openhab.transform.geocoding.internal;

import static org.junit.jupiter.api.Assertions.fail;
import static org.openhab.transform.geocoding.internal.OSMGeoConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.transform.geocoding.internal.config.OSMGeoConfig;
import org.openhab.transform.geocoding.internal.osm.OSMGeocoding;
import org.openhab.transform.geocoding.internal.osm.OSMReverseGeocoding;

/**
 * Testing with real API calls towards from OpenStreetMap
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class OSMRealTest {

    void testReverseGeocoding() {
        HttpClient httpClient = getHttpClient();
        String coordinates = "40.74162115629083, -73.99000345325618";
        // String coordinates = "1, 1";
        OSMGeoConfig osmConfig = new OSMGeoConfig();
        osmConfig.language = "de-DE";
        osmConfig.format = ROW_ADDRESS_FORMAT;
        osmConfig.format = RAW_FORMAT;
        OSMReverseGeocoding toObserve = new OSMReverseGeocoding(PointType.valueOf(coordinates), osmConfig, httpClient);
        toObserve.resolve();
    }

    void testGeocoding() {
        HttpClient httpClient = getHttpClient();
        String search = "bimbambum";
        OSMGeocoding toObserve = new OSMGeocoding(search, httpClient);
        toObserve.resolve();
    }

    private HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
        try {
            httpClient.start();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return httpClient;
    }
}
