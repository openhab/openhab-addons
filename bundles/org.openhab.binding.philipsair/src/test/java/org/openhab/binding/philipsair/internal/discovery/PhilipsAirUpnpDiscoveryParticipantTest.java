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
package org.openhab.binding.philipsair.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URISyntaxException;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.mockito.Mock;
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;

/**
 * Test cases for {@link PhilipsAirUpnpDiscoveryParticipantTest}. The tests
 * provide mocks for supporting entities using Mockito.
 * 
 * Covers recognition of things based on recieved UPNP info
 *
 * @author michalboronski - Initial contribution
 */
public class PhilipsAirUpnpDiscoveryParticipantTest extends JavaTest {

    @Mock
    RemoteDevice device;

    @Mock
    DeviceDetails deviceDetails;

    @Mock
    RemoteDeviceIdentity remoteDeviceIdentity;

    @Mock
    ModelDetails modelDetails;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetThingUID() throws URISyntaxException {
        PhilipsAirUpnpDiscoveryParticipant participant = new PhilipsAirUpnpDiscoveryParticipant();
        when(device.getDetails()).thenReturn(deviceDetails);

        when(device.getDisplayString()).thenReturn("dummy");
        when(device.getIdentity()).thenReturn(remoteDeviceIdentity);
        when(deviceDetails.getModelDetails()).thenReturn(modelDetails);

        when(modelDetails.getModelName()).thenReturn("AirPurifier");
        when(modelDetails.getModelNumber()).thenReturn("AC2889");
        when(remoteDeviceIdentity.getUdn()).thenReturn(new UDN("12345678-1234-1234-1234-e8c1d7007123"));

        ThingUID thing = participant.getThingUID(device);
        assertNotNull(thing);
        assertThat(thing.getThingTypeUID(), is(PhilipsAirBindingConstants.THING_TYPE_AC2889_10));

        when(modelDetails.getModelNumber()).thenReturn("AC3829");
        thing = participant.getThingUID(device);
        assertNotNull(thing);
        assertThat(thing.getThingTypeUID(), is(PhilipsAirBindingConstants.THING_TYPE_AC3829_10));

        when(modelDetails.getModelNumber()).thenReturn("AC1214");
        thing = participant.getThingUID(device);
        assertNotNull(thing);
        assertThat(thing.getThingTypeUID(), is(PhilipsAirBindingConstants.THING_TYPE_AC1214_10));

        when(modelDetails.getModelNumber()).thenReturn("AC2729");
        thing = participant.getThingUID(device);
        assertNotNull(thing);
        assertThat(thing.getThingTypeUID(), is(PhilipsAirBindingConstants.THING_TYPE_AC2729));

        when(modelDetails.getModelNumber()).thenReturn("AC3333");
        thing = participant.getThingUID(device);
        assertNotNull(thing);
        assertThat(thing.getThingTypeUID(), is(PhilipsAirBindingConstants.THING_TYPE_UNIVERSAL));
    }
}
