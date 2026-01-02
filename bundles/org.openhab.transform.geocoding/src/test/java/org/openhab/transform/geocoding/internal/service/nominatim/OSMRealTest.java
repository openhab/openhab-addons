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
package org.openhab.transform.geocoding.internal.service.nominatim;

import static org.junit.jupiter.api.Assertions.fail;
import static org.openhab.transform.geocoding.internal.GeoProfileConstants.ROW_ADDRESS_FORMAT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.BaseGeoResolver;
import org.openhab.transform.geocoding.internal.provider.nominatim.OSMGeoResolver;

/**
 * The {@link OSMRealTest} executes real API calls to Nominatim/OpenStreetMap therefore not marked with Test annotation.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class OSMRealTest {

    void testReverseGeocoding() {
        HttpClient httpClient = getHttpClient();
        String coordinates = "40.74162115629083, -73.99000345325618";
        GeoProfileConfig osmConfig = new GeoProfileConfig();
        osmConfig.language = "de-DE";
        osmConfig.format = ROW_ADDRESS_FORMAT;
        BaseGeoResolver toObserve = new OSMGeoResolver(PointType.valueOf(coordinates), osmConfig, httpClient);
        toObserve.setUserAgentSupplier(this::getUserAgent);
        toObserve.resolve();
    }

    void testGeocoding() {
        HttpClient httpClient = getHttpClient();
        String search = "bimbambum";
        GeoProfileConfig osmConfig = new GeoProfileConfig();
        osmConfig.language = "de-DE";
        osmConfig.format = ROW_ADDRESS_FORMAT;
        OSMGeoResolver toObserve = new OSMGeoResolver(new StringType(search), osmConfig, httpClient);
        toObserve.setUserAgentSupplier(this::getUserAgent);
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

    public String getUserAgent() {
        return "openHAB/unitTest";
    }
}
