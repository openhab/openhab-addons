/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class HttpUtilTest {
    @Test
    public void whenTheResponseHasARetryAfterHeaderThenItIsParsedAndPassedWithTheException() {
        // given:
        HttpFields httpFields = mock(HttpFields.class);
        when(httpFields.containsKey("Retry-After")).thenReturn(true);
        when(httpFields.get("Retry-After")).thenReturn("100");

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(429);
        when(response.getReason()).thenReturn("Too many requests!");
        when(response.getHeaders()).thenReturn(httpFields);

        // when:
        try {
            HttpUtil.checkHttpSuccess(response);
            fail();
        } catch (TooManyRequestsException e) {
            // then:
            assertEquals(100L, e.getSecondsUntilRetry());
        }
    }
}
