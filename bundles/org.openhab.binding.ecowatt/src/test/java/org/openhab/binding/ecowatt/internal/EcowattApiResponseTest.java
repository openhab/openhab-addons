/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecowatt.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ecowatt.internal.handler.EcowattHandler;
import org.openhab.binding.ecowatt.internal.restapi.EcowattApiResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * Tests of methods getting channel state from API response
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattApiResponseTest {

    private static final DecimalType STATE_ONE = new DecimalType(1);
    private static final DecimalType STATE_TWO = new DecimalType(2);
    private static final DecimalType STATE_THREE = new DecimalType(3);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
            (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> OffsetDateTime
                    .parse(json.getAsJsonPrimitive().getAsString()).toZonedDateTime())
            .create();
    private static @Nullable EcowattApiResponse apiResponse;

    @BeforeEach
    public void loadApiResponse() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/ApiResponse.json");
        assertNotNull(resourceStream);
        final String response = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        apiResponse = gson.fromJson(response, EcowattApiResponse.class);
        assertNotNull(apiResponse);
    }

    @Test
    public void getDaySignalStateWithNullResponse() {
        assertEquals(UnDefType.UNDEF,
                EcowattHandler.getDaySignalState(null, ZonedDateTime.parse("2022-09-19T21:30:00+02:00")));
    }

    @Test
    public void getDaySignalStateWithSameOffset() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2022-09-19T21:30:00+02:00");
        assertEquals(UnDefType.UNDEF, EcowattHandler.getDaySignalState(apiResponse, dateTime.minusDays(1)));
        assertEquals(STATE_THREE, EcowattHandler.getDaySignalState(apiResponse, dateTime));
        assertEquals(STATE_TWO, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(1)));
        assertEquals(STATE_ONE, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(2)));
        assertEquals(STATE_ONE, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(3)));
        assertEquals(UnDefType.UNDEF, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(4)));
    }

    @Test
    public void getDaySignalStateWithOtherOffset() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2022-09-20T00:30:00+05:00");
        assertEquals(UnDefType.UNDEF, EcowattHandler.getDaySignalState(apiResponse, dateTime.minusDays(1)));
        assertEquals(STATE_THREE, EcowattHandler.getDaySignalState(apiResponse, dateTime));
        assertEquals(STATE_TWO, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(1)));
        assertEquals(STATE_ONE, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(2)));
        assertEquals(STATE_ONE, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(3)));
        assertEquals(UnDefType.UNDEF, EcowattHandler.getDaySignalState(apiResponse, dateTime.plusDays(4)));
    }

    @Test
    public void getHourSignalStateWithNullResponse() {
        assertEquals(UnDefType.UNDEF,
                EcowattHandler.getHourSignalState(null, ZonedDateTime.parse("2022-09-19T00:30:00+02:00")));
    }

    @Test
    public void getHourSignalStateWithSameOffset() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2022-09-19T00:30:00+02:00");

        assertEquals(UnDefType.UNDEF, EcowattHandler.getHourSignalState(apiResponse, dateTime.minusHours(1)));

        State expectedState;
        for (int h = 0; h < 24; h++) {
            switch (h) {
                case 7:
                case 11:
                case 19:
                case 21:
                    expectedState = STATE_TWO;
                    break;
                case 8:
                case 9:
                case 10:
                case 20:
                    expectedState = STATE_THREE;
                    break;
                default:
                    expectedState = STATE_ONE;
            }
            assertEquals(expectedState, EcowattHandler.getHourSignalState(apiResponse, dateTime.plusHours(h)));
        }

        dateTime = dateTime.plusDays(1);
        for (int h = 0; h < 24; h++) {
            expectedState = h == 20 ? STATE_TWO : STATE_ONE;
            assertEquals(expectedState, EcowattHandler.getHourSignalState(apiResponse, dateTime.plusHours(h)));
        }

        dateTime = dateTime.plusDays(1);
        for (int h = 0; h < 24; h++) {
            expectedState = STATE_ONE;
            assertEquals(expectedState, EcowattHandler.getHourSignalState(apiResponse, dateTime.plusHours(h)));
        }

        dateTime = dateTime.plusDays(1);
        for (int h = 0; h < 24; h++) {
            expectedState = h == 23 ? UnDefType.UNDEF : STATE_ONE;
            assertEquals(expectedState, EcowattHandler.getHourSignalState(apiResponse, dateTime.plusHours(h)));
        }

        dateTime = dateTime.plusDays(1);
        assertEquals(UnDefType.UNDEF, EcowattHandler.getHourSignalState(apiResponse, dateTime));
    }

    @Test
    public void getHourSignalStateWithOtherOffset() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2022-09-19T00:30:00+05:00");
        State expectedState;
        for (int h = 0; h < 24; h++) {
            switch (h) {
                case 0:
                case 1:
                case 2:
                    expectedState = UnDefType.UNDEF;
                    break;
                case 10:
                case 14:
                case 22:
                    expectedState = STATE_TWO;
                    break;
                case 11:
                case 12:
                case 13:
                case 23:
                    expectedState = STATE_THREE;
                    break;
                default:
                    expectedState = STATE_ONE;
            }
            assertEquals(expectedState, EcowattHandler.getHourSignalState(apiResponse, dateTime.plusHours(h)));
        }
    }
}
