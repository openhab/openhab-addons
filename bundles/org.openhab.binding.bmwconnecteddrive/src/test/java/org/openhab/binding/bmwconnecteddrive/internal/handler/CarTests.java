/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.CarType;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarTests} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CarTests {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);
    // ConnectedCarHandler cch = new Co

    @Before
    public void setup() {
    }

    @Test
    public void testi3Rex() {
        String content = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        HttpClient hc = mock(HttpClient.class);
        ConnectedCarHandler cch = new ConnectedCarHandler(thing, hc, CarType.ELECTRIC_REX.toString(), true);
        ThingHandlerCallback tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        cch.updateRangeValues(content);

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        ;

        // ArgumentCaptor<List<ChannelUID>> channelCaptor;
        // ArgumentCaptor<List<State>> stateCaptor;

        verify(tc, times(12)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        List allChannels = channelCaptor.getAllValues();
        List allStates = stateCaptor.getAllValues();
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
        assertEquals("Results ", 12, allChannels.size());
    }

    @Test
    public void testi3RexStatus() {
        String content = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        HttpClient hc = mock(HttpClient.class);
        ConnectedCarHandler cch = new ConnectedCarHandler(thing, hc, CarType.ELECTRIC_REX.toString(), true);
        ThingHandlerCallback tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        cch.updateVehicleStatus(content);

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        ;

        // ArgumentCaptor<List<ChannelUID>> channelCaptor;
        // ArgumentCaptor<List<State>> stateCaptor;

        verify(tc, times(6)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        List allChannels = channelCaptor.getAllValues();
        List allStates = stateCaptor.getAllValues();
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }
}
