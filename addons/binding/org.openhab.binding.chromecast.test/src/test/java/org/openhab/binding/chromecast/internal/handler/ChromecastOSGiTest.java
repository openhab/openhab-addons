/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.openhab.binding.chromecast.internal.ChromecastBindingConstants.THING_TYPE_CHROMECAST;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.chromecast.internal.ChromecastBindingConstants;

/**
 * Tests for {@link ChromecastHandler}.
 *
 * @author François Pelsser, Wouter Born - Initial contribution
 */
public class ChromecastOSGiTest extends JavaOSGiTest {

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    @Before
    public void setUp() {
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test
    public void creationOfChromecastHandler() {
        ChromecastHandler handler = getService(ThingHandler.class, ChromecastHandler.class);
        assertThat(handler, is(nullValue()));

        Configuration configuration = new Configuration();
        configuration.put(ChromecastBindingConstants.HOST, "hostname");

        Thing thing = ThingBuilder.create(THING_TYPE_CHROMECAST, "tv").withConfiguration(configuration).build();
        managedThingProvider.add(thing);

        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));
        assertThat(thing.getConfiguration(), is(notNullValue()));
        assertThat(thing.getHandler(), is(instanceOf(ChromecastHandler.class)));
    }

}
