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
import org.openhab.binding.mybmw.internal.dto.StatusWrapper;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleTests} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class VehicleTests {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private static final int STATUS_ELECTRIC = 12;
    private static final int STATUS_CONV = 9;
    private static final int RANGE_HYBRID = 9;
    private static final int RANGE_CONV = 4;
    private static final int RANGE_ELECTRIC = 4;
    private static final int DOORS = 11;
    private static final int CHECK_EMPTY = 3;
    private static final int CHECK_AVAILABLE = 3;
    private static final int SERVICE_AVAILABLE = 3;
    private static final int SERVICE_EMPTY = 3;
    private static final int LOCATION = 4;
    private static final int CHARGE_PROFILE = 44;
    private static final int TIRES = 8;
    public static final PointType HOME_LOCATION = new PointType("54.321,9.876");
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

    /**
     * Prepare environment for Vehicle Status Updates
     */
    public void setup(String type, String vin) {
        driveTrain = type;
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        MyBMWCommandOptionProvider cop = mock(MyBMWCommandOptionProvider.class);
        LocationProvider locationProvider = mock(LocationProvider.class);
        when(locationProvider.getLocation()).thenReturn(HOME_LOCATION);
        cch = new VehicleHandler(thing, cop, locationProvider, type);
        VehicleConfiguration vc = new VehicleConfiguration();
        vc.vin = vin;
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
        cch.vehicleStatusCallback.onResponse(statusContent);
        verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
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
    public void testI01Rex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), Constants.ANONYMOUS);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + RANGE_HYBRID + DOORS + LOCATION + SERVICE_AVAILABLE
                + CHECK_EMPTY + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testF11() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "some_vin_F11");
        String content = FileReader.readFileInString("src/test/resources/responses/F11/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + LOCATION + TIRES,
                Optional.empty()));
    }

    @Test
    public void testF31() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "some_vin_F31");
        String content = FileReader.readFileInString("src/test/resources/responses/F31/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + LOCATION + TIRES,
                Optional.empty()));
    }

    @Test
    public void testF44() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "some_vin_F44");
        String content = FileReader.readFileInString("src/test/resources/responses/F44/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }

    @Test
    public void testF45() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "some_vin_F45");
        String content = FileReader.readFileInString("src/test/resources/responses/F45/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testF48() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), "some_vin_F48");
        String content = FileReader.readFileInString("src/test/resources/responses/F48/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_AVAILABLE + LOCATION + TIRES,
                Optional.empty()));
    }

    @Test
    public void testG01() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "some_vin_G01");
        String content = FileReader.readFileInString("src/test/resources/responses/G01/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testG05() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "some_vin_G05");
        String content = FileReader.readFileInString("src/test/resources/responses/G05/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testG08() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "some_vin_G08");
        String content = FileReader.readFileInString("src/test/resources/responses/G08/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testG21() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "some_vin_G21");
        String content = FileReader.readFileInString("src/test/resources/responses/G21/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testG30() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "some_vin_G30");
        String content = FileReader.readFileInString("src/test/resources/responses/G30/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void testI01NoRex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), "some_vin_I01_NOREX");
        String content = FileReader.readFileInString("src/test/resources/responses/I01_NOREX/vehicles_v2_bmw_0.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void test530e() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.PLUGIN_HYBRID.toString(), "anonymous");
        String content = FileReader.readFileInString("src/test/resources/responses/530e/vehicles.json");
        assertTrue(testVehicle(content, STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY
                + LOCATION + CHARGE_PROFILE + TIRES, Optional.empty()));
    }

    @Test
    public void test340i() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.MILD_HYBRID.toString(), "anonymous");
        String content = FileReader.readFileInString("src/test/resources/responses/G21/340i.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + LOCATION + SERVICE_EMPTY + CHECK_EMPTY + TIRES, Optional.empty()));
    }
}
