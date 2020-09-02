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

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ErrorResponseTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ErrorResponseTest {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);
    // ConnectedCarHandler cch = new Co

    @Nullable
    ArgumentCaptor<ChannelUID> channelCaptor;
    @Nullable
    ArgumentCaptor<State> stateCaptor;
    @Nullable
    ThingHandlerCallback tc;
    @Nullable
    ConnectedCarHandler cch;
    @Nullable
    List<ChannelUID> allChannels;
    @Nullable
    List<State> allStates;
    @Nullable
    String driveTrain;
    boolean imperial;

    /**
     * Prepare environment for Vehicle Status Updates
     */
    public void setup(String type, boolean imperial) {
        driveTrain = type;
        this.imperial = imperial;
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        HttpClient hc = mock(HttpClient.class);
        cch = new ConnectedCarHandler(thing, hc, type, imperial);
        tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    // private boolean testCar(@Nullable String statusContent, int callbacksExpected) {
    // assertNotNull(statusContent);
    //
    // cch.vehicleStatusCallback.onResponse(Optional.of(statusContent));
    // verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
    // allChannels = channelCaptor.getAllValues();
    // allStates = stateCaptor.getAllValues();
    //
    // assertNotNull(driveTrain);
    // StatusWrapper checker = new StatusWrapper(driveTrain, imperial, statusContent);
    // trace();
    // return checker.checkResults(allChannels, allStates);
    // }
    //
    // private void trace() {
    // for (int i = 0; i < allChannels.size(); i++) {
    // logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
    // }
    // }

    @Test
    public void testErrorResponseCallbacks() {
        String error = "{\"error\":true,\"reason\":\"offline\"}";
        setup("BEV", false);
        cch.vehicleStatusCallback.onResponse(Optional.of(error));
        cch.allTripsCallback.onResponse(Optional.of(error));
        cch.lastTripCallback.onResponse(Optional.of(error));
    }
}
