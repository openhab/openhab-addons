/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nest.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import javax.ws.rs.client.ClientBuilder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.handler.NestRedirectUrlSupplier;
import org.openhab.binding.nest.test.NestTestBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * Tests cases for {@link NestBridgeHandler}.
 *
 * @author David Bennett - Initial contribution
 */
public class NestBridgeHandlerTest {

    private ThingHandler handler;

    private AutoCloseable mocksCloseable;

    private @Mock Bridge bridge;
    private @Mock ThingHandlerCallback callback;
    private @Mock ClientBuilder clientBuilder;
    private @Mock Configuration configuration;
    private @Mock SseEventSourceFactory eventSourceFactory;
    private @Mock NestRedirectUrlSupplier redirectUrlSupplier;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = openMocks(this);
        handler = new NestTestBridgeHandler(bridge, clientBuilder, eventSourceFactory, "http://localhost");
        handler.setCallback(callback);
    }

    @AfterEach
    public void afterEach() throws Exception {
        mocksCloseable.close();
    }

    @SuppressWarnings("null")
    @Test
    public void initializeShouldCallTheCallback() {
        when(bridge.getConfiguration()).thenReturn(configuration);
        NestBridgeConfiguration bridgeConfig = new NestBridgeConfiguration();
        when(configuration.as(eq(NestBridgeConfiguration.class))).thenReturn(bridgeConfig);
        bridgeConfig.accessToken = "my token";

        // we expect the handler#initialize method to call the callback during execution and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of the thing.
        handler.initialize();

        // the argument captor will capture the argument of type ThingStatusInfo given to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // verify the interaction with the callback and capture the ThingStatusInfo argument:
        verify(callback).statusUpdated(eq(bridge), statusInfoCaptor.capture());
        // assert that the ThingStatusInfo given to the callback was build with the UNKNOWN status:
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertThat(thingStatusInfo.getStatus(), is(equalTo(ThingStatus.UNKNOWN)));
    }
}
