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
package org.openhab.binding.tradfri.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;
import static org.openhab.binding.tradfri.internal.config.TradfriDeviceConfig.CONFIG_ID;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests cases for {@link TradfriGatewayHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriHandlerOSGiTest extends JavaOSGiTest {

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;

    @BeforeEach
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        Map<String, Object> properties = new HashMap<>();
        properties.put(GATEWAY_CONFIG_HOST, "1.2.3.4");
        properties.put(GATEWAY_CONFIG_IDENTITY, "identity");
        properties.put(GATEWAY_CONFIG_PRE_SHARED_KEY, "pre-shared-secret-key");
        bridge = BridgeBuilder.create(GATEWAY_TYPE_UID, "1").withLabel("My Gateway")
                .withConfiguration(new Configuration(properties)).build();

        properties = new HashMap<>();
        properties.put(CONFIG_ID, "65537");
        thing = ThingBuilder.create(THING_TYPE_DIMMABLE_LIGHT, "1").withLabel("My Bulb").withBridge(bridge.getUID())
                .withConfiguration(new Configuration(properties)).build();
    }

    @AfterEach
    public void tearDown() {
        managedThingProvider.remove(thing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void creationOfTradfriGatewayHandler() {
        assertThat(bridge.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));

        configurationOfTradfriGatewayHandler();
    }

    private void configurationOfTradfriGatewayHandler() {
        Configuration configuration = bridge.getConfiguration();
        assertThat(configuration, is(notNullValue()));

        assertThat(configuration.get(GATEWAY_CONFIG_HOST), is("1.2.3.4"));
        assertThat(configuration.get(GATEWAY_CONFIG_IDENTITY), is("identity"));
        assertThat(configuration.get(GATEWAY_CONFIG_PRE_SHARED_KEY), is("pre-shared-secret-key"));
    }

    @Test
    public void creationOfTradfriLightHandler() {
        assertThat(thing.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        managedThingProvider.add(thing);
        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));

        configurationOfTradfriLightHandler();
    }

    private void configurationOfTradfriLightHandler() {
        Configuration configuration = thing.getConfiguration();
        assertThat(configuration, is(notNullValue()));

        assertThat(configuration.get(CONFIG_ID), is("65537"));
    }
}
