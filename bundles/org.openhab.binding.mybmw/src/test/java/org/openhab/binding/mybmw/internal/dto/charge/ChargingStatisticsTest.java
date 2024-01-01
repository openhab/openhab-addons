/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto.charge;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.MyBMWVehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.ChargingStatisticsWrapper;
import org.openhab.binding.mybmw.internal.handler.MyBMWCommandOptionProvider;
import org.openhab.binding.mybmw.internal.handler.VehicleHandler;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChargingStatisticsTest} is responsible for handling commands, which
 * are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - updated to new vehicles
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ChargingStatisticsTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private static final int EXPECTED_UPDATE_COUNT = 3;

    @Nullable
    ArgumentCaptor<ChannelUID> channelCaptor;
    @Nullable
    ArgumentCaptor<State> stateCaptor;
    @Nullable
    ThingHandlerCallback thingHandlerCallback;
    @Nullable
    VehicleHandler vehicleHandler;
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
        MyBMWCommandOptionProvider myBmwCommandOptionProvider = mock(MyBMWCommandOptionProvider.class);
        LocationProvider locationProvider = mock(LocationProvider.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        vehicleHandler = new VehicleHandler(thing, myBmwCommandOptionProvider, locationProvider, timeZoneProvider,
                type);
        MyBMWVehicleConfiguration vc = new MyBMWVehicleConfiguration();
        vc.setVin(Constants.ANONYMOUS);

        try {
            Field vehicleConfigurationField = VehicleHandler.class.getDeclaredField("vehicleConfiguration");
            vehicleConfigurationField.setAccessible(true);
            vehicleConfigurationField.set(vehicleHandler, Optional.of(vc));
        } catch (Exception e) {
            logger.error("vehicleConfiguration could not be set", e);
            fail("vehicleConfiguration could not be set", e);
        }
        thingHandlerCallback = mock(ThingHandlerCallback.class);
        vehicleHandler.setCallback(thingHandlerCallback);
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    private boolean testVehicle(String statusContent, int callbacksExpected,
            Optional<Map<String, State>> concreteChecks) {
        assertNotNull(statusContent);

        try {
            Method updateChargeStatisticsMethod = VehicleHandler.class.getDeclaredMethod("updateChargingStatistics",
                    ChargingStatisticsContainer.class, String.class);
            updateChargeStatisticsMethod.setAccessible(true);
            updateChargeStatisticsMethod.invoke(vehicleHandler,
                    JsonStringDeserializer.getChargingStatistics(statusContent), null);
        } catch (Exception e) {
            logger.error("chargeStatistics could not be set", e);
            fail("chargeStatistics could not be set", e);
        }
        verify(thingHandlerCallback, times(callbacksExpected)).stateUpdated(channelCaptor.capture(),
                stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        ChargingStatisticsWrapper checker = new ChargingStatisticsWrapper(statusContent);
        trace();
        return checker.checkResults(allChannels, allStates);
    }

    private void trace() {
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }

    @Test
    public void testBevIx() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.fileToString("responses/BEV/charging_statistics.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testBevIX3() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.fileToString("responses/BEV3/charging_statistics.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testIceX320d() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), false);
        String content = FileReader.fileToString("responses/ICE2/charging_statistics.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }

    @Test
    public void testPhev330e() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), false);
        String content = FileReader.fileToString("responses/PHEV2/charging_statistics.json");
        assertTrue(testVehicle(content, EXPECTED_UPDATE_COUNT, Optional.empty()));
    }
}
