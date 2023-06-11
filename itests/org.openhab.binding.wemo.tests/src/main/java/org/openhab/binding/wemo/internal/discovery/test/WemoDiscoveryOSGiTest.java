/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.core.config.discovery.inbox.InboxPredicates.forThingUID;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.Device;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.discovery.WemoDiscoveryService;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link WemoDiscoveryService}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
@NonNullByDefault
public class WemoDiscoveryOSGiTest extends GenericWemoOSGiTest {

    // UpnP service information
    private static final String SERVICE_ID = "basicevent";
    private static final String SERVICE_NUMBER = "1";

    private @NonNullByDefault({}) Inbox inbox;

    @BeforeEach
    public void setUp() throws IOException {
        setUpServices();

        inbox = getService(Inbox.class);
        assertThat(inbox, is(notNullValue()));
    }

    @AfterEach
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
            Device device = devices.iterator().next();
            assertThat(device.getDetails().getModelDetails().getModelName(), is(model));
        });

        ThingUID thingUID = new ThingUID(thingType, DEVICE_UDN);

        waitForAssert(() -> {
            assertTrue(inbox.stream().anyMatch(forThingUID(thingUID)));
        });

        inbox.approve(thingUID, DEVICE_FRIENDLY_NAME, null);

        waitForAssert(() -> {
            Thing thing = thingRegistry.get(thingUID);
            assertThat(thing, is(notNullValue()));
        });
    }
}
