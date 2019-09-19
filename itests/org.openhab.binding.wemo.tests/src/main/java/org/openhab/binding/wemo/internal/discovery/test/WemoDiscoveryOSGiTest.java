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
package org.openhab.binding.wemo.internal.discovery.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.Device;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.discovery.WemoDiscoveryService;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;

/**
 * Tests for {@link WemoDiscoveryService}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoDiscoveryOSGiTest extends GenericWemoOSGiTest {

    // UpnP service information
    private final String SERVICE_ID = "basicevent";
    private final String SERVICE_NUMBER = "1";

    private Inbox inbox;

    @Before
    public void setUp() throws IOException {
        setUpServices();

        inbox = getService(Inbox.class);
        assertThat(inbox, is(notNullValue()));
    }

    @After
    public void tearDown() {
        List<DiscoveryResult> results = inbox.getAll();
        assertThat(results.size(), is(0));
    }

    @Test
    public void assertSupportedThingIsDiscovered()
            throws MalformedURLException, URISyntaxException, ValidationException {
        ThingTypeUID thingType = WemoBindingConstants.THING_TYPE_INSIGHT;
        String model = WemoBindingConstants.THING_TYPE_INSIGHT.getId();

        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, model);

        waitForAssert(() -> {
            Collection<Device> devices = mockUpnpService.getRegistry().getDevices();
            assertThat(devices.size(), is(1));
            Device device = null;
            for (Device d : devices) {
                device = d;
                break;
            }
            assertThat(device.getDetails().getModelDetails().getModelName(), is(model));
        });

        ThingUID thingUID = new ThingUID(thingType, DEVICE_UDN);

        waitForAssert(() -> {
            List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUID, null));
            assertFalse(results.isEmpty());
        });

        inbox.approve(thingUID, DEVICE_FRIENDLY_NAME);

        waitForAssert(() -> {
            Thing thing = thingRegistry.get(thingUID);
            assertThat(thing, is(notNullValue()));
        });
    }
}
