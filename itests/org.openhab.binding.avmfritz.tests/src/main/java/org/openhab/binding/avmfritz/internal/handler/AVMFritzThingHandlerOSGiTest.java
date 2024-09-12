/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

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

    @BeforeAll
    public static void setUpClass() throws Exception {
        httpClient.start();
    }

    @BeforeEach
    public void setUp() {
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertNotNull(managedThingProvider, "Could not get ManagedThingProvider");

        bridge = buildBridge();
        assertNotNull(bridge.getConfiguration());

        managedThingProvider.add(bridge);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        bridgeHandler = new BoxHandler(bridge, httpClient, mock(AVMFritzDynamicCommandDescriptionProvider.class));
        assertNotNull(bridgeHandler);

        bridgeHandler.setCallback(callback);

        ThingHandler oldHandler = bridge.getHandler();
        if (oldHandler != null) {
            oldHandler.dispose();
        }
        bridge.setHandler(bridgeHandler);
        assertNotNull(bridge.getHandler());

        bridgeHandler.initialize();
    }

    @AfterEach
    public void tearDown() {
        if (bridge != null) {
            managedThingProvider.remove(bridge.getUID());
        }

        unregisterService(volatileStorageService);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        httpClient.stop();
    }

    private Bridge buildBridge() {
        return BridgeBuilder.create(BRIDGE_THING_TYPE, "1") //
                .withLabel(BOX_MODEL_NAME) //
                .withConfiguration(new Configuration(Map.of( //
                        CONFIG_IP_ADDRESS, "fritz.box", //
                        CONFIG_PROTOCOL, "http", //
                        CONFIG_USER, "user", //
                        CONFIG_PASSWORD, "password", //
                        CONFIG_POLLING_INTERVAL, 15, //
                        CONFIG_SYNC_TIMEOUT, 2000))) //
                .build();
    }
}
