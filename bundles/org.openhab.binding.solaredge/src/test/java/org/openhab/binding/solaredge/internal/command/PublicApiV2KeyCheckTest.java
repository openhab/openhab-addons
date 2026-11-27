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
package org.openhab.binding.solaredge.internal.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.config.PublicApiVersion;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * Tests Monitoring API V2 rate-limit response propagation.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PublicApiV2KeyCheckTest {

    @Test
    public void rateLimitedResponseRequestsPollingPause() {
        SolarEdgeHandler handler = mock(SolarEdgeHandler.class);
        SolarEdgeConfiguration config = new SolarEdgeConfiguration();
        config.setPublicApiVersion(PublicApiVersion.V2);
        when(handler.getConfiguration()).thenReturn(config);
        Response response = mock(Response.class);
        HttpFields headers = mock(HttpFields.class);
        when(response.getStatus()).thenReturn(HttpStatus.TOO_MANY_REQUESTS_429);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get("Retry-After")).thenReturn("120");

        new PublicApiV2KeyCheck(handler, mock(StatusUpdateListener.class)).onSuccess(response);

        verify(handler).updatePublicApiV2RateLimit(null, null, "120", true);
    }
}
