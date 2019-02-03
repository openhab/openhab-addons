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
package org.openhab.binding.tradfri.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.tradfri.internal.discovery.TradfriDiscoveryParticipant;

/**
 * Tests for {@link TradfriDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriDiscoveryParticipantOSGITest extends JavaOSGiTest {

    private MDNSDiscoveryParticipant discoveryParticipant;

    @Mock
    private ServiceInfo tradfriGateway;

    @Mock
    private ServiceInfo otherDevice;

    @Before
    public void setUp() {
        initMocks(this);
        discoveryParticipant = getService(MDNSDiscoveryParticipant.class, TradfriDiscoveryParticipant.class);

        when(tradfriGateway.getType()).thenReturn("_coap._udp.local.");
        when(tradfriGateway.getName()).thenReturn("gw:12-34-56-78-90-ab");
        when(tradfriGateway.getHostAddresses()).thenReturn(new String[] { "192.168.0.5" });
        when(tradfriGateway.getPort()).thenReturn(1234);
        when(tradfriGateway.getPropertyString("version")).thenReturn("1.1");

        when(otherDevice.getType()).thenReturn("_coap._udp.local.");
        when(otherDevice.getName()).thenReturn("something");
        when(otherDevice.getHostAddresses()).thenReturn(new String[] { "192.168.0.5" });
        when(otherDevice.getPort()).thenReturn(1234);
        when(otherDevice.getPropertyString("version")).thenReturn("1.1");
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().size(), is(1));
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().iterator().next(), is(GATEWAY_TYPE_UID));
    }

    @Test
    public void correctThingUID() {
        when(tradfriGateway.getName()).thenReturn("gw:12-34-56-78-90-ab");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        when(tradfriGateway.getName()).thenReturn("gw:1234567890ab");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        when(tradfriGateway.getName()).thenReturn("gw-12-34-56-78-90-ab");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        when(tradfriGateway.getName()).thenReturn("gw:1234567890ab");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        when(tradfriGateway.getName()).thenReturn("gw:1234567890abServiceInfo");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        when(tradfriGateway.getName()).thenReturn("gw:12-34-56-78-90-ab-service-info");
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));

        // restore original value
        when(tradfriGateway.getName()).thenReturn("gw:12-34-56-78-90-ab");
    }

    @Test
    public void validDiscoveryResult() {
        DiscoveryResult result = discoveryParticipant.createResult(tradfriGateway);

        assertNotNull(result);
        assertThat(result.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("1.1"));
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID(), is(new ThingUID("tradfri:gateway:gw1234567890ab")));
        assertThat(result.getThingTypeUID(), is(GATEWAY_TYPE_UID));
        assertThat(result.getBridgeUID(), is(nullValue()));
        assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("IKEA of Sweden"));
        assertThat(result.getProperties().get(GATEWAY_CONFIG_HOST), is("192.168.0.5"));
        assertThat(result.getProperties().get(GATEWAY_CONFIG_PORT), is(1234));
        assertThat(result.getRepresentationProperty(), is(GATEWAY_CONFIG_HOST));
    }

    @Test
    public void noThingUIDForUnknownDevice() {
        when(otherDevice.getName()).thenReturn("something");
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));

        when(otherDevice.getName()).thenReturn("gw_1234567890ab");
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));

        when(otherDevice.getName()).thenReturn("gw:12-3456--7890-ab");
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));

        when(otherDevice.getName()).thenReturn("gw1234567890ab");
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));

        // restore original value
        when(otherDevice.getName()).thenReturn("something");
    }

    @Test
    public void noDiscoveryResultForUnknownDevice() {
        assertThat(discoveryParticipant.createResult(otherDevice), is(nullValue()));
    }
}
