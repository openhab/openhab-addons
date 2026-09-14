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
package org.openhab.binding.netatmo.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * @author Martin Littkovsky - Initial contribution
 */
public class NetatmoExceptionTest {
    private static NADeserializer gson;

    @BeforeAll
    public static void init() {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        gson = new NADeserializer(timeZoneProvider);
    }

    @Test
    public void testKnownServiceErrorKeepsExistingFormat() throws NetatmoException {
        String body = """
                {\
                  "error": {\
                    "code": 26,\
                    "message": "Usage max reached"\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError);

        assertEquals("Rest call failed: statusCode=MAXIMUM_USAGE_REACHED, message=Usage max reached",
                exception.getMessage());
    }

    @Test
    public void testKnownServiceErrorIgnoresHttpContextEvenIfProvided() throws NetatmoException {
        String body = """
                {\
                  "error": {\
                    "code": 26,\
                    "message": "Usage max reached"\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError, 503, "26");

        assertEquals("Rest call failed: statusCode=MAXIMUM_USAGE_REACHED, message=Usage max reached",
                exception.getMessage());
    }

    @Test
    public void testUnclassifiedErrorKeepsHttpStatusAndRawCode() throws NetatmoException {
        // code 50 does not map to any ServiceError value, so it classifies as UNKNOWN
        String body = """
                {\
                  "error": {\
                    "code": 50,\
                    "message": "Service temporarily unavailable"\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError, 503, "50");

        assertEquals("Service temporarily unavailable (HTTP 503, error code 50)", exception.getMessage());
    }

    @Test
    public void testUnclassifiedErrorWithoutRawCodeOmitsCodeSuffix() throws NetatmoException {
        String body = """
                {\
                  "error": {\
                    "code": 50,\
                    "message": "Service temporarily unavailable"\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError, 503, null);

        assertEquals("Service temporarily unavailable (HTTP 503)", exception.getMessage());
    }

    @Test
    public void testUnclassifiedErrorWithEmptyMessageAvoidsLeadingSpace() throws NetatmoException {
        String body = """
                {\
                  "error": {\
                    "code": 50,\
                    "message": ""\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError, 503, "50");

        assertEquals("(HTTP 503, error code 50)", exception.getMessage());
    }

    @Test
    public void testUnclassifiedErrorWithoutHttpContextKeepsPlainMessage() throws NetatmoException {
        String body = """
                {\
                  "error": {\
                    "code": 50,\
                    "message": "Service temporarily unavailable"\
                  }\
                }\
                """;
        ApiError apiError = gson.deserialize(ApiError.class, body);

        NetatmoException exception = new NetatmoException(apiError);

        assertEquals("Service temporarily unavailable", exception.getMessage());
    }
}
