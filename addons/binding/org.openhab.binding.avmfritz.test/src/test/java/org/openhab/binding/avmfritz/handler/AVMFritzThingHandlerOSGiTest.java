/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Tests for {@link AVMFritzThingHandlerOSGiTest}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public abstract class AVMFritzThingHandlerOSGiTest extends JavaOSGiTest {

    private static HttpClient httpClient;

    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private ManagedThingProvider managedThingProvider;

    protected Bridge bridge;
    protected BoxHandler bridgeHandler;

    @BeforeClass
    public static void setUpClass() throws Exception {
        httpClient = new HttpClient();
        httpClient.start();
    }

    @Before
    public void setUp() {
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertNotNull("Could not get ManagedThingProvider", managedThingProvider);

        bridge = buildBridge();
        assertNotNull(bridge.getConfiguration());

        managedThingProvider.add(bridge);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        bridgeHandler = new BoxHandler(bridge, httpClient);
        assertNotNull(bridgeHandler);

        bridgeHandler.setCallback(callback);

        assertNull(bridge.getHandler());
        bridge.setHandler(bridgeHandler);
        assertNotNull(bridge.getHandler());

        bridgeHandler.initialize();
    }

    @After
    public void tearDown() {
        if (bridge != null) {
            managedThingProvider.remove(bridge.getUID());
        }

        unregisterService(volatileStorageService);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        httpClient.stop();
    }

    private Bridge buildBridge() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_IP_ADDRESS, "fritz.box");
        properties.put(CONFIG_PROTOCOL, "http");
        properties.put(CONFIG_USER, "user");
        properties.put(CONFIG_PASSWORD, "password");
        properties.put(CONFIG_POLLING_INTERVAL, 15);
        properties.put(CONFIG_SYNC_TIMEOUT, 2000);

        return BridgeBuilder.create(BRIDGE_THING_TYPE, "1").withLabel(BOX_MODEL_NAME)
                .withConfiguration(new Configuration(properties)).build();
    }
}
