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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;
import org.openhab.core.thing.Bridge;

/**
 * Unit tests for the {@link BridgeHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class BridgeHandlerTest {

    @Nullable
    private BridgeHandler fixture;

    @Nullable
    private BoschHttpClient httpClient;

    @BeforeEach
    void beforeEach() {
        Bridge bridge = mock(Bridge.class);
        fixture = new BridgeHandler(bridge);
        httpClient = mock(BoschHttpClient.class);
        fixture.httpClient = httpClient;
    }

    @Test
    void postAction() throws InterruptedException, TimeoutException, ExecutionException {
        String endpoint = "/intrusion/actions/arm";
        String url = "https://127.0.0.1:8444/smarthome/intrusion/actions/arm";
        when(httpClient.getBoschSmartHomeUrl(endpoint)).thenReturn(url);
        Request mockRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), any(), any())).thenReturn(mockRequest);
        ArmActionRequest request = new ArmActionRequest();
        request.profileId = "0";

        fixture.postAction(endpoint, request);
        verify(httpClient).createRequest(eq(url), same(HttpMethod.POST), same(request));
        verify(mockRequest).send();
    }
}
