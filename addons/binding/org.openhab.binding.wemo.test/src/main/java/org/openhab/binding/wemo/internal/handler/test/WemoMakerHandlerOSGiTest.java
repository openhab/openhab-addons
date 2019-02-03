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
package org.openhab.binding.wemo.internal.handler.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.model.ValidationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.handler.WemoMakerHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;

/**
 * Tests for {@link WemoMakerHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoMakerHandlerOSGiTest extends GenericWemoOSGiTest {

    // Specific Thing information
    private final String DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_RELAY;
    private final String DEFAULT_TEST_CHANNEL_TYPE = "Switch";
    private final ThingTypeUID THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MAKER;

    // Specific UpnP service information
    private final String MODEL = THING_TYPE_UID.getId();
    private final String BASIC_EVENT_SERVICE_ID = "basicevent";
    private final String SERVICE_NUMBER = "1";

    @Before
    public void setUp() throws IOException {
        setUpServices();
    }

    @After
    public void tearDown() {
        removeThing();
    }

    @Test
    public void assertThatThingHandlesOnOffCommandCorrectly()
            throws MalformedURLException, URISyntaxException, ValidationException {
        Command command = OnOffType.OFF;

        WemoHttpCall mockCaller = Mockito.spy(new WemoHttpCall());
        Thing thing = createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE, mockCaller);

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        });

        // The Device is registered as UPnP Device after the initialization, this will ensure that the polling job will
        // not start
        addUpnpDevice(BASIC_EVENT_SERVICE_ID, SERVICE_NUMBER, MODEL);

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL);
        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);
        handler.handleCommand(channelUID, command);

        ArgumentCaptor<String> captur = ArgumentCaptor.forClass(String.class);
        verify(mockCaller, atLeastOnce()).executeCall(any(), any(), captur.capture());

        List<String> results = captur.getAllValues();
        boolean found = false;
        for (String result : results) {
            // Binary state 0 is equivalent to OFF
            if (result.contains("<BinaryState>0</BinaryState>")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void assertThatThingHandlesREFRESHCommand()
            throws MalformedURLException, URISyntaxException, ValidationException {
        Command command = RefreshType.REFRESH;

        WemoHttpCall mockCaller = Mockito.spy(new WemoHttpCall());
        Thing thing = createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE, mockCaller);

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        });

        // The Device is registered as UPnP Device after the initialization, this will ensure that the polling job will
        // not start
        addUpnpDevice(BASIC_EVENT_SERVICE_ID, SERVICE_NUMBER, MODEL);

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL);
        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);
        handler.handleCommand(channelUID, command);

        ArgumentCaptor<String> captur = ArgumentCaptor.forClass(String.class);
        verify(mockCaller, atLeastOnce()).executeCall(any(), any(), captur.capture());

        List<String> results = captur.getAllValues();
        boolean found = false;
        for (String result : results) {
            if (result.contains("<u:GetAttributes xmlns:u=\"urn:Belkin:service:deviceevent:1\"></u:GetAttributes>")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    private void removeThing() {
        if (thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID());
            assertThat(removedThing, is(notNullValue()));
        }

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.UNINITIALIZED));
        });
    }
}
