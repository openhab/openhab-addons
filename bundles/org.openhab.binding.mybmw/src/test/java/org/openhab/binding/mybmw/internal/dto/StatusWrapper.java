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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.properties.CBS;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.handler.VehicleTests;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link StatusWrapper} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class StatusWrapper {
    private static final Unit<Length> KILOMETRE = Constants.KILOMETRE_UNIT;

    private Vehicle vehicle;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    private Map<String, State> specialHandlingMap = new HashMap<String, State>();

    public StatusWrapper(String type, String statusJson) {
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.MILD_HYBRID.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        List<Vehicle> vl = Converter.getVehicleList(statusJson);
        assertEquals(1, vl.size(), "Vehciles found");
        vehicle = Converter.getConsistentVehcile(vl.get(0));
    }

    /**
     * Test results auctomatically against json values
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
        OnOffType oot;
        Unit<Length> wantedUnit;
        switch (cUid) {
            case MILEAGE:
                switch (gUid) {
                    case CHANNEL_GROUP_RANGE:
                        if (!state.equals(UnDefType.UNDEF)) {
                            assertTrue(state instanceof QuantityType);
                            qt = ((QuantityType) state);
                            if (Constants.KM_JSON.equals(vehicle.status.currentMileage.units)) {
                                assertEquals(KILOMETRE, qt.getUnit(), "KM");
                            } else {
                                assertEquals(ImperialUnits.MILE, qt.getUnit(), "Miles");
                            }
                            assertEquals(qt.intValue(), vehicle.status.currentMileage.mileage, "Mileage");
                        } else {
                            assertEquals(Constants.INT_UNDEF, vehicle.status.currentMileage.mileage,
                                    "Mileage undefined");
                        }
                        break;
                    case CHANNEL_GROUP_SERVICE:
                        State wantedMileage = QuantityType.valueOf(Constants.INT_UNDEF, Constants.KILOMETRE_UNIT);
                        if (!vehicle.properties.serviceRequired.isEmpty()) {
                            if (vehicle.properties.serviceRequired.get(0).distance != null) {
                                if (vehicle.properties.serviceRequired.get(0).distance.units
                                        .equals(Constants.KILOMETERS_JSON)) {
                                    wantedMileage = QuantityType.valueOf(
                                            vehicle.properties.serviceRequired.get(0).distance.value,
                                            Constants.KILOMETRE_UNIT);
                                } else {
                                    wantedMileage = QuantityType.valueOf(
                                            vehicle.properties.serviceRequired.get(0).distance.value,
                                            ImperialUnits.MILE);
                                }
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
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, vehicle), qt.intValue(),
                        "Range Electric");
                break;
            case RANGE_FUEL:
                assertTrue(hasFuel, "Has Fuel");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, vehicle), qt.intValue(),
                        "Range Combustion");
                break;
            case RANGE_HYBRID:
                assertTrue(isHybrid, "Is Hybrid");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(VehicleStatusUtils.getRange(Constants.PHEV, vehicle), qt.intValue(), "Range Combined");
                break;
            case REMAINING_FUEL:
                assertTrue(hasFuel, "Has Fuel");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.LITRE, qt.getUnit(), "Liter Unit");
                assertEquals(vehicle.properties.fuelLevel.value, qt.intValue(), "Fuel Level");
                break;
            case SOC:
                assertTrue(isElectric, "Is Ee<lctric");
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals(Units.PERCENT, qt.getUnit(), "Percent");
                assertEquals(vehicle.properties.chargingState.chargePercentage, qt.intValue(), "Charge Level");
                break;
            case LOCK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.getLockState(vehicle.properties.areDoorsLocked), st, "Vehicle locked");
                break;
            case DOORS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.getClosedState(vehicle.properties.areDoorsClosed), st, "Doors Closed");
                break;
            case WINDOWS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (specialHandlingMap.containsKey(WINDOWS)) {
                    assertEquals(specialHandlingMap.get(WINDOWS).toString(), st.toString(), "Windows");
                } else {
                    assertEquals(Converter.getClosedState(vehicle.properties.areWindowsClosed), st, "Windows");
                }

                break;
            case CHECK_CONTROL:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (specialHandlingMap.containsKey(CHECK_CONTROL)) {
                    assertEquals(specialHandlingMap.get(CHECK_CONTROL).toString(), st.toString(), "Check Control");
                } else {
                    assertEquals(vehicle.status.checkControlMessagesGeneralState, st.toString(), "Check Control");
                }
                break;
            case CHARGE_INFO:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.getLocalTime(VehicleStatusUtils.getChargeInfo(vehicle)), st.toString(),
                        "Charge Info");
                break;
            case CHARGE_STATUS:
                assertTrue(isElectric, "Is Electric");
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.toTitleCase(VehicleStatusUtils.getChargStatus(vehicle)), st.toString(),
                        "Charge Status");
                break;
            case PLUG_CONNECTION:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals(Converter.getConnectionState(vehicle.properties.chargingState.isChargerConnected), st,
                        "Plug Connection State");
                break;
            case LAST_UPDATE:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                DateTimeType expected = DateTimeType
                        .valueOf(Converter.zonedToLocalDateTime(vehicle.properties.lastUpdatedAt));
                assertEquals(expected.toString(), dtt.toString(), "Last Update");
                break;
            case GPS:
                if (state instanceof PointType) {
                    pt = (PointType) state;
                    assertNotNull(vehicle.properties.vehicleLocation);
                    assertEquals(
                            PointType.valueOf(Double.toString(vehicle.properties.vehicleLocation.coordinates.latitude)
                                    + "," + Double.toString(vehicle.properties.vehicleLocation.coordinates.longitude)),
                            pt, "Coordinates");
                } // else no check needed
                break;
            case HEADING:
                if (state instanceof QuantityType) {
                    qt = ((QuantityType) state);
                    assertEquals(Units.DEGREE_ANGLE, qt.getUnit(), "Angle Unit");
                    assertNotNull(vehicle.properties.vehicleLocation);
                    assertEquals(vehicle.properties.vehicleLocation.heading, qt.intValue(), 0.01, "Heading");
                } // else no check needed
                break;
            case RANGE_RADIUS_ELECTRIC:
                assertTrue(state instanceof QuantityType);
                assertTrue(isElectric);
                qt = ((QuantityType) state);
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(
                        Converter.guessRangeRadius(VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, vehicle)),
                        qt.intValue(), "Range Radius Electric");
                break;
            case RANGE_RADIUS_FUEL:
                assertTrue(state instanceof QuantityType);
                assertTrue(hasFuel);
                qt = (QuantityType) state;
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(
                        Converter.guessRangeRadius(VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, vehicle)),
                        qt.intValue(), "Range Radius Fuel");
                break;
            case RANGE_RADIUS_HYBRID:
                assertTrue(state instanceof QuantityType);
                assertTrue(isHybrid);
                qt = (QuantityType) state;
                wantedUnit = VehicleStatusUtils.getLengthUnit(vehicle.status.fuelIndicators);
                assertEquals(wantedUnit, qt.getUnit());
                assertEquals(Converter.guessRangeRadius(VehicleStatusUtils.getRange(Constants.PHEV, vehicle)),
                        qt.intValue(), "Range Radius Combined");
                break;
            case DOOR_DRIVER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.doors.driverFront));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.doors.driverRear));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.doors.passengerFront));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.doors.passengerRear));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case TRUNK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.trunk));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case HOOD:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.hood));
                assertEquals(wanted.toString(), st.toString(), "Door");
                break;
            case WINDOW_DOOR_DRIVER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.windows.driverFront));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.windows.driverRear));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.windows.passengerFront));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case WINDOW_DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType
                        .valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.windows.passengerRear));
                assertEquals(wanted.toString(), st.toString(), "Window");
                break;
            case SUNROOF:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(Converter.toTitleCase(vehicle.properties.doorsAndWindows.moonroof));
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
                                    .getNextServiceDate(vehicle.properties.serviceRequired).toString();
                            DateTimeType expectedDTT = DateTimeType.valueOf(dueDateString);
                            assertEquals(expectedDTT.toString(), dtt.toString(), "Next Service");
                        }
                    } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                        String dueDateString = vehicle.properties.serviceRequired.get(0).dateTime;
                        DateTimeType expectedDTT = DateTimeType.valueOf(Converter.zonedToLocalDateTime(dueDateString));
                        assertEquals(expectedDTT.toString(), dtt.toString(), "First Service Date");
                    }
                }
                break;
            case SERVICE_MILEAGE:
                if (!state.equals(UnDefType.UNDEF)) {
                    qt = ((QuantityType) state);
                    if (gUid.contentEquals(CHANNEL_GROUP_STATUS)) {
                        QuantityType<Length> wantedQt = (QuantityType) VehicleStatusUtils
                                .getNextServiceMileage(vehicle.properties.serviceRequired);
                        assertEquals(wantedQt.getUnit(), qt.getUnit(), "Next Service Miles");
                        assertEquals(wantedQt.intValue(), qt.intValue(), "Mileage");
                    } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                        assertEquals(vehicle.properties.serviceRequired.get(0).distance.units, qt.getUnit(),
                                "First Service Unit");
                        assertEquals(vehicle.properties.serviceRequired.get(0).distance.value, qt.intValue(),
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
                        if (!vehicle.properties.serviceRequired.isEmpty()) {
                            wanted = StringType
                                    .valueOf(Converter.toTitleCase(vehicle.properties.serviceRequired.get(0).type));
                        }
                        assertEquals(wanted.toString(), st.toString(), "Service Name");
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        wanted = StringType.valueOf(Constants.NO_ENTRIES);
                        if (!vehicle.status.checkControlMessages.isEmpty()) {
                            wanted = StringType.valueOf(vehicle.status.checkControlMessages.get(0).title);
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
                        if (!vehicle.properties.serviceRequired.isEmpty()) {
                            wanted = StringType
                                    .valueOf(Converter.toTitleCase(vehicle.properties.serviceRequired.get(0).type));
                        }
                        assertEquals(wanted.toString(), st.toString(), "Service Details");
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        wanted = StringType.valueOf(Constants.NO_ENTRIES);
                        if (!vehicle.status.checkControlMessages.isEmpty()) {
                            wanted = StringType.valueOf(vehicle.status.checkControlMessages.get(0).longDescription);
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
                if (!vehicle.status.checkControlMessages.isEmpty()) {
                    wanted = StringType.valueOf(vehicle.status.checkControlMessages.get(0).state);
                }
                assertEquals(wanted.toString(), st.toString(), "CheckControl Details");
                break;
            case DATE:
                if (state.equals(UnDefType.UNDEF)) {
                    for (CBS serviceEntry : vehicle.properties.serviceRequired) {
                        assertTrue(serviceEntry.dateTime == null, "No Service Date available");
                    }
                } else {
                    assertTrue(state instanceof DateTimeType);
                    dtt = (DateTimeType) state;
                    switch (gUid) {
                        case CHANNEL_GROUP_SERVICE:
                            String dueDateString = vehicle.properties.serviceRequired.get(0).dateTime;
                            DateTimeType expectedDTT = DateTimeType
                                    .valueOf(Converter.zonedToLocalDateTime(dueDateString));
                            assertEquals(expectedDTT.toString(), dtt.toString(), "ServiceSate");
                            break;
                        default:
                            assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                            break;
                    }
                }
                break;
            case FRONT_LEFT_CURRENT:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.frontLeft.status.currentPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case FRONT_LEFT_TARGET:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.frontLeft.status.targetPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case FRONT_RIGHT_CURRENT:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.frontRight.status.currentPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case FRONT_RIGHT_TARGET:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.frontRight.status.targetPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_LEFT_CURRENT:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.rearLeft.status.currentPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_LEFT_TARGET:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.rearLeft.status.targetPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_RIGHT_CURRENT:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.rearRight.status.currentPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case REAR_RIGHT_TARGET:
                if (vehicle.properties.tires != null) {
                    assertTrue(state instanceof QuantityType);
                    qt = (QuantityType) state;
                    assertEquals(vehicle.properties.tires.rearRight.status.targetPressure / 100, qt.doubleValue(),
                            "Fron Left Current");
                } else {
                    assertTrue(state.equals(UnDefType.UNDEF));
                }
                break;
            case MOTION:
                assertTrue(state instanceof OnOffType);
                oot = (OnOffType) state;
                if (vehicle.properties.inMotion) {
                    assertEquals(oot.toFullString(), OnOffType.ON.toFullString(), "Vehicle Driving");
                } else {
                    assertEquals(oot.toFullString(), OnOffType.OFF.toFullString(), "Vehicle Stationary");
                }
                break;
            case ADDRESS:
                if (state instanceof StringType) {
                    st = (StringType) state;
                    assertEquals(st.toFullString(), vehicle.properties.vehicleLocation.address.formatted,
                            "Location Address");
                } // else no check needed
                break;
            case HOME_DISTANCE:
                if (state instanceof QuantityType) {
                    qt = (QuantityType) state;
                    PointType vehicleLocation = PointType
                            .valueOf(Double.toString(vehicle.properties.vehicleLocation.coordinates.latitude) + ","
                                    + Double.toString(vehicle.properties.vehicleLocation.coordinates.longitude));
                    int distance = vehicleLocation.distanceFrom(VehicleTests.HOME_LOCATION).intValue();
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
