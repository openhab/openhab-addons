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
package org.openhab.binding.avmfritz.internal.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.avmfritz.internal.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicStateDescriptionProvider;

/**
 * Tests for {@link AVMFritzThingHandlerOSGiTest}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AVMFritzThingHandlerOSGiTest extends JavaOSGiTest {

    private static HttpClient httpClient = new HttpClient();

    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private @NonNullByDefault({}) ManagedThingProvider managedThingProvider;

    protected @NonNullByDefault({}) Bridge bridge;
    protected @NonNullByDefault({}) BoxHandler bridgeHandler;

    @BeforeClass
    public static void setUpClass() throws Exception {
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

        bridgeHandler = new BoxHandler(bridge, httpClient, new AVMFritzDynamicStateDescriptionProvider());
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
