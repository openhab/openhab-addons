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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.ADDRESS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHANNEL_GROUP_CHARGE_PROFILE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHANNEL_GROUP_CHECK_CONTROL;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHANNEL_GROUP_RANGE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHANNEL_GROUP_SERVICE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHANNEL_GROUP_STATUS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHARGE_REMAINING;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHARGE_STATUS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.CHECK_CONTROL;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DATE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DETAILS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DOORS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DOOR_DRIVER_FRONT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DOOR_DRIVER_REAR;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DOOR_PASSENGER_FRONT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.DOOR_PASSENGER_REAR;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.ESTIMATED_FUEL_L_100KM;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.ESTIMATED_FUEL_MPG;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.FRONT_LEFT_CURRENT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.FRONT_LEFT_TARGET;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.FRONT_RIGHT_CURRENT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.FRONT_RIGHT_TARGET;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.GPS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.HEADING;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.HOME_DISTANCE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.HOOD;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.LAST_FETCHED;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.LAST_UPDATE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.LOCK;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.MILEAGE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.NAME;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.PLUG_CONNECTION;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_ELECTRIC;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_FUEL;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_HYBRID;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_RADIUS_ELECTRIC;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_RADIUS_FUEL;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RANGE_RADIUS_HYBRID;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.RAW;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REAR_LEFT_CURRENT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REAR_LEFT_TARGET;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REAR_RIGHT_CURRENT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REAR_RIGHT_TARGET;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMAINING_FUEL;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.SERVICE_DATE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.SERVICE_MILEAGE;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.SEVERITY;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.SOC;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.SUNROOF;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.TRUNK;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.WINDOWS;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.WINDOW_DOOR_DRIVER_FRONT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.WINDOW_DOOR_DRIVER_REAR;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.WINDOW_DOOR_PASSENGER_FRONT;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.WINDOW_DOOR_PASSENGER_REAR;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.vehicle.RequiredService;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleState;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.VehicleHandlerTest;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link StatusWrapper} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - updates for v2 API
 * @author Mark Herwege - remaining charging time test
 */
@NonNullByDefault
@SuppressWarnings("null")
public class StatusWrapper {
    private static final Unit<Length> KILOMETRE = Constants.KILOMETRE_UNIT;

    private VehicleState vehicleState;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    private Map<String, State> specialHandlingMap = new HashMap<>();

    public StatusWrapper(String type, String statusJson) {
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.MILD_HYBRID.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        VehicleStateContainer vehicleStateContainer = JsonStringDeserializer.getVehicleState(statusJson);
        vehicleState = vehicleStateContainer.getState();
    }

    /**
     * Test results automatically against json values
     *
     * @param channels
     * @param states
     * @return
     */
    public boolean checkResults(@Nullable List<ChannelUID> channels, @Nullable List<State> states) {
        assertNotNull(channels);
        assertNotNull(states);
        assertTrue(channels.size() == states.size(), "Same list sizes");
        for (int i = 0; i < channels.size(); i++) {
            checkResult(channels.get(i), states.get(i));
        }
        return true;
    }

    /**
     * Add a specific check for a value e.g. hard coded "Upcoming Service" in order to check the right ordering
     *
     * @param specialHand
     * @return
     */
    public StatusWrapper append(Map<String, State> compareMap) {
        specialHandlingMap.putAll(compareMap);
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        String gUid = channelUID.getGroupId();
        QuantityType<Length> qt;
        StringType st;
        StringType wanted;
        DateTimeType dtt;
        PointType pt;
        DecimalType dt;
        Unit<Length> wantedUnit = KILOMETRE;
        switch (cUid) {
            case MILEAGE:
                switch (gUid) {
                    case CHANNEL_GROUP_RANGE:
                        if (!state.equals(UnDefType.UNDEF)) {
                            assertTrue(state instanceof QuantityType);
                            qt = ((QuantityType) state);
                            assertEquals(qt.intValue(), vehicleState.getCurrentMileage(), "Mileage");
                        } else {
                            assertEquals(Constants.INT_UNDEF, vehicleState.getCurrentMileage(), "Mileage undefined");
                        }
                        break;
                    case CHANNEL_GROUP_SERVICE:
                        State wantedMileage = UnDefType.UNDEF;
                        if (!vehicleState.getRequiredServices().isEmpty()) {
                            if (vehicleState.getRequiredServices().get(0).getMileage() > 0) {
                                wantedMileage = QuantityType.valueOf(
                                        vehicleState.getRequiredServices().get(0).getMileage(),
                                        Constants.KILOMETRE_UNIT);
                            } else {
                                wantedMileage = UnDefType.UNDEF;
                            }
                        }
                        assertEquals(wantedMileage, state, "Service Mileage");
                        break;
                    default:
                        assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                        break;
                }
                break;
            case RANGE_ELECTRIC:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(vehicleState.getElectricChargingState().getRange(), qt.intValue(), "Range Electric");
                break;
            case RANGE_HYBRID:
                assertTrue(isHybrid, "Is hybrid");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(vehicleState.getRange(), qt.intValue(), "Range combined hybrid");
                break;
            case RANGE_FUEL:
                assertTrue(hasFuel, "Has Fuel");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (!isHybrid) {
                    assertEquals(vehicleState.getCombustionFuelLevel().getRange(), qt.intValue(), "Range Combustion");
                } else {
                    assertEquals(
                            vehicleState.getCombustionFuelLevel().getRange()
                                    - vehicleState.getElectricChargingState().getRange(),
                            qt.intValue(), "Range Combustion");
                }
                break;
            case REMAINING_FUEL:
                assertTrue(hasFuel, "Has Fuel");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.LITRE, qt.getUnit(), "Liter Unit");
                assertEquals(vehicleState.getCombustionFuelLevel().getRemainingFuelLiters(), qt.intValue(),
                        "Fuel Level");
                break;
            case ESTIMATED_FUEL_L_100KM:
                assertTrue(hasFuel, "Has Fuel");

                if (vehicleState.getCombustionFuelLevel().getRemainingFuelLiters() > 0
                        && vehicleState.getCombustionFuelLevel().getRange() > 0) {
                    assertTrue(state instanceof DecimalType);
                    dt = ((DecimalType) state);
                    double estimatedFuelConsumptionL100km = vehicleState.getCombustionFuelLevel()
                            .getRemainingFuelLiters() * 1.0 / vehicleState.getCombustionFuelLevel().getRange() * 100.0;
                    assertEquals(estimatedFuelConsumptionL100km, dt.doubleValue(),
                            "Estimated Fuel Consumption l/100km");
                } else {
                    assertTrue(state instanceof UnDefType);
                }
                break;
            case ESTIMATED_FUEL_MPG:
                assertTrue(hasFuel, "Has Fuel");

                if (vehicleState.getCombustionFuelLevel().getRemainingFuelLiters() > 0
                        && vehicleState.getCombustionFuelLevel().getRange() > 0) {
                    assertTrue(state instanceof DecimalType);
                    dt = ((DecimalType) state);
                    double estimatedFuelConsumptionMpg = 235.214583
                            / (vehicleState.getCombustionFuelLevel().getRemainingFuelLiters() * 1.0
                                    / vehicleState.getCombustionFuelLevel().getRange() * 100.0);
                    assertEquals(estimatedFuelConsumptionMpg, dt.doubleValue(), "Estimated Fuel Consumption mpg");
                } else {
                    assertTrue(state instanceof UnDefType);
                }
                break;
            case SOC:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.PERCENT, qt.getUnit(), "Percent");
                assertEquals(vehicleState.getElectricChargingState().getChargingLevelPercent(), qt.intValue(),
                        "Charge Level");
                break;
            case LOCK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getCombinedSecurityState()));
                assertEquals(wanted.toString(), st.toString(), "Vehicle locked");
                break;
            case DOORS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getCombinedState()));
                assertEquals(wanted.toString(), st.toString(), "Doors closed");
                break;
            case WINDOWS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (specialHandlingMap.containsKey(WINDOWS)) {
                    assertEquals(specialHandlingMap.get(WINDOWS).toString(), st.toString(), "Windows");
                } else {
                    wanted = StringType
                            .valueOf(Converter.toTitleCase(vehicleState.getWindowsState().getCombinedState()));
                    assertEquals(wanted.toString(), st.toString(), "Windows");
                }

                break;
            case CHECK_CONTROL:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (specialHandlingMap.containsKey(CHECK_CONTROL)) {
                    assertEquals(specialHandlingMap.get(CHECK_CONTROL).toString(), st.toString(), "Check Control");
                } else {
                    wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getOverallCheckControlStatus()));
                    assertEquals(wanted.toString(), st.toString(), "Check Control");
                }
                break;
            case CHARGE_REMAINING:
                // charge-remaining can be either a number in minutes, or UNDEF
                assertTrue(isElectric, "Is Electric");
                if (state instanceof QuantityType) {
                    assertTrue(state instanceof QuantityType);
                    qt = ((QuantityType) state);
                    assertEquals(Units.MINUTE, qt.getUnit(), "Minute Unit");
                    assertEquals(vehicleState.getElectricChargingState().getRemainingChargingMinutes(), qt.intValue(),
                            "Charge Time Remaining");
                } else {
                    assertTrue(state instanceof UnDefType);
                }
                break;
            case CHARGE_STATUS:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.toTitleCase(vehicleState.getElectricChargingState().getChargingStatus()),
                        st.toString(), "Charge Status");
                break;
            case PLUG_CONNECTION:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.getConnectionState(vehicleState.getElectricChargingState().isChargerConnected()),
                        st, "Plug Connection State");
                break;
            case LAST_UPDATE:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                State expectedUpdateDate = Converter.zonedToLocalDateTime(vehicleState.getLastUpdatedAt(),
                        ZoneId.systemDefault());
                assertEquals(expectedUpdateDate.toString(), dtt.toString(), "Last Update");
                break;
            case LAST_FETCHED:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                State expectedFetchedDate = Converter.zonedToLocalDateTime(vehicleState.getLastFetched(),
                        ZoneId.systemDefault());
                assertEquals(expectedFetchedDate.toString(), dtt.toString(), "Last Fetched");
                break;
            case GPS:
                if (state instanceof PointType) {
                    pt = (PointType) state;
                    assertNotNull(vehicleState.getLocation());
                    assertEquals(
                            PointType.valueOf(Double.toString(vehicleState.getLocation().getCoordinates().getLatitude())
                                    + ","
                                    + Double.toString(vehicleState.getLocation().getCoordinates().getLongitude())),
                            pt, "Coordinates");
                } // else no check needed
                break;
            case HEADING:
                if (state instanceof QuantityType quantityCommand) {
                    qt = quantityCommand;
                    assertEquals(Units.DEGREE_ANGLE, qt.getUnit(), "Angle Unit");
                    assertNotNull(vehicleState.getLocation());
                    assertEquals(vehicleState.getLocation().getHeading(), qt.intValue(), 0.01, "Heading");
                } // else no check needed
                break;
            case RANGE_RADIUS_ELECTRIC:
                assertTrue(state instanceof QuantityType);
                assertTrue(isElectric);
                qt = ((QuantityType) state);
                assertEquals(Converter.guessRangeRadius(vehicleState.getElectricChargingState().getRange()),
                        qt.intValue(), "Range Radius Electric");
                break;
            case RANGE_RADIUS_FUEL:
                assertTrue(state instanceof QuantityType);
                assertTrue(hasFuel);
                qt = (QuantityType) state;
                if (!isHybrid) {
                    assertEquals(Converter.guessRangeRadius(vehicleState.getCombustionFuelLevel().getRange()),
                            qt.intValue(), "Range Radius Fuel");
                } else {
                    assertEquals(
                            Converter.guessRangeRadius(vehicleState.getCombustionFuelLevel().getRange()
                                    - vehicleState.getElectricChargingState().getRange()),
                            qt.intValue(), "Range Radius Fuel");
                }
                break;
            case RANGE_RADIUS_HYBRID:
                assertTrue(state instanceof QuantityType);
                assertTrue(isHybrid);
                qt = (QuantityType) state;
                assertEquals(Converter.guessRangeRadius(vehicleState.getRange()), qt.intValue(), "Range Radius Hybrid");
                break;
            case DOOR_DRIVER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getLeftFront()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getLeftRear()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getRightFront()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getRightRear()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case TRUNK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getTrunk()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case HOOD:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getDoorsState().getHood()));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case WINDOW_DOOR_DRIVER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getWindowsState().getLeftFront()));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getWindowsState().getLeftRear()));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getWindowsState().getRightFront()));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getWindowsState().getRightRear()));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case SUNROOF:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicleState.getRoofState().getRoofState()));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case SERVICE_DATE:
                if (!state.equals(UnDefType.UNDEF)) {
                    assertTrue(state instanceof DateTimeType);
                    dtt = (DateTimeType) state;
                    if (gUid.contentEquals(CHANNEL_GROUP_STATUS)) {
                        if (specialHandlingMap.containsKey(SERVICE_DATE)) {
                            assertEquals(specialHandlingMap.get(SERVICE_DATE).toString(), dtt.toString(),
                                    "Next Service");
                        } else {
                            String dueDateString = VehicleStatusUtils
                                    .getNextServiceDate(vehicleState.getRequiredServices()).toString();
                            DateTimeType expectedDTT = DateTimeType.valueOf(dueDateString);
                            assertEquals(expectedDTT.toString(), dtt.toString(), "Next Service");
                        }
                    } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                        String dueDateString = vehicleState.getRequiredServices().get(0).getDateTime();
                        State expectedDTT = Converter.zonedToLocalDateTime(dueDateString, ZoneId.systemDefault());
                        assertEquals(expectedDTT.toString(), dtt.toString(), "First Service Date");
                    }
                }
                break;
            case SERVICE_MILEAGE:
                if (!state.equals(UnDefType.UNDEF)) {
                    qt = ((QuantityType) state);
                    if (gUid.contentEquals(CHANNEL_GROUP_STATUS)) {
                        QuantityType<Length> wantedQt = (QuantityType) VehicleStatusUtils
                                .getNextServiceMileage(vehicleState.getRequiredServices());
                        assertEquals(wantedQt.getUnit(), qt.getUnit(), "Next Service Miles");
                        assertEquals(wantedQt.intValue(), qt.intValue(), "Mileage");
                    } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                        assertEquals(vehicleState.getRequiredServices().get(0).getMileage(), qt.intValue(),
                                "First Service Mileage");
                    }
                }
                break;
            case NAME:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_SERVICE:
                        wanted = StringType.valueOf(Constants.NO_ENTRIES);
                        if (!vehicleState.getRequiredServices().isEmpty()) {
                            wanted = StringType.valueOf(
                                    Converter.toTitleCase(vehicleState.getRequiredServices().get(0).getType()));
                        }
                        assertEquals(wanted.toString(), st.toString(), "Service Name");
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        wanted = StringType.valueOf(Constants.NO_ENTRIES);
                        if (!vehicleState.getCheckControlMessages().isEmpty()) {
                            wanted = StringType.valueOf(
                                    Converter.toTitleCase(vehicleState.getCheckControlMessages().get(0).getType()));
                        }
                        assertEquals(wanted.toString(), st.toString(), "CheckControl Name");
                        break;
                    default:
                        assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                        break;
                }
                break;
            case DETAILS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_SERVICE:
                        wanted = StringType.valueOf(Converter.toTitleCase(Constants.NO_ENTRIES));
                        if (!vehicleState.getRequiredServices().isEmpty()) {
                            wanted = StringType.valueOf(vehicleState.getRequiredServices().get(0).getDescription());
                        }
                        assertEquals(wanted.toString(), st.toString(), "Service Details");
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        wanted = StringType.valueOf(Constants.NO_ENTRIES);
                        if (!vehicleState.getCheckControlMessages().isEmpty()) {
                            wanted = StringType.valueOf(vehicleState.getCheckControlMessages().get(0).getDescription());
                        }
                        assertEquals(wanted.toString(), st.toString(), "CheckControl Details");
                        break;
                    default:
                        assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                        break;
                }
                break;
            case SEVERITY:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Constants.NO_ENTRIES);
                if (!vehicleState.getCheckControlMessages().isEmpty()) {
                    wanted = StringType.valueOf(
                            Converter.toTitleCase(vehicleState.getCheckControlMessages().get(0).getSeverity()));
                }
                assertEquals(wanted.toString(), st.toString(), "CheckControl Severity");
                break;
            case DATE:
                if (state.equals(UnDefType.UNDEF)) {
                    for (RequiredService serviceEntry : vehicleState.getRequiredServices()) {
                        assertTrue(serviceEntry.getDateTime() == null, "No Service Date available");
                    }
                } else {
                    assertTrue(state instanceof DateTimeType);
                    dtt = (DateTimeType) state;
                    switch (gUid) {
                        case CHANNEL_GROUP_SERVICE:
                            String dueDateString = vehicleState.getRequiredServices().get(0).getDateTime();
                            State expectedDTT = Converter.zonedToLocalDateTime(dueDateString, ZoneId.systemDefault());
                            assertEquals(expectedDTT.toString(), dtt.toString(), "ServiceSate");
                            break;
                        default:
                            assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                            break;
                    }
                }
                break;
            case FRONT_LEFT_CURRENT:
                if (vehicleState.getTireState().getFrontLeft().getStatus().getCurrentPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getFrontLeft().getStatus().getCurrentPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertEquals(state, UnDefType.UNDEF);
                }
                break;
            case FRONT_LEFT_TARGET:
                if (vehicleState.getTireState().getFrontLeft().getStatus().getTargetPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getFrontLeft().getStatus().getTargetPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case FRONT_RIGHT_CURRENT:
                if (vehicleState.getTireState().getFrontRight().getStatus().getCurrentPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getFrontRight().getStatus().getCurrentPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case FRONT_RIGHT_TARGET:
                if (vehicleState.getTireState().getFrontRight().getStatus().getTargetPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getFrontRight().getStatus().getTargetPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_LEFT_CURRENT:
                if (vehicleState.getTireState().getRearLeft().getStatus().getCurrentPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getRearLeft().getStatus().getCurrentPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_LEFT_TARGET:
                if (vehicleState.getTireState().getRearLeft().getStatus().getTargetPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getRearLeft().getStatus().getTargetPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_RIGHT_CURRENT:
                if (vehicleState.getTireState().getRearRight().getStatus().getCurrentPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getRearRight().getStatus().getCurrentPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_RIGHT_TARGET:
                if (vehicleState.getTireState().getRearRight().getStatus().getTargetPressure() > 0) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicleState.getTireState().getRearRight().getStatus().getTargetPressure() / 100.0,
                            qt.doubleValue(), "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case ADDRESS:
                if (state instanceof StringType) {
                    st = (StringType) state;
                    assertEquals(st.toFullString(), vehicleState.getLocation().getAddress().getFormatted(),
                            "Location Address");
                } // else no check needed
                break;
            case HOME_DISTANCE:
                if (state instanceof QuantityType quantity) {
                    qt = quantity;
                    PointType vehicleLocation = PointType
                            .valueOf(Double.toString(vehicleState.getLocation().getCoordinates().getLatitude()) + ","
                                    + Double.toString(vehicleState.getLocation().getCoordinates().getLongitude()));
                    int distance = vehicleLocation.distanceFrom(VehicleHandlerTest.HOME_LOCATION).intValue();
                    assertEquals(qt.intValue(), distance, "Distance from Home");
                    assertEquals(qt.getUnit(), SIUnits.METRE, "Distance from Home Unit");
                } // else no check needed
                break;
            case RAW:
                // don't assert raw channel
                break;
            default:
                if (!gUid.equals(CHANNEL_GROUP_CHARGE_PROFILE)) {
                    // fail in case of unknown update
                    assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                }
                break;
        }
    }
}
