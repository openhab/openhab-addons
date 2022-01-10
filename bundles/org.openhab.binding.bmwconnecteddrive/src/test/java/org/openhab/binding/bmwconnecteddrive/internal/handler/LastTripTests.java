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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.TripWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LastTripTests} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class LastTripTests {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private static final int HYBRID_CALL_TIMES = 6;

    @Nullable
    ArgumentCaptor<ChannelUID> channelCaptor;
    @Nullable
    ArgumentCaptor<State> stateCaptor;
    @Nullable
    ThingHandlerCallback tc;
    @Nullable
    VehicleHandler cch;
    @Nullable
    List<ChannelUID> allChannels;
    @Nullable
    List<State> allStates;
    String driveTrain = Constants.EMPTY;
    boolean imperial;

    /**
     * Prepare environment for Vehicle Status Updates
     */
    public void setup(String type, boolean imperial) {
        driveTrain = type;
        this.imperial = imperial;
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        BMWConnectedDriveOptionProvider op = mock(BMWConnectedDriveOptionProvider.class);
        cch = new VehicleHandler(thing, op, type, imperial);
        tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    private boolean testTrip(String statusContent, int callbacksExpected, Optional<Map<String, State>> concreteChecks) {
        assertNotNull(statusContent);
        cch.lastTripCallback.onResponse(statusContent);
        verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        TripWrapper checker = new TripWrapper(driveTrain, imperial, statusContent);
        trace();
        if (concreteChecks.isPresent()) {
            return checker.append(concreteChecks.get()).checkResults(allChannels, allStates);
        } else {
            return checker.checkResults(allChannels, allStates);
        }
    }

    private void trace() {
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }

    @Test
    public void testi3Rex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/last-trip.json");
        assertTrue(testTrip(content, HYBRID_CALL_TIMES, Optional.empty()));
    }

    @Test
    public void test530E() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/530E/last-trip.json");
        assertTrue(testTrip(content, HYBRID_CALL_TIMES, Optional.empty()));
    }

    @Test
    public void testi3RexImperial() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/last-trip.json");
        assertTrue(testTrip(content, HYBRID_CALL_TIMES, Optional.empty()));
    }

    @Test
    public void test530EImperial() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/530E/last-trip.json");
        assertTrue(testTrip(content, HYBRID_CALL_TIMES, Optional.empty()));
    }
}
