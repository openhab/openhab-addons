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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.handler.NestRedirectUrlSupplier;
import org.openhab.binding.nest.test.NestTestBridgeHandler;

/**
 * Tests cases for {@link NestBridgeHandler}.
 *
 * @author David Bennett - Initial contribution
 */
public class NestBridgeHandlerTest {

    private ThingHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge bridge;

    @Mock
    private Configuration configuration;

    @Mock
    private NestRedirectUrlSupplier redirectUrlSupplier;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new NestTestBridgeHandler(bridge, "http://localhost");
        handler.setCallback(callback);
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
