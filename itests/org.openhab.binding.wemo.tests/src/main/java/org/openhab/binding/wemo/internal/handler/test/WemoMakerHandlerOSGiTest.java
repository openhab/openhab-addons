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
package org.openhab.binding.wemo.internal.handler.test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jupnp.model.ValidationException;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.handler.WemoMakerHandler;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Tests for {@link WemoMakerHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoMakerHandlerOSGiTest extends GenericWemoOSGiTest {

    // Specific Thing information
    private static final String DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_RELAY;
    private static final String DEFAULT_TEST_CHANNEL_TYPE = "Switch";
    private static final ThingTypeUID THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MAKER;

    // Specific UpnP service information
    private static final String MODEL = THING_TYPE_UID.getId();
    private static final String BASIC_EVENT_SERVICE_ID = "basicevent";
    private static final String SERVICE_NUMBER = "1";

    @BeforeEach
    public void setUp() throws IOException {
        setUpServices();
    }

    @AfterEach
    public void tearDown() {
        removeThing();
    }

    @Test
    public void assertThatThingHandlesOnOffCommandCorrectly()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Command command = OnOffType.OFF;

        Thing thing = createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
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
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Command command = RefreshType.REFRESH;

        Thing thing = createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
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
