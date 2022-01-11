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
package org.openhab.binding.hue.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_SERIAL_NUMBER;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultFlag;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link org.openhab.binding.hue.internal.discovery.HueBridgeDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Höfer - Added representation
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueBridgeDiscoveryParticipantOSGITest extends JavaOSGiTest {

    UpnpDiscoveryParticipant discoveryParticipant;

    RemoteDevice hueDevice;
    RemoteDevice otherDevice;

    @BeforeEach
    public void setUp() {
        discoveryParticipant = getService(UpnpDiscoveryParticipant.class, HueBridgeDiscoveryParticipant.class);
        assertThat(discoveryParticipant, is(notNullValue()));

        try {
            final RemoteService remoteService = null;

            hueDevice = new RemoteDevice(
                    new RemoteDeviceIdentity(new UDN("123"), 60, new URL("http://hue"), null, null),
                    new DeviceType("namespace", "type"),
                    new DeviceDetails(new URL("http://1.2.3.4/"), "Hue Bridge", new ManufacturerDetails("Philips"),
                            new ModelDetails("Philips hue bridge"), "serial123", "upc", null),
                    remoteService);

            otherDevice = new RemoteDevice(
                    new RemoteDeviceIdentity(new UDN("567"), 60, new URL("http://acme"), null, null),
                    new DeviceType("namespace", "type"), new DeviceDetails("Some Device",
                            new ManufacturerDetails("Taiwan"), new ModelDetails("$%&/"), "serial567", "upc"),
                    remoteService);
        } catch (final ValidationException | MalformedURLException ex) {
            fail("Internal test error.");
        }
    }

    @AfterEach
    public void cleanUp() {
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().size(), is(1));
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().iterator().next(), is(THING_TYPE_BRIDGE));
    }

    @Test
    public void correctThingUID() {
        assertThat(discoveryParticipant.getThingUID(hueDevice), is(new ThingUID("hue:bridge:serial123")));
    }

    @Test
    public void validDiscoveryResult() {
        final DiscoveryResult result = discoveryParticipant.createResult(hueDevice);
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID(), is(new ThingUID("hue:bridge:serial123")));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_BRIDGE));
        assertThat(result.getBridgeUID(), is(nullValue()));
        assertThat(result.getProperties().get(HOST), is("1.2.3.4"));
        assertThat(result.getProperties().get(PROPERTY_SERIAL_NUMBER), is("serial123"));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_SERIAL_NUMBER));
    }

    @Test
    public void noThingUIDForUnknownDevice() {
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));
    }

    @Test
    public void noDiscoveryResultForUnknownDevice() {
        assertThat(discoveryParticipant.createResult(otherDevice), is(nullValue()));
    }
}
