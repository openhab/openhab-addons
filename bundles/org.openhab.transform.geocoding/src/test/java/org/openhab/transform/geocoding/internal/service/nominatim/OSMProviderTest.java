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
package org.openhab.transform.geocoding.internal.service.nominatim;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.transform.geocoding.internal.GeoProfileConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.geocoding.internal.profiles.GeoProfile;
import org.openhab.transform.geocoding.internal.provider.BaseGeoResolver;

/**
 * The {@link OSMProviderTest} tests GeoResolverFactory and GeoResolver basic classes. Tests are executed with different
 * configurations, responses and expected results.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class OSMProviderTest {

    public static Stream<Arguments> testResponses() {
        return Stream.of( //
                Arguments.of(null, null, 200, "", PointType.valueOf("0,1"), false, ""), //
                Arguments.of("nominatim-osm", null, 200, "", PointType.valueOf("0,1"), false, ""), //
                Arguments.of("nominatim-osm", null, 200, "", UnDefType.UNDEF, false, ""), //
                Arguments.of("nominatim-osm", null, 400, readFile("src/test/resources/geo-reverse-result.json"),
                        PointType.valueOf("0,1"), false, ""), //
                Arguments.of("nominatim-osm", null, 200, readFile("src/test/resources/geo-reverse-result.json"),
                        PointType.valueOf("0,1"), true, "Am Friedrichshain 22, 10407 Berlin Pankow"), //
                Arguments.of("nominatim-osm", US_ADDRESS_FORMAT, 200,
                        readFile("src/test/resources/geo-reverse-nyc.json"), PointType.valueOf("0,1"), true,
                        "6 West 23rd Street, City of New York Manhattan 10010"), //
                Arguments.of("nominatim-osm", JSON_FORMAT, 200, readFile("src/test/resources/geo-reverse-result.json"),
                        PointType.valueOf("0,1"), true,
                        (new JSONObject(readFile("src/test/resources/geo-reverse-result.json")))
                                .getJSONObject(ADDRESS_KEY).toString()), //
                Arguments.of("nominatim-osm", null, 200, readFile("src/test/resources/geo-reverse-result-no-road.json"),
                        PointType.valueOf("0,1"), true, "10407 Berlin Pankow"), //
                Arguments.of("nominatim-osm", null, 400, "", new StringType("Not necessary"), false, ""), //
                Arguments.of("nominatim-osm", null, 200, "Inavlid response", new StringType("Not necessary"), false,
                        ""), //
                Arguments.of("nominatim-osm", null, 200, "[]", new StringType("Not necessary"), false, ""), //
                Arguments.of("nominatim-osm", US_ADDRESS_FORMAT, 200,
                        readFile("src/test/resources/geo-search-result.json"), new StringType("Not necessary"), true,
                        "52.5252949,13.3706843") //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testResponses(@Nullable String provider, @Nullable String format, int mockStatus, String mockResponse,
            State toBeResolved, boolean expectedResolved, String expectedResult) {
        Configuration config = new Configuration();
        if (provider != null) {
            config.put("provider", provider);
        }
        if (format != null) {
            config.put("format", format);
        }

        GeoProfile profile = getGeoProfile(config, mockStatus, mockResponse);
        BaseGeoResolver resolver = profile.createResolver(toBeResolved);
        assertEquals("nominatim-osm", resolver.getProvider());

        resolver.setUserAgentSupplier(this::getUserAgent);
        resolver.resolve();
        assertEquals(expectedResolved, resolver.isResolved());
        assertEquals(expectedResult, resolver.getResolved());
    }

    GeoProfile getGeoProfile(Configuration config, int responseStatus, String response) {
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.ENGLISH);

        ProfileContext context = mock(ProfileContext.class);
        when(context.getConfiguration()).thenReturn(config);

        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.header(any(HttpHeader.class), anyString())).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        try {
            when(request.send()).thenReturn(contentResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        when(contentResponse.getStatus()).thenReturn(responseStatus);
        when(contentResponse.getContentAsString()).thenReturn(response);

        GeoProfile profile = new GeoProfile(mock(ProfileCallback.class), context, httpClient, localeProvider);
        return profile;
    }

    static String readFile(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    public String getUserAgent() {
        return "openHAB/unitTest";
    }
}
