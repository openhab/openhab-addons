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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.junit.Assert.*;
import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Doors;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Windows;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.Gson;

/**
 * The {@link StatusWrapper} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StatusWrapper {
    private static final Gson GSON = new Gson();
    private static final Unit<Length> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);

    private VehicleStatus vStatus;
    private boolean imperial;
    private boolean isElectric;
    private boolean hasFuel;
    private boolean isHybrid;

    private Map<String, State> specialHandlingMap = new HashMap<String, State>();

    public StatusWrapper(String type, boolean imperial, String statusJson) {
        this.imperial = imperial;
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        VehicleStatusContainer container = GSON.fromJson(statusJson, VehicleStatusContainer.class);
        assertNotNull(container);
        assertNotNull(container.vehicleStatus);
        vStatus = container.vehicleStatus;
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
        assertTrue("Same list sizes ", channels.size() == states.size());
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

    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        String gUid = channelUID.getGroupId();
        QuantityType<Length> qt;
        StringType st;
        StringType wanted;
        DateTimeType dtt;
        DecimalType dt;
        switch (cUid) {
            case MILEAGE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                }
                switch (gUid) {
                    case CHANNEL_GROUP_RANGE:
                        assertEquals("Mileage", qt.intValue(), vStatus.mileage);
                        break;
                    case CHANNEL_GROUP_SERVICE:
                        assertEquals("Mileage", qt.intValue(), vStatus.cbsData.get(0).cbsRemainingMileage);
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        assertEquals("Mileage", qt.intValue(), vStatus.checkControlMessages.get(0).ccmMileage);
                        break;
                    default:
                        assertFalse("Channel " + channelUID + " " + state + " not found", true);
                        break;
                }
                break;
            case RANGE_ELECTRIC:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectricMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectric), 0.01);
                }
                break;
            case RANGE_FUEL:
                assertTrue("Has Fuel", hasFuel);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeFuelMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeFuel), 0.01);
                }
                break;
            case RANGE_HYBRID:
                assertTrue("Is Hybrid", isHybrid);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (imperial) {
                    assertEquals("Miles", ImperialUnits.MILE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectricMls + vStatus.remainingRangeFuelMls), 0.01);
                } else {
                    assertEquals("KM", KILOMETRE, qt.getUnit());
                    assertEquals("Mileage", Converter.round(qt.floatValue()),
                            Converter.round(vStatus.remainingRangeElectric + vStatus.remainingRangeFuel), 0.01);
                }
                break;
            case REMAINING_FUEL:
                assertTrue("Has Fuel", hasFuel);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Percent", SmartHomeUnits.LITRE, qt.getUnit());
                assertEquals("Percent", Converter.round(vStatus.remainingFuel), Converter.round(qt.floatValue()), 0.01);
                break;
            case SOC:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Percent", SmartHomeUnits.PERCENT, qt.getUnit());
                assertEquals("Percent", Converter.round(vStatus.chargingLevelHv), Converter.round(qt.floatValue()),
                        0.01);
                break;
            case LOCK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                assertEquals("Vehicle locked", Converter.toTitleCase(vStatus.doorLockState), st.toString());
                break;
            case DOORS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                Doors doorState = GSON.fromJson(GSON.toJson(vStatus), Doors.class);
                assertEquals("Doors Closed", VehicleStatus.checkClosed(doorState), st.toString());
                break;
            case WINDOWS:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                Windows windowState = GSON.fromJson(GSON.toJson(vStatus), Windows.class);
                if (specialHandlingMap.containsKey(WINDOWS)) {
                    assertEquals("Windows", specialHandlingMap.get(WINDOWS).toString(), st.toString());
                } else {
                    assertEquals("Windows", VehicleStatus.checkClosed(windowState), st.toString());
                }
                break;
            case CHECK_CONTROL:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (specialHandlingMap.containsKey(CHECK_CONTROL)) {
                    assertEquals("Check Control", specialHandlingMap.get(CHECK_CONTROL).toString(), st.toString());
                } else {
                    assertEquals("Check Control", vStatus.getCheckControl(), st.toString());
                }
                break;
            case CHARGE_STATUS:
                assertTrue("Is Eelctric", isElectric);
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                if (vStatus.chargingStatus.contentEquals(Constants.INVALID)) {
                    assertEquals("Charge Status", Converter.toTitleCase(vStatus.lastChargingEndReason), st.toString());
                } else {
                    assertEquals("Charge Status", Converter.toTitleCase(vStatus.chargingStatus), st.toString());
                }
                break;
            case LAST_UPDATE:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                DateTimeType expected = DateTimeType.valueOf(Converter.getLocalDateTime(vStatus.getUpdateTime()));
                assertEquals("Last Update", expected.toString(), dtt.toString());
                break;
            case LATITUDE:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                assertNotNull(vStatus.position);
                assertEquals("Latitude", Converter.round(vStatus.position.lat), Converter.round(dt.floatValue()), 0.01);
                break;
            case LONGITUDE:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                assertNotNull(vStatus.position);
                assertEquals("Longitude", Converter.round(vStatus.position.lon), Converter.round(dt.floatValue()),
                        0.01);
                break;
            case HEADING:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                assertEquals("Angle Unit", SmartHomeUnits.DEGREE_ANGLE, qt.getUnit());
                assertNotNull(vStatus.position);
                assertEquals("Heading", vStatus.position.heading, qt.intValue(), 0.01);
                break;
            case RANGE_RADIUS_ELECTRIC:
                assertTrue(state instanceof QuantityType);
                assertTrue(isElectric);
                qt = (QuantityType) state;
                if (imperial) {
                    assertEquals("Range Radius Electric mi",
                            Converter.guessRangeRadius(vStatus.remainingRangeElectricMls), qt.floatValue(), 0.1);
                } else {
                    assertEquals("Range Radius Electric km", Converter.guessRangeRadius(vStatus.remainingRangeElectric),
                            qt.floatValue(), 0.1);
                }
                break;
            case RANGE_RADIUS_FUEL:
                assertTrue(state instanceof QuantityType);
                assertTrue(hasFuel);
                qt = (QuantityType) state;
                if (imperial) {
                    assertEquals("Range Radius Fuel mi", Converter.guessRangeRadius(vStatus.remainingRangeFuelMls),
                            qt.floatValue(), 0.1);
                } else {
                    assertEquals("Range Radius Fuel km", Converter.guessRangeRadius(vStatus.remainingRangeFuel),
                            qt.floatValue(), 0.1);
                }
                break;
            case RANGE_RADIUS_HYBRID:
                assertTrue(state instanceof QuantityType);
                assertTrue(isHybrid);
                qt = (QuantityType) state;
                if (imperial) {
                    assertEquals("Range Radius Hybrid mi",
                            Converter.guessRangeRadius(
                                    vStatus.remainingRangeElectricMls + vStatus.remainingRangeFuelMls),
                            qt.floatValue(), 0.1);
                } else {
                    assertEquals("Range Radius Hybrid km",
                            Converter.guessRangeRadius(vStatus.remainingRangeElectric + vStatus.remainingRangeFuel),
                            qt.floatValue(), 0.1);
                }
                break;
            case DOOR_DRIVER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.doorDriverFront);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.doorDriverRear);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.doorPassengerFront);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.doorPassengerRear);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case TRUNK:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.trunk);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case HOOD:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.hood);
                assertEquals("Door", wanted.toString(), st.toString());
                break;
            case WINDOW_DOOR_DRIVER_FORNT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.windowDriverFront);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case WINDOW_DOOR_DRIVER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.windowDriverRear);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case WINDOW_DOOR_PASSENGER_FRONT:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.windowPassengerFront);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case WINDOW_DOOR_PASSENGER_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.windowPassengerRear);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case WINDOW_REAR:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.rearWindow);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case SUNROOF:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                wanted = StringType.valueOf(vStatus.sunroof);
                assertEquals("Window", wanted.toString(), st.toString());
                break;
            case SIZE:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        assertEquals("Number CheckControl Messages", vStatus.checkControlMessages.size(),
                                dt.intValue());
                        break;
                    case CHANNEL_GROUP_SERVICE:
                        assertEquals("Number of Services", vStatus.cbsData.size(), dt.intValue());
                        break;
                    default:
                        assertFalse("Channel " + channelUID + " " + state + " not found", true);
                        break;
                }
                break;
            case INDEX:
                assertTrue(state instanceof DecimalType);
                dt = (DecimalType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_SERVICE:
                        assertEquals("Index of Services", 0, dt.intValue());
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        assertEquals("Index of CheckControls", 0, dt.intValue());
                        break;
                    default:
                        assertFalse("Channel " + channelUID + " " + state + " not found", true);
                        break;
                }
                break;
            case SERVICE_DATE:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                if (gUid.contentEquals(CHANNEL_GROUP_STATUS)) {
                    if (specialHandlingMap.containsKey(SERVICE_DATE)) {
                        assertEquals("Next Service", specialHandlingMap.get(SERVICE_DATE).toString(), dtt.toString());
                    } else {
                        String dueDateString = vStatus.getNextServiceDate();
                        DateTimeType expectedDTT = DateTimeType.valueOf(Converter.getLocalDateTime(dueDateString));
                        assertEquals("Next Service", expectedDTT.toString(), dtt.toString());
                    }
                } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                    String dueDateString = vStatus.cbsData.get(0).getDueDate();
                    DateTimeType expectedDTT = DateTimeType.valueOf(Converter.getLocalDateTime(dueDateString));
                    assertEquals("First Service Date", expectedDTT.toString(), dtt.toString());
                }
                break;
            case SERVICE_MILEAGE:
                assertTrue(state instanceof QuantityType);
                qt = ((QuantityType) state);
                if (gUid.contentEquals(CHANNEL_GROUP_STATUS)) {
                    if (imperial) {
                        assertEquals("Next Service Miles", ImperialUnits.MILE, qt.getUnit());
                        assertEquals("Mileage", vStatus.getNextServiceMileage(), qt.intValue());
                    } else {
                        assertEquals("Next Service KM", KILOMETRE, qt.getUnit());
                        assertEquals("Mileage", vStatus.getNextServiceMileage(), qt.intValue());
                    }
                } else if (gUid.equals(CHANNEL_GROUP_SERVICE)) {
                    if (imperial) {
                        assertEquals("First Service Miles", ImperialUnits.MILE, qt.getUnit());
                        assertEquals("First Service Mileage", vStatus.cbsData.get(0).cbsRemainingMileage,
                                qt.intValue());
                    } else {
                        assertEquals("First Service KM", KILOMETRE, qt.getUnit());
                        assertEquals("First Service Mileage", vStatus.cbsData.get(0).cbsRemainingMileage,
                                qt.intValue());
                    }
                }
                break;
            case NAME:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_SERVICE:
                        wanted = StringType.valueOf(Converter.toTitleCase(vStatus.cbsData.get(0).getType()));
                        assertEquals("Window", wanted.toString(), st.toString());
                        break;
                    case CHANNEL_GROUP_CHECK_CONTROL:
                        wanted = StringType.valueOf(vStatus.checkControlMessages.get(0).ccmDescriptionShort);
                        assertEquals("Window", wanted.toString(), st.toString());
                        break;
                    default:
                        assertFalse("Channel " + channelUID + " " + state + " not found", true);
                        break;
                }
                break;
            case DATE:
                assertTrue(state instanceof DateTimeType);
                dtt = (DateTimeType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_SERVICE:
                        String dueDateString = vStatus.cbsData.get(0).getDueDate();
                        DateTimeType expectedDTT = DateTimeType.valueOf(Converter.getLocalDateTime(dueDateString));
                        assertEquals("Service", expectedDTT.toString(), dtt.toString());
                        break;
                    default:
                        assertFalse("Channel " + channelUID + " " + state + " not found", true);
                        break;
                }
                break;
            default:
                // fail in case of unknown update
                assertFalse("Channel " + channelUID + " " + state + " not found", true);
                break;
        }
    }
}
