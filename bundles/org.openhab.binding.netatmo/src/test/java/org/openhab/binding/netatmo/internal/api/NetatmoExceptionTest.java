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
        // (httpStatus, rawErrorCode) are only used by getMessage() for the UNKNOWN case - a caller that passes
        // them for an ApiError that DOES classify (e.g. by mistake, or via a generic call site) must not see
        // them leak into the message; the classified format takes precedence silently.
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
        // code 50 is not one of the 24 classified ServiceError values (the 25-value enum includes the UNKNOWN
        // sentinel itself, mapped to code 99), so it is classified as UNKNOWN - this is the case that used to
        // lose the HTTP status and the raw code entirely.
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
        // extractRawErrorCode() in ApiBridgeHandler returns null when the body carries no code at all - the
        // ", error code ..." clause must then disappear entirely rather than print a placeholder that could be
        // confused with the ServiceError.UNKNOWN enum name.
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
        // an explicit empty "message" - the "<message> (HTTP ...)" join must not leave a stray leading space.
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
        // the plain ApiError-only constructor (no HTTP status/raw code supplied) must keep behaving exactly
        // as before this change for any caller that does not go through ApiBridgeHandler.executeUri().
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
