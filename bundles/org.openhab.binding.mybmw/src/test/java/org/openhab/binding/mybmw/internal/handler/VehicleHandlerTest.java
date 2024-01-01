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
package org.openhab.binding.mybmw.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.MyBMWVehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.StatusWrapper;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandlerTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandlerTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    // counters for the number of properties per section
    private static final int STATUS_ELECTRIC = 12;
    private static final int STATUS_CONV = 9;
    private static final int RANGE_HYBRID = 11;
    private static final int RANGE_CONV = 6;
    private static final int RANGE_ELECTRIC = 4;
    private static final int DOORS = 11;
    private static final int CHECK_EMPTY = 3;
    private static final int SERVICE_AVAILABLE = 4;
    private static final int SERVICE_EMPTY = 4;
    private static final int LOCATION = 4;
    private static final int CHARGE_PROFILE = 44;
    private static final int TIRES = 8;
    public static final PointType HOME_LOCATION = new PointType("54.321,9.876");

    // I couldn't resolve all NonNull compile errors, hence I'm initializing the values here...
    ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
    ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
    ThingHandlerCallback thingHandlerCallback = mock(ThingHandlerCallback.class);
    VehicleHandler vehicleHandler = mock(VehicleHandler.class);
    List<ChannelUID> allChannels = new ArrayList<>();
    List<State> allStates = new ArrayList<>();

    String driveTrain = Constants.EMPTY;

    /**
     * Prepare environment for Vehicle Status Updates
     */
    private void setup(String type, String vin) {
        driveTrain = type;
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        MyBMWCommandOptionProvider cop = mock(MyBMWCommandOptionProvider.class);
        LocationProvider locationProvider = mock(LocationProvider.class);
        when(locationProvider.getLocation()).thenReturn(HOME_LOCATION);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        vehicleHandler = new VehicleHandler(thing, cop, locationProvider, timeZoneProvider, type);
        MyBMWVehicleConfiguration vehicleConfiguration = new MyBMWVehicleConfiguration();
        vehicleConfiguration.setVin(vin);

        setVehicleConfigurationToVehicleHandler(vehicleHandler, vehicleConfiguration);
        thingHandlerCallback = mock(ThingHandlerCallback.class);
        try {
            vehicleHandler.setCallback(thingHandlerCallback);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    private void setVehicleConfigurationToVehicleHandler(@Nullable VehicleHandler vehicleHandler,
            MyBMWVehicleConfiguration vehicleConfiguration) {
        try {
            Field vehicleConfigurationField = VehicleHandler.class.getDeclaredField("vehicleConfiguration");
            vehicleConfigurationField.setAccessible(true);
            vehicleConfigurationField.set(vehicleHandler, Optional.of(vehicleConfiguration));
        } catch (Exception e) {
            logger.error("vehicleConfiguration could not be set", e);
            fail("vehicleConfiguration could not be set", e);
        }
    }

    private boolean testVehicle(String statusContent, int callbacksExpected,
            Optional<Map<String, State>> concreteChecks) {
        assertNotNull(statusContent);

        try {
            Method triggerVehicleStatusUpdateMethod = VehicleHandler.class
                    .getDeclaredMethod("triggerVehicleStatusUpdate", VehicleStateContainer.class, String.class);
            triggerVehicleStatusUpdateMethod.setAccessible(true);
            triggerVehicleStatusUpdateMethod.invoke(vehicleHandler,
                    JsonStringDeserializer.getVehicleState(statusContent), null);
        } catch (Exception e) {
            logger.error("vehicleState could not be set", e);
            fail("vehicleState could not be set", e);
        }

        verify(thingHandlerCallback, times(callbacksExpected)).stateUpdated(channelCaptor.capture(),
                stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        StatusWrapper checker = new StatusWrapper(driveTrain, statusContent);
        trace();
        if (concreteChecks.isPresent()) {
            return checker.append(concreteChecks.get()).checkResults(allChannels, allStates);
        } else {
            return checker.checkResults(allChannels, allStates);
        }
    }

    private void trace() {
        for (int i = 0; i < allChannels.size(); i++) {
            // change to info for debugging channel updates
            logger.debug("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }

    @Test
    public void testPressureConversion() {
        try {
            Method calculatePressureMethod = VehicleHandler.class.getDeclaredMethod("calculatePressure", int.class);
            calculatePressureMethod.setAccessible(true);
            State state = (State) calculatePressureMethod.invoke(vehicleHandler, 110);
            assertInstanceOf(QuantityType.class, state);
            assertEquals(1.1, ((QuantityType) state).doubleValue());
            state = (State) calculatePressureMethod.invoke(vehicleHandler, 280);
            assertEquals(2.8, ((QuantityType) state).doubleValue());

            state = (State) calculatePressureMethod.invoke(vehicleHandler, -1);
            assertInstanceOf(UnDefType.class, state);
        } catch (Exception e) {
            logger.error("vehicleState could not be set", e);
            fail("vehicleState could not be set", e);
        }
    }

    /**
     * Test various Vehicles from users which delivered their fingerprint.
     * The tests are checking the chain from "JSON to Channel update".
     * Checks are done in an automated way cross checking the data from JSON and data delivered via Channel.
     * Also important the updates are counted in order to check if code changes are affecting Channel Updates.
     *
     * With the given output the updated Channels are visible.
     * Example:
     *
     * testi3Rex
     * Channel testbinding::test:status#lock Locked
     * Channel testbinding::test:status#service-date 2023-11-01T00:00:00.000+0100
     * Channel testbinding::test:status#check-control No Issues
     * Channel testbinding::test:status#last-update 2021-12-21T16:46:02.000+0100
     * Channel testbinding::test:status#doors Closed
     * Channel testbinding::test:status#windows Closed
     * Channel testbinding::test:status#plug-connection Not connected
     * Channel testbinding::test:status#charge Not Charging
     * Channel testbinding::test:status#charge-type Not Available
     * Channel testbinding::test:range#electric 76 km
     * Channel testbinding::test:range#radius-electric 60.800000000000004 km
     * Channel testbinding::test:range#fuel 31 km
     * Channel testbinding::test:range#radius-fuel 24.8 km
     * Channel testbinding::test:range#hybrid 31 km
     * Channel testbinding::test:range#radius-hybrid 24.8 km
     * Channel testbinding::test:range#mileage 31537 km
     * Channel testbinding::test:range#soc 74 %
     * Channel testbinding::test:range#remaining-fuel 4 l
     * Channel testbinding::test:doors#driver-front Closed
     * Channel testbinding::test:doors#driver-rear Closed
     * Channel testbinding::test:doors#passenger-front Closed
     * Channel testbinding::test:doors#passenger-rear Closed
     * Channel testbinding::test:doors#trunk Closed
     * Channel testbinding::test:doors#hood Closed
     * Channel testbinding::test:doors#win-driver-front Closed
     * Channel testbinding::test:doors#win-driver-rear Undef
     * Channel testbinding::test:doors#win-passenger-front Closed
     * Channel testbinding::test:doors#win-passenger-rear Undef
     * Channel testbinding::test:doors#sunroof Closed
     * Channel testbinding::test:location#gps 1.2345,6.789
     * Channel testbinding::test:location#heading 222 °
     * Channel testbinding::test:service#name Brake Fluid
     * Channel testbinding::test:service#date 2023-11-01T00:00:00.000+0100
     * Channel testbinding::test:profile#prefs Chargingwindow
     * Channel testbinding::test:profile#mode Immediatecharging
     * Channel testbinding::test:profile#control Weeklyplanner
     * Channel testbinding::test:profile#target 100
     * Channel testbinding::test:profile#limit OFF
     * Channel testbinding::test:profile#climate OFF
     * Channel testbinding::test:profile#window-start 1970-01-01T11:00:00.000+0100
     * Channel testbinding::test:profile#window-end 1970-01-01T14:30:00.000+0100
     * Channel testbinding::test:profile#timer1-departure 1970-01-01T16:00:00.000+0100
     * Channel testbinding::test:profile#timer1-enabled OFF
     * Channel testbinding::test:profile#timer1-day-mon ON
     * Channel testbinding::test:profile#timer1-day-tue ON
     * Channel testbinding::test:profile#timer1-day-wed ON
     * Channel testbinding::test:profile#timer1-day-thu ON
     * Channel testbinding::test:profile#timer1-day-fri ON
     * Channel testbinding::test:profile#timer1-day-sat ON
     * Channel testbinding::test:profile#timer1-day-sun ON
     * Channel testbinding::test:profile#timer2-departure 1970-01-01T12:02:00.000+0100
     * Channel testbinding::test:profile#timer2-enabled ON
     * Channel testbinding::test:profile#timer2-day-mon OFF
     * Channel testbinding::test:profile#timer2-day-tue OFF
     * Channel testbinding::test:profile#timer2-day-wed OFF
     * Channel testbinding::test:profile#timer2-day-thu OFF
     * Channel testbinding::test:profile#timer2-day-fri OFF
     * Channel testbinding::test:profile#timer2-day-sat OFF
     * Channel testbinding::test:profile#timer2-day-sun ON
     * Channel testbinding::test:profile#timer3-departure 1970-01-01T13:03:00.000+0100
     * Channel testbinding::test:profile#timer3-enabled OFF
     * Channel testbinding::test:profile#timer3-day-mon OFF
     * Channel testbinding::test:profile#timer3-day-tue OFF
     * Channel testbinding::test:profile#timer3-day-wed OFF
     * Channel testbinding::test:profile#timer3-day-thu OFF
     * Channel testbinding::test:profile#timer3-day-fri OFF
     * Channel testbinding::test:profile#timer3-day-sat ON
     * Channel testbinding::test:profile#timer3-day-sun OFF
     * Channel testbinding::test:profile#timer4-departure 1970-01-01T12:02:00.000+0100
     * Channel testbinding::test:profile#timer4-enabled OFF
     * Channel testbinding::test:profile#timer4-day-mon OFF
     * Channel testbinding::test:profile#timer4-day-tue OFF
     * Channel testbinding::test:profile#timer4-day-wed OFF
     * Channel testbinding::test:profile#timer4-day-thu OFF
     * Channel testbinding::test:profile#timer4-day-fri OFF
     * Channel testbinding::test:profile#timer4-day-sat OFF
     * Channel testbinding::test:profile#timer4-day-sun ON
     */

    @Test
    public void testBevIx() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "anonymous");
        String content = FileReader.fileToString("responses/BEV/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testBevI3() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "anonymous");
        String content = FileReader.fileToString("responses/BEV2/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testBevIX3() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "anonymous");
        String content = FileReader.fileToString("responses/BEV3/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testBevI4() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "anonymous");
        String content = FileReader.fileToString("responses/BEV4/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testBevI7() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "anonymous");
        String content = FileReader.fileToString("responses/BEV5/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testIceMiniCooper() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "anonymous");
        String content = FileReader.fileToString("responses/ICE/vehicles_state.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testIceX320d() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "anonymous");
        String content = FileReader.fileToString("responses/ICE2/vehicles_state.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testIce530d() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "anonymous");
        String content = FileReader.fileToString("responses/ICE3/vehicles_state.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testIce435i() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "anonymous");
        String content = FileReader.fileToString("responses/ICE4/vehicles_state.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testMildHybrid340i() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.MILD_HYBRID.toString(), "anonymous");
        String content = FileReader.fileToString("responses/MILD_HYBRID/vehicles_state.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testPhev530e() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "anonymous");
        String content = FileReader.fileToString("responses/PHEV/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testPhev330e() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "anonymous");
        String content = FileReader.fileToString("responses/PHEV2/vehicles_state.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }
}
