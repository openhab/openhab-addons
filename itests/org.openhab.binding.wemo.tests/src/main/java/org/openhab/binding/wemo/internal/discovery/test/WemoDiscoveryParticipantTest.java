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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.discovery.WemoDiscoveryParticipant;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link WemoDiscoveryParticipant}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoDiscoveryParticipantTest {
    UpnpDiscoveryParticipant participant = new WemoDiscoveryParticipant();

    private static final String DEVICE_UDN = GenericWemoOSGiTest.DEVICE_MANUFACTURER + "_3434xxx";
    private static final String DEVICE_FRIENDLY_NAME = "Wemo Test";

    RemoteDevice createUpnpDevice(String modelName)
            throws MalformedURLException, ValidationException, URISyntaxException {
        return new RemoteDevice(new RemoteDeviceIdentity(new UDN(DEVICE_UDN), 60, new URL("http://wemo"), null, null),
                new DeviceType("namespace", "type"),
                new DeviceDetails(DEVICE_FRIENDLY_NAME,
                        new ManufacturerDetails(GenericWemoOSGiTest.DEVICE_MANUFACTURER), new ModelDetails(modelName),
                        new URI("http://1.2.3.4/")),
                (RemoteService) null);
    }

    @Test
    public void assertDiscoveryResultForSocketIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_SOCKET);
    }

    @Test
    public void assertDiscoveryRresultForInsightIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_INSIGHT);
    }

    @Test
    public void assertDiscoveryResultForLightswitchIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_LIGHTSWITCH);
    }

    @Test
    public void assertDiscoveryResultForMotionIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_MOTION);
    }

    @Test
    public void assertDiscoveryResultForBridgeIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_BRIDGE);
    }

    @Test
    public void assertDiscoveryResultForMakerIsCorrect()
            throws MalformedURLException, ValidationException, URISyntaxException {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_MAKER);
    }

    public void testDiscoveryResult(ThingTypeUID thingTypeUid)
            throws MalformedURLException, ValidationException, URISyntaxException {
        String thingTypeId = thingTypeUid.getId();
        RemoteDevice device = createUpnpDevice(thingTypeId);
        DiscoveryResult result = participant.createResult(device);

        assertNotNull(result);
        assertThat(result.getThingUID(), is(new ThingUID(thingTypeUid, DEVICE_UDN)));
        assertThat(result.getThingTypeUID(), is(thingTypeUid));
        assertThat(result.getBridgeUID(), is(nullValue()));
        assertThat(result.getProperties().get(WemoBindingConstants.UDN), is(DEVICE_UDN));
        assertThat(result.getRepresentationProperty(), is(WemoBindingConstants.UDN));
    }
}
