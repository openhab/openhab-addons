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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.StatusWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleAttributesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.library.types.StringType;
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
    private static final int STATUS_CONV = 8;
    private static final int RANGE_HYBRID = 12;
    private static final int RANGE_CONV = 4;
    private static final int RANGE_ELECTRIC = 5;
    private static final int DOORS = 12;
    private static final int CHECK_EMPTY = 3;
    private static final int CHECK_AVAILABLE = 3;
    private static final int SERVICE_AVAILABLE = 4;
    private static final int SERVICE_EMPTY = 4;
    private static final int POSITION = 2;

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

    private boolean testVehicle(String statusContent, int callbacksExpected,
            Optional<Map<String, State>> concreteChecks) {
        assertNotNull(statusContent);
        cch.vehicleStatusCallback.onResponse(statusContent);
        verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        StatusWrapper checker = new StatusWrapper(driveTrain, imperial, statusContent);
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
     * [main] INFO org.eclipse.jetty.util.log - Logging initialized @1731ms
     * Channel testbinding::test:status#lock Secured
     * Channel testbinding::test:status#service-date 2021-11-01T13:00:00.000+0100
     * Channel testbinding::test:status#service-mileage -1.0 km
     * Channel testbinding::test:status#check-control Not Active
     * Channel testbinding::test:status#last-update 2020-08-24T17:55:32.000+0200
     * Channel testbinding::test:status#doors CLOSED
     * Channel testbinding::test:status#windows CLOSED
     * Channel testbinding::test:doors#driver-front CLOSED
     * Channel testbinding::test:doors#driver-rear CLOSED
     * Channel testbinding::test:doors#passenger-front CLOSED
     * Channel testbinding::test:doors#passenger-rear CLOSED
     * Channel testbinding::test:doors#trunk CLOSED
     * Channel testbinding::test:doors#hood CLOSED
     * Channel testbinding::test:doors#window-driver-front CLOSED
     * Channel testbinding::test:doors#window-driver-rear CLOSED
     * Channel testbinding::test:doors#window-passenger-front CLOSED
     * Channel testbinding::test:doors#window-passenger-rear CLOSED
     * Channel testbinding::test:doors#window-rear INVALID
     * Channel testbinding::test:doors#sunroof CLOSED
     * Channel testbinding::test:range#mileage 17273.0 km
     * Channel testbinding::test:range#electric 148.0 km
     * Channel testbinding::test:range#radius-electric 118.4 km
     * Channel testbinding::test:range#fuel 70.0 km
     * Channel testbinding::test:range#radius-fuel 56.0 km
     * Channel testbinding::test:range#hybrid 218.0 km
     * Channel testbinding::test:range#radius-hybrid 174.4 km
     * Channel testbinding::test:range#soc 71.0 %
     * Channel testbinding::test:range#remaining-fuel 4.0 l
     * Channel testbinding::test:status#charge Charging Goal Reached
     * Channel testbinding::test:check#size 0
     * Channel testbinding::test:check#name INVALID
     * Channel testbinding::test:check#mileage -1.0 km
     * Channel testbinding::test:check#index -1
     * Channel testbinding::test:service#size 4
     * Channel testbinding::test:service#name Brake Fluid
     * Channel testbinding::test:service#date 2021-11-01T13:00:00.000+0100
     * Channel testbinding::test:service#mileage 15345.0 km
     * Channel testbinding::test:service#index 0
     * Channel testbinding::test:location#latitude 50.55604934692383
     * Channel testbinding::test:location#longitude 8.4956693649292
     * Channel testbinding::test:location#heading 219.0 °
     *
     */

    @Test
    public void testi3Rex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + RANGE_HYBRID + DOORS + CHECK_EMPTY + SERVICE_AVAILABLE + POSITION, Optional.empty()));
    }

    @Test
    public void testi3RexMiles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        // assertTrue(testVehicle(content, HYBRID_CALL_TIMES + LIST_UPDATES, Optional.empty()));
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + RANGE_HYBRID + DOORS + CHECK_EMPTY + SERVICE_AVAILABLE + POSITION, Optional.empty()));
    }

    @Test
    public void testF15() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F15/status.json");
        // Check earliest Service by hard
        Map<String, State> m = new HashMap<String, State>();
        // Don>'t test on concrete timestamp - it's is different on each machine
        // Check for cbsType which is "Oil" instead
        // m.put(ConnectedDriveConstants.SERVICE_DATE, DateTimeType.valueOf("2018-06-01T14:00:00.000+0200"));
        m.put(ConnectedDriveConstants.NAME, StringType.valueOf("Oil"));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_AVAILABLE + CHECK_EMPTY,
                Optional.of(m)));
    }

    @Test
    public void testF15Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F15/status.json");
        // Check earliest Service by hard
        Map<String, State> m = new HashMap<String, State>();
        // Don>'t test on concrete timestamp - it's idfferent on each machine
        // Check for cbsType which is "Oil" instead
        // m.put(ConnectedDriveConstants.SERVICE_DATE, DateTimeType.valueOf("2018-06-01T14:00:00.000+0200"));
        m.put(ConnectedDriveConstants.NAME, StringType.valueOf("Oil"));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_AVAILABLE + CHECK_EMPTY,
                Optional.of(m)));
    }

    @Test
    public void testF31() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F31/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_AVAILABLE + CHECK_EMPTY,
                Optional.empty()));
    }

    @Test
    public void testF31Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F31/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_AVAILABLE + CHECK_EMPTY,
                Optional.empty()));
    }

    @Test
    public void testF35() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F35/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_EMPTY + CHECK_EMPTY,
                Optional.empty()));
    }

    @Test
    public void testF35Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F35/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + POSITION + SERVICE_EMPTY + CHECK_EMPTY,
                Optional.empty()));
    }

    @Test
    public void testF45() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F45/status.json");
        // assertTrue(testVehicle(content, 27, Optional.empty()));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_EMPTY + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testF45Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F45/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_EMPTY + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testF48() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F48/status.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_AVAILABLE + POSITION, Optional.empty()));
    }

    @Test
    public void testF48Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F48/status.json");
        assertTrue(testVehicle(content,
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_AVAILABLE + POSITION, Optional.empty()));
    }

    @Test
    public void testG31NBTEvo() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/G31_NBTevo/status.json");
        // assertTrue(testVehicle(content, 27, Optional.empty()));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testG31NBTEvoMiles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/G31_NBTevo/status.json");
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testI01NoRex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_NOREX/status.json");
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testI01NoRexMiles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_NOREX/status.json");
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + DOORS + RANGE_ELECTRIC + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testI01Rex() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/status.json");
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION, Optional.empty()));
    }

    @Test
    public void testI01RexMiles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/status.json");
        assertTrue(testVehicle(content,
                STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION, Optional.empty()));
    }

    @Test
    public void test318iF31() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F31/status-318i.json");
        Map<String, State> m = new HashMap<String, State>();
        m.put(ConnectedDriveConstants.WINDOWS, StringType.valueOf(Constants.INTERMEDIATE));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void test318iF31Miles() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F31/status-318i.json");
        Map<String, State> m = new HashMap<String, State>();
        m.put(ConnectedDriveConstants.WINDOWS, StringType.valueOf(Constants.INTERMEDIATE));
        assertTrue(testVehicle(content, STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION,
                Optional.empty()));
    }

    @Test
    public void testI01RexCompat() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/api/vehicle/vehicle-ccm.json");
        VehicleAttributesContainer vac = Converter.getGson().fromJson(content, VehicleAttributesContainer.class);
        assertTrue(testVehicle(Converter.transformLegacyStatus(vac),
                STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_AVAILABLE + POSITION,
                Optional.empty()));
    }

    @Test
    public void testI01RexMilesCompat() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/api/vehicle/vehicle-ccm.json");
        VehicleAttributesContainer vac = Converter.getGson().fromJson(content, VehicleAttributesContainer.class);
        assertTrue(testVehicle(Converter.transformLegacyStatus(vac),
                STATUS_ELECTRIC + DOORS + RANGE_HYBRID + SERVICE_AVAILABLE + CHECK_AVAILABLE + POSITION,
                Optional.empty()));
    }

    @Test
    public void testF11Compat() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/responses/F11/vehicle-status.json");
        VehicleAttributesContainer vac = Converter.getGson().fromJson(content, VehicleAttributesContainer.class);
        assertTrue(testVehicle(Converter.transformLegacyStatus(vac),
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION, Optional.empty()));
    }

    @Test
    public void testF11MilesCompat() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.CONVENTIONAL.toString(), true);
        String content = FileReader.readFileInString("src/test/resources/responses/F11/vehicle-status.json");
        VehicleAttributesContainer vac = Converter.getGson().fromJson(content, VehicleAttributesContainer.class);
        assertTrue(testVehicle(Converter.transformLegacyStatus(vac),
                STATUS_CONV + DOORS + RANGE_CONV + SERVICE_AVAILABLE + CHECK_EMPTY + POSITION, Optional.empty()));
    }
}
