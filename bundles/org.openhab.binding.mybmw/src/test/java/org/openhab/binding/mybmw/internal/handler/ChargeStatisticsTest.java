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
package org.openhab.binding.mybmw.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.VehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.ChargeStatisticWrapper;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChargeStatisticsTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ChargeStatisticsTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private static final int EXPECTED_UPDATE_COUNT = 3;

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
        MyBMWCommandOptionProvider cop = mock(MyBMWCommandOptionProvider.class);
        LocationProvider locationProvider = mock(LocationProvider.class);
        cch = new VehicleHandler(thing, cop, locationProvider, type);
        VehicleConfiguration vc = new VehicleConfiguration();
        vc.vin = Constants.ANONYMOUS;
        Optional<VehicleConfiguration> ovc = Optional.of(vc);
        cch.configuration = ovc;
        tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    private boolean testVehicle(String statusContent, int callbacksExpected,
            Optional<Map<String, State>> concreteChecks) {
        assertNotNull(statusContent);
        cch.chargeStatisticsCallback.onResponse(statusContent);
        verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        ChargeStatisticWrapper checker = new ChargeStatisticWrapper(statusContent);
        trace();
        return checker.checkResults(allChannels, allStates);
    }

    private void trace() {
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }

    @Test
    public void testI01REX() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/charge-statistics-de.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testG21() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/G21/charging-statistics_0.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testG30() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/G30/charging-statistics_0.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testI01NOREX() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), false);
        String content = FileReader
                .readFileInString("src/test/resources/responses/I01_NOREX/charging-statistics_0.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }
}
