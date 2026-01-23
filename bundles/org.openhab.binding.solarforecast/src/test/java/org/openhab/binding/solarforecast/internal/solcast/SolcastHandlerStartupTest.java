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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.binding.solarforecastinternal.solcast.mock.SolcastBridgeMock;
import org.openhab.binding.solarforecastinternal.solcast.mock.SolcastMockFactory;
import org.openhab.binding.solarforecastinternal.solcast.mock.SolcastPlaneMock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * {@link SolcastHandlerStartupTest} checks different startup scenarios of Solcast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastHandlerStartupTest {
    private static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };
    static String planeId = "sc-plane-1-test";

    @BeforeEach
    void setFixedTimeJul17() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(timeZoneProvider);
    }

    public static Stream<Arguments> testFirstStartupNoStorage() {
        return Stream.of( //
                Arguments.of(null, Map.of("resourceId", planeId), HttpStatus.OK_200, ThingStatus.ONLINE,
                        Map.of("200", 2, "429", 0, "other", 0)), //
                Arguments.of(SolcastPlaneMock.getPreparedForecast(), Map.of("resourceId", planeId), HttpStatus.OK_200,
                        ThingStatus.ONLINE, Map.of("200", 1, "429", 0, "other", 0)), //
                Arguments.of(null, Map.of("resourceId", planeId), HttpStatus.TOO_MANY_REQUESTS_429, ThingStatus.OFFLINE,
                        Map.of("200", 0, "429", 1, "other", 0)), //
                Arguments.of(null, Map.of("resourceId", planeId, "guessActuals", false), HttpStatus.OK_200,
                        ThingStatus.ONLINE, Map.of("200", 2, "429", 0, "other", 0)), //
                Arguments.of(SolcastPlaneMock.getPreparedForecast(), Map.of("resourceId", planeId),
                        HttpStatus.INTERNAL_SERVER_ERROR_500, ThingStatus.ONLINE,
                        Map.of("200", 0, "429", 0, "other", 1)), //
                Arguments.of(null, Map.of("resourceId", planeId), HttpStatus.INTERNAL_SERVER_ERROR_500,
                        ThingStatus.OFFLINE, Map.of("200", 0, "429", 0, "other", 1)), //
                Arguments.of(SolcastPlaneMock.getPreparedForecast(),
                        Map.of("resourceId", planeId, "guessActuals", false), HttpStatus.OK_200, ThingStatus.ONLINE,
                        Map.of("200", 2, "429", 0, "other", 0)) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFirstStartupNoStorage(@Nullable JSONArray storageContent, Map<String, Object> configuration,
            int httpStatus, ThingStatus expectedThingStatus, Map<String, Integer> expectedApiCounts) {

        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        HttpClient httpMock = mock(HttpClient.class);

        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId, httpStatus,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId, httpStatus,
                "src/test/resources/solcast/forecasts.json");

        Storage<String> store = new VolatileStorage<>();
        if (storageContent != null) {
            store.put("solarforecast:sc-plane:solcast-site-test:" + planeId + FORECAST_APPENDIX,
                    storageContent.toString());
        }
        SolcastPlaneMock planeMock = SolcastMockFactory.createPlaneHandler(planeId, bridgeMock, httpMock, store,
                expectedThingStatus, new Configuration(configuration));
        CallbackMock callback = (CallbackMock) planeMock.getCallback();
        assertNotNull(callback);

        State counterState = callback
                .getLastState("solarforecast:sc-plane:solcast-site-test:sc-plane-1-test:update#api-count");
        assertTrue(counterState instanceof StringType);
        Map<String, Object> actualApiCounts = new JSONObject(((StringType) counterState).toFullString()).toMap();
        expectedApiCounts.forEach((key, value) -> {
            assertEquals(value, actualApiCounts.get(key), "API call count for " + key + " mismatched");
            actualApiCounts.remove(key);
        });
        assertTrue(actualApiCounts.isEmpty(), "Unexpected API call counts: " + actualApiCounts.keySet());
        assertEquals(expectedThingStatus, callback.getStatus().getStatus(), "Status mismatched");
        if (expectedThingStatus == ThingStatus.OFFLINE) {
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, callback.getStatus().getStatusDetail(),
                    "Status detail mismatched");
        }
    }
}
