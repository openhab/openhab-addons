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

import static org.hamcrest.CoreMatchers.is;
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
import org.openhab.binding.wemo.internal.handler.WemoLightHandler;
import org.openhab.binding.wemo.internal.test.GenericWemoLightOSGiTestParent;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Tests for {@link WemoLightHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoLightHandlerOSGiTest extends GenericWemoLightOSGiTestParent {

    private static final String GET_ACTION = "GetDeviceStatus";
    private static final String SET_ACTION = "SetDeviceStatus";

    @BeforeEach
    public void setUp() throws IOException {
        setUpServices();
    }

    @AfterEach
    public void tearDown() {
        removeThing();
    }

    @Test
    public void handleONcommandForBRIGHTNESSchannel()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Command command = OnOffType.ON;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS;

        // Command ON for this channel sends the following data to the device
        String action = SET_ACTION;
        // ON is equal to brightness value of 255
        String value = "255:0";
        String capitability = "10008";

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    @Test
    public void handlePercentCommandForBRIGHTNESSChannel()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        // Set brightness value to 20 Percent
        Command command = new PercentType(20);
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS;

        String action = SET_ACTION;
        // 20 Percent brightness is equal to a brightness value of 51
        String value = "51:0";
        String capitability = "10008";

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    @Test
    public void handleIncreaseCommandForBRIGHTNESSchannel()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        // The value is increased by 5 Percents by default
        Command command = IncreaseDecreaseType.INCREASE;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS;

        String action = SET_ACTION;
        // 5 Percents brightness is equal to a brightness value of 12
        String value = "12:0";
        String capitability = "10008";

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    @Test
    public void handleDecreaseCommandForBRIGHTNESSchannel()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        // The value can not be decreased below 0
        Command command = IncreaseDecreaseType.DECREASE;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS;

        String action = SET_ACTION;
        String value = "0:0";
        String capitability = "10008";

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    @Test
    public void handleOnCommandForSTATEChannel()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Command command = OnOffType.ON;
        String channelID = WemoBindingConstants.CHANNEL_STATE;

        // Command ON for this channel sends the following data to the device
        String action = SET_ACTION;
        String value = "1";
        String capitability = "10006";

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    @Test
    public void handleREFRESHCommandForChannelSTATE()
            throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Command command = RefreshType.REFRESH;
        String channelID = WemoBindingConstants.CHANNEL_STATE;

        String action = GET_ACTION;
        String value = null;
        String capitability = null;

        assertRequestForCommand(channelID, command, action, value, capitability);
    }

    private void assertRequestForCommand(String channelID, Command command, String action, String value,
            String capitability) throws MalformedURLException, URISyntaxException, ValidationException, IOException {
        Thing bridge = createBridge(BRIDGE_TYPE_UID);

        Thing thing = createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert(() -> {
            assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));
        });

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        });

        // The device is registered as UPnP Device after the initialization, this will ensure that the polling job will
        // not start
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, DEVICE_MODEL_NAME);

        ThingUID thingUID = new ThingUID(THING_TYPE_UID, TEST_THING_ID);
        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);

        handler.handleCommand(channelUID, command);

        ArgumentCaptor<String> captur = ArgumentCaptor.forClass(String.class);

        verify(mockCaller, atLeastOnce()).executeCall(any(), any(), captur.capture());

        List<String> results = captur.getAllValues();
        // we might catch multiple calls. iterate through them to find the one matching our settings
        boolean found = false;
        for (String result : results) {
            boolean matchesCapability = result.contains("CapabilityID&gt;" + capitability + "&lt;");
            boolean matchesValue = result.contains("CapabilityValue&gt;" + value + "&lt;");
            boolean matchesAction = result.contains("<s:Body><u:" + action);

            if (action != null) {
                if (!matchesAction) {
                    continue;
                }
            }
            if (capitability != null) {
                if (!matchesCapability) {
                    continue;
                }
            }
            if (value != null) {
                if (!matchesValue) {
                    continue;
                }
            }
            found = true;
            break;
        }
        assertTrue(found);
    }
}
