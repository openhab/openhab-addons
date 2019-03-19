/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverConnection;
import org.openhab.binding.onewire.internal.owserver.OwserverConnectionState;

/**
 * Tests cases for {@link OwserverBridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwserverBridgeHandlerTest extends JavaTest {

    private static final String TEST_HOST = "foo.bar";
    private static final int TEST_PORT = 4711;
    Map<String, Object> bridgeProperties = new HashMap<>();

    @Mock
    OwserverConnection owserverConnection;

    Bridge bridge;

    @Mock
    ThingHandlerCallback thingHandlerCallback;

    OwserverBridgeHandler bridgeHandler;

    @Before
    public void setup() {
        bridgeProperties.put(CONFIG_ADDRESS, TEST_HOST);
        bridgeProperties.put(CONFIG_PORT, TEST_PORT);

        initMocks(this);

        bridge = BridgeBuilder.create(THING_TYPE_OWSERVER, "owserver").withLabel("owserver")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

        bridgeHandler = new OwserverBridgeHandler(bridge, owserverConnection);
        bridgeHandler.getThing().setHandler(bridgeHandler);
        bridgeHandler.setCallback(thingHandlerCallback);
    }

    @After
    public void tearDown() {
        bridgeHandler.dispose();
    }

    @Test
    public void testInitializationStartsConnectionWithOptions() {
        bridgeHandler.initialize();

        Mockito.verify(owserverConnection).setHost(TEST_HOST);
        Mockito.verify(owserverConnection).setPort(TEST_PORT);

        Mockito.verify(owserverConnection, timeout(5000)).start();
    }

    @Test
    public void testInitializationReportsRefreshableOnSuccessfullConnection() {
        Mockito.doAnswer(answer -> {
            bridgeHandler.reportConnectionState(OwserverConnectionState.OPENED);
            return null;
        }).when(owserverConnection).start();

        bridgeHandler.initialize();

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        waitForAssert(() -> {
            verify(thingHandlerCallback, times(2)).statusUpdated(eq(bridge), statusCaptor.capture());
        });
        assertThat(statusCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));
        assertThat(statusCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.ONLINE));

        waitForAssert(() -> assertTrue(bridgeHandler.isRefreshable()));
    }

    @Test
    public void testInitializationReportsNotRefreshableOnFailedConnection() {
        Mockito.doAnswer(answer -> {
            bridgeHandler.reportConnectionState(OwserverConnectionState.FAILED);
            return null;
        }).when(owserverConnection).start();

        bridgeHandler.initialize();

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        waitForAssert(() -> {
            verify(thingHandlerCallback, times(2)).statusUpdated(eq(bridge), statusCaptor.capture());
        });
        assertThat(statusCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));
        assertThat(statusCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertFalse(bridgeHandler.isRefreshable()));
    }

}
