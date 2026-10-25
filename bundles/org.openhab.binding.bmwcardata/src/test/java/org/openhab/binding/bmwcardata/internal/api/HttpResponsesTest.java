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
package org.openhab.binding.bmwcardata.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Response;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HttpResponses}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class HttpResponsesTest {

    @Test
    void recognizesAnUnauthorizedResponseAnywhereInTheCauses() {
        HttpResponseException unauthorized = responseException(401);

        assertTrue(HttpResponses.isUnauthorizedResponse(unauthorized));
        // as thrown by Request.send() and wrapped by core's OAuth client service
        assertTrue(HttpResponses.isUnauthorizedResponse(new ExecutionException(unauthorized)));
        assertTrue(HttpResponses.isUnauthorizedResponse(new IOException(new ExecutionException(unauthorized))));
    }

    @Test
    void ignoresOtherFailures() {
        assertFalse(HttpResponses.isUnauthorizedResponse(responseException(403)));
        assertFalse(HttpResponses.isUnauthorizedResponse(new ExecutionException(new ConnectException("refused"))));
        assertFalse(HttpResponses.isUnauthorizedResponse(null));
    }

    private static HttpResponseException responseException(int status) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        return new HttpResponseException("HTTP " + status, response);
    }
}
