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
package org.openhab.binding.tradfri;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;
import static org.openhab.binding.tradfri.internal.config.TradfriDeviceConfig.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.tradfri.internal.handler.TradfriGatewayHandler;

/**
 * Tests cases for {@link TradfriGatewayHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriHandlerTest extends JavaOSGiTest {

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;

    @Before
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

    @After
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
